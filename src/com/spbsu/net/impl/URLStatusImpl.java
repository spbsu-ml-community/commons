package com.spbsu.net.impl;

import com.spbsu.net.DownloadManager;
import com.spbsu.util.Logger;
import com.spbsu.util.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;


/**
 * User: solar
 * Date: 10.06.2007
 */
public class URLStatusImpl implements DownloadManager.URLStatus {
  private static final Logger LOG = Logger.create("com.spbsu.net.impl.URLStatusImpl");
  private final String encoding;
  private final int statusCode;
  private Pair<String, Integer>[] redirects;
  private final String contentType;
  private final IOException exception;
  private final String url;


  public URLStatusImpl(String url, IOException exception) {
    encoding = null;
    statusCode = -1;
    this.url = url;
    contentType = null;
    this.exception = exception;
  }

  public URLStatusImpl(String url, String encoding, int statusCode, String contentType) {
    this.encoding = encoding;
    this.statusCode = statusCode;
    this.contentType = contentType;
    this.exception = null;
    this.url = url;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Pair<String, Integer>[] getRedirectPath() {
    if(redirects != null) return redirects;
    final List<Pair<String, Integer>> found = new LinkedList<Pair<String, Integer>>();
    try {
      URL base = null;
      String location = url;
      while(location != null){
        URL url;
        if(base != null) url = new URL(base, location);
        else url = new URL(location);
        final URLConnection urlConnection = url.openConnection();
        urlConnection.setReadTimeout(DownloadManagerImpl.READ_TIMEOUT);
        urlConnection.setConnectTimeout(DownloadManagerImpl.CONNECTION_TIMEOUT);
        if(urlConnection instanceof HttpURLConnection){
          final HttpURLConnection connection = (HttpURLConnection) urlConnection;
          connection.setInstanceFollowRedirects(false);
          connection.connect();
          final int rc = connection.getResponseCode();
          found.add(new Pair<String, Integer>(url.toString(), rc));
          base = url;
          location = null;
          if(rc > 300 && rc < 400) // increment
            location = connection.getHeaderField("Location");
        }
        else {
          found.add(new Pair<String, Integer>(url.toString(), -1));
          break;
        }
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }

    //noinspection unchecked
    return redirects = found.toArray(new Pair[found.size()]);
  }

  public String getContentType() {
    return contentType;
  }

  public String getContentEncoding() {
    return encoding;
  }

  public IOException getException() {
    return exception;
  }
}
