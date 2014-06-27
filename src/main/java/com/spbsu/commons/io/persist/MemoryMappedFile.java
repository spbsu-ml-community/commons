package com.spbsu.commons.io.persist;

import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.util.cache.Cache;
import com.spbsu.commons.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * User: terry
 * Date: 03.02.2006
 */

public class MemoryMappedFile {
  private static final Logger LOG = Logger.create(MemoryMappedFile.class);

  private final FileChannel cachingFile;
  private final int pageSize;
  private final Cache<Integer, Buffer> cache;

  public MemoryMappedFile(File file, String mode, int pageSize, Cache<Integer, Buffer> cache) throws IOException {
    this(new RandomAccessFile(file, mode), pageSize, cache);
  }

  public MemoryMappedFile(RandomAccessFile cachingFile, int pageSize, Cache<Integer, Buffer> cache) {
    this.cache = cache;
    this.cachingFile = cachingFile.getChannel();
    this.pageSize = pageSize;
  }


  public Buffer getPageByIndex(int pageIndex) throws IOException {
    Buffer page = cache.get(pageIndex);
    if (page == null) {
      page = loadPage(pageIndex);
      cache.put(pageIndex, page);
    }
    return BufferFactory.duplicate(page);
  }

  public int createNewPage() throws IOException {
    final byte[] tmpBuff = new byte[pageSize];
    Arrays.fill(tmpBuff, (byte) 0);
    cachingFile.position(cachingFile.size()).write(ByteBuffer.wrap(tmpBuff));
    return pagesCount() - 1;
  }

  public int pagesCount() throws IOException {
    return (int) (cachingFile.size() / pageSize);
  }

  boolean notEmpty = false;
  public boolean isEmpty() throws IOException {
    return !notEmpty && !(notEmpty = (pagesCount() > 0));
  }

  private Buffer loadPage(int pageNo) throws IOException {
    final int pageOffset = pageNo * pageSize;
    if (cachingFile.size() < pageOffset + pageSize) {
      throw new IllegalArgumentException("page with number '" + pageNo + "' not exists!");
    }
    return BufferFactory.wrap(cachingFile.map(FileChannel.MapMode.READ_WRITE, pageOffset, pageSize));
  }

  public void cleanup() {
    cache.clear();
  }

  public void close() {
    try {
      /* Closing the channel has no effect on mapping, mapping exists as long as
      * respective Buffers are not GCed, so we will never release physical file at
      * this point. Cache must be cleared and GC directly called to make sure we
      * actually had released the file. 
      * Corresponding test is PersistentMapTest.testCloseAndDeleteFile() */
      cleanup();
      System.gc();
      cachingFile.close();
    } catch (IOException e) {
      LOG.error("fail to close file cache", e);
    }
  }
}
