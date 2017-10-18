package com.expleague.commons.seq;

import gnu.trove.list.array.TByteArrayList;

@SuppressWarnings("EqualsAndHashcode")
public class ByteSeqBuilder implements SeqBuilder<Byte> {
  TByteArrayList base = new TByteArrayList();
  @Override
  public SeqBuilder<Byte> add(final Byte aByte) {
    base.add(aByte);
    return this;
  }

  public SeqBuilder<Byte> add(final byte aByte) {
    base.add(aByte);
    return this;
  }

  @Override
  public SeqBuilder<Byte> addAll(final Seq<Byte> values) {
    base.addAll((byte[]) CharSeqTools.toArray(values));
    return this;
  }

  public SeqBuilder<Byte> addAll(final byte[] buf, final int off, final int len) {
    base.add(buf, off, len);
    return this;
  }

  @Override
  public ByteSeq build() {
    final ByteSeq result = new ByteSeq(base.toArray());
    base.clear();
    return result;
  }

  @Override
  public void clear() {
    base.clear();
  }
}
