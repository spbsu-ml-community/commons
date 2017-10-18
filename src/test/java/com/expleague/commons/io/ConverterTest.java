package com.expleague.commons.io;

import com.expleague.commons.func.converters.*;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: igorkuralenok
 * Date: 22.06.2009
 */
public class ConverterTest extends TestCase {
  public void testSizeConvertion() {
    final int[] sizes = new int[]{0, 1, 100, 10000, 299999, 17000000};
    for (final int size : sizes) {
      final ByteBuffer buffer = ByteBuffer.allocate(5);
      NioConverterTools.storeSize(size, buffer);
      buffer.rewind();
      assertEquals(size, NioConverterTools.restoreSize(buffer));
    }
  }

  public void testStringConverter() {
    final String s = "string";
    final String2ByteBufferConverter conv = new String2ByteBufferConverter();
    final Buffer buffer = conv.convertTo(s);
    final String s1 = conv.convertFrom(buffer);
    assertEquals(s, s1);
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testListIntegerConverter() {
    final ArrayList<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);

    final List2BufferConverter<Integer> conv = new ArrayList2BufferConverter<Integer>(new Integer2BufferConverter());
    final Buffer buffer = conv.convertTo(list);

    final List<Integer> result = conv.convertFrom(buffer);
    assertEquals(list.size(), result.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i), result.get(i));
    }
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testArrayIntegerBufferConverter() {
    final Integer[] array = new Integer[]{1, 2};

    final Array2BufferConverter<Integer> conv = new Array2BufferConverter<Integer>(
        new Integer[0], new Integer2BufferConverter());
    final Buffer buffer = conv.convertTo(array);

    final Integer[] result = conv.convertFrom(buffer);
    assertEquals(array.length, result.length);

    for (int i = 0; i < array.length; i++) {
      assertEquals(array[i], result[i]);
    }
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testListStringConverter() {
    final ArrayList<String> list = new ArrayList<String>();
    list.add("1");
    list.add("2");

    final List2BufferConverter<String> conv = new ArrayList2BufferConverter<String>(new String2ByteBufferConverter());
    final Buffer buffer = conv.convertTo(list);

    final List<String> result = conv.convertFrom(buffer);
    assertEquals(list.size(), result.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i), result.get(i));
    }
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testLong2BufferConverter() {
    final Long s = Long.MAX_VALUE / 2;
    final Long2BufferConverter conv = new Long2BufferConverter();
    final Buffer buffer = conv.convertTo(s);
    final Long s1 = conv.convertFrom(buffer);
    assertEquals(s, s1);
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testMapConverter() throws Exception {
    final HashMap<CharSequence, Integer> map = new HashMap<CharSequence, Integer>();
    for (int i = 0; i < 100; i++) {
      map.put("" + i, i);
    }

    final Map2BufferConverter<CharSequence, Integer> conv = new Map2BufferConverter<CharSequence, Integer>(
      new CharSequence2BufferConverterOld() {
        @Override
        protected CharSequence createCharsequence(final char[] chars) {
          return new String(chars);
        }
      },
      new Integer2BufferConverter()
    );

    final Buffer buffer = conv.convertTo(map);
    final Map<CharSequence,Integer> map1 = conv.convertFrom(buffer);
    assertEquals(map.size(), map1.size());

    for (int i = 0; i < 100; i++) {
      assertEquals(i, map1.get("" + i).intValue());
    }
  }
}
