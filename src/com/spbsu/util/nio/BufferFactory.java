package com.spbsu.util.nio;

import com.spbsu.util.nio.impl.ByteBufferWrapper;
import com.spbsu.util.nio.impl.CompositeBuffer;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:31:06
 * To change this template use File | Settings | File Templates.
 */
public class BufferFactory {
  public static ReadBuffer wrapOnRead(byte[] array) {
    return new ByteBufferWrapper(ByteBuffer.wrap(array));
  }

  public static WriteBuffer wrapOnWrite(byte[] array) {
    return new ByteBufferWrapper(ByteBuffer.wrap(array));
  }

  public static RWBuffer wrap(byte[] array) {
    return new ByteBufferWrapper(ByteBuffer.wrap(array));
  }

  public static ReadBuffer wrapOnRead(ByteBuffer... buffers) {
    if (buffers.length == 1)
      return new ByteBufferWrapper(buffers[0]);
    return new CompositeBuffer(buffers);
  }

  public static WriteBuffer wrapOnWrite(ByteBuffer... buffers) {
    if (buffers.length == 1)
      return new ByteBufferWrapper(buffers[0]);
    return new CompositeBuffer(buffers);
  }

  public static RWBuffer wrap(ByteBuffer... buffers) {
    if (buffers.length == 1)
      return new ByteBufferWrapper(buffers[0]);
    return new CompositeBuffer(buffers);
  }

  public <T extends Buffer> T duplicate(T original) {
    if (original instanceof CompositeBuffer)
      //noinspection unchecked
      return (T)((CompositeBuffer)original).duplicate();
    if (original instanceof ByteBufferWrapper)
      //noinspection unchecked
      return (T)((ByteBufferWrapper)original).duplicate();
    throw new UnsupportedOperationException();
  }
}
