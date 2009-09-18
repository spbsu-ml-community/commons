package com.spbsu.util;

import com.spbsu.util.cache.CacheStrategy;
import com.spbsu.util.cache.FixedSizeCache;
import com.spbsu.util.nio.Buffer;
import com.spbsu.util.nio.BufferFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * User: Dunisher
 * Date: 03.02.2006
 */
public class FileCache {
  private FileChannel cachingFile;
  private int slotCapacity;
  private FixedSizeCache<Integer, Buffer> slots;

  public FileCache(RandomAccessFile cachingFile, int slotCapacity, int cacheCapacity) {
    this.cachingFile = cachingFile.getChannel();
    this.slotCapacity = slotCapacity;
    slots = new FixedSizeCache<Integer, Buffer>(cacheCapacity, CacheStrategy.Type.LRU);
    //todo: may be not WeakMap.
  }

  public Buffer getPage(int fileOffset) throws IOException {
    final int pageID = fileOffset / slotCapacity;
    final Buffer page = slots.get(pageID);
    return page != null ? page : loadPage(pageID);
  }

  public Buffer getPageByNo(int pageNum) throws IOException {
    final Buffer page = slots.get(pageNum);
    return page != null ? page : loadPage(pageNum);
  }

  public int size() throws IOException {
    return (int)(cachingFile.size() / slotCapacity);
  }

  private Buffer loadPage(int pageID) throws IOException {
    final Buffer buffer;
    final int pageOffset = pageID * slotCapacity;
    if (cachingFile.size() < pageOffset + slotCapacity) {
      final int buffSize = pageOffset + slotCapacity - (int) cachingFile.size();
      final byte[] tmpBuff = new byte[buffSize];
      Arrays.fill(tmpBuff, (byte) 0);
      cachingFile.position(cachingFile.size()).write(ByteBuffer.wrap(tmpBuff));
    }
    buffer = BufferFactory.wrap(cachingFile.map(FileChannel.MapMode.READ_WRITE, pageOffset, slotCapacity));
    slots.put(pageID, buffer);

    return buffer;
  }

  public void close() {
    try {
      cachingFile.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
