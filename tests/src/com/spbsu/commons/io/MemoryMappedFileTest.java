package com.spbsu.commons.io;

import com.spbsu.commons.io.persist.MemoryMappedFile;
import com.spbsu.commons.util.cache.CacheStrategy;
import com.spbsu.commons.util.cache.impl.FixedSizeCache;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: terry
 * Date: 07.11.2009
 * Time: 17:01:54
 * To change this template use File | Settings | File Templates.
 */
public class MemoryMappedFileTest extends TestCase {

  private MemoryMappedFile memoryMappedFile;
  private File tmpFile;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tmpFile = File.createTempFile("persist", "");
  }

  @Override
  protected void tearDown() throws Exception {
    if (memoryMappedFile != null) memoryMappedFile.close();
  }

  private MemoryMappedFile createMemoryMappedFile(File file, int pageSize, int cacheSize) {
    try {
      return memoryMappedFile = new MemoryMappedFile(file, "rw", pageSize,
          new FixedSizeCache<Integer, Buffer>(cacheSize, CacheStrategy.Type.LRU));
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
    throw new RuntimeException();
  }

  private MemoryMappedFile createMemoryMappedFile(int pageSize, int cacheSize) {
    return createMemoryMappedFile(tmpFile, pageSize, cacheSize);
  }

  public void testCreateAndGetPages() throws IOException {
    final MemoryMappedFile memoryMappedFile = createMemoryMappedFile(8, 5);
    assertEquals(0, memoryMappedFile.pagesCount());
    int index = memoryMappedFile.createNewPage();
    assertNotNull(memoryMappedFile.getPageByIndex(index));
    assertEquals(1, memoryMappedFile.pagesCount());
    index = memoryMappedFile.createNewPage();
    assertEquals(2, memoryMappedFile.pagesCount());
    assertNotNull(memoryMappedFile.getPageByIndex(index));
  }

  public void testWriteAndReopen() throws IOException {
    final MemoryMappedFile memoryMappedFile = createMemoryMappedFile(8, 5);
    int index = memoryMappedFile.createNewPage();
    Buffer buffer = memoryMappedFile.getPageByIndex(index);
    buffer.putInt(12345);
    assertEquals(12345, memoryMappedFile.getPageByIndex(index).getInt());
    memoryMappedFile.close();

    MemoryMappedFile mappedFile = createMemoryMappedFile(tmpFile, 8, 5);
    assertEquals(1, mappedFile.pagesCount());
    Buffer byIndex = mappedFile.getPageByIndex(0);
    assertEquals(12345, byIndex.getInt());
  }

  public void testWriteAndReadCacheMiss() throws IOException {
    final MemoryMappedFile memoryMappedFile = createMemoryMappedFile(8, 1);
    int index = memoryMappedFile.createNewPage();
    Buffer buffer = memoryMappedFile.getPageByIndex(index);
    buffer.putInt(12345);
    int nextIndex = memoryMappedFile.createNewPage();
    buffer = memoryMappedFile.getPageByIndex(nextIndex);
    assertEquals(0, buffer.getInt());

    Buffer notCachedBuffer = memoryMappedFile.getPageByIndex(index);
    assertEquals(12345, notCachedBuffer.getInt());
  }
}
