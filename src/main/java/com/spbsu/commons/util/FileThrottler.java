package com.spbsu.commons.util;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Experts League
 * Created by solar on 13/02/16.
 */
public class FileThrottler {
  private final Thread thread;

  private static class FileOperation implements Delayed {
    final long delay;
    final File file;
    final Runnable runnable;

    private FileOperation(long delay, File file, Runnable runnable) {
      this.delay = delay;
      this.file = file;
      this.runnable = runnable;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      return TimeUnit.MILLISECONDS.convert(delay, unit);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
      return Long.compare(delay, o.getDelay(TimeUnit.MILLISECONDS));
    }
  }

  private DelayQueue<FileOperation> queue = new DelayQueue<>();
  private TObjectLongMap<String> lastRuns = new TObjectLongHashMap<>();
  private Set<Class<? extends Runnable>> alreadyInQueue = new HashSet<>();

  public synchronized void schedule(File file, long notOften, Runnable todo) {
    if (alreadyInQueue.contains(todo.getClass()))
      return;
    final String path = file.getAbsolutePath();
    final long lastRun = lastRuns.get(path);
    long time = System.currentTimeMillis();
    final long past = time - lastRun;
    queue.add(new FileOperation(past > notOften ? 0 : notOften - past, file, todo));
    alreadyInQueue.add(todo.getClass());
  }

  public FileThrottler() {
    //noinspection InfiniteLoopStatement
    thread = new Thread(() -> {
      //noinspection InfiniteLoopStatement
      while (true) {
        final FileOperation poll;
        try {
          poll = queue.take();
        }
        catch (InterruptedException e) {
          break;
        }
        synchronized (FileThrottler.this) {
          alreadyInQueue.remove(poll.runnable.getClass());
          poll.runnable.run();
          lastRuns.put(poll.file.getAbsolutePath(), System.currentTimeMillis());
        }
      }
    });
    thread.setDaemon(true);
    thread.setName("FileThrottler consumer");
    thread.start();
  }

  @Override
  protected void finalize() throws Throwable {
    thread.interrupt();
    super.finalize();
  }
}
