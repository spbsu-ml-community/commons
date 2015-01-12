package com.spbsu.commons.io;

import junit.framework.TestCase;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 17:27:30
 * To change this template use File | Settings | File Templates.
 */
public class NIOTest extends TestCase {
  public void testComposite0() {
    final Buffer b = BufferFactory.wrap();
    boolean caught = false;
    try {
      b.getInt();
    }
    catch (BufferUnderflowException bue) {
      caught = true;
    }
    assertTrue(caught);
  }

  public void testComposite1() {
    final Buffer b = BufferFactory.wrap(new byte[4]);
    b.putInt(48);
    b.position(0);
    assertEquals(48, b.getInt());
  }

  public void testComposite2() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putInt(48);
    b.position(0);
    assertEquals(48, b.getInt());
  }

  public void testComposite3() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putInt(48);
    b.position(0);
    assertEquals(48, b.getInt());
    assertEquals(48, b.getInt(0));
  }

  public void testComposite4() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putFloat(0.187236f);
    b.position(0);
    assertEquals(1044363979, b.getInt(0));
    assertEquals(0.187236f, b.getFloat());
    assertEquals(0, b.remaining());
  }

  public void testComposite5() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(new byte[1]));
    b.position(0);
    assertEquals(2, b.remaining());
  }

  public void testComposite6() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[3]), ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[3]));
    b.putShort((short) 48);
    final int a = (int) 1E+50;
    b.putInt(a);
    b.putShort((short) 200);
    b.position(0);
    assertEquals(48, b.getShort());
    assertEquals(a, b.getInt());
    assertEquals(200, b.getShort());
  }

  public void testComposite7() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[100]), ByteBuffer.wrap(new byte[100]));
    b.putInt(Integer.MAX_VALUE);
    b.putInt(Integer.MIN_VALUE);
    b.position(0);
    assertEquals(Integer.MAX_VALUE, b.getInt());
    assertEquals(Integer.MIN_VALUE, b.getInt());
    assertEquals(Integer.MAX_VALUE, b.getInt(0));
    assertEquals(Integer.MIN_VALUE, b.getInt(4));
  }

  public void testComposite8() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putInt(Integer.MAX_VALUE);
    b.position(0);
    assertEquals(Integer.MAX_VALUE, b.getInt());
    assertEquals(Integer.MAX_VALUE, b.getInt(0));
  }

  public void testComposite9() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[100]), ByteBuffer.wrap(new byte[100]));
    b.putLong(Integer.MAX_VALUE);
    b.putLong(Integer.MIN_VALUE);
    b.position(0);
    assertEquals(Integer.MAX_VALUE, b.getLong());
    assertEquals(Integer.MIN_VALUE, b.getLong());
    assertEquals(Integer.MAX_VALUE, b.getLong(0));
    assertEquals(Integer.MIN_VALUE, b.getLong(8));
  }

  public void testComposite10() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[100]), ByteBuffer.wrap(new byte[100]));
    b.putShort(Short.MAX_VALUE);
    b.putShort(Short.MIN_VALUE);
    b.position(0);
    assertEquals(Short.MAX_VALUE, b.getShort());
    assertEquals(Short.MIN_VALUE, b.getShort());
    assertEquals(Short.MAX_VALUE, b.getShort(0));
    assertEquals(Short.MIN_VALUE, b.getShort(2));
  }

  public void testComposite11() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[100]), ByteBuffer.wrap(new byte[100]));
    b.putChar(Character.MAX_VALUE);
    b.putChar(Character.MIN_VALUE);
    b.position(0);
    assertEquals(Character.MAX_VALUE, b.getChar());
    assertEquals(Character.MIN_VALUE, b.getChar());
    assertEquals(Character.MAX_VALUE, b.getChar(0));
    assertEquals(Character.MIN_VALUE, b.getChar(2));
  }

  public void testComposite12() throws Exception {
    final ByteBuffer b1 = ByteBuffer.wrap(new byte[10]);
    b1.putLong(Long.MAX_VALUE);
    final Buffer b = BufferFactory.wrap(b1, ByteBuffer.wrap(new byte[100]));
    final int v = 123567;
    b.putInt(v);
    assertEquals(Long.MAX_VALUE, b1.getLong(0));
    b.position(0);
    assertEquals(v, b.getInt());
  }

  public void testComposite13() throws Exception {
    final ByteBuffer b1 = ByteBuffer.wrap(new byte[10]);
    b1.putLong(Long.MAX_VALUE);
    final Buffer b = BufferFactory.wrap(b1, ByteBuffer.wrap(new byte[100]));
    final char c = 'y';
    b.putChar(c);
    assertEquals(Long.MAX_VALUE, b1.getLong(0));
    b.position(0);
    assertEquals(c, b.getChar());
  }

  public void testComposite14() throws Exception {
    final ByteBuffer b1 = ByteBuffer.wrap(new byte[10]);
    b1.putLong(Long.MAX_VALUE);
    final Buffer b = BufferFactory.wrap(b1, ByteBuffer.wrap(new byte[100]));
    final short v = 23567;
    b.putShort(v);
    assertEquals(Long.MAX_VALUE, b1.getLong(0));
    b.position(0);
    assertEquals(v, b.getShort());
  }

  public void testFactoryWrite1() throws Exception {
    final Buffer src = BufferFactory.join(BufferFactory.wrap(new byte[5]), BufferFactory.wrap(new byte[3]));
    final double value = Math.PI;
    src.putDouble(value);
    src.position(0);
    final Buffer dst = BufferFactory.join(BufferFactory.wrap(new byte[5]), BufferFactory.wrap(new byte[3]));
    BufferFactory.write(src, dst);
    assertEquals(value, dst.getDouble(0));
  }

  public void testFactoryWrite2() throws Exception {
    final Buffer src = BufferFactory.join(BufferFactory.wrap(new byte[5]), BufferFactory.wrap(new byte[3]));
    final double value = Math.PI;
    src.putDouble(value);
    src.position(0);
    final Buffer dst = BufferFactory.join(BufferFactory.wrap(new byte[1]), BufferFactory.wrap(new byte[7]));
    BufferFactory.write(src, dst);
    assertEquals(value, dst.getDouble(0));
  }

  public void testLocalPosMove() throws Exception {
    final byte[] zlo = new byte[] {'z', 'l', 'o'};
    final Buffer src = BufferFactory.join(BufferFactory.wrap(new byte[3]), BufferFactory.wrap(new byte[1]));
    src.putByte((byte)2);
    src.put(zlo);
    src.position(0);
    assertEquals((byte)2, src.getByte());
    final byte[] read = new byte[3];
    src.get(read);
    assertTrue(Arrays.equals(zlo, read));
  }

  public void testLocalPosMove1() throws Exception {
    final byte[] zlo = new byte[] {'z', 'l', 'o'};
    final Buffer buffer = BufferFactory.wrap(new byte[4]);
    buffer.putByte((byte)3);
    final Buffer src = BufferFactory.join(buffer, BufferFactory.wrap(new byte[1]));
    src.putByte((byte)2);
    src.put(zlo);
    src.position(0);
    assertEquals((byte)2, src.getByte());
    final byte[] read = new byte[3];
    src.get(read);
    assertTrue(Arrays.equals(zlo, read));
  }
}
