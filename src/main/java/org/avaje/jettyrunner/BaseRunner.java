package org.avaje.jettyrunner;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Base class for runnable war (RunWar) and IDE jetty runner (JettyRun).
 */
public abstract class BaseRunner {

  public static final String WEBAPP_HTTP_PORT = "webapp.http.port";
  public static final String WEBAPP_CONTEXT_PATH = "webapp.context.path";
  public static final String WEBAPP_SECURE_COOKIES = "webapp.secure.cookies";
  public static final String WEBAPP_RESOURCE_BASE = "webapp.resource.base";

  public static final String WEBAPP_EXTRA_CONFIGURATION_CLASSES = "webapp.configClasses";

  protected static final int DEFAULT_HTTP_PORT = 8080;

  protected static final String DEFAULT_CONTEXT_PATH = "/";

  protected static final Logger log = Log.getLogger("org.avaje.jettyrunner");

  /**
   * A modification from WebAppContext.__dftServerClasses that exposes JDT
   * so that jsp works.
   *
   * @see org.eclipse.jetty.webapp.WebAppContext
   */
  public final static String[] exposeJdt_dftServerClasses = {
      "-org.eclipse.jetty.continuation.", // don't hide continuation classes
      "-org.eclipse.jetty.jndi.",         // don't hide naming classes
      "-org.eclipse.jetty.jaas.",         // don't hide jaas classes
      "-org.eclipse.jetty.servlets.",     // don't hide jetty servlets
      "-org.eclipse.jetty.servlet.DefaultServlet", // don't hide default servlet
      "-org.eclipse.jetty.servlet.listener.", // don't hide useful listeners
      "-org.eclipse.jetty.websocket.",    // don't hide websocket classes from webapps (allow webapp to use ones from system classloader)
      "-org.eclipse.jetty.apache.",       // don't hide jetty apache impls
      "-org.eclipse.jetty.util.log.",     // don't hide server log
      "org.objectweb.asm.",               // hide asm used by jetty
      //"org.eclipse.jdt.",                 // hide jdt used by jetty
      "org.eclipse.jetty."                // hide other jetty classes
    } ;

  /**
   * Set this on for IDE JettyRun use (for shutdown in IDE console).
   */
  protected boolean useStdInShutdown;
  
  protected int httpPort;

  protected String contextPath;

  protected boolean secureCookies;

  protected WebAppContext webapp;

  protected Server server;

  protected ServerContainer serverContainer;

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
    webapp.setThrowUnavailableOnStartupException(true);
    webapp.setServerClasses(getServerClasses());
    webapp.setContextPath(getContextPath());
    setSecureCookies();
  }

  protected void setupForWebSocket() {

    try {
      serverContainer = WebSocketServerContainerInitializer.configureContext(webapp);
      // you can manually register endpoints to this serverContainer
      // or register them via a ServletContextListener
      //serverContainer.addEndpoint(MyWebSocketServerEndpoint.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Refer to WebAppContext __dftServerClasses. This exposes JDT to the webapp for jsp use.
   */
  protected String[] getServerClasses() {
    return exposeJdt_dftServerClasses;
  }

  /**
   * Wrap handlers as you need with statistics collection or proxy request handling.
   */
  protected Handler wrapHandlers() {

    return webapp;
  }

  /**
   * Start the Jetty server.
   */
  public void startServer() {

    server = new Server(httpPort);
    server.setHandler(wrapHandlers());

    setupForWebSocket();
    try {
      server.start();
      log.info("server started");

      Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));
      
      if (useStdInShutdown) {
        // generally for use in IDE via JettyRun, Use CTRL-D in IDE console to shutdown
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        while((systemIn.readLine()) != null) {
          // ignore anything except CTRL-D by itself
        }
        System.out.println("Shutdown via CTRL-D");
        System.exit(0);
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
    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(100);
    }
  }

  protected class ShutdownRunnable implements Runnable {

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
   * Return the context path to use.
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * Return true if secure cookies should be used.
   */
  public boolean isSecureCookies() {
    return secureCookies;
  }

  /**
   * Return true if stand input should be read to determine shutdown.
   *
   * This should really only be true for use when running in an IDE and
   * CTRL-D in the IDE console can be used to trigger shutdown.
   */
  public boolean isUseStdInShutdown() {
    return useStdInShutdown;
  }

  /**
   * Set if stand input should be read to determine shutdown.
   *
   * This should really only be true for use when running in an IDE and
   * CTRL-D in the IDE console can be used to trigger shutdown.
   */
  public void setUseStdInShutdown(boolean useStdInShutdown) {
    this.useStdInShutdown = useStdInShutdown;
  }
}
