package org.avaje.jettyrunner;

import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * Provides a main method to register with a war/jar so that it can be a runnable war.
 * <p>
 * Relative to JettyRun this uses setupForWar() which registers the containing war
 * (as the war location) and does not set useStdInShutdown.
 * </p>
 */
public class RunWar extends BaseRunner {

  private static final String JAR_FILE_PREFIX = "jar:file:";

  private static final String WAR_BANG_SUFFIX = ".war!";

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

    setDefaultLogbackConfig();
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
    String warFilePath = trimToFile(location.toExternalForm());
    File warFile = new File(warFilePath);
    if (!warFile.exists()) {
      throw new IllegalStateException("war file not found: " + warFilePath);
    }

    webapp.setWar(warFilePath);
    webapp.setClassLoader(Thread.currentThread().getContextClassLoader());

    if (!Boolean.getBoolean("webapp.extractWar")) {
      try {
        webapp.setExtractWAR(false);
        webapp.setBaseResource(JarResource.newJarResource(Resource.newResource(warFile)));
      } catch (IOException e) {
        throw new RuntimeException("Error setting base resource to:" + warFilePath, e);
      }
    }

    if (log().isDebugEnabled()) {
      ClassLoader classLoader = webapp.getClassLoader();
      log().debug("webapp classLoader: " + classLoader);
      if (classLoader instanceof URLClassLoader) {
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        log().debug("webapp classLoader URLs: " + Arrays.toString(urls));
      }
    }
  }

  /**
   * If this is a jar:file location as per spring boot loader runnable war then
   * trim to get the containing war file.
   */
  private String trimToFile(String warLocation) {
    if (warLocation.startsWith(JAR_FILE_PREFIX)) {
      warLocation = warLocation.substring(JAR_FILE_PREFIX.length());
      int warBangPos = warLocation.indexOf(WAR_BANG_SUFFIX);
      warLocation = warLocation.substring(0, warBangPos + 4);
    }
    return warLocation;
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
