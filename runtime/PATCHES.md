# org.eclipse.equinox.jsp.jasper

## META-INF/MANIFEST.MF

```
diff --git a/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF b/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF
index 518e007..cf68456 100644
--- a/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF
+++ b/bundles/org.eclipse.equinox.jsp.jasper/META-INF/MANIFEST.MF
@@ -10,15 +10,14 @@
  javax.servlet.annotation;version="2.6";resolution:=optional,
  javax.servlet.descriptor;version="2.6";resolution:=optional,
  javax.servlet.http;version="[2.4, 3.2)",
- javax.servlet.jsp;version="[2.0, 2.3)",
+ javax.servlet.jsp;version="[2.0, 2.4)",
  org.apache.jasper.servlet;version="[0, 8)",
  org.osgi.framework;version="1.3.0",
  org.osgi.service.http;version="1.2.0",
  org.osgi.service.packageadmin;version="1.2.0",
  org.osgi.util.tracker;version="1.3.1"
 Export-Package: org.eclipse.equinox.jsp.jasper;version="1.0.0"
-Bundle-RequiredExecutionEnvironment: CDC-1.0/Foundation-1.0,
- J2SE-1.3
+Bundle-RequiredExecutionEnvironment: JavaSE-1.6
 Comment-Header: Both Eclipse-LazyStart and Bundle-ActivationPolicy are specified for compatibility with 3.2
 Eclipse-LazyStart: true
 Bundle-ActivationPolicy: lazy
```

### Allow javax.servlet.jsp to be of version 2.4. 

Changed `javax.servlet.jsp;version="[2.0, 2.3)",`  to `javax.servlet.jsp;version="[2.0, 2.4)",` 

### Change to Java SE 6

Changed to `Bundle-RequiredExecutionEnvironment: JavaSE-1.6`

# org.apache.jasper.glassfish

## META-INF/MANIFEST.MF

### Automatically import TLD classes

The backend system provides all TLD files to each JspServlet. So all defined tag and function
classes must be resolvable by this bundle. We do this by using a DynamicImport to `*`.

```
DynamicImport-Package: *
```

## src/org/apache/jasper/runtime/TldScanner.java

### Remove jstl core uri from system map

Jasper seems to handle the JSTL core tag library special. However this interferes with
using a different implementation.

Comment out:

```java
// systemUris.add("http://java.sun.com/jsp/jstl/core");
```

## src/org/apache/jasper/compiler/JDTJavaCompiler.java

Allow processing Java 1.6, 1.7 and 1.8.

```java
    public void setSourceVM(String sourceVM) {
        if(sourceVM.equals("1.1")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_1);
        } else if(sourceVM.equals("1.2")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_2);
        } else if(sourceVM.equals("1.3")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_3);
        } else if(sourceVM.equals("1.4")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_4);
        } else if(sourceVM.equals("1.5")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_5);
        } else if(sourceVM.equals("1.6")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_6);
        } else if(sourceVM.equals("1.7")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_7);
        } else if(sourceVM.equals("1.8")) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_8);
        } else {
            log.warning("Unknown source VM " + sourceVM + " ignored.");
            settings.put(CompilerOptions.OPTION_Source,
                    CompilerOptions.VERSION_1_5);
        }
    }

    public void setTargetVM(String targetVM) {
        if(targetVM.equals("1.1")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_1);
        } else if(targetVM.equals("1.2")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_2);
        } else if(targetVM.equals("1.3")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_3);
        } else if(targetVM.equals("1.4")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_4);
        } else if(targetVM.equals("1.5")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_5);
        } else if(targetVM.equals("1.6")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_6);
        } else if(targetVM.equals("1.7")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_7);
        } else if(targetVM.equals("1.8")) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_8);
        } else {
            log.warning("Unknown target VM " + targetVM + " ignored.");
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                    CompilerOptions.VERSION_1_5);
        }
    }
    
```
