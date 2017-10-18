package com.expleague.commons.io;

import com.expleague.commons.random.FastRandom;
import junit.framework.TestCase;

import java.nio.ByteBuffer;

/**
 * User: solar
 * Date: 02.06.14
 * Time: 11:37
 */
public class BitIOTest extends TestCase {
  public void testSimple() {
    final ByteBuffer buffer = ByteBuffer.allocate(32);

    {
      final BitOutput output = new BitOutputBuffer(buffer);
      output.write(10, 5);
      output.flush();
      buffer.rewind();
      final BitInput input = new BitInputBuffer(buffer);
      assertEquals(10, input.read(5));
      buffer.rewind();
    }
    {
      final BitOutput output = new BitOutputBuffer(buffer);
      output.write(255, 8);
      output.flush();
      buffer.rewind();
      final BitInput input = new BitInputBuffer(buffer);
      assertEquals(255, input.read(8));
      buffer.rewind();
    }

    {
      final BitOutput output = new BitOutputBuffer(buffer);
      output.write(2550, 13);
      output.flush();
      buffer.rewind();
      final BitInput input = new BitInputBuffer(buffer);
      assertEquals(2550, input.read(13));
      buffer.rewind();
    }
    {
      final BitOutput output = new BitOutputBuffer(buffer);
      output.write(1284312458, 19);
      output.flush();
      buffer.rewind();
      final BitInput input = new BitInputBuffer(buffer);
      final int read = input.read(19);
      assertEquals(1284312458 & (0xFFFFFFFF >>> (32 - 19)), read);
      buffer.rewind();
    }
    {
      final BitOutput output = new BitOutputBuffer(buffer);
      output.write(1284312458, 19);
      output.write(-1976303954, 15);
      output.flush();
      buffer.rewind();
      final BitInput input = new BitInputBuffer(buffer);
      final int read = input.read(19);
      assertEquals(1284312458 & (0xFFFFFFFF >>> (32 - 19)), read);
      buffer.rewind();
    }
  }

  public void testRandomBitIO() {
    final FastRandom rng = new FastRandom(0);
    for (int t = 0; t < 1000000; t++) {
      final int[] randSeq = new int[rng.nextPoisson(10)];
      final int[] bitCount = new int[randSeq.length];
      final ByteBuffer buffer = ByteBuffer.allocate(randSeq.length * 4);
      final BitOutput output = new BitOutputBuffer(buffer);
      for (int i = 0; i < randSeq.length; i++) {
        randSeq[i] = rng.nextInt();
        bitCount[i] = rng.nextInt(32);
        output.write(randSeq[i], bitCount[i]);
      }
      output.flush();
      buffer.rewind();
      final BitInput input = new BitInputBuffer(buffer);
      for (int i = 0; i < randSeq.length; i++) {
        final int read = input.read(bitCount[i]);
        final int shouldBe = bitCount[i] > 0 ? 0xFFFFFFFF >>> (32 - bitCount[i]) : 0;
        if ((randSeq[i] & shouldBe) != read)
          assertEquals(randSeq[i] & shouldBe, read);
      }
    }
  }
}
