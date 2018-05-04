package com.expleague.commons.math;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import org.jetbrains.annotations.NotNull;

public interface DiscontinuousTrans extends Trans {
  @NotNull
  Vec left(Vec x);
  @NotNull
  Vec right(Vec x);
  @NotNull
  Vec leftTo(Vec x, Vec to);
  @NotNull
  Vec rightTo(Vec x, Vec to);

  abstract class Stub extends Trans.Stub implements DiscontinuousTrans {
    @NotNull
    @Override
    public Vec left(Vec x) {
      Vec result = new ArrayVec(xdim());
      return leftTo(x, result);
    }

    @NotNull
    @Override
    public Vec right(Vec x) {
      Vec result = new ArrayVec(xdim());
      return rightTo(x, result);
    }
  }
}
