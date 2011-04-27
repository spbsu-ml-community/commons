package com.spbsu.commons.io;

import com.spbsu.commons.func.Processor;

import java.nio.ByteBuffer;

/**
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:31:06
 */
public class BufferFactory {
  public static Buffer wrap(byte[] array) {
    return new ByteBufferWrapper(ByteBuffer.wrap(array));
  }

  public static Buffer wrap(ByteBuffer... buffers) {
    if (buffers.length == 1)
      return new ByteBufferWrapper(buffers[0]);
    return new CompositeBuffer(buffers);
  }

  private static void visitSimpleBuffers(Processor<Buffer> visitor, Buffer... buffers) {
    for (Buffer buffer : buffers) {
      if (buffer instanceof CompositeBuffer)
        ((CompositeBuffer) buffer).visitParts(visitor);
      else
        visitor.process(buffer);
    }
  }

  public static Buffer join(Buffer... buffers) {
    if (buffers.length == 1)
      return buffers[0];
    { // compact
      class BufferAnalyzer implements Processor<Buffer> {
        int count;
        int totalLength;
        boolean isDirect;
        public void process(Buffer arg) {
          totalLength += arg.remaining();
          count++;
          if (arg instanceof ByteBufferWrapper)
            isDirect |= ((ByteBufferWrapper) arg).isDirect();
        }
      }
      final BufferAnalyzer analyzer = new BufferAnalyzer();
      visitSimpleBuffers(analyzer, buffers);
      if (!analyzer.isDirect && analyzer.count > 10 && analyzer.count * 1024 > analyzer.totalLength) { // need hard compaction
        final byte[] buffer = new byte[analyzer.totalLength];
        class ContentsCopier implements Processor<Buffer> {
          int index;
          public void process(Buffer arg) {
            int old = arg.position();
            try {
              index += arg.get(buffer, index, buffer.length - index);
            }
            finally {
              arg.position(old);
            }
          }
        }
        visitSimpleBuffers(new ContentsCopier(), buffers);
        return wrap(buffer);
      }
      else if (analyzer.count > buffers.length) { // need tree compaction
        final Buffer[] compactedBuffers = new Buffer[analyzer.count];
        class BuffersCopier implements Processor<Buffer> {
          int index;
          public void process(Buffer arg) {
            compactedBuffers[index++] = arg;
          }
        }
        visitSimpleBuffers(new BuffersCopier(), buffers);
        return new CompositeBuffer(compactedBuffers);
      }
    }
    return new CompositeBuffer(buffers);
  }

  public static void write(final Buffer src, final Buffer dst) {
    if (src instanceof CompositeBuffer) {
      final int toCopy = Math.min(dst.remaining(), src.remaining());
      ((CompositeBuffer) src).visitParts(new Processor<Buffer>() {
        public void process(Buffer arg) {
          final int old = arg.position();
          try {
            write(arg, dst);
          }
          finally {
            arg.position(old);
          }
        }
      });
      src.position(src.position() + toCopy);
    }
    else if (src instanceof ByteBufferWrapper) {
      final int howManyToCopy = Math.min(src.remaining(), dst.remaining());
      final ByteBufferWrapper cast = (ByteBufferWrapper) src;
      if(!cast.hasArray()) {
        final byte[] buffer = new byte[1024];
        int index = 0;
        while (index < howManyToCopy) {
          int read = cast.get(buffer);
          dst.put(buffer, 0, Math.min(howManyToCopy - index, read));
          index += read;
        }
      }
      else {
        dst.put(cast.array(), cast.position(), howManyToCopy);
        cast.position(cast.position() + howManyToCopy);
      }
    }
    else
      throw new UnsupportedOperationException();
  }


  public static Buffer duplicate(Buffer original) {
    if (original instanceof CompositeBuffer)
      //noinspection unchecked
      return ((CompositeBuffer)original).duplicate();
    if (original instanceof ByteBufferWrapper)
      //noinspection unchecked
      return ((ByteBufferWrapper)original).duplicate();
    throw new UnsupportedOperationException();
  }
}
