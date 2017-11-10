package com.expleague.commons.io;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:31:06
 */
public class BufferFactory {
  public static Buffer wrap(final byte[] array) {
    return new ByteBufferWrapper(ByteBuffer.wrap(array));
  }

  public static Buffer wrap(final ByteBuffer... buffers) {
    if (buffers.length == 1)
      return new ByteBufferWrapper(buffers[0]);
    return new CompositeBuffer(buffers);
  }

  private static void visitSimpleBuffers(final Consumer<Buffer> visitor, final Buffer... buffers) {
    for (final Buffer buffer : buffers) {
      if (buffer instanceof CompositeBuffer)
        ((CompositeBuffer) buffer).visitParts(visitor);
      else
        visitor.accept(buffer);
    }
  }

  public static Buffer join(final Buffer... buffers) {
    if (buffers.length == 1)
      return buffers[0];
    { // compact
      class BufferAnalyzer implements Consumer<Buffer> {
        int count;
        int totalLength;
        boolean isDirect;
        @Override
        public void accept(final Buffer arg) {
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
        class ContentsCopier implements Consumer<Buffer> {
          int index;
          @Override
          public void accept(final Buffer arg) {
            final int old = arg.position();
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
        class BuffersCopier implements Consumer<Buffer> {
          int index;
          @Override
          public void accept(final Buffer arg) {
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
      ((CompositeBuffer) src).visitParts(arg -> {
        final int old = arg.position();
        try {
          write(arg, dst);
        }
        finally {
          arg.position(old);
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
          final int read = cast.get(buffer);
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


  public static Buffer duplicate(final Buffer original) {
    if (original instanceof CompositeBuffer)
      //noinspection unchecked
      return ((CompositeBuffer)original).duplicate();
    if (original instanceof ByteBufferWrapper)
      //noinspection unchecked
      return ((ByteBufferWrapper)original).duplicate();
    throw new UnsupportedOperationException();
  }
}
