package org.avaje.jettyrunner;

import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ShutdownWatcher implements Runnable {
  
  private static final Logger log = Log.getLog();//gerFactory.getLogger(ShutdownWatcher.class);

  private final CountDownLatch latch;

  public ShutdownWatcher(CountDownLatch latch) {
    this.latch = latch;
  }

  @Override
  public void run() {
    if (latch.getCount() > 0) {
      log.debug("Received signal, triggering shutdown");
      latch.countDown();
    }
  }
}
