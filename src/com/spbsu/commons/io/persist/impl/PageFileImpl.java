package com.spbsu.commons.io.persist.impl;

import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.persist.MemoryMappedFile;
import com.spbsu.commons.io.persist.PageFile;
import com.spbsu.commons.io.persist.PageFileAddress;
import com.spbsu.commons.util.cache.CacheStrategy;
import com.spbsu.commons.util.cache.impl.FixedSizeCache;
import com.spbsu.commons.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * User: igorkuralenok
 * Date: 14.10.2009
 * Time: 15:13:51
 */
public class PageFileImpl implements PageFile {
  private static final Logger LOG = Logger.create(PageFileImpl.class);

  public static final short PAGE_SIZE = Short.MAX_VALUE / 2 + 1;

  private static final int PAGE_DEG = degree(PAGE_SIZE);
  private static final int MIN_DEGREE = degree(PageFileAddress.SIZE_OF);

  MemoryMappedFile file;
  PageFileAddress[] emptyStarts;
  private final Queue<PageFileAddress> queue = new LinkedList<PageFileAddress>();

  public PageFileImpl(File file) throws IOException {
    this(file, 1000);
  }

  public PageFileImpl(File file, int cacheSize) throws IOException {
    this.file = new MemoryMappedFile(new RandomAccessFile(file, "rw"), PAGE_SIZE,
      new FixedSizeCache<Integer, Buffer>(cacheSize, CacheStrategy.Type.LRU));
    emptyStarts = new PageFileAddress[PAGE_DEG - MIN_DEGREE + 1];
    if (!this.file.isEmpty()) {
      final Buffer buffer = read(new PageFileAddress(0, (short)0, PAGE_SIZE));
      for (int i = 0; i < emptyStarts.length; i++) {
        buffer.position((emptyStarts.length - i - 1) * PageFileAddress.SIZE_OF);
        emptyStarts[i] = PageFileAddress.CONVERTER.convertFrom(buffer);
      }
      recoverCorruptedPointers();
    } else {
      for (int i = 0; i < emptyStarts.length; i++) {
        emptyStarts[i] = PageFileAddress.UNKNOWN;
        writeHeader(-1 - i, emptyStarts[i]);
      }
    }
  }

  /**
   * If file corrupted flush points on empty pages on UNKNOWN
   */
  private void recoverCorruptedPointers() throws IOException {
    for (int i = 0; i < emptyStarts.length; i++) {
      recoverPointer(i);
    }
  }

  private void recoverPointer(final int degree) throws IOException {
    final PageFileAddress address = emptyStarts[degree];
    final short slotSize = (short)(1 << (degree + MIN_DEGREE));
    if (address.getLength() != slotSize || address.getPageNo() >= file.pagesCount() || address.getOffset() + slotSize > PAGE_SIZE) {
      emptyStarts[degree] = PageFileAddress.UNKNOWN;
      writeHeader(-1 - degree, emptyStarts[degree]);
    }
  }

  public Buffer read(PageFileAddress address) {
    try {
      if (address.getLength() > 0) {
        final Buffer buffer = file.getPageByIndex(address.getPageNo());
        buffer.position(address.getOffset());
        buffer.limit(address.getOffset() + address.getLength());
        return buffer;
      } else {
        final List<Buffer> composite = new LinkedList<Buffer>();
        PageFileAddress next = address;
        while (next.getPageNo() >= 0) {
          final Buffer buffer = read(new PageFileAddress(next.getPageNo(), (short)0, PAGE_SIZE));
          composite.add(buffer);
          next = PageFileAddress.CONVERTER.convertFrom(buffer);
        }
        final Buffer[] buffers = composite.toArray(new Buffer[composite.size()]);
        buffers[buffers.length - 1].limit(next.getLength());
        return BufferFactory.join(buffers);
      }
    }
    catch (IOException e) {
      LOG.warning(e);
    }
    return null;
  }

  public PageFileAddress write(Buffer source) {
    if (source.remaining() == 0)
      return PageFileAddress.UNKNOWN;
    final int size = source.remaining();
    try {
      if (size > PAGE_SIZE) { // creating composite page
        final PageFileAddress start;
        int writenLength = 0;
        PageFileAddress current = start = getEmptyPage(PAGE_SIZE);
        while (size > writenLength) {
          final Buffer buffer = file.getPageByIndex(current.getPageNo());
          writenLength += PAGE_SIZE - PageFileAddress.SIZE_OF;
          current = writenLength < size ?
              getEmptyPage(PAGE_SIZE) : // not last page
              new PageFileAddress(-1, (short)0, (short) ((size - writenLength) + PAGE_SIZE)); // last page
          buffer.position(0);
          BufferFactory.write(PageFileAddress.CONVERTER.convertTo(current), buffer);
          BufferFactory.write(source, buffer);
        }
        source.position(0);
        return new PageFileAddress(start.getPageNo(), (short)0, (short)-1);
      }
      final PageFileAddress address = getEmptyPage(size);

      final Buffer buffer = file.getPageByIndex(address.getPageNo());
      final int start = address.getOffset();
      buffer.position(start);
      buffer.limit(start + size);
      BufferFactory.write(source, buffer);
      source.position(0);
      return new PageFileAddress(address.getPageNo(), address.getOffset(), (short)size);
    }
    catch (IOException ioe) {
      LOG.warning(ioe);
    }
    return null;
  }

  public PageFileAddress readHeader(int index) {
    try {
      final Buffer buffer = file.getPageByIndex(0);
      buffer.position((index + emptyStarts.length) * PageFileAddress.SIZE_OF);
      return PageFileAddress.CONVERTER.convertFrom(buffer);
    }
    catch (IOException ioe) {
      LOG.warning(ioe);
    }
    return PageFileAddress.UNKNOWN;
  }

  public void writeHeader(int offset, PageFileAddress pageFileAddress) {
    try {
      if (file.isEmpty()) file.createNewPage();
      final Buffer buffer = file.getPageByIndex(0);
      buffer.position((offset + emptyStarts.length) * PageFileAddress.SIZE_OF);
      BufferFactory.write(PageFileAddress.CONVERTER.convertTo(pageFileAddress), buffer);
    }
    catch (IOException ioe) {
      LOG.error(ioe);
    }
  }

  public synchronized void enqueueForCleanup(PageFileAddress addr) {
    if (addr.equals(PageFileAddress.UNKNOWN))
      return;
    if (addr.getLength() <= 0) {
      while (addr.getPageNo() > 0) {
        queue.add(new PageFileAddress(addr.getPageNo(), (short)0, PAGE_SIZE));
        addr = PageFileAddress.CONVERTER.convertFrom(
          read(new PageFileAddress(addr.getPageNo(), (short)0, PageFileAddress.SIZE_OF))
        );
      }
    }
    else queue.add(addr);
  }

  public synchronized void cleanup() {
    while (!queue.isEmpty()) {
      final PageFileAddress addr = queue.poll();
      int degree = degree(addr.getLength()) - MIN_DEGREE;
      if (degree > emptyStarts.length - 1)
        degree = emptyStarts.length - 1;
      if (degree < 0)
        degree = 0;
      try {
        final Buffer buffer = file.getPageByIndex(addr.getPageNo());

        final int offset = addr.getOffset();
        buffer.position(offset);
        final short slotSize = (short)(1 << (degree + MIN_DEGREE));
        buffer.limit(offset + slotSize);
        BufferFactory.write(PageFileAddress.CONVERTER.convertTo(emptyStarts[degree]), buffer);
        emptyStarts[degree] = new PageFileAddress(addr.getPageNo(), addr.getOffset(), slotSize);
        writeHeader(-degree - 1, emptyStarts[degree]);
      }
      catch (IOException ioe) {
        LOG.warning(ioe);
      }
    }
    file.cleanup();
  }

  public void close() {
    cleanup();
    file.close();
  }

  public long size() {
    try {
      return file.pagesCount();
    }
    catch (IOException e) {
      LOG.error(e);
    }
    return -1;
  }

  private synchronized PageFileAddress getEmptyPage(int size) {
    int degree = degree(size) - MIN_DEGREE;
    if (degree > emptyStarts.length - 1)
      degree = emptyStarts.length - 1;
    if (degree < 0)
      degree = 0;

    try {
      recoverPointer(degree);
      if (emptyStarts[degree].equals(PageFileAddress.UNKNOWN)) {
        final int newPageNo = file.createNewPage();
        final Buffer buffer = file.getPageByIndex(newPageNo);
        int offset = 0;
        short partSize = (short)(1 << (degree + MIN_DEGREE));
        emptyStarts[degree] = new PageFileAddress(newPageNo, (short)0, partSize);
        while (offset < buffer.capacity()) {
          buffer.position(offset);
          offset += partSize;
          final PageFileAddress toWrite;
          if (offset < buffer.capacity() - PageFileAddress.SIZE_OF)
            toWrite = new PageFileAddress(newPageNo, (short)offset, partSize);
          else
            toWrite = PageFileAddress.UNKNOWN;
          BufferFactory.write(PageFileAddress.CONVERTER.convertTo(toWrite), buffer);
        }
      }
    }
    catch (IOException ioe) {
      LOG.warning(ioe);
    }
    final PageFileAddress address = emptyStarts[degree];
    writeHeader(-degree - 1, emptyStarts[degree] = PageFileAddress.CONVERTER.convertFrom(read(address)));
    return address;
  }

  private static int degree(int size) {
    int deg = 0;
    int sum = 0;
    while (size > 0) {
      sum += size & 1;
      size >>= 1;
      deg++;
    }
    return sum > 1 ? deg : deg - 1;
  }
}
