# Dev Utils Processor
This project provides an annotation processor to provide useful tools
during development time

# How to use
Add repo to maven dependencies
```Groovy
maven { url "https://dl.bintray.com/wgr1984/DevUtilsProcessor"}
```
and add the following dependencies to you module
```Groovy
    releaseAnnotationProcessor "de.wr.devutilsprocessor:devUtilsProcessorRelease:0.1"
    debugAnnotationProcessor "de.wr.devutilsprocessor:devUtilsProcessorDebug:0.1"
    provided "de.wr.devutilsprocessor:libDevUtilsProcessor:0.1"
```
Now you can use e.g. ```@debug``` inside your project
```Java
@Debug
private Object testDebugMethod() {
    System.out.println("This is a debug method");
    return "Should not be seen !";
}
```
which will replace all code inside the method in case of
an release build by
```Java
private Object testDebugMehtod()
  {
    throw new UnsupportedOperationException("Error: do not call this method on release.");
  }
```
inside the generated ```.class``` file

By default all methods marked as ```@Debug``` are expected to be private
to ensure it is not used by classes / modules outside.
If for any good reason this guard shall deactivated just add
```allowNonPrivate=true``` as parameter to the annotation
```Java
@Debug(allowNonPrivate = true)
static List<String> testDebugMethod2() {
    System.out.println("This is a debug method");
    return emptyList();
}
```

Furthermore there is the a check included to enforce a certain
pattern regarding naming of annotated methods. By default it is
set to ```".*[Dd]ebug.*"```, in oder to change that it can be overwritten
passing ```methodPattern``` argument towards the annotation:
```Java
@Debug(methodPattern = "debug_.*")
private Object debug_test() {
    System.out.println("This is a debug method");
    return "Should not be seen !";
}
```

Another function offered by the dev untls processor is ```@RemovedUntilVersion```.
```Java
@RemovedUntilVersion("1.1.1")
private void depricatedUntil() {
    System.out.println("This method is about to expire");
}
```
and provide the app version towards the annotation processor
```Groovy
javaCompileOptions {
    annotationProcessorOptions {
        arguments = ['devutils.currentVersion': defaultConfig.versionName]
    }
}
```
and once version is reached, compilation will fail
```
Error:(38, 20) error: Method debug_test is marked to be removed until version: 1.1.0
```
