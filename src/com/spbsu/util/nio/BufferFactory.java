package com.spbsu.util.nio;

import com.spbsu.util.nio.ByteBufferWrapper;
import com.spbsu.util.nio.CompositeBuffer;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:31:06
 * To change this template use File | Settings | File Templates.
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

  public static Buffer compile(Buffer... buffers) {
    if (buffers.length == 1)
      return buffers[0];
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
