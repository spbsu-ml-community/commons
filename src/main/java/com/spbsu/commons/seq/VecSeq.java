package com.spbsu.commons.seq;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.impl.vectors.VecBuilder;

import java.util.Arrays;

/**
 * Created by vkokarev on 22.07.14.
 */
public class VecSeq extends Seq.Stub<Vec> {
  final Vec[] vecs;
  public final int start;
  public final int end;


  public VecSeq(final Seq<Vec> vecSeq) {
    this(vecSeq, 0, vecSeq.length());
  }

  public VecSeq(final Seq<Vec> vecSeq, final int start, final int end) {
    this.start = start;
    this.end = end;
    this.vecs = new Vec[end - start];
    for (int i = start; i < end; ++i) {
      vecs[i - start] = vecSeq.at(i);
    }
  }

  public VecSeq(final Vec[] vecs) {
    this(vecs, 0, vecs.length);
  }

  public VecSeq(final Vec[] vecs, final int start, final int end) {
    if (start < 0 || end > vecs.length)
      throw new ArrayIndexOutOfBoundsException();
    this.vecs = vecs;
    this.start = start;
    this.end = end;
  }

  @Override
  public Vec at(final int i) {
    return vecs[start + i];
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  public Vec concat() {
    final VecBuilder vb = new VecBuilder();
    for (int i = start; i < end; ++i) {
      vb.addAll(vecs[i]);
    }
    return vb.build();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VecSeq)) {
      return false;
    }

    final VecSeq vecSeq = (VecSeq) o;

    if (end != vecSeq.end) {
      return false;
    }
    if (start != vecSeq.start) {
      return false;
    }
    return Arrays.equals(vecs, vecSeq.vecs);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(vecs);
    result = 31 * result + start;
    result = 31 * result + end;
    return result;
  }

  @Override
  public Class<Vec> elementType() {
    return Vec.class;
  }
}
