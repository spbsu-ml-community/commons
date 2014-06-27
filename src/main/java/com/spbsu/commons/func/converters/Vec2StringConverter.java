package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.math.vectors.impl.basis.IntBasis;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;

import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 29.05.12
 * Time: 0:31
 * To change this template use File | Settings | File Templates.
 */
public class Vec2StringConverter implements Converter<Vec, String>{
  @Override
  public Vec convertFrom(String source) {
    StringTokenizer tok = new StringTokenizer(source, "\t");
    Vec vec;
    if (source.contains(":")) {
      vec = new SparseVec(new IntBasis(Integer.parseInt(tok.nextToken())));
      while(tok.hasMoreElements()) {
        StringTokenizer p = new StringTokenizer(tok.nextToken(), ":");
        vec.set(Integer.parseInt(p.nextToken()), Double.parseDouble(p.nextToken()));
      }
    }
    else {
      vec = new ArrayVec(Integer.parseInt(tok.nextToken()));
      for (int i = 0; i < vec.dim() && tok.hasMoreElements(); i++) {
        vec.set(i, Double.parseDouble(tok.nextToken()));
      }
    }
    return vec;
  }

  @Override
  public String convertTo(Vec v) {
    StringBuilder builder = new StringBuilder();
    builder.append(v.dim());
    if (v instanceof SparseVec) {
      final VecIterator it = v.nonZeroes();
      while(it.advance()) {
        builder.append("\t");
        builder.append(it.index()).append(":").append(it.value());
      }
    }
    else {
      for (int i = 0; i < v.dim(); i++) {
        builder.append("\t");
        builder.append(v.get(i));
      }
    }
    return builder.toString();
  }

  /**
   * don't write length of vec!!
   */
  public CharSequence convertToSparse(final Vec vec) {
    final StringBuilder sb = new StringBuilder();
    final VecIterator it = vec.nonZeroes();
    while (it.advance()) {
      sb.append(it.index())
          .append(":")
          .append(it.value())
          .append(" ");
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb;
  }
}
