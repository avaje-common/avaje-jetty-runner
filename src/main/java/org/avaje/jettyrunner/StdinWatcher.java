package org.avaje.jettyrunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class StdinWatcher implements Runnable {
  
  private static final Logger log = Log.getLog();//gerFactory.getLogger(StdinWatcher.class);

  private final CountDownLatch latch;
  
  private final InputStream stream;

  public StdinWatcher(CountDownLatch latch) {
    this.latch = latch;
    this.stream = System.in;

    log.debug("Watching stdin");
  }

  @Override
  public void run() {
    try {
      while (stream.read() >= 0) /* wait */ ;
    } catch (IOException e) {
        /* ignored */
    }
    if (latch.getCount() > 0) {
      log.debug("shutting down as no longer able to read stdin");
      latch.countDown();
    }
  }
}
