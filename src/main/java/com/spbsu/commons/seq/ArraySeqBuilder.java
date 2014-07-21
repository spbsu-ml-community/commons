package com.spbsu.commons.seq;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;

public class ArraySeqBuilder<T> implements SeqBuilder<T>{
  private final List<T> objects = new ArrayList<>();
  private final Class<T> clazz;

  public ArraySeqBuilder(final Class<T> clazz) {
    this.clazz = clazz;
  }

  public ArraySeqBuilder<T> add(T t) {
    objects.add(t);
    return this;
  }

  public ArraySeqBuilder<T> addAll(Seq<T> values) {
    for (int i = 0; i < values.length(); i++) {
      objects.add(values.at(i));
    }
    return this;
  }

  public Seq<T> build() {
    if (Double.class.equals(clazz)) {
      double[] arr = new double[objects.size()];
      for (int i = 0; i < objects.size(); i++) {
        arr[i] = (Double)objects.get(i);
      }
      return (Seq<T>)new ArrayVec(arr);
    }
    else if (Integer.class.equals(clazz)) {
      int[] arr = new int[objects.size()];
      for (int i = 0; i < objects.size(); i++) {
        arr[i] = (Integer)objects.get(i);
      }
      return (Seq<T>)new IntSeq(arr);
    }
    else {
      return new ArraySeq<>(objects.toArray((T[])Array.newInstance(clazz, objects.size())));
    }
  }
}