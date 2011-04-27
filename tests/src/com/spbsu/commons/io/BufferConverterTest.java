package com.spbsu.commons.io;

import com.spbsu.commons.io.converters.NioConverterTools;
import com.spbsu.commons.io.converters.signals.BinarySignal2BufferConverter;
import com.spbsu.commons.io.converters.signals.IntSignal2BufferCompressingConverter;
import com.spbsu.commons.io.converters.signals.IntSignal2BufferCompressingConverter2;
import com.spbsu.commons.io.converters.signals.IntSignal2BufferConverter;
import com.spbsu.commons.io.converters.util.*;
import com.spbsu.commons.math.signals.numeric.BinarySignal;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.text.CharArrayCharSequence;
import com.spbsu.commons.text.CharSequenceTools;
import com.spbsu.commons.util.Factories;
import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
    assertTrue(CharSequenceTools.equals("fuck", converter.convertFrom(buffer)));
  }

  public void testCharsequenceCoverter2() {
    final CharSequence2BufferConverter<CharArrayCharSequence> converter = new CharSequence2BufferConverter<CharArrayCharSequence>(new CharArrayCSFactory());
    Buffer buffer = converter.convertTo("путенг");
    assertTrue(CharSequenceTools.equals("путенг", converter.convertFrom(buffer)));
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

  public void testBinarySignalConverter() {
    final BinarySignal binarySignal = new BinarySignal(new long[]{1, 2, 3, 4, 5});
    final BinarySignal2BufferConverter converter = new BinarySignal2BufferConverter();
    final Buffer buffer = converter.convertTo(binarySignal);
    BinarySignal converted = converter.convertFrom(buffer);
    assertTrue(Arrays.equals(binarySignal.getTimestamps(), converted.getTimestamps()));
  }

  public void testIntSignalConverter() {
    final IntSignal intSignal = new IntSignal(new long[]{1, 2, 3, 4, 5}, new int[] {2,3,4,5,6});
    final IntSignal2BufferConverter converter = new IntSignal2BufferConverter();
    final Buffer buffer = converter.convertTo(intSignal);
    IntSignal converted = converter.convertFrom(buffer);
    assertTrue(Arrays.equals(intSignal.getTimestamps(), converted.getTimestamps()));
    assertTrue(Arrays.equals(intSignal.getNativeValues(), converted.getNativeValues()));
  }

  public void testIntSignalCompressingConverter() {
    final long inc = 1000000000000L;
    final IntSignal intSignal = new IntSignal(new long[]{
      inc,
      inc + inc,
      inc + 2* inc,
      inc + 3* inc,
      inc + 4* inc,
    }, new int[] {2,3,4,5,6});
    final IntSignal2BufferCompressingConverter converter = new IntSignal2BufferCompressingConverter();
    final Buffer buffer = converter.convertTo(intSignal);
    IntSignal converted = converter.convertFrom(buffer);
    assertEquals(0, buffer.remaining());
    assertTrue(Arrays.equals(intSignal.getTimestamps(), converted.getTimestamps()));
    assertTrue(Arrays.equals(intSignal.getNativeValues(), converted.getNativeValues()));
  }

  public void testIntSignalCompressingConverter2() {
    final IntSignal intSignal = new IntSignal();

    final long time = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      intSignal.occur(time + i * 1000, i * 100);
    }
    final IntSignal2BufferCompressingConverter converter = new IntSignal2BufferCompressingConverter();
    final Buffer buffer = converter.convertTo(intSignal);
    IntSignal converted = converter.convertFrom(buffer);
    assertEquals(0, buffer.remaining());
    assertTrue(Arrays.equals(intSignal.getTimestamps(), converted.getTimestamps()));
    assertTrue(Arrays.equals(intSignal.getNativeValues(), converted.getNativeValues()));
  }

  public void testIntSignalCompressingConverter3() {
    final IntSignal intSignal = new IntSignal();
    final Random random = new Random();
    final long time = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      intSignal.occur(time + i, (int) (Math.abs(random.nextGaussian()) * 3 + 1));
    }

    final IntSignal2BufferConverter plain = new IntSignal2BufferConverter();
    final Buffer plainBuffer = plain.convertTo(intSignal);

    final IntSignal2BufferCompressingConverter converter = new IntSignal2BufferCompressingConverter();
    final Buffer compressedBuffer = converter.convertTo(intSignal);

    final IntSignal2BufferCompressingConverter2 converter2 = new IntSignal2BufferCompressingConverter2();
    final Buffer compressedBuffer2 = converter2.convertTo(intSignal);

    System.out.println("Compression ratio 1: " + ((double)plainBuffer.remaining())/compressedBuffer.remaining() );
    System.out.println("Compression ratio 2: " + ((double)plainBuffer.remaining())/compressedBuffer2.remaining() );
  }

  public void testIntSignalCompressingConverterPerformance() {
    final IntSignal intSignal = new IntSignal();
    final Random random = new Random();
    final long time = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      intSignal.occur(time + i, (int) (Math.abs(random.nextGaussian()) * 3 + 1));
    }

    final IntSignal2BufferConverter plain = new IntSignal2BufferConverter();
    final IntSignal2BufferCompressingConverter converter = new IntSignal2BufferCompressingConverter();
    final IntSignal2BufferCompressingConverter2 converter2 = new IntSignal2BufferCompressingConverter2();

    final int cnt = 500;
    Interval.start();
    for (int i = 0; i < cnt; i++) {
      final Buffer plainBuffer = plain.convertTo(intSignal);
      plain.convertFrom(plainBuffer);
    }
    Interval.stopAndPrint();

    Interval.start();
    for (int i = 0; i < cnt; i++) {
      final Buffer plainBuffer = converter.convertTo(intSignal);
      converter.convertFrom(plainBuffer);
    }
    Interval.stopAndPrint();

    Interval.start();
    for (int i = 0; i < cnt; i++) {
      final Buffer plainBuffer = converter2.convertTo(intSignal);
      converter2.convertFrom(plainBuffer);
    }
    Interval.stopAndPrint();
  }

  public void testIntSignalCompressingConverter4() {
    final long inc = 1000000000000L;
    final IntSignal intSignal = new IntSignal(new long[]{
      inc,
      inc + inc,
      inc + 2* inc,
      inc + 3* inc,
      inc + 4* inc,
      inc + 5* inc,
    }, new int[] {2,3,4,5,6});
    final IntSignal2BufferCompressingConverter2 converter = new IntSignal2BufferCompressingConverter2();
    final Buffer buffer = converter.convertTo(intSignal);
    IntSignal converted = converter.convertFrom(buffer);
    assertEquals(0, buffer.remaining());
    assertTrue(Arrays.equals(intSignal.getTimestamps(), converted.getTimestamps()));
    assertTrue(Arrays.equals(intSignal.getNativeValues(), converted.getNativeValues()));
  }

  public void testPackUnpackValues() throws Exception {
    performTestPackUnpackValues(new int[] {0});
    performTestPackUnpackValues(new int[] {1});
    performTestPackUnpackValues(new int[] {0, 0});
    performTestPackUnpackValues(new int[] {1, 1});
    performTestPackUnpackValues(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    performTestPackUnpackValues(new int[] {10, 1});
    performTestPackUnpackValues(new int[] {1, 10});
    performTestPackUnpackValues(new int[] {1, 10, 1});
    performTestPackUnpackValues(new int[] {1, 10, 1, 1, 5});
    performTestPackUnpackValues(new int[] {1000, 10, 1, 1, 5});
    performTestPackUnpackValues(new int[] {1000, 100000, 123, 110, 1, 1, 1});
  }

  public static void performTestPackUnpackValues(final int[] ints) throws Exception {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream(out);

    IntSignal2BufferCompressingConverter2.packValues(ints, dos);
    assertTrue(Arrays.equals(ints, IntSignal2BufferCompressingConverter2.unpackValues(new DataInputStream(new ByteArrayInputStream(out.toByteArray())))));
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
