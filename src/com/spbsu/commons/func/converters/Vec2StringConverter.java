package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.math.vectors.IntBasis;
import com.spbsu.commons.math.vectors.impl.SparseVec;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;

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
    StringTokenizer tok = new StringTokenizer(source, " ");
    SparseVec vec = new SparseVec(new IntBasis(Integer.parseInt(tok.nextToken())));
    while(tok.hasMoreElements()) {
      StringTokenizer p = new StringTokenizer(tok.nextToken(), ":");
      vec.set(Integer.parseInt(p.nextToken()), Double.parseDouble(p.nextToken()));
    }
    return vec;
  }

  @Override
  public String convertTo(Vec v) {
    StringBuilder builder = new StringBuilder();
    builder.append(v.basis().size());
    final VecIterator it = v.nonZeroes();
    while(it.advance()) {
      builder.append(" ");
      builder.append(it.index()).append(":").append(it.value());
    }
    return builder.toString();
  }
}
