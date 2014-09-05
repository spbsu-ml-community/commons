package com.spbsu.commons.net.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.concurrent.*;


import com.spbsu.commons.io.StreamTools;
import com.spbsu.commons.net.DownloadManager;
import com.spbsu.commons.net.Policy;
import com.spbsu.commons.net.Task;
import com.spbsu.commons.net.URLConnectionTools;
import com.spbsu.commons.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: solar
 * Date: 10.06.2007
 * Time: 16:33:44
 */
public class DownloadManagerImpl implements DownloadManager {
  private static final Log LOG = LogFactory.getLog(DownloadManagerImpl.class);
  private final ExecutorService executor = new ThreadPoolExecutor(10, 20, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

    public Thread newThread(Runnable r) {
      Thread newThread = Executors.defaultThreadFactory().newThread(r);
      newThread.setDaemon(true);
      return newThread;
    }

  });
  private final Queue<Pair<String, Pair<Type, Task>>> tasksQueue = new LinkedList<Pair<String, Pair<Type, Task>>>();
  private final Map<Task, URLConnection> runningTasksMap = new HashMap<Task, URLConnection>();

  private final Map<String, Integer> hostPath2connectionCount = new HashMap<String, Integer>();
  private final Map<String, List<Long>> connectionHistory = new HashMap<String, List<Long>>();
  private final Map<String, Policy> host2customPolicy = new HashMap<String, Policy>();
  private final Map<String, Proxy> host2proxy = new HashMap<String, Proxy>();

  static final int READ_TIMEOUT = 100000;
  static final int CONNECTION_TIMEOUT = 30000;

  //ToDo: fix it
  static final String MAGIC_POST_REQUEST = "some_hack_string_for_post_message_throwing";
  private static final String DEFAULT_ENCODING = "windows-1251";

  private enum Type {
    HEAD, BINARY, TEXT, POST
  }

  public DownloadManagerImpl() {
    this(DEFAULT_POLICY);
  }

  public DownloadManagerImpl(final Policy policy) {
    final Thread downloadTimer =
        new Thread("Download timer") {
          @Override
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
                LOG.warn(e);
              }
            }
          }
        };
    downloadTimer.setDaemon(true);
    downloadTimer.start();

    final Thread downloadQueue =
        new Thread("Download queue") {
          @Override
          public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
              Pair<String, Pair<Type, Task>> pair;
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
                String hostPath;
                final Pair<String, Policy> path2CustomPolicy = getPolicyForURL(pair.getFirst());
                if (path2CustomPolicy != null) {
                  hostPath = path2CustomPolicy.getFirst();
                } else {
                  try {
                    hostPath = new URI(url).getHost();
                  } catch (URISyntaxException e) {
                    LOG.warn("url '" + url + "' not valid!", e);
                    continue;
                  }
                }
                Policy customPolicy = path2CustomPolicy != null ? path2CustomPolicy.getSecond() : null;
                if (customPolicy == null) {
                  customPolicy = policy;
                }
                boolean skipTask = false;
                synchronized (hostPath2connectionCount) {
                  Integer connectionCount = hostPath2connectionCount.get(hostPath);
                  if (connectionCount == null) {
                    connectionCount = 0;
                    connectionHistory.put(hostPath, new LinkedList<Long>());
                  }
                  final List<Long> history = connectionHistory.get(hostPath);
                  final Iterator<Long> iterator = history.iterator();
                  final long timeMs = System.currentTimeMillis();
                  while (iterator.hasNext()) {
                    final Long time = iterator.next();
                    if (time < timeMs - customPolicy.intervalMs()) {
                      iterator.remove();
                    }
                  }
                  if (!(connectionCount >= customPolicy.maxConnection() || history.size() >= customPolicy.maxConnectionPerInterval())) {
                    connectionHistory.get(hostPath).add(System.currentTimeMillis());
                    hostPath2connectionCount.put(hostPath, connectionCount + 1);
                  } else {
                    skipTask = true;
                  }
                }
                if (skipTask) {
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
              try {
                final Type type = pair.getSecond().getFirst();
                final Task task = pair.getSecond().getSecond();
                final Downloader command = new Downloader(pair.getFirst(), type, task);

                executor.execute(command);
              }
              catch (Exception th) {
                LOG.warn(th);
              }
            }
          }
        };
    downloadQueue.setDaemon(true);
    downloadQueue.start();
  }

  public void setCustomPolicy(final String host, final Policy policy) {
    host2customPolicy.put(host, policy);
  }

  public void setProxy(final String host, final Proxy proxy) {
    host2proxy.put(host, proxy);
  }

  public Pair<String, Policy> getPolicyForURL(String url) {
    if (url == null) {
      return null;
    }
    final String[] policyPaths = host2customPolicy.keySet().toArray(new String[host2customPolicy.size()]);
    String candidate = null;
    for (String path : policyPaths) {
      if (url.contains(path)) {
        if (candidate == null || path.length() > candidate.length()) {
          candidate = path;
        }
      }
    }
    return candidate == null ? null : Pair.create(candidate, host2customPolicy.get(candidate));
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

      public void setRequestProperty(String key, String value) {
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

  public void requestCharDataWithPost(String url, String msg, Task<Pair<URLStatus, CharSequence>> resultHandler) {
    synchronized (tasksQueue) {
      resultHandler.setRequestProperty(MAGIC_POST_REQUEST, msg);
      tasksQueue.add(Pair.create(url, Pair.create(Type.POST, (Task) resultHandler)));
      tasksQueue.notify();
    }
  }

  public void cancelRequest(Task<?> resultHandler) {
    synchronized (tasksQueue) {
      final Iterator<Pair<String, Pair<Type, Task>>> iterator = tasksQueue.iterator();
      while (iterator.hasNext()) {
        final Pair<String, Pair<Type, Task>> pair = iterator.next();
        if (pair.getSecond().getSecond() == resultHandler) {
          iterator.remove();
        }
      }
      final URLConnection connection = runningTasksMap.get(resultHandler);
      if (connection instanceof HttpURLConnection) {
        ((HttpURLConnection) connection).disconnect();
      }
    }
  }

  public void waitFor(Task<?>... resultHandler) {
    for (Task<?> task : resultHandler) {
      //noinspection SynchronizationOnLocalVariableOrMethodParameter
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
    private static final String DOWNLOADER_NAME = "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9";
    private static final String SUPPORT_ENCODING = "windows-1251,utf-8;q=0.7,*;q=0.7";

    public Downloader(String url, Type type, Task task) {
      this.url = url;
      this.type = type;
      this.task = task;
    }

    @SuppressWarnings("unchecked")
    public void run() {
      URLConnection urlConnection = null;
      IOException ioe = null;
      try {
        try {
          final URL structedUrl = new URL(url);
          Proxy proxy = host2proxy.get(structedUrl.getHost().toLowerCase());
          urlConnection = proxy != null ? structedUrl.openConnection(proxy) : structedUrl.openConnection();
          urlConnection.setReadTimeout(READ_TIMEOUT);
          urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
          urlConnection.setRequestProperty("User-Agent", DOWNLOADER_NAME);
          urlConnection.setRequestProperty("Accept-Charset", SUPPORT_ENCODING);
//          urlConnection.setRequestProperty("Referer", "http://ru.yahoo.com/");
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
              headTask.start(URLStatusImpl.create(url, ioe));
            } else if (urlConnection instanceof HttpURLConnection) {
              final HttpURLConnection connection = (HttpURLConnection) urlConnection;
              try {
                connection.setRequestMethod("HEAD");
                final Iterator<Pair<String, String>> propertiesIterator = task.getPropertiesIterator();
                while (propertiesIterator.hasNext()) {
                  Pair<String, String> nextProp = propertiesIterator.next();
                  connection.setRequestProperty(nextProp.getFirst(), nextProp.getSecond());
                }
                connection.connect();
                headTask.start(new URLStatusImpl(url, URLConnectionTools.determineEncoding(connection), connection.getResponseCode(),
                    URLConnectionTools.extractContentType(connection.getContentType()), connection.getContentLength()));
              }
              catch (IOException e) {
                headTask.start(URLStatusImpl.create(url, e));
              }
            } else {
              headTask.start(URLStatusImpl.create(url, new IOException("Head method can be called for http protocol only")));
            }
            break;
          case BINARY:
            //noinspection unchecked
            Task<Pair<URLStatus, byte[]>> binaryTask = (Task<Pair<URLStatus, byte[]>>) task;
            if (ioe != null) {
              binaryTask.start(Pair.create(URLStatusImpl.create(url, ioe), (byte[]) null));
            } else {
              try {
                //noinspection ConstantConditions
                final byte[] content = StreamTools.readByteStream(urlConnection.getInputStream());
                URLStatus status;
                if (urlConnection instanceof HttpURLConnection) {
                  final HttpURLConnection connection = (HttpURLConnection) urlConnection;
                    status = new URLStatusImpl(url, URLConnectionTools.determineEncoding(connection, DEFAULT_ENCODING), connection.getResponseCode(),
                      URLConnectionTools.extractContentType(connection.getContentType()), connection.getContentLength());
                } else {
                  status = new URLStatusImpl(url, null, -1, null, -1);
                }
                binaryTask.start(Pair.create(status, content));
              }
              catch (IOException e) {
                binaryTask.start(Pair.create(URLStatusImpl.create(url, e), (byte[]) null));
              }
            }
            break;
          case TEXT:
            //noinspection unchecked
            Task<Pair<URLStatus, CharSequence>> textTask = (Task<Pair<URLStatus, CharSequence>>) task;
            if (ioe != null) {
              textTask.start(Pair.create(URLStatusImpl.create(url, ioe), (CharSequence) null));
            } else if (urlConnection instanceof HttpURLConnection) {
              final HttpURLConnection connection = (HttpURLConnection) urlConnection;
              try {
                connection.setRequestMethod("GET");
                final Iterator<Pair<String, String>> propertiesIterator = task.getPropertiesIterator();
                while (propertiesIterator.hasNext()) {
                  Pair<String, String> nextProp = propertiesIterator.next();
                  connection.setRequestProperty(nextProp.getFirst(), nextProp.getSecond());
                }
                connection.connect();
                String encoding = URLConnectionTools.determineEncoding(connection);
                if (encoding == null) {
                  encoding = DEFAULT_ENCODING;
                }
                Charset charset;
                try {
                  charset = Charset.forName(encoding);
                } catch (UnsupportedCharsetException ex) {
                  encoding = DEFAULT_ENCODING;
                  charset = Charset.forName(encoding);
                }
                final CharSequence content = StreamTools.readStream(connection.getInputStream(), charset);
                textTask.start(Pair.create(URLStatusImpl.create(url, encoding, connection), content));
              }
              catch (IOException e) {
                textTask.start(Pair.create(URLStatusImpl.create(url, e), (CharSequence) null));
              }
            } else {
              textTask.start(Pair.create(URLStatusImpl.create(url, new IOException("Head method can be called for http protocol only")), (CharSequence) null));
            }
            break;
          case POST:
            Task<Pair<URLStatus, CharSequence>> postTask = (Task<Pair<URLStatus, CharSequence>>) task;
            if (ioe != null) {
              postTask.start(Pair.create(URLStatusImpl.create(url, ioe), (CharSequence) null));
            } else if (urlConnection instanceof HttpURLConnection) {
              final HttpURLConnection connection = (HttpURLConnection) urlConnection;
              try {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                final Iterator<Pair<String, String>> propertiesIterator = postTask.getPropertiesIterator();
                String msg = "";
                String encoding = "";
                while (propertiesIterator.hasNext()) {
                  Pair<String, String> nextProp = propertiesIterator.next();
                  if (MAGIC_POST_REQUEST.equals(nextProp.getFirst())) {
                    msg = nextProp.getSecond();
                  } else {
                    if ("content-type".equalsIgnoreCase(nextProp.getFirst())) {
                      encoding = URLConnectionTools.extractEncoding(nextProp.getSecond());
                    }
                    connection.setRequestProperty(nextProp.getFirst(), nextProp.getSecond());
                  }
                }

                if (encoding == null) {
                  encoding = DEFAULT_ENCODING;
                }

                connection.connect();
                final Writer writer = new OutputStreamWriter(connection.getOutputStream(), encoding);
                writer.write(msg);
                writer.close();
                encoding = URLConnectionTools.determineEncoding(connection);
                if (encoding == null) {
                  encoding = DEFAULT_ENCODING;
                }
                final CharSequence content = StreamTools.readStream(connection.getInputStream(), Charset.forName(encoding));
                postTask.start(Pair.create(URLStatusImpl.create(url, encoding, connection), content)
                );
              }
              catch (IOException e) {
                postTask.start(Pair.create(URLStatusImpl.create(url, e), (CharSequence) null));
              }
            } else {
              postTask.start(Pair.create(URLStatusImpl.create(url, new IOException("Post method can be called for http protocol only")), (CharSequence) null));
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
        synchronized (hostPath2connectionCount) {
          String hostPath;
          Pair<String, Policy> path2CustomPolicy = getPolicyForURL(url);
          if (path2CustomPolicy != null) {
            hostPath = path2CustomPolicy.getFirst();
          } else {
            hostPath = URI.create(url).getHost();
          }
          Integer conectionCount = hostPath2connectionCount.get(hostPath);
          if (conectionCount != null && conectionCount > 0) {
            hostPath2connectionCount.put(hostPath, conectionCount - 1);
          }
        }
      }
    }
  }

  public static final Policy DEFAULT_POLICY = new Policy(10, 10, 1000);
}
