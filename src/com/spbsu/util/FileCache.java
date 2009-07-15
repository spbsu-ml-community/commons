package com.spbsu.util;

import com.spbsu.util.cache.FixedSizeCache;
import com.spbsu.util.cache.CacheStrategy;

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
  private FixedSizeCache<Integer, ByteBuffer> slots;

  public FileCache(RandomAccessFile cachingFile, int slotCapacity, int cacheCapacity) {
    this.cachingFile = cachingFile.getChannel();
    this.slotCapacity = slotCapacity;
    slots = new FixedSizeCache<Integer, ByteBuffer>(cacheCapacity, CacheStrategy.Type.LRU);
    //todo: may be not WeakMap.
  }

  public ByteBuffer getPage(int fileOffset) throws IOException {
    final int pageID = fileOffset / slotCapacity;
    final ByteBuffer page = slots.get(pageID);
    return page != null ? page : loadPage(pageID);
  }

  public ByteBuffer getPageByNo(int pageNum) throws IOException {
    final ByteBuffer page = slots.get(pageNum);
    return page != null ? page : loadPage(pageNum);
  }

  private ByteBuffer loadPage(int pageID) throws IOException {
    final ByteBuffer buffer;
    final int pageOffset = pageID * slotCapacity;
    if (cachingFile.size() < pageOffset + slotCapacity) {
      final int buffSize = pageOffset + slotCapacity - (int) cachingFile.size();
      final byte[] tmpBuff = new byte[buffSize];
      Arrays.fill(tmpBuff, (byte) 0);
      cachingFile.position(cachingFile.size()).write(ByteBuffer.wrap(tmpBuff));
    }
    buffer = cachingFile.map(FileChannel.MapMode.READ_WRITE, pageOffset, slotCapacity);
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
