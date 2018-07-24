package com.expleague.commons.csv;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqBuilder;
import com.expleague.commons.seq.CharSeqTools;
import gnu.trove.map.TObjectIntMap;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Optional;

public class WritableCsvRow implements CsvRow {
  private final CharSeq[] split;
  private final TObjectIntMap<String> names;

  public WritableCsvRow(CharSeq[] split, TObjectIntMap<String> names) {
    this.split = split;
    this.names = names;
  }

  @Override
  public CharSeq at(int i) {
    return split[i];
  }

  public WritableCsvRow set(int i, CharSeq seq) {
    if (seq != null)
      split[i] = seq;
    return this;
  }

  public WritableCsvRow set(String name, CharSeq seq) {
    final int index = names.get(name);
    if (index == 0)
      throw new RuntimeException("Stream does not contain required column '" + name + "'!");
    if (seq != null)
      split[index - 1] = seq;
    return this;
  }

  public WritableCsvRow set(String name, int v) {
    set(name, CharSeq.create(Integer.toString(v)));
    return this;
  }

  public WritableCsvRow set(String name, long v) {
    set(name, CharSeq.create(Long.toString(v)));
    return this;
  }

  public WritableCsvRow set(String name, double v) {
    set(name, CharSeq.create(Double.toString(v)));
    return this;
  }

  public WritableCsvRow set(String name, float v) {
    set(name, CharSeq.create(Float.toString(v)));
    return this;
  }

  public WritableCsvRow set(String name, boolean v) {
    set(name, CharSeq.create(Boolean.toString(v)));
    return this;
  }

  public WritableCsvRow set(String name, String v) {
    if (v != null)
      set(name, CharSeq.create(v));
    return this;
  }

  @Override
  public CsvRow names() {
    final CharSeq[] names = new CharSeq[Arrays.stream(this.names.values()).max().orElse(0)];
    Arrays.fill(names, CharSeq.create("duplicate"));
    this.names.forEachEntry((name, index) -> {
      names[index - 1] = CharSeq.create(name);
      return true;
    });
    return new WritableCsvRow(names, this.names);
  }

  @Override
  public Optional<CharSeq> apply(String name) {
    final int index = names.get(name);
    if (index == 0)
      throw new RuntimeException("Stream does not contain required column '" + name + "'!");
    final CharSeq part = split[index - 1];
    return part.length() > 0 ? Optional.of(part) : Optional.empty();
  }

  @Override
  public String toString() {
    final CharSeqBuilder builder = new CharSeqBuilder();
    for (int i = 0; i < split.length; i++) {
      builder.append('"').append(CharSeqTools.replace(split[i], "\"", "\"\"")).append('"');
      if (i < split.length - 1)
        builder.append(',');
    }
    return builder.toString();
  }

  public void writeln(Writer out) {
    try {
      for (int i = 0; i < split.length; i++) {
        out.append('"').append(CharSeqTools.replace(split[i], "\"", "\"\"")).append('"');
        if (i < split.length - 1)
          out.append(',');
      }
      out.append('\n');
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public CsvRow clone() {
    CharSeq[] split = new CharSeq[this.split.length];
    for (int i = 0; i < split.length; i++) {
      split[i] = CharSeq.intern(this.split[i]);
    }
    return new WritableCsvRow(split, names);
  }
}
