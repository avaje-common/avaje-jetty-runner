package org.avaje.jettyrunner;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Base class for runnable war (RunWar) and IDE jetty runner (JettyRun).
 */
public abstract class BaseRunner {

  public static final String WEBAPP_HTTP_PORT = "webapp.http.port";
  public static final String WEBAPP_CONTEXT_PATH = "webapp.context.path";
  public static final String WEBAPP_SECURE_COOKIES = "webapp.secure.cookies";
  public static final String WEBAPP_RESOURCE_BASE = "webapp.resource.base";

  public static final String WEBAPP_EXTRA_CONFIGURATION_CLASSES = "webapp.configClasses";

  protected static final int DEFAULT_HTTP_PORT = 8090;
  protected static final String DEFAULT_CONTEXT_PATH = "/";

  protected static final Logger log = Log.getLogger("org.avaje.jettyrunner");

  /**
   * Set this on for IDE JettyRun use (for shutdown in IDE console).
   */
  protected boolean useStdInShutdown;
  
  protected int httpPort;

  protected String contextPath;

  protected boolean secureCookies;

  protected WebAppContext webapp;

  //protected StatisticsHandler statistics;

  protected Server server;

  /**
   * Construct reading appropriate system properties.
   */
  protected BaseRunner() {
    this.httpPort = Integer.getInteger(WEBAPP_HTTP_PORT, DEFAULT_HTTP_PORT);
    this.contextPath = System.getProperty(WEBAPP_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
    this.secureCookies = Boolean.parseBoolean(System.getProperty(WEBAPP_SECURE_COOKIES, "true"));
  }

  /**
   * Create the WebAppContext with basic configurations set like context path
   * etc.
   */
  protected void createWebAppContext() {
    webapp = new WebAppContext();
    webapp.setContextPath(getContextPath());
    setSecureCookies();
  }

  protected Handler wrapHandlers() {

    // Pondering statistics collection and reporting
//  statistics = new StatisticsHandler();
//  statistics.setHandler(webapp);
//  return statistics;
    
    return webapp;
  }

  /**
   * Start the Jetty server.
   */
  public void startServer() {

    server = new Server(httpPort);
    server.setHandler(wrapHandlers());

    try {
      server.start();
      log.info("server started");

      Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));
      
      if (useStdInShutdown) {
        // generally for use in IDE via JettyRun
        System.in.read();
        shutdownNicely(false);
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }

  protected void shutdownNicely(boolean normalShutdown) {
    try {
      server.stop();
      server.join();
      if (normalShutdown) {
        // only want to log this once
        log.info("server stopped");
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }

  class ShutdownRunnable implements Runnable {

    @Override
    public void run() {
      log.info("server shutting down");
      shutdownNicely(true);
    }
  }


  /**
   * Set the secure cookies setting on the jetty session manager.
   */
  protected void setSecureCookies() {

    SessionManager sessionManager = webapp.getSessionHandler().getSessionManager();
    if (!(sessionManager instanceof AbstractSessionManager)) {
      throw new RuntimeException("Cannot set secure cookies on session manager.");
    }

    AbstractSessionManager abstractSessionManager = (AbstractSessionManager) sessionManager;

    abstractSessionManager.getSessionCookieConfig().setSecure(isSecureCookies());
    abstractSessionManager.setHttpOnly(true);
  }

  /**
   * Return the http port to use.
   */
  public int getHttpPort() {
    return httpPort;
  }

  /**
   * Set the http port to use.
   */
  public void setHttpPort(int httpPort) {
    this.httpPort = httpPort;
  }

  /**
   * Return the context path to use.
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * Set the context path to use.
   */
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  /**
   * Return true if secure cookies should be used.
   */
  public boolean isSecureCookies() {
    return secureCookies;
  }

  /**
   * Set the secure cookies setting.
   */
  public void setSecureCookies(boolean secureCookies) {
    this.secureCookies = secureCookies;
  }

}
