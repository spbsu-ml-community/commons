package com.spbsu.commons.io;

import com.spbsu.commons.io.codec.ArithmeticCoding;
import com.spbsu.commons.random.FastRandom;
import junit.framework.TestCase;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * User: solar
 * Date: 02.06.14
 * Time: 12:40
 */
public class ArithmeticCodingTest extends TestCase {

  public void testFixed() {
    {
      final int[] freqs = new int[]{1, 100};
      final ByteBuffer buffer = ByteBuffer.allocate(2 * 4);
      final ArithmeticCoding.Encoder encoder = new ArithmeticCoding.Encoder(buffer, freqs);
      final int[] symbols = new int[]{1};
      for (int i = 0; i < symbols.length; i++) {
        encoder.write(symbols[i]);
      }
      encoder.flush();
      buffer.rewind();
      final ArithmeticCoding.Decoder decoder = new ArithmeticCoding.Decoder(buffer, freqs);
      for (int i = 0; i < symbols.length; i++) {
        final int read = decoder.read();
        if (symbols[i] != read)
          assertEquals(symbols[i], read);
      }
    }
    {
      final int[] freqs = new int[]{1, 100};
      final ByteBuffer buffer = ByteBuffer.allocate(2 * 4);
      final ArithmeticCoding.Encoder encoder = new ArithmeticCoding.Encoder(buffer, freqs);
      final int[] symbols = new int[]{1, 1};
      for (int i = 0; i < symbols.length; i++) {
        encoder.write(symbols[i]);
      }
      encoder.flush();
      buffer.rewind();
      final ArithmeticCoding.Decoder decoder = new ArithmeticCoding.Decoder(buffer, freqs);
      for (int i = 0; i < symbols.length; i++) {
        final int read = decoder.read();
        if (symbols[i] != read)
          assertEquals(symbols[i], read);
      }
    }
    {
      final int[] freqs = new int[]{1, 100, 1000};
      final ByteBuffer buffer = ByteBuffer.allocate(2 * 4);
      final ArithmeticCoding.Encoder encoder = new ArithmeticCoding.Encoder(buffer, freqs);
      final int[] symbols = new int[]{2, 1, 0};
      for (int i = 0; i < symbols.length; i++) {
        encoder.write(symbols[i]);
      }
      encoder.flush();
      buffer.rewind();
      final ArithmeticCoding.Decoder decoder = new ArithmeticCoding.Decoder(buffer, freqs);
      for (int i = 0; i < symbols.length; i++) {
        final int read = decoder.read();
        if (symbols[i] != read)
          assertEquals(symbols[i], read);
      }
    }
  }

  public void testRandom() {
    final FastRandom rng = new FastRandom(0);
    for (int t = 0; t < 1000; t++) {
      final int[] freqs = new int[rng.nextPoisson(1000)];
      for (int i = 0; i < freqs.length; i++) {
        freqs[i] = rng.nextInt(10000) + 1;
      }
      final int length = rng.nextPoisson(1000);
      final ByteBuffer buffer = ByteBuffer.allocate(length * 4);
      final ArithmeticCoding.Encoder encoder = new ArithmeticCoding.Encoder(buffer, freqs);
      final int[] symbols = new int[length];
      for (int i = 0; i < symbols.length; i++) {
        symbols[i] = rng.nextInt(freqs.length);
        encoder.write(symbols[i]);
      }
      encoder.flush();
      buffer.rewind();
      final ArithmeticCoding.Decoder decoder = new ArithmeticCoding.Decoder(buffer, freqs);
      for (int i = 0; i < symbols.length; i++) {
        decoder.mark();
        final int read = decoder.read();
        if (symbols[i] != read) {
          decoder.reset();
          assertEquals(symbols[i], decoder.read());
        }
      }
    }
  }

  public void testRandomStream() {
    final FastRandom rng = new FastRandom(0);
    for (int t = 0; t < 1000; t++) {
      final int[] freqs = new int[rng.nextPoisson(1000)];
      for (int i = 0; i < freqs.length; i++) {
        freqs[i] = rng.nextInt(10000) + 1;
      }
      final int length = rng.nextPoisson(1000);
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream(length * 4);
      final ArithmeticCoding.Encoder encoder = new ArithmeticCoding.Encoder(buffer, freqs);
      final int[] symbols = new int[length];
      for (int i = 0; i < symbols.length; i++) {
        symbols[i] = rng.nextInt(freqs.length);
        encoder.write(symbols[i]);
      }
      encoder.flush();
      final ArithmeticCoding.Decoder decoder = new ArithmeticCoding.Decoder(new ByteArrayInputStream(buffer.toByteArray()), freqs);
      for (int i = 0; i < symbols.length; i++) {
        final int read = decoder.read();
        if (symbols[i] != read) {
          assertEquals(symbols[i], decoder.read());
        }
      }
    }
  }
}
