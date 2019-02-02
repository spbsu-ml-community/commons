package com.expleague.commons.math.vectors;

public enum Distance {
  COS {
    @Override
    public double distance(Vec left, Vec right) {
      return (1 - VecTools.cosine(left, right)) / 2;
    }
  },
  L2 {
    @Override
    public double distance(Vec left, Vec right) {
      return VecTools.distance(left, right);
    }
  },
  ;

  public abstract double distance(Vec left, Vec right);
}
