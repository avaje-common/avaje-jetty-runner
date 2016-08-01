avaje-jetty-runner
=================

* avaje-jetty-runner : [![Maven Central : avaje-jetty-runner](https://maven-badges.herokuapp.com/maven-central/org.avaje.jetty/avaje-jetty-runner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.avaje.jetty/avaje-jetty-runner)

Jetty webapp runner for a war file (aka runnable war) or expanded webapp (aka IDE development mode).

It is literally 3 classes (11k as a jar) and provides convenience for using Jetty to run standard war format webapps.
Jetty comes with `WebAppContext` and this "Jetty runner" is fairly lightweight configuring an underlying 
jetty `WebAppContext` and `server`.


What it does
-------------
 - Configures the WebAppContext (port, context path, tmp directory, cookie handler etc)
 - Detects if WebSocket support is in the classpath and if so sets up WebSocket support
 - Sets up a shutdown hook
 - RunWar handles extract/expand war (by default not expanded) 
 - RunWar handles runnable jar format (i.e. Spring boot loader war format)


Runnable wars with Spring boot loader
---------------------------
Spring boot loader provides both jar and war packaging with the associated `class loader` support. We can use this
along with Jetty runner to create a `runnable war`.
 
- Package a web application using Spring boot loader
- Include appropriate Jetty dependencies (webapp + optional websocket support)
- Use the Spring boot loader `class loader`
- Jetty runner provides a `main()` method to configure and run a Jetty `WebAppContext` 



Using JettyRun for development
---------------
JettyRun provides a run() method that launches Jetty. This by default expects an expanded webapp to 
located in the src/main/webapp directory.

The main benefit of using JettyRun to run the webapp via the IDE is that it will use the classpath
from the IDE.


```java
package example.main;

import org.avaje.jettyrunner.JettyRun;

/**
 * Run the webapp using Jetty. 
 * 
 * This uses the IDE classpath, debugger etc.
 */
public class RunJettyWebappInIDE {

  public static void main(String[] args) {
    
    
    JettyRun jettyRun = new JettyRun();
    jettyRun.setHttpPort(8090);                    // default to 8080
    jettyRun.setContextPath("/hello");             // defaults to "/"
    jettyRun.setResourceBase("src/main/webapp");   // defaults to "src/main/webapp"
    
    jettyRun.runServer();
  }
}
```
