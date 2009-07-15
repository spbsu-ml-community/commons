package com.spbsu.net;

import com.spbsu.util.Pair;
import com.spbsu.util.Task;

import java.io.IOException;

/**
 * User: solar
 * Date: 10.06.2007
 */
public interface DownloadManager {
  void request(String url, Task<CharSequence> resultHandler);
  void requestCharData(String url, Task<Pair<URLStatus, CharSequence>> resultHandler);
  void requestBinaryData(String url, Task<Pair<URLStatus, byte[]>> resultHandler);
  void requestStatus(String url, Task<URLStatus> resultHandler);

  void cancelRequest(Task<?> resultHandler);
  void waitFor(Task<?>... resultHandler);

  void setCustomPolicy(String host, Policy policy);

  interface URLStatus{
    int getStatusCode();
    Pair<String, Integer>[] getRedirectPath();
    String getContentType();
    String getContentEncoding();

    IOException getException();
  }
}
