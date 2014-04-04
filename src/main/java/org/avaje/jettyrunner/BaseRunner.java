package org.avaje.jettyrunner;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.StatisticsHandler;
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
  public static final String WEBAPP_SHUTDOWN_STDIN_PROPERTY = "webapp.shutdown.stdin";

  public static final String WEBAPP_SHUTDOWN_TIMEOUT_PROPERTY = "webapp.shutdown.timeout";
  //public static final String WEBAPP_LOCKFILE_PROPERTY = "webapp.lockfile";
  //public static final String WEBAPP_WEBDEFAULT_XML_LOCATION = "webapp.webdefaultxml";
  //public static final String WEBAPP_SECURE_COOKIES_PROPERTY = "webapp.cookies.secure";
  public static final String WEBAPP_EXTRA_CONFIGURATION_CLASSES = "webapp.configClasses";
  //public static final String WEBAPP_WAR_FILENAME = "webapp.warFile";


  //public static final String WEBDEFAULT_XML = "nz/ac/auckland/war/webdefault.xml";
  //public static final String WEBDEFAULT_DEV_XML = "nz/ac/auckland/war/webdefault-dev.xml";

  protected static final int DEFAULT_HTTP_PORT = 8090;
  protected static final int DEFAULT_SHUTDOWN_TIMEOUT_ = 12000;
  protected static final String DEFAULT_CONTEXT_PATH = "/";

  protected static final Logger log = Log.getLog();

  protected int httpPort;

  protected String contextPath;

  protected boolean secureCookies;
  
  protected final long shutdownTimeout;
  
  protected final boolean useStdinShutdown;

  protected WebAppContext webapp;

  protected StatisticsHandler statistics;
  
  protected Server server;
  
  /**
   * Construct reading appropriate system properties.
   */
  protected BaseRunner() {
    this.httpPort = Integer.getInteger(WEBAPP_HTTP_PORT, DEFAULT_HTTP_PORT);
    this.contextPath = System.getProperty(WEBAPP_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
    this.secureCookies = Boolean.parseBoolean(System.getProperty(WEBAPP_SECURE_COOKIES, "true"));
    this.shutdownTimeout = Integer.getInteger(WEBAPP_SHUTDOWN_TIMEOUT_PROPERTY, DEFAULT_SHUTDOWN_TIMEOUT_);
    this.useStdinShutdown = Boolean.getBoolean(WEBAPP_SHUTDOWN_STDIN_PROPERTY);
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

    // stats handler keeps count of who is currently using us, so if we still
    // have active connections we can delay shutdown
    statistics = new StatisticsHandler();
    statistics.setHandler(webapp);
    return statistics;
  }

  /**
   * Start the Jetty server.
   */
  public void startServer() {

    server = new Server(httpPort);
    server.setHandler(wrapHandlers());

    try {
      server.start();
      System.out.println("started...");
      
      Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));
      
      //System.in.read();
      //System.out.println("shutdown starting ...");
      //shutdownNicely();
      
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
    
//    try {
//      start();
//      waitForShutdown();
//    } finally {
//      stop();
//    }
    
  }
  
  protected void shutdownRequested() {
    System.out.println("shutdown requested...");
    shutdownNicely();
  }
  
  protected void shutdownNicely() {
    System.out.println("attempting to shutdown nicely...");
    try {
      try {
        attemptCleanClose();
      } catch (Exception e) {
        System.out.println("error while attempting clean shutdown");
      }
      System.out.println("shutdown now");
      server.stop();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }
  
  class ShutdownRunnable implements Runnable {

    @Override
    public void run() {
      shutdownNicely();
    }
    
  }


//  protected void start() {
//
//    try {
//      server.start();
//  
//      // Handler/context startup errors aren't propagated, we have to do it manually.
//      Throwable error = webapp.getUnavailableException();
//  
//      if (error != null) {
//        log.warn("Jetty context startup failed", error);
//        throw new RuntimeException("WebApp context startup is unavailable", error);
//      }
//  
//      if (webapp.isFailed()) {
//        throw new RuntimeException("WebApp context startup failed");
//      }
//      log.info("WebApp server started");
//      
//    } catch (Exception e) {
//      throw new RuntimeException("Failed to start server", e);
//    }
//  }

//  protected void stop() {
//    
//    if (server == null) {
//      log.warn("Never started, can't stop!");
//      return;
//    }
//
//    attemptCleanClose();
//
//    try {
//      log.info("jetty shutdown: stopping server");
//      server.stop();
//      
//      log.info("WebApp server shutdown complete");
//      
//    } catch (Exception e) {
//      throw new RuntimeException("WebApp server shutdown failed", e);
//      
//    } finally {
//      statistics = null;
//      server = null;
//      webapp = null;
//    }
//  }
  

  /*
   * Attempts a clean close of the connectors and will wait for remaining connections if they are still
   * open.
   */
  protected void attemptCleanClose() {
    
    if (shutdownTimeout > 0) {
      log.info("jetty shutdown: requesting shutdown");
      System.out.println("------- 1 requesting shutdown ");

      try {
        Connector[] connectors = server.getConnectors();
        if (connectors != null) {
          for (Connector connector : connectors) {
            System.out.println("------- connector "+connector);

            connector.shutdown();
          }
        }

        int open = statistics.getRequestsActive();

        System.out.println("------- active requests "+open);
        
        if (open > 0) {
          System.out.println("------- waitForConnections "+open);

          waitForConnections(shutdownTimeout, open);
        }
      } catch (Exception e) {
        System.out.println("------- shutdown failed ");
        e.printStackTrace();

        log.warn("jetty shutdown: formal shutdown failed", e);
      }
    }
  }

  protected void waitForConnections(long timeout, int open) {
    
    System.out.println("------- waitForConnections timeout:"+timeout+" open:"+open);

    log.info("jetty shutdown: {} requests are active, delaying for {} ms", open, timeout);

    timeout += System.currentTimeMillis();

    while (true) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.warn("jetty shutdown: clean shutdown failed sleep interval");
      }

      open = statistics.getRequestsActive();

      if (open <= 0)
        break;

      if (System.currentTimeMillis() >= timeout) {
        log.warn("jetty shutdown: {} requests not finished, kicking them out", open);
        break;
      }
    }
  }
//  
//  protected void waitForShutdown() {
//    CountDownLatch latch = new CountDownLatch(1);
//    try {
//      try {
//        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownWatcher(latch), "shutdown-hook"));
//      } catch (IllegalStateException e) {
//        // ignore
//      }
//
//      if (useStdinShutdown) {
//        Thread stdin = new Thread(new StdinWatcher(latch), "shutdown-stdin");
//        stdin.setDaemon(true);
//        stdin.start();
//      }
//
//      log.info("WebApp container is up and running on port {}", httpPort);
//      try {
//        latch.await();
//      } catch (InterruptedException e) {
//      }
//    } finally {
//      // Just so the shutdown triggers don't report having triggered
//      // a shutdown when it's already happened because of a startup error.
//      latch.countDown();
//    }
//  }
  
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
