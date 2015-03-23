package com.spbsu.commons.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * TODO amosov-f: create ChunkedInputStream
 *  
 * User: amosov-f
 * Date: 21.03.15
 * Time: 1:47
 */
public final class ChunkReader {
  private ChunkReader() {
  }

  @Nullable
  public static byte[] readChunk(@NotNull final DataInputStream in) throws IOException {
    final int chunkSize = in.readInt();
    if (chunkSize < 0) {
      return null;
    }
    final byte[] chunk = new byte[chunkSize];
    in.readFully(chunk);
    if (in.read() != ChunkedOutputStream.MAGIC) {
      throw new IllegalStateException();
    }
    return chunk;
  }
  
  @NotNull
  public static byte[] readChunks(@NotNull final DataInputStream in) throws IOException {
    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    byte[] chunk;
    while ((chunk = readChunk(in)) != null) {
      bout.write(chunk);
    }
    return bout.toByteArray();
  }
}
