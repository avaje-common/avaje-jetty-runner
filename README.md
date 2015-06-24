avaje-jetty-runner
=================

Jetty webapp runner for a war file (aka runnable war) or expanded webapp (aka IDE development mode)

* avaje-jetty-runner : [![Maven Central : avaje-jetty-runner](https://maven-badges.herokuapp.com/maven-central/org.avaje.jetty/avaje-jetty-runner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.avaje.jetty/avaje-jetty-runner)
* avaje-runnable-war : [![Maven Central : runnable-war](https://maven-badges.herokuapp.com/maven-central/org.avaje.jetty/runnable-war/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.avaje.jetty/runnable-war)


Using JettyRun
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
