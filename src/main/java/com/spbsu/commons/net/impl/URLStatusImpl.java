package com.spbsu.commons.net.impl;

import com.spbsu.commons.util.logging.Logger;
import com.spbsu.commons.util.Pair;
import com.spbsu.commons.net.DownloadManager;
import com.spbsu.commons.net.URLConnectionTools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;


/**
 * User: solar
 * Date: 10.06.2007
 * Time: 17:25:29
 */
public final class URLStatusImpl implements DownloadManager.URLStatus {
  private static final Logger LOG = Logger.create(URLStatusImpl.class);
  private final String encoding;
  private final int statusCode;
  private Pair<String, Integer>[] redirects;
  private final String contentType;
  private final IOException exception;
  private final String url;
  private long contentLength;
  private long lastModified;


  private URLStatusImpl(final String url, final IOException exception) {
    encoding = null;
    statusCode = -1;
    this.url = url;
    contentType = null;
    this.exception = exception;
  }

  public URLStatusImpl(final String url, final String encoding, final int statusCode, final String contentType, final long contentLength) {
    this.encoding = encoding;
    this.statusCode = statusCode;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.exception = null;
    this.url = url;
  }

  private URLStatusImpl(final String url, final String encoding, final int statusCode, final String contentType, final long contentLength, final long lastModified) {
    this.encoding = encoding;
    this.statusCode = statusCode;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.exception = null;
    this.url = url;
    this.lastModified = lastModified;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public long getLastModified() {
    return lastModified;
  }

  public Pair<String, Integer>[] getRedirectPath() {
    if (redirects != null) {
      return redirects;
    }
    final List<Pair<String, Integer>> found = new LinkedList<Pair<String, Integer>>();
    try {
      URL base = null;
      String location = url;
      while (location != null) {
        final URL url;
        if (base != null) {
          url = new URL(base, location);
        } else {
          url = new URL(location);
        }
        final URLConnection urlConnection = url.openConnection();
        urlConnection.setReadTimeout(DownloadManagerImpl.READ_TIMEOUT);
        urlConnection.setConnectTimeout(DownloadManagerImpl.CONNECTION_TIMEOUT);
        if (urlConnection instanceof HttpURLConnection) {
          final HttpURLConnection connection = (HttpURLConnection) urlConnection;
          connection.setInstanceFollowRedirects(false);
          connection.connect();
          final int rc = connection.getResponseCode();
          found.add(Pair.create(url.toString(), rc));
          base = url;
          location = null;
          if (rc > 300 && rc < 400) {
            location = connection.getHeaderField("Location");
          }
        } else {
          found.add(Pair.create(url.toString(), -1));
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

  public long getContentlength() {
    return contentLength;
  }

  public String getContentEncoding() {
    return encoding;
  }

  public IOException getException() {
    return exception;
  }


  public static DownloadManager.URLStatus create(final String url, final String encoding, final HttpURLConnection connection)
      throws IOException {
    return new URLStatusImpl(
        url,
        encoding,
        connection.getResponseCode(),
        URLConnectionTools.extractContentType(connection.getContentType()),
        connection.getContentLength(),
        connection.getLastModified()
    );
  }

  public static DownloadManager.URLStatus create(final String url, final IOException e) {
    return new URLStatusImpl(url, e);
  }
}
