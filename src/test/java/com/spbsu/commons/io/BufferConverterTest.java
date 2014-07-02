package com.spbsu.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


import com.spbsu.commons.func.converters.*;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.util.Factories;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: terry
 * Date: 14.11.2009
 * Time: 13:33:39
 * To change this template use File | Settings | File Templates.
 */
public class BufferConverterTest extends TestCase {

  public void testCharsequenceCoverter() {
    final CharSequence2BufferConverter<String> converter = new CharSequence2BufferConverter<String>(new StringCSFactory());
    Buffer buffer = converter.convertTo("fuck");
    assertTrue(CharSeqTools.equals("fuck", converter.convertFrom(buffer)));
  }

  public void testCharsequenceCoverter2() {
    final CharSequence2BufferConverter<String> converter = new CharSequence2BufferConverter<String>(new StringCSFactory());
    Buffer buffer = converter.convertTo("путенг");
    assertTrue(CharSeqTools.equals("путенг", converter.convertFrom(buffer)));
  }

  public void testStoreSize() throws Exception {
    final int size = 14;
    final Buffer buffer = NioConverterTools.storeSize(size);
    assertEquals(size, NioConverterTools.restoreSize(buffer));
  }

  public void testStoreSize2() throws Exception {
    final int size = 14;
    final ByteArrayOutputStream stream = new ByteArrayOutputStream(size);
    final DataOutputStream output = new DataOutputStream(stream);
    NioConverterTools.storeSize(size, output);
    assertEquals(size, NioConverterTools.restoreSize(new DataInputStream(new ByteArrayInputStream(stream.toByteArray()))));
  }

  public void testArrayConverter() {
    final Integer[] array = new Integer[]{1, 2};

    final Array2BufferConverter<Integer> conv =
        new Array2BufferConverter<Integer>(new Integer[0], new Integer2BufferConverter());
    final Buffer buffer = conv.convertTo(array);

    final Integer[] result = conv.convertFrom(buffer);
    assertEquals(array.length, result.length);
    assertTrue(Arrays.equals(array, result));
  }

  public void testSetConverter() {
    final Set2BufferConverter<Long> converter =
        new HashSet2BufferConverter<Long>(new Long2BufferConverter());
    Set<Long> longs = Factories.hashSet(1L, 2L);
    Buffer buffer = converter.convertTo(longs);
    Set<Long> result = converter.convertFrom(buffer);
    assertEquals(longs, result);
  }

  public void testListConverter() {
    final List2BufferConverter<Long> converter =
        new ArrayList2BufferConverter<Long>(new Long2BufferConverter());
    List<Long> longs = Factories.arrayList(1L, 2L);
    Buffer buffer = converter.convertTo(longs);
    List<Long> result = converter.convertFrom(buffer);
    assertEquals(longs, result);
  }
}
