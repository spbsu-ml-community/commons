package com.spbsu.util;

import com.spbsu.util.cache.CacheStrategy;
import com.spbsu.util.cache.FixedSizeCache;
import com.spbsu.util.nio.BufferFactory;
import com.spbsu.util.nio.RWBuffer;

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
  private FixedSizeCache<Integer, RWBuffer> slots;

  public FileCache(RandomAccessFile cachingFile, int slotCapacity, int cacheCapacity) {
    this.cachingFile = cachingFile.getChannel();
    this.slotCapacity = slotCapacity;
    slots = new FixedSizeCache<Integer, RWBuffer>(cacheCapacity, CacheStrategy.Type.LRU);
    //todo: may be not WeakMap.
  }

  public RWBuffer getPage(int fileOffset) throws IOException {
    final int pageID = fileOffset / slotCapacity;
    final RWBuffer page = slots.get(pageID);
    return page != null ? page : loadPage(pageID);
  }

  public RWBuffer getPageByNo(int pageNum) throws IOException {
    final RWBuffer page = slots.get(pageNum);
    return page != null ? page : loadPage(pageNum);
  }

  private RWBuffer loadPage(int pageID) throws IOException {
    final RWBuffer buffer;
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
