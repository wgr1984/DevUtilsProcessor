package de.wr.annotationprocessor.processor

import com.sun.source.util.Trees
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Name
import de.wr.libsimplecomposition.Debug
import io.reactivex.rxkotlin.toObservable
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.SourceVersion.latestSupported
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import com.github.javaparser.ast.Modifier as AstModifier
import com.github.javaparser.ast.type.Type as AstType
import com.sun.tools.javac.util.List as JCList

abstract class DevUtilsProcessor : AbstractProcessor() {

    private lateinit var objectType: String
    private lateinit var typeUtils: Types
    private lateinit var elementUtils: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var trees: Trees
    private lateinit var treeMaker: TreeMaker

    override fun getSupportedSourceVersion(): SourceVersion = latestSupported()

    override fun getSupportedAnnotationTypes() = supportedAnnotations

    abstract val isDebug: Boolean

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        typeUtils = processingEnv.typeUtils
        elementUtils = processingEnv.elementUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
        trees = Trees.instance(processingEnv)
        treeMaker = TreeMaker.instance((processingEnv as JavacProcessingEnvironment).getContext());
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val elements = supportedAnnotationsClasses.toObservable()
            .flatMap {
                roundEnv.getElementsAnnotatedWith(it).toObservable()
            }.toList().blockingGet()

        handleDebugAnnotation(elements)

        return true
    }

    private fun handleDebugAnnotation(elements: MutableList<Element>) {
        if (!isDebug) {
            elements.filter { it.getAnnotation(Debug::class.java) != null }.forEach {
                (trees.getTree(it) as JCTree).accept(object : TreeTranslator() {
                    override fun visitMethodDef(p0: JCTree.JCMethodDecl?) {
                        info(it, "Method: ${p0?.name} will be removed! (debugOnly)")
                        super.visitMethodDef(p0)

                        val throwException = treeMaker.Throw(treeMaker.NewClass(null, JCList.nil(),
                                makeSelectExpr("UnsupportedOperationException"),
                                JCList.of(treeMaker.Literal("Error: do not call this method on release.")), null));

                        val list = JCList.of<JCTree.JCStatement>(throwException);
                        val block = treeMaker.Block(0, list)
                        (this.result as JCTree.JCMethodDecl).body = block
                    }
                })
            }
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
            }.forEach { supportedAnnotations.add(it.canonicalName) }
        }
    }
}