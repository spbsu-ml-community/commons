package com.expleague.commons.seq;

import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArraySeqBuilder<T> implements SeqBuilder<T>{
  private final List<T> objects = new ArrayList<>();
  private final Class<T> clazz;

  public ArraySeqBuilder(final Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public ArraySeqBuilder<T> add(final T t) {
    objects.add(t);
    return this;
  }

  @Override
  public ArraySeqBuilder<T> addAll(final Seq<T> values) {
    for (int i = 0; i < values.length(); i++) {
      objects.add(values.at(i));
    }
    return this;
  }

  @Override
  public Seq<T> build() {
    if (Double.class.equals(clazz)) {
      final double[] arr = new double[objects.size()];
      for (int i = 0; i < objects.size(); i++) {
        arr[i] = (Double)objects.get(i);
      }
      return (Seq<T>)new ArrayVec(arr);
    }
    else if (Integer.class.equals(clazz)) {
      final int[] arr = new int[objects.size()];
      for (int i = 0; i < objects.size(); i++) {
        arr[i] = (Integer)objects.get(i);
      }
      return (Seq<T>)new IntSeq(arr);
    }
    else {
      return new ArraySeq<>(objects.toArray((T[])Array.newInstance(clazz, objects.size())));
    }
  }

  @Override
  public void clear() {
    objects.clear();
  }
}