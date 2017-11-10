package com.expleague.commons.seq;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.vectors.VecBuilder;

import java.util.Arrays;
import java.util.stream.BaseStream;

/**
 * Created by vkokarev on 22.07.14.
 */
public class VecSeq extends ArraySeq<Vec> {
  public VecSeq(final Seq<Vec> vecSeq) {
    this(vecSeq, 0, vecSeq.length());
  }

  public VecSeq(final Seq<Vec> vecSeq, final int start, final int end) {
    super(vecSeq, start, end);
  }

  public VecSeq(final Vec... vecs) {
    this(vecs, 0, vecs.length);
  }

  public VecSeq(final Vec[] vecs, final int start, final int end) {
    super(vecs, start, end);
  }

  public Vec concat() {
    final VecBuilder vb = new VecBuilder();
    stream().forEach(vb::addAll);
    return vb.build();
  }
}
