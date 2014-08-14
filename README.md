avaje-jetty-runner
=================

Jetty webapp runner for a war file (aka runnable war) or expanded webapp (aka IDE development mode)


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

    // Or in the case where the defaults suit 
    // run the webapp on port 8080, resources in src/main/webapp
    // and the context path of "/"
    JettyRun.run();

    
  }
}
```
