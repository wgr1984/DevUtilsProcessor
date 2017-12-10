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