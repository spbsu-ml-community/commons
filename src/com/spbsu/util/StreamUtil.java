package com.spbsu.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 25.03.2006
 * Time: 16:40:37
 * To change this template use File | Settings | File Templates.
 */
public class StreamUtil {
  private static final Logger LOG = Logger.create("com.spbsu.util.StreamUtil");
  private static final int BUFFER_LENGTH = 4096;

  private StreamUtil() {
  }

  public static CharSequence readReader(final Reader reader) throws IOException {
    char[] buffer = new char[BUFFER_LENGTH];
    final ArrayList<CharSequence> fragments = new ArrayList<CharSequence>();
    int read;
    while ((read = reader.read(buffer)) != -1) {
      fragments.add(new CharArrayCharSequence(buffer, 0, read));
      buffer = new char[BUFFER_LENGTH];
    }
    return new CompositeCharSequence(fragments.toArray(new CharSequence[fragments.size()]));
  }

  public static CharSequence readStream(final InputStream stream) throws IOException {
    return readStream(stream, Charset.forName("UTF-8"));
  }

  public static byte[] readByteStream(final InputStream stream) throws IOException {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    transferData(stream, output);
    return output.toByteArray();
  }

  public static CharSequence readStream(final InputStream stream, final Charset charset) throws IOException, UnsupportedEncodingException {
    Reader reader = null;
    try {
      reader = new InputStreamReader(new BufferedInputStream(stream), charset);
      return StreamUtil.readReader(reader);
    }
    finally {
      if (reader != null) reader.close();
    }
  }

  public static CharSequence readFile(File file) throws IOException {
    return readFile(file, Charset.forName("UTF-8"));
  }

  public static CharSequence readFile(File file, Charset charset) throws IOException {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      return readStream(inputStream, charset);
    }
    finally {
      if (inputStream != null) inputStream.close();
    }
  }

  public static void transferData(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[BUFFER_LENGTH];
    final InputStream bufferedIn = new BufferedInputStream(in);
    final OutputStream bufferedOut = new BufferedOutputStream(out);
    int read;
    while ((read = bufferedIn.read(buffer)) != -1) {
      bufferedOut.write(buffer, 0, read);
    }
    bufferedOut.flush();
  }

  public static void transferData(final Reader in, final Writer out) throws IOException {
    final char[] buffer = new char[BUFFER_LENGTH];
    final Reader bufferedIn = new BufferedReader(in);
    final Writer bufferedOut = new BufferedWriter(out);
    int read;
    while ((read = bufferedIn.read(buffer)) != -1) {
      bufferedOut.write(buffer, 0, read);
    }
    bufferedOut.flush();
  }

  public static void deleteDirectoryWithContents(File dir) {
    final File[] files = dir.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (file.isDirectory()) deleteDirectoryWithContents(file);
        else file.delete();
      }
    }
    dir.delete();
  }

  public static Reader communicate(String urlStr, CharSequence toSend) {
    OutputStream outputStream = null;
    try {
      final URL url = new URL(urlStr);
      final URLConnection urlConnection = url.openConnection();
      urlConnection.setDoOutput(true);
      urlConnection.setDoInput(true);
      outputStream = urlConnection.getOutputStream();
      OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
      writer.append(toSend);
      writer.close();
      return new InputStreamReader(urlConnection.getInputStream(), "UTF-8");
    }
    catch (IOException e) {
      LOG.warning(e);
    }
    finally {
      if (outputStream != null) try {
        outputStream.close();
      }
      catch (IOException e) {
        LOG.warning(e);
      }
    }
    return null;
  }
}
