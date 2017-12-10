package de.wr.annotationprocessor.processor

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.sun.source.util.Trees
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Name
import com.sun.xml.internal.ws.util.VersionUtil
import de.wr.libsimplecomposition.Debug
import de.wr.libsimplecomposition.RemovedUntilVersion
import io.reactivex.rxkotlin.toObservable
import java.io.BufferedWriter
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.SourceVersion.latestSupported
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import com.github.javaparser.ast.Modifier as AstModifier
import com.github.javaparser.ast.type.Type as AstType
import com.sun.tools.javac.util.List as JCList

abstract class DevUtilsProcessor : AbstractProcessor() {

    private lateinit var typeUtils: Types
    private lateinit var elementUtils: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var trees: Trees
    private lateinit var treeMaker: TreeMaker
    private lateinit var currentVersion: String

    override fun getSupportedSourceVersion(): SourceVersion = latestSupported()

    override fun getSupportedAnnotationTypes() = supportedAnnotations

    abstract val isDebug: Boolean

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf("devutils.currentVersion")
    }


    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        typeUtils = processingEnv.typeUtils
        elementUtils = processingEnv.elementUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
        trees = Trees.instance(processingEnv)
        treeMaker = TreeMaker.instance((processingEnv as JavacProcessingEnvironment).getContext());
        currentVersion = processingEnv.options["devutils.currentVersion"].orEmpty()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val elements = supportedAnnotationsClasses.toObservable()
            .flatMap {
                roundEnv.getElementsAnnotatedWith(it).toObservable()
            }.toList().blockingGet()

        handleDebugAnnotation(elements.filter { it.getAnnotation(Debug::class.java) != null })
        handleVersionAnnotation(elements.filter { it.getAnnotation(RemovedUntilVersion::class.java) != null })
        if (elements.isNotEmpty()) {
            createUtilsClass()
        }

        return true
    }

    private fun createUtilsClass() {
        try {
            val fileName = "DevUtils"
            val source = processingEnv.filer.createSourceFile("${this::class.java.`package`.name}.$fileName")

            val writer = BufferedWriter(source.openWriter())

            val cu = CompilationUnit()
            // set the package
            cu.setPackageDeclaration(this::class.java.`package`.name);

            val type = JavaParser.parseType("boolean")

            cu.addClass(fileName, AstModifier.PUBLIC, AstModifier.FINAL)
                    .addField(type,"IS_DEBUG", AstModifier.PUBLIC, AstModifier.STATIC)
                    .setVariable(0, VariableDeclarator(type, "IS_DEBUG", BooleanLiteralExpr(isDebug))
            )

            writer.run {
                write(cu.toString())
                flush()
                close()
            }
        } catch (e: IOException) {
            System.err.println("$e ${e.message}")
        }
    }

    private fun handleVersionAnnotation(elements: List<Element>) {
        elements.forEach {
            val annotation = it.getAnnotation(RemovedUntilVersion::class.java)
            if (VersionUtil.compare(currentVersion, annotation.value) >= 0) {
                error(it, "Method ${it.simpleName} is marked to be removed until version: ${annotation.value}")
            }
        }
    }

    private fun handleDebugAnnotation(elements: List<Element>) {
        elements.forEach {
            (trees.getTree(it) as JCTree).accept(object : TreeTranslator() {
                override fun visitMethodDef(p0: JCTree.JCMethodDecl?) {
                    val debugAnnotation = it.getAnnotation(Debug::class.java);
                    // Check if method is private
                    if (p0?.modifiers?.getFlags()?.any { it == Modifier.PRIVATE} == false) {
                        // Check if non private is allowed by exception
                        if (debugAnnotation?.allowNonPrivate == false) {
                            error(it, "Method ${p0.name} is marked as debug but not private")
                        }
                    }
                    // Check if method matches defined pattern
                    if (p0?.name?.matches(Regex(debugAnnotation.methodPattern)) == false) {
                        error(it, "Method ${p0.name} does not match pattern: ${debugAnnotation.methodPattern}")
                    }
                    if (!isDebug) {
                        info(it, "Method: ${p0?.name} will be removed! (debug)")
                        super.visitMethodDef(p0)

                        val throwException = treeMaker.Throw(treeMaker.NewClass(null, JCList.nil(),
                                makeSelectExpr("UnsupportedOperationException"),
                                JCList.of(treeMaker.Literal("Error: do not call this method on release.")), null));

                        val list = JCList.of<JCTree.JCStatement>(throwException);
                        val block = treeMaker.Block(0, list)
                        (this.result as JCTree.JCMethodDecl).body = block
                    }
                }
            })
        }
    }

    fun makeSelectExpr(select: String): JCTree.JCExpression? {
        val parts = select.split("\\.");
        var expression = ident(parts[0]);
        for (i in 1 until parts.size) {
            expression = treeMaker.Select(expression, elementUtils.getName(parts[i]) as Name);
        }
        return expression;
    }

    fun ident(name: String): JCTree.JCExpression? {
        return treeMaker.Ident(elementUtils.getName(name) as Name);
    }

    private fun getPackageName(typeElement: TypeElement) =
            typeElement.qualifiedName.substring(0, typeElement.qualifiedName.length - typeElement.simpleName.length - 1)

    private fun error(e: Element, msg: String, vararg args: Any) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, *args),
                e)
    }

    private fun info(e: Element, msg: String, vararg args: Any) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(msg, *args),
                e)
    }


    companion object {
        private var supportedAnnotations = HashSet<String>()
        private var supportedAnnotationsClasses = mutableListOf<Class<out Annotation>>()

        init {
            supportedAnnotationsClasses.apply {
                add(Debug::class.java)
                add(RemovedUntilVersion::class.java)
            }.forEach { supportedAnnotations.add(it.canonicalName) }
        }
    }
}