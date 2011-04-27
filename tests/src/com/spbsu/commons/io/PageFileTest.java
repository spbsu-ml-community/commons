package com.spbsu.commons.io;

import com.spbsu.commons.io.persist.PageFile;
import com.spbsu.commons.io.persist.PageFileAddress;
import com.spbsu.commons.io.persist.impl.PageFileImpl;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * User: igorkuralenok
 * Date: 15.10.2009
 * Time: 15:17:35
 */
public class PageFileTest extends TestCase {
  private static final String SAMPLE_STRING = "sick my duck";

  private PageFile pageFile;
  private File file;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    file = File.createTempFile("persist", "");
    pageFile = new PageFileImpl(file);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    pageFile.close();
  }

  private static void assertEquals(String source, Buffer buffer) throws IOException {
    assertEquals(source.getBytes("UTF-8").length, buffer.remaining());
    byte[] array = new byte[buffer.remaining()];
    buffer.get(array);
//    for (int i = 0; i < array.length; i++) {
//      array[i] = buffer.getByte();
//    }
    assertEquals(source, new String(array, "UTF-8"));
  }

  private String createBigData(int factor) {
    StringBuilder bigString = new StringBuilder("");
    for (int i = 0; i < factor; i++)
      bigString.append(SAMPLE_STRING);
    return bigString.toString();
  }

  private String createBigData() {
    StringBuilder bigString = new StringBuilder("");
    for (int i = 0; i < 100000; i++)
      bigString.append(SAMPLE_STRING);
    return bigString.toString();
  }

  public void testWriteReadLittleData() throws IOException {
    final PageFileAddress address = pageFile.write(BufferFactory.wrap("xxx".getBytes("UTF-8")));
    final Buffer buffer = pageFile.read(address);
    assertEquals("xxx", buffer);
  }

  public void testWriteReadSinglePage() throws IOException {
    final PageFileAddress address = pageFile.write(BufferFactory.wrap(SAMPLE_STRING.getBytes("UTF-8")));
    final Buffer buffer = pageFile.read(address);
    assertEquals(SAMPLE_STRING, buffer);
  }

  public void testWriteReopenAndReadSinglePage() throws IOException {
    final PageFileAddress address = pageFile.write(BufferFactory.wrap(SAMPLE_STRING.getBytes("UTF-8")));
    pageFile.close();

    pageFile = new PageFileImpl(file);
    final Buffer buffer = pageFile.read(address);
    assertEquals(SAMPLE_STRING, buffer);
  }

  public void testWriteReopenReadManyPages() throws IOException {
    final String bigString = createBigData();

    PageFileAddress address = pageFile.write(BufferFactory.wrap(bigString.getBytes("UTF-8")));
    Buffer buffer = pageFile.read(address);
    assertEquals(bigString, buffer);
    pageFile.close();

    pageFile = new PageFileImpl(file); // isn't it a reopen?
    buffer = pageFile.read(address);
    assertEquals(bigString, buffer);
  }

  public void testWriteBufferParamsAfterWrite() throws Exception {
    final Buffer source = BufferFactory.wrap(SAMPLE_STRING.getBytes("UTF-8"));
    final PageFileAddress address = pageFile.write(source);
    assertEquals(source.capacity(), source.limit());
    assertEquals(0, source.position());
  }

  public void testWriteCleanUpAndWriteAgainInOnePage() throws Exception {
    for (int i = 0; i < 10000; i++) {
      Buffer source = BufferFactory.wrap(createBigData(100).getBytes("UTF-8"));
      final PageFileAddress address = pageFile.write(source);
      assertTrue(address.getPageNo() <= 2);
      pageFile.enqueueForCleanup(address);
      pageFile.cleanup();
    }
  }

  public void testWriteCleanUpAndWriteAgainInOnePageReopen() throws Exception {
    for (int i = 0; i < 200; i++) {
      Buffer source = BufferFactory.wrap(createBigData(100).getBytes("UTF-8"));
      final PageFileAddress address = pageFile.write(source);
      assertTrue(address.getPageNo() <= 2);
      pageFile.enqueueForCleanup(address);
      pageFile.cleanup();
      pageFile.close();
      pageFile = new PageFileImpl(file);
    }
  }

  public void testWriteCleanUpAndWriteAgainInManyPagesReopen() throws Exception {
    Buffer source = BufferFactory.wrap(createBigData(100).getBytes("UTF-8"));
    final PageFileAddress address = pageFile.write(source);
    assertTrue(address.getPageNo() <= 2);

    Buffer source2 = BufferFactory.wrap(createBigData(100).getBytes("UTF-8"));
    final PageFileAddress address2 = pageFile.write(source2);

    pageFile.enqueueForCleanup(address);
    pageFile.cleanup();
    pageFile.close();
    pageFile = new PageFileImpl(file);

    Buffer source3 = BufferFactory.wrap(createBigData(100).getBytes("UTF-8"));
    final PageFileAddress address3 = pageFile.write(source3);
    assertEquals(1, address3.getPageNo());
  }

  public void testWriteCompositeCleanUpAndWriteAgainInManyPagesReopen() throws Exception {
    final Random random = new Random();
    final byte[] array = new byte[2 << 18];
    for (int i = 0; i < array.length; i++) {
      array[i] = (byte) random.nextInt(array.length);
    }

    Buffer source = BufferFactory.wrap(array);
    final PageFileAddress address = pageFile.write(source);

    Buffer source2 = BufferFactory.wrap(createBigData(100).getBytes("UTF-8"));
    final PageFileAddress address2 = pageFile.write(source2);

    final Buffer buffer = pageFile.read(address);
    for (int i = 0; i < array.length; i++) {
//      System.out.println("i = " + i);
      assertEquals((byte)array[i], (byte)buffer.getByte(i));
    }
    pageFile.enqueueForCleanup(address);
    pageFile.cleanup();
    pageFile.close();
    pageFile = new PageFileImpl(file);

    Buffer source3 = BufferFactory.wrap(new byte[2 << 18]);
    final PageFileAddress address3 = pageFile.write(source3);
    assertEquals(address.getLength(), address3.getLength());
  }

  public void testWriteCleanUpAndWriteAgainManyPagesReopen() throws Exception {
    final String bigString = createBigData();
    Buffer source = BufferFactory.wrap(bigString.getBytes("UTF-8"));
    Buffer source2 = BufferFactory.wrap(bigString.getBytes("UTF-8"));
    final PageFileAddress address = pageFile.write(source);
    long size = pageFile.size();
    pageFile.enqueueForCleanup(address);
    pageFile.cleanup();
    pageFile.close();
    pageFile = new PageFileImpl(file);
    final PageFileAddress address2 = pageFile.write(source2);
    assertEquals(size, pageFile.size());
//    assertEquals(address.getPageNo(), address2.getPageNo()); //todo:empty addresses order backward
//    assertEquals(address.getOffset(), address2.getOffset());
  }

  public void testWriteReadReopenCheckSize() throws Exception {
    final String bigString = createBigData();
    final Buffer source = BufferFactory.wrap(bigString.getBytes("UTF-8"));
    final PageFileAddress address = pageFile.write(source);
    final long size = pageFile.size();
    pageFile.close();
    pageFile = new PageFileImpl(file);
    pageFile.read(address);
    assertEquals(size, pageFile.size());
  }

  public void testCleanup1() throws IOException {
    final Buffer buffer = BufferFactory.wrap(SAMPLE_STRING.getBytes("UTF-8"));
    for (int i = 0; i < 100000; i++) {
      pageFile.enqueueForCleanup(pageFile.write(BufferFactory.duplicate(buffer)));
      pageFile.cleanup();
    }
  }

  public void testCleanup2() throws IOException {
    final Buffer buffer = BufferFactory.wrap(SAMPLE_STRING.getBytes("UTF-8"));
    for (int i = 0; i < 1000; i++) {
      for (int j = 0; j < 1000; j++) {
        pageFile.enqueueForCleanup(pageFile.write(BufferFactory.duplicate(buffer)));
      }
      pageFile.cleanup();
    }
  }

  public void testCleanup3() throws IOException {
    final Buffer buffer = BufferFactory.wrap(new byte[1]);
    pageFile.enqueueForCleanup(pageFile.write(BufferFactory.duplicate(buffer)));
    pageFile.cleanup();
  }

//  public void testCompositeCleanup1() throws IOException {
//    final String bigString = createBigData();
//
//    for (int i = 0; i < 1000; i++) {
//      for (int j = 0; j < 1000; j++) {
//        final PageFileAddress address = pageFile.write(BufferFactory.wrap(bigString.getBytes("UTF-8")));
//        pageFile.enqueueForCleanup(address);
//      }
//      pageFile.cleanup();
//    }
//  }
}
