package com.spbsu.commons;

import junit.framework.TestCase;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * User: Igor Kuralenok
 * Date: 24.05.2006
 */
public abstract class FileTestCase extends TestCase {
  public static final File TESTS_DIR = new File("./wd/tests");
  protected String fileText;

  protected void setUp() throws Exception {
    try{
      final String ext = getInputFileExtension();
      if(ext != null){
        final FileInputStream fileInputStream = new FileInputStream(getTestDataPath() + getTestName() + ext);
        fileText = new String(readStream(fileInputStream), "UTF-8");
      }
    }
    catch(IOException ioe) {
      fileText = null;
//      ioe.printStackTrace();
    }
  }

  protected abstract String getInputFileExtension();
  protected abstract String getResultFileExtension();
  protected abstract String getTestDataPath();

  protected void checkResultByFile(CharSequence result) throws IOException {
    final String resultsFileName = getTestDataPath() + getTestName() + getResultFileExtension();
    try{
      final byte[] bytes;
      if (!new File(resultsFileName).exists() && new File(resultsFileName + ".gz").exists()) {
        bytes = readStream(new GZIPInputStream(new FileInputStream(resultsFileName + ".gz")));
      }
      else {
        bytes = readStream(new FileInputStream(resultsFileName));
      }
      assertEquals(new String(bytes, "UTF-8"), result.toString());
    }
    catch(FileNotFoundException ioe) {
      System.out.println("Results file not found, created");

      final OutputStream os;
      if (result.length() > 1000000)
        os = new GZIPOutputStream(new FileOutputStream(resultsFileName + ".gz"));
      else
        os = new FileOutputStream(resultsFileName);
      os.write(result.toString().getBytes("UTF-8"));
      os.close();
      assertEquals("", result);
    }
  }

  private String getTestName() {
    final String name = getName();
    return Character.toLowerCase(name.charAt(4)) + name.substring(5);
  }

  public static byte[] readStream(InputStream inputStream) throws IOException {
    byte[] result = new byte[512 * 1024];
    final byte[] buffer = new byte[256 * 1024];
    int read;
    int size = 0;
    while ((read = inputStream.read(buffer)) >= 0) {
      if(result.length < size + read) {
        byte[] newResult = new byte[size + read];
        System.arraycopy(result, 0, newResult, 0, size);
        result = newResult;
      }
      System.arraycopy(buffer, 0, result, size, read);
      size += read;
    }
    if(result.length > size){
      byte[] newResult = new byte[size];
      System.arraycopy(result, 0, newResult, 0, size);
      result = newResult;
    }

    return result;
  }
}
