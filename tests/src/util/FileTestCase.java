package util;

import junit.framework.TestCase;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 24.05.2006
 * Time: 20:52:05
 * To change this template use File | Settings | File Templates.
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

  protected void checkResultByFile(String result) throws IOException {
    final String resultsFileName = getTestDataPath() + getTestName() + getResultFileExtension();
    try{
      final byte[] bytes = readStream(new FileInputStream(resultsFileName));
      assertEquals(new String(bytes, "UTF-8"), result);
    }
    catch(FileNotFoundException ioe) {
      System.out.println("Results file not found, created");
      final FileOutputStream fileOutputStream = new FileOutputStream(resultsFileName);
      fileOutputStream.write(result.getBytes("UTF-8"));
      assertEquals("", result);
    }
  }

  private String getTestName() {
    final String name = getName();
    return Character.toLowerCase(name.charAt(4)) + name.substring(5);
  }

  public static byte[] readStream(InputStream inputStream) throws IOException {
    byte[] result = new byte[128];
    final byte[] buffer = new byte[256];
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
