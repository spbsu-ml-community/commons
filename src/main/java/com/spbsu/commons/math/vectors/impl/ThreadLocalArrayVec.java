package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;

/**
 * User: solar
 * Date: 01.06.15
 * Time: 19:10
 */
public class ThreadLocalArrayVec extends ThreadLocal<Vec> {
  @Override
  protected Vec initialValue() {
    return new ArrayVec();
  }

  public Vec get(int dim) {
    Vec vec = get();
    if (dim > vec.dim()) {
      set(vec = new ArrayVec(dim));
    }
    else if (dim < vec.dim())
      vec = vec.sub(0, dim);
    VecTools.fill(vec, 0.);
    return vec;
  }
}
