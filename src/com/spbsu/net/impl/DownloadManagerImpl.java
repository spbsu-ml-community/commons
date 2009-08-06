package com.spbsu.net.impl;

import com.spbsu.net.DownloadManager;
import com.spbsu.net.Policy;
import com.spbsu.util.Pair;
import com.spbsu.util.StreamUtil;
import com.spbsu.util.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: solar
 * Date: 10.06.2007
 */
@SuppressWarnings({"ALL"})
public class DownloadManagerImpl implements DownloadManager {
  private static final Log LOG = LogFactory.getLog(DownloadManagerImpl.class);
  private final ExecutorService executor = new ThreadPoolExecutor(10, 20, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  private final Queue<Pair<String, Pair<Type, Task>>> tasksQueue = new LinkedList<Pair<String, Pair<Type, Task>>>();
  private final Map<Task, URLConnection> runningTasksMap = new HashMap<Task, URLConnection>();

  private final Map<String, Integer> host2connectionCount = new HashMap<String, Integer>();
  private final Map<String, List<Long>> connectionHistory = new HashMap<String, List<Long>>();
  private final Map<String, Policy> host2customPolicy = new HashMap<String, Policy>();

  static final int READ_TIMEOUT = 100000;
  static final int CONNECTION_TIMEOUT = 30000;
  private static final String DEFAULT_ENCODING = "windows-1251";

  enum Type {
    HEAD, BINARY, TEXT
  }

  public DownloadManagerImpl() {
    this(DEFAULT_POLICY);
  }

  public DownloadManagerImpl(final Policy policy) {
    new Thread("Download timer") {
      public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
          try {
            Thread.sleep(100);
            synchronized (tasksQueue) {
              tasksQueue.notifyAll();
            }
          }
          catch (InterruptedException e) {
          }
        }
      }
    }.start();

    new Thread("Download queue") {
      public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
          Pair<String, Pair<Type, Task>> pair;
          String host;
          final long timeMs = System.currentTimeMillis();
          synchronized (tasksQueue) {
            while (tasksQueue.isEmpty()) {
              try {
                tasksQueue.wait();
              }
              catch (InterruptedException e) {
                LOG.error(e);
              }
            }
            pair = tasksQueue.remove();
            final String url = pair.getFirst();
            host = URI.create(url).getHost();

            synchronized (host2connectionCount) {
              Integer connectionCount = host2connectionCount.get(host);
              if (connectionCount == null) {
                host2connectionCount.put(host, 0);
                connectionHistory.put(host, new LinkedList<Long>());
              } else {
                final List<Long> history = connectionHistory.get(host);
                final Iterator<Long> iterator = history.iterator();
                Policy customPolicy = host2customPolicy.get(host);
                if (customPolicy == null) customPolicy = policy;

                while (iterator.hasNext()) {
                  final Long time = iterator.next();
                  if (time < timeMs - customPolicy.intervalMs()) iterator.remove();
                }
                if (connectionCount >= customPolicy.maxConnection() || history.size() >= customPolicy.maxConnectionPerInterval()) {
                  tasksQueue.add(pair);
                  try {
                    tasksQueue.wait();
                  }
                  catch (InterruptedException e) {
                    LOG.error(e);
                  }
                  continue;
                }
              }
            }
          }

          try {
            connectionHistory.get(host).add(timeMs);
            host2connectionCount.put(host, host2connectionCount.get(host) + 1);

            final Type type = pair.getSecond().getFirst();
            final Task task = pair.getSecond().getSecond();
            final Downloader command = new Downloader(pair.getFirst(), type, task);

            executor.execute(command);
          }
          catch (Throwable th) {
            LOG.warn(th);
          }
        }
      }
    }.start();
  }

  public void setCustomPolicy(final String host, final Policy policy) {
    host2customPolicy.put(host, policy);
  }

  private String getEncoding(HttpURLConnection connection) {
    String encoding = connection.getContentEncoding();
    if (encoding == null) {
      final String type = connection.getContentType();
      if (type != null) {
        final String str = "charset=";
        int index = type.lastIndexOf(str);
        if (index >= 0) {
          encoding = type.substring(index + str.length()).trim();
          int ind = 0;
          while (ind < encoding.length()) {
            final char ch = encoding.charAt(ind++);
            if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_') {
              encoding = encoding.substring(0, ind - 1);
            }
          }
          try {
            if (!Charset.isSupported(encoding)) encoding = DEFAULT_ENCODING;
          }
          catch (Exception e) {
            LOG.warn(e);
          }
        }
      }
    }
    if (encoding == null || encoding.length() == 0) encoding = DEFAULT_ENCODING;
    return encoding;
  }

  public void request(String url, final Task<CharSequence> resultHandler) {
    requestCharData(url, new Task<Pair<URLStatus, CharSequence>>() {
      public void start(Pair<URLStatus, CharSequence> param) {
        resultHandler.start(param.getSecond());
      }

      public void setCompleted() {
        resultHandler.setCompleted();
      }

      public boolean isCompleted() {
        return resultHandler.isCompleted();
      }

      public Iterator<Pair<String, String>> getPropertiesIterator() {
        return Collections.<Pair<String, String>>emptySet().iterator();
      }

      public boolean equals(final Object obj) {
        return resultHandler.equals(obj);
      }

      public int hashCode() {
        return resultHandler.hashCode();
      }
    });
  }

  public void requestCharData(String url, Task<Pair<URLStatus, CharSequence>> resultHandler) {
    synchronized (tasksQueue) {
      tasksQueue.add(Pair.create(url, Pair.create(Type.TEXT, (Task) resultHandler)));
      tasksQueue.notify();
    }
  }

  public void requestBinaryData(String url, Task<Pair<URLStatus, byte[]>> resultHandler) {
    synchronized (tasksQueue) {
      tasksQueue.add(Pair.create(url, Pair.create(Type.BINARY, (Task) resultHandler)));
      tasksQueue.notify();
    }
  }

  public void requestStatus(String url, Task<URLStatus> resultHandler) {
    synchronized (tasksQueue) {
      tasksQueue.add(Pair.create(url, Pair.create(Type.HEAD, (Task) resultHandler)));
      tasksQueue.notify();
    }
  }

  public void cancelRequest(Task<?> resultHandler) {
    synchronized (tasksQueue) {
      final Iterator<Pair<String, Pair<Type, Task>>> iterator = tasksQueue.iterator();
      while (iterator.hasNext()) {
        final Pair<String, Pair<Type, Task>> pair = iterator.next();
        if (pair.getSecond().getSecond() == resultHandler) iterator.remove();
      }
      final URLConnection connection = runningTasksMap.get(resultHandler);
      if (connection instanceof HttpURLConnection) {
        ((HttpURLConnection) connection).disconnect();
      }
    }
  }

  public void waitFor(Task<?>... resultHandler) {
    for (Task<?> task : resultHandler) {
      synchronized (task) {
        while (!task.isCompleted()) {
          try {
            task.wait();
          }
          catch (InterruptedException e) {
            //skip
          }
        }
      }
    }
  }

  private class Downloader implements Runnable {
    private final String url;
    private final Type type;
    private final Task task;
    private static final String DOWNLOADER_NAME = "";

    public Downloader(String url, Type type, Task task) {
      this.url = url;
      this.type = type;
      this.task = task;
    }

    public void run() {
      URLConnection urlConnection = null;
      IOException ioe = null;
      try {
        try {
          urlConnection = new URL(url).openConnection();
          urlConnection.setReadTimeout(READ_TIMEOUT);
          urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
          urlConnection.setRequestProperty("User-Agent", DOWNLOADER_NAME);
          synchronized (runningTasksMap) {
            runningTasksMap.put(task, urlConnection);
          }
        }
        catch (IOException e) {
          ioe = e;
        }
        switch (type) {
          case HEAD:
            //noinspection unchecked
            Task<URLStatus> headTask = (Task<URLStatus>) task;
            if (ioe != null) {
              headTask.start(new URLStatusImpl(url, ioe));
            } else if (urlConnection instanceof HttpURLConnection) {
              final HttpURLConnection connection = (HttpURLConnection) urlConnection;
              try {
                connection.setRequestMethod("HEAD");
                connection.connect();
                headTask.start(new URLStatusImpl(url, getEncoding(connection), connection.getResponseCode(), connection.getContentType()));
              }
              catch (IOException e) {
                headTask.start(new URLStatusImpl(url, e));
              }
            } else {
              headTask.start(new URLStatusImpl(url, new IOException("Head method can be called for http protocol only")));
            }
            break;
          case BINARY:
            //noinspection unchecked
            Task<Pair<URLStatus, byte[]>> binaryTask = (Task<Pair<URLStatus, byte[]>>) task;
            if (ioe != null) {
              binaryTask.start(new Pair<URLStatus, byte[]>(new URLStatusImpl(url, ioe), null));
            } else {
              try {
                //noinspection ConstantConditions
                final byte[] content = StreamUtil.readByteStream(urlConnection.getInputStream());
                URLStatusImpl status;
                if (urlConnection instanceof HttpURLConnection) {
                  final HttpURLConnection connection = (HttpURLConnection) urlConnection;
                  status = new URLStatusImpl(url, null, connection.getResponseCode(), connection.getContentType());
                } else {
                  status = new URLStatusImpl(url, null, -1, null);
                }
                binaryTask.start(new Pair<URLStatus, byte[]>(status, content));
              }
              catch (IOException e) {
                binaryTask.start(new Pair<URLStatus, byte[]>(new URLStatusImpl(url, e), null));
              }
            }
            break;
          case TEXT:
            //noinspection unchecked
            Task<Pair<URLStatus, CharSequence>> textTask = (Task<Pair<URLStatus, CharSequence>>) task;
            if (ioe != null) {
              textTask.start(new Pair<URLStatus, CharSequence>(new URLStatusImpl(url, ioe), null));
            } else if (urlConnection instanceof HttpURLConnection) {
              final HttpURLConnection connection = (HttpURLConnection) urlConnection;
              try {
                connection.setRequestMethod("GET");
                connection.connect();
                final String encoding = getEncoding(connection);
                final CharSequence content = StreamUtil.readStream(connection.getInputStream(), Charset.forName(encoding));
                textTask.start(
                    new Pair<URLStatus, CharSequence>(
                        new URLStatusImpl(
                            url,
                            encoding,
                            connection.getResponseCode(),
                            connection.getContentType()
                        ),
                        content
                    )
                );
              }
              catch (IOException e) {
                textTask.start(new Pair<URLStatus, CharSequence>(new URLStatusImpl(url, e), null));
              }
            } else {
              textTask.start(new Pair<URLStatus, CharSequence>(new URLStatusImpl(url, new IOException("Head method can be called for http protocol only")), null));
            }
            break;
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        synchronized (runningTasksMap) {
          runningTasksMap.remove(task);
          task.setCompleted();
        }
        synchronized (host2connectionCount) {
          final String host = URI.create(url).getHost();
          Integer conectionCount = host2connectionCount.get(host);
          if (conectionCount != null && conectionCount > 0) host2connectionCount.put(host, conectionCount - 1);
        }
      }
    }
  }

  public static final Policy DEFAULT_POLICY = new PolicyImpl(5, 5, 1000);
}
