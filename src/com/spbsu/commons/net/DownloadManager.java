package com.spbsu.commons.net;

import com.spbsu.commons.util.Pair;

import java.io.IOException;
import java.net.Proxy;

/**
 * User: solar
 * Date: 10.06.2007
 * Time: 13:55:36
 */
public interface DownloadManager {
  void request(String url, Task<CharSequence> resultHandler);
  void requestCharData(String url, Task<Pair<URLStatus, CharSequence>> resultHandler);
  void requestBinaryData(String url, Task<Pair<URLStatus, byte[]>> resultHandler);
  void requestStatus(String url, Task<URLStatus> resultHandler);
  void requestCharDataWithPost(String url, String msg, Task<Pair<URLStatus, CharSequence>> resultHandler);

  void cancelRequest(Task<?> resultHandler);
  void waitFor(Task<?>... resultHandler);

  void setCustomPolicy(String host, Policy policy);
  void setProxy(String host, Proxy proxy);

  interface URLStatus{
    int getStatusCode();
    Pair<String, Integer>[] getRedirectPath();
    String getContentType();    
    long getContentlength();
    String getContentEncoding();

    IOException getException();
    long getLastModified();
  }
}
