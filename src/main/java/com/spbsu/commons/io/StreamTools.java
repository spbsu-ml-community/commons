package com.spbsu.commons.io;

import com.spbsu.commons.func.Processor;
import com.spbsu.commons.seq.CharSeqBuilder;
import com.spbsu.commons.util.logging.Logger;
import gnu.trove.list.array.TByteArrayList;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: terry
 * Date: 10.10.2009
 */
public class StreamTools {
  public static final Charset UTF = Charset.forName("UTF-8");
  private static final Logger LOG = Logger.create(StreamTools.class);
  private static final int BUFFER_LENGTH = 4096;
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private StreamTools() {
  }

  public static CharSequence readReader(final Reader reader) throws IOException {
    final char[] buffer = new char[BUFFER_LENGTH];
    final CharSeqBuilder stringBuilder = new CharSeqBuilder();
    int read;
    while ((read = reader.read(buffer)) != -1) {
      stringBuilder.append(buffer, 0, read);
    }
    return stringBuilder.build();
  }

  public static CharSequence readReaderNoIO(final Reader reader) {
    try {
      return readReader(reader);
    } catch (IOException e) {
      throw new RuntimeException("unexpected io error: ", e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        throw new RuntimeException("unexpected io error: ", e);
      }
    }
  }

  public static CharSequence readStream(final InputStream stream) throws IOException {
    return readStream(stream, DEFAULT_CHARSET);
  }

  public static byte[] readByteStream(final InputStream stream) throws IOException {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    transferData(stream, output);
    return output.toByteArray();
  }

  public static CharSequence readStream(final InputStream stream, final Charset charset) throws IOException {
    try(final Reader reader = new InputStreamReader(new BufferedInputStream(stream), charset)) {
      return StreamTools.readReader(reader);
    }
  }

  public static CharSequence readFile(final File file, final Charset charset) throws IOException {
    try (final InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      return readReader(new InputStreamReader(in, charset));
    }
  }

  public static void readFile(final File file, final Processor<CharSequence> lineProcessor) throws IOException {
    readFile(file, DEFAULT_CHARSET, lineProcessor);
  }

  public static void readFile(final File file, final Charset charset, final Processor<CharSequence> lineProcessor) throws IOException {
    final LineNumberReader reader = new LineNumberReader(
        new InputStreamReader(new FileInputStream(file), charset), BUFFER_LENGTH);
    int lineCounter = 0;
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        lineProcessor.process(line);
        lineCounter++;
      }
    } catch (Exception th) {
      throw new RuntimeException("line number = " + lineCounter, th);
    } finally {
      reader.close();
    }
  }

  public static CharSequence readFile(final File file) throws IOException {
    return readFile(file, DEFAULT_CHARSET);
  }

  public static void transferData(final InputStream in, final OutputStream out) throws IOException {
    transferData(in, out, false);
  }

  public static void transferData(final InputStream in, final OutputStream out, final boolean sync) throws IOException {
    final byte[] buffer = new byte[BUFFER_LENGTH];
    final InputStream bufferedIn = new BufferedInputStream(in);
    final OutputStream bufferedOut = new BufferedOutputStream(out);
    int read;
    while ((read = bufferedIn.read(buffer)) != -1) {
      bufferedOut.write(buffer, 0, read);
      if (sync)
        bufferedOut.flush();
    }
    if (!sync)
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

  public static void deleteDirectoryWithContents(final File dir) {
    final File[] files = dir.listFiles();
    for (final File file : files) {
      if (file.isDirectory()) {
        deleteDirectoryWithContents(file);
      } else {
        file.delete();
      }
    }
    dir.delete();
  }

  public static void deleteDirectoryContents(final File dir) {
    final File[] files = dir.listFiles();
    for (final File file : files) {
      if (file.isDirectory()) {
        deleteDirectoryWithContents(file);
      } else {
        file.delete();
      }
    }
  }

  public static CharSequence readURL(final String urlStr) throws IOException {
    return readURL(urlStr, DEFAULT_CHARSET);
  }

  public static CharSequence readURL(final String urlStr, final Charset charset) throws IOException {
    InputStream inputStream = null;
    try {
      final URL url = new URL(urlStr);
      final URLConnection urlConnection = url.openConnection();
      urlConnection.setDoOutput(true);
      urlConnection.setDoInput(true);
      inputStream = urlConnection.getInputStream();
      return readStream(inputStream, charset);
    } catch (IOException ex) {
      LOG.warn("exception caught", ex);
      throw ex;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  public static Reader communicate(final String urlStr, final CharSequence toSend) {
    OutputStream outputStream = null;
    try {
      final URL url = new URL(urlStr);
      final URLConnection urlConnection = url.openConnection();
      urlConnection.setDoOutput(true);
      urlConnection.setDoInput(true);
      outputStream = urlConnection.getOutputStream();
      final OutputStreamWriter writer = new OutputStreamWriter(outputStream, DEFAULT_CHARSET.name());
      writer.append(toSend);
      writer.close();
      return new InputStreamReader(urlConnection.getInputStream(), DEFAULT_CHARSET.name());
    }
    catch (IOException e) {
      LOG.warn("exception caught", e);
    }
    finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          LOG.warn("exception caught", e);
        }
      }
    }
    return null;
  }

  public static void writeChars(final CharSequence sequence, final File file) throws IOException {
    writeChars(sequence, file, DEFAULT_CHARSET);
  }

  public static void writeChars(final CharSequence sequence, final File file, final Charset charset) throws IOException {
    final PrintWriter writer = new PrintWriter(file, charset.name());
    try {
      writer.print(sequence);
    } finally {
      writer.close();
    }
  }

  public static void writeBytes(final byte[] bytes, final File file) throws IOException {
    final DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
    try {
      outputStream.write(bytes);
    } finally {
      outputStream.close();
    }
  }

  public static byte[] readFileBytes(final File file) throws IOException {
    DataInputStream dataInputStream = null;
    try {
      final byte[] bytes = new byte[(int) file.length()];
      dataInputStream = new DataInputStream(new FileInputStream(file));
      dataInputStream.readFully(bytes);
      return bytes;
    }
    finally {
      if (dataInputStream != null) {
        dataInputStream.close();
      }
    }
  }

  public static void unzipDirectory(final File zip, final File outDir) throws IOException {
    if (!outDir.mkdirs()) throw new IOException("Cannot write to directory " + outDir);
    final ZipInputStream input = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
    try {
      ZipEntry zipEntry;
      while ((zipEntry = input.getNextEntry()) != null) {
        try {
          transferData(input, new FileOutputStream(new File(outDir, zipEntry.getName())));
        }
        finally {
          input.closeEntry();
        }
      }
    }
    finally {
      input.close();
    }
  }

  public static TByteArrayList transformByExternalCommand(final String command, final InputStream input) throws IOException {
    final Process process = Runtime.getRuntime().exec(command);
    final InputStream in = process.getInputStream();
    final OutputStream out = process.getOutputStream();
    final TByteArrayList bytes = new TByteArrayList(100500);
    final byte[] buffer = new byte[1024 * 1024];
    try {
      int read;
      while ((read = input.read(buffer)) > 0) {
        out.write(buffer, 0, read);
        if (in.available() > 0) {
          read = in.read(buffer);
          bytes.add(buffer, 0, read);
        }
      }
      out.close();
      while ((read = in.read(buffer)) > 0) {
        bytes.add(buffer, 0, read);
      }
      in.close();
    }
    catch (IOException ioe) {
      System.err.println(readByteStream(process.getErrorStream()));
      LOG.error(ioe);
      throw ioe;
    }
    return bytes;
  }

  public static void writeChars(final CharSequence command, final OutputStream outputStream) {
    try (final OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"))) {
      writer.write(command.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void appendChars(final CharSequence seq, final File file) {
    try(final FileOutputStream outputStream = new FileOutputStream(file, true)) {
      writeChars(seq, outputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void visitFiles(final File dir, final Processor<String> processor) {
    final String absolutePath = dir.getAbsolutePath();
    visitFiles(dir, processor, absolutePath.endsWith("/") ? absolutePath.length() : absolutePath.length() + 1);
  }

  private static void visitFiles(final File file, final Processor<String> processor, final int prefix) {
    final String absolutePath = file.getAbsolutePath();
    processor.process(prefix < absolutePath.length() ? absolutePath.substring(prefix) : "");
    if (file.isDirectory()) {
      for (final String next : file.list()) {
        visitFiles(new File(file, next), processor, prefix);
      }
    }
  }
}
