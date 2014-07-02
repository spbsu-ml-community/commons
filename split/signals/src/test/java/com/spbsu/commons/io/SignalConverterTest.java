package com.spbsu.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Random;


import com.spbsu.commons.func.converters.signals.BinarySignal2BufferConverter;
import com.spbsu.commons.func.converters.signals.IntSignal2BufferCompressingConverter;
import com.spbsu.commons.func.converters.signals.IntSignal2BufferCompressingConverter2;
import com.spbsu.commons.func.converters.signals.IntSignal2BufferConverter;
import com.spbsu.commons.math.signals.numeric.BinarySignal;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: terry
 * Date: 14.11.2009
 * Time: 13:33:39
 * To change this template use File | Settings | File Templates.
 */
public class SignalConverterTest extends TestCase {
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

    System.out.println("Compression ratio 1: " + ((double) plainBuffer.remaining()) / compressedBuffer.remaining());
    System.out.println("Compression ratio 2: " + ((double) plainBuffer.remaining()) / compressedBuffer2.remaining());
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
    performTestPackUnpackValues(new int[]{1000, 100000, 123, 110, 1, 1, 1});
  }

  public static void performTestPackUnpackValues(final int[] ints) throws Exception {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream(out);

    IntSignal2BufferCompressingConverter2.packValues(ints, dos);
    assertTrue(Arrays.equals(ints, IntSignal2BufferCompressingConverter2.unpackValues(new DataInputStream(new ByteArrayInputStream(out.toByteArray())))));
  }
}
