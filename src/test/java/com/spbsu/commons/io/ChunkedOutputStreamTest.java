package com.spbsu.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;


import org.junit.Assert;
import org.junit.Test;

/**
 * User: amosov-f
 * Date: 21.03.15
 * Time: 0:38
 */
@SuppressWarnings("MagicNumber")
public final class ChunkedOutputStreamTest {
  @Test
  public void test() throws Exception {
    final ByteArrayOutputStream expected = new ByteArrayOutputStream();
    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    final ChunkedOutputStream out = new ChunkedOutputStream(new DataOutputStream(bout), 20);
    final Random random = new Random(0);
    for (int i = 0; i < 1000; i++) {
      if (i % 2 == 0) {
        out.write(i);
        expected.write(i);
      } else {
        final byte[] b = new byte[random.nextInt(40) + 1];
        random.nextBytes(b);
        final int off = random.nextInt(b.length);
        final int len = random.nextInt(b.length - off);
        out.write(b, off, len);
        expected.write(b, off, len);
      }
    }
    out.endChunks();
    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
    Assert.assertArrayEquals(expected.toByteArray(), new ChunkReader().readChunks(in, 32));
  }
}