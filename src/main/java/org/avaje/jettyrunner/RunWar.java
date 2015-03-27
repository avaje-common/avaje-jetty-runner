package org.avaje.jettyrunner;

import java.net.URL;
import java.security.ProtectionDomain;

/**
 * Provides a main method to register with a war/jar so that it can be a
 * runnable war.
 */
public class RunWar extends BaseRunner {

  /**
   * Main method registered in runnable war.
   */
  public static void main(String[] args) throws Exception {

    new RunWar().runServer();
  }

  /**
   * Configure and run Jetty using the containing WAR.
   */
  public void runServer() {

    createWebAppContext();

    setupForWar();

    startServer();
  }

  /**
   * Setup the webapp pointing to the war file that contains this class.
   */
  protected void setupForWar() {

    // Identify the war file that contains this class
    ProtectionDomain protectionDomain = RunWar.class.getProtectionDomain();
    URL location = protectionDomain.getCodeSource().getLocation();
    webapp.setWar(location.toExternalForm());
  }

  /**
   * Set the http port to use.
   */
  public RunWar setHttpPort(int httpPort) {
    this.httpPort = httpPort;
    return this;
  }
  
 /**
  * Set the context path to use.
  */
  public RunWar setContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  /**
   * Set the secure cookies setting.
   */
  public RunWar setSecureCookies(boolean secureCookies) {
    this.secureCookies = secureCookies;
    return this;
  }
}
