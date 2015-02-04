package com.spbsu.commons.io;

import com.spbsu.commons.seq.CharSeq;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ArrayBlockingQueue;

/**
* User: solar
* Date: 29.01.15
* Time: 19:34
*/
public class QueueReader extends Reader {
  private final ArrayBlockingQueue<CharSeq> readqueue;
  CharSeq take;
  int offset;
  boolean closed;

  public QueueReader(ArrayBlockingQueue<CharSeq> readqueue) {
    this.readqueue = readqueue;
    offset = 0;
    closed = false;
  }

  @Override
  public int read(@NotNull char[] cbuf, int off, int len) throws IOException {
    if (closed)
      return -1;
    if (take == null) {
      try {
        take = readqueue.take();
        offset = 0;
        if (take == CharSeq.EMPTY)
          throw new InterruptedException();
      } catch (InterruptedException e) {
        closed = true;
        return -1;
      }
    }
    final int copied = Math.min(take.length() - offset, len);
    take.copyToArray(offset, cbuf, off, copied);
    offset += copied;
    if (offset >= take.length())
      take = null;
    return copied;
  }

  @Override
  public void close() throws IOException {
    closed = true;
  }
}
