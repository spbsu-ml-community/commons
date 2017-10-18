package com.expleague.commons.math.vectors.impl.mx;

import com.expleague.commons.math.vectors.Mx;
import com.expleague.commons.math.vectors.MxBuilder;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;

import java.util.LinkedList;
import java.util.List;

/**
 * User: qdeee
 * Date: 20.03.15
 */
public class MxByRowsBuilder implements MxBuilder {
  private final List<Vec> vecs = new LinkedList<>();

  @Override
  public MxBuilder add(final Vec vec) {
    this.vecs.add(vec);
    return this;
  }

  @Override
  public MxBuilder addAll(final List<Vec> vecs) {
    this.vecs.addAll(vecs);
    return this;
  }

  @Override
  public Mx build() {
    if (vecs.isEmpty()) {
      throw new IllegalStateException("Mx builder is empty");
    }

    final Vec joinedVec = VecTools.join(vecs);
    return new VecBasedMx(vecs.get(0).dim(), joinedVec);
  }
}
