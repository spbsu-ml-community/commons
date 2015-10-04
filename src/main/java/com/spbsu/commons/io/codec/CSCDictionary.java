package com.spbsu.commons.io.codec;

import com.spbsu.commons.io.codec.seq.DictExpansion;
import com.spbsu.commons.io.codec.seq.Dictionary;
import com.spbsu.commons.io.codec.seq.ListDictionary;
import com.spbsu.commons.seq.ByteSeq;
import com.spbsu.commons.seq.Seq;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: amosov-f
 * Date: 29.09.14
 * Time: 19:10
 */
public final class CSCDictionary {
  private static final String ID = "cscd";

  @NotNull
  private final Dictionary<Byte> dict;
  @NotNull
  private final int[] freqs;

  public CSCDictionary(@NotNull final DictExpansion<Byte> expansion) {
    this(expansion.result(), expansion.resultFreqs());
  }

  public CSCDictionary(@NotNull final Dictionary<Byte> dict, @NotNull final int[] freqs) {
    this.dict = dict;
    this.freqs = freqs;
  }

  @NotNull
  public static CSCDictionary read(@NotNull final InputStream in) throws IOException {
    return read(new DataInputStream(in));
  }

  @NotNull
  private static CSCDictionary read(@NotNull final DataInputStream in) throws IOException {
    final byte[] magic = new byte[ID.length()];
    in.readFully(magic);
    if (!new String(magic).equals(ID)) {
      throw new IOException("Bad magic number!");
    }

    final int length = in.readInt();
    final ByteSeq[] dict = new ByteSeq[length];
    final int[] freqs = new int[length];

    for (int i = 0; i < length; i++) {
      final int freq = in.readInt();
      final int l = in.readInt();
      final byte[] seq = new byte[l];

      for (int j = 0; j < l; j++) {
        seq[j] = in.readByte();
      }

      freqs[i] = freq;
      dict[i] = new ByteSeq(seq);
    }

    return new CSCDictionary(new ListDictionary<>(dict), freqs);
  }

  @NotNull
  public Dictionary<Byte> getDict() {
    return dict;
  }

  @NotNull
  public int[] getFreqs() {
    return freqs;
  }

  @NotNull
  public List<String> getWords() {
    final List<String> words = new ArrayList<>();
    for (int i = 0; i < dict.size(); i++) {
      final byte[] bytes = new byte[dict.get(i).length()];
      for (int j = 0; j < bytes.length; j++) {
        bytes[j] = dict.get(i).at(j);
      }
      words.add(new String(bytes));
    }
    return words;
  }

  @NotNull
  public CSCInputStream createInputStream(@NotNull final InputStream in) {
    return new CSCInputStream(in, dict, freqs);
  }

  @NotNull
  public CSCOutputStream createOutputStream(@NotNull final OutputStream out) {
    return new CSCOutputStream(out, dict, freqs);
  }

  public void write(@NotNull final OutputStream out) throws IOException {
    write(new DataOutputStream(out));
  }

  private void write(@NotNull final DataOutputStream out) throws IOException {
    final int length = freqs.length;

    out.writeBytes(ID);
    out.writeInt(length);

    for (int i = 0; i < length; i++) {
      final int freq = freqs[i];
      final Seq<Byte> seq = dict.get(i);

      out.writeInt(freq);
      final int l = seq.length();
      out.writeInt(l);

      for (int j = 0; j < l; j++) {
        out.writeByte(seq.at(j));
      }
    }
    out.flush();
  }
}