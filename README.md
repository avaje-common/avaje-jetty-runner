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
    
    // run the webapp (expected to be in src/main/webapp) 
    JettyRun.run();
  }
}
```
