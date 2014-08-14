package org.avaje.jettyrunner;


/**
 * Provides a Jetty Runner that can be used in an IDE to run a local expanded war.
 * <p>
 * By default this assumes a maven webapp structure with a 'resource base' of "src/main/webapp".
 * </p>
 */
public class JettyRun extends BaseRunner {

  /**
   * Run the Jetty server using an expanded webapp (in src/main/webapp by default).
   */
  public static void run() {
    new JettyRun().runServer();
  }
  
  /**
   * The path to the webapp resource base ("src/main/webapp" by default).
   */
  protected String resourceBase;

  /**
   * Construct reading system properties for http port etc.
   */
  public JettyRun() {
    this.resourceBase = System.getProperty(WEBAPP_RESOURCE_BASE,"src/main/webapp");
    // In IDE can use this to shutdown in a console window
    this.useStdInShutdown = true;
  }
  
  /**
   * Configure and run the webapp using jetty.
   */
  public void runServer() {

    setDefaultLogbackConfig();

    createWebAppContext();

    setupForExpandedWar();

    startServer();
  }

  /**
   * If logback.configurationFile is not set then setup to look for logback.xml in the current working directory.
   */
  protected void setDefaultLogbackConfig() {

    String logbackFile = System.getProperty("logback.configurationFile");
    if (logbackFile == null) {
      // set default behaviour to look in current working directory for logback.xml
      System.setProperty("logback.configurationFile", "logback.xml");
    }
  }

  /**
   * Setup for an expanded webapp with resource base as a relative path.
   */
  protected void setupForExpandedWar() {

    webapp.setServerClasses(getServerClasses());
    webapp.setDescriptor(webapp + "/WEB-INF/web.xml");
    webapp.setResourceBase(resourceBase);
    webapp.setParentLoaderPriority(false);
  }

  /**
   * Set the resource base.
   */
  public void setResourceBase(String resourceBase) {
    this.resourceBase = resourceBase;
  }

  /**
   * Return the resource base.
   */
  public String getResourceBase() {
    return resourceBase;
  }
  
}
