package com.expleague.commons.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;


/**
 * User: solar
 * Date: 17.09.13
 * Time: 14:42
 */
public final class ThreadTools {
  public static final Logger log = LoggerFactory.getLogger(ThreadTools.class);
  public static final int COMPUTE_UNITS = Runtime.getRuntime().availableProcessors();

  private ThreadTools() {
  }

  @NotNull
  public static ThreadPoolExecutor createBGExecutor(@NotNull final String name, final int queueSize) {
    final BlockingQueue<Runnable> workQueue;
    if (queueSize > 0) {
      workQueue = new ArrayBlockingQueue<>(queueSize);
    } else {
      workQueue = new LinkedBlockingQueue<>();
    }

    return new ThreadPoolExecutor(COMPUTE_UNITS, COMPUTE_UNITS, 5, TimeUnit.SECONDS, workQueue, new ThreadFactory() {
      @NotNull
      @Override
      public Thread newThread(@NotNull final Runnable r) {
        final Thread thread = new Thread(r, name);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(@NotNull final Thread t, @NotNull final Throwable e) {
            log.error("Thread " + t + " dead", e);
          }
        });
        return thread;
      }
    });

  }

  public static void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {}
  }
}
