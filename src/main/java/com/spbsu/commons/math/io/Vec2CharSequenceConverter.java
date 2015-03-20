package com.spbsu.commons.math.io;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;


import com.spbsu.commons.func.Converter;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * Created by vkokarev on 03.12.14.
 */
public class Vec2CharSequenceConverter implements Converter<Vec, CharSequence> {
  @Override
  public Vec convertFrom(final CharSequence source) {
    final CharSequence[] parts = CharSeqTools.indexOf(source, "\t") >= 0 ? CharSeqTools.split(source, "\t") : CharSeqTools.split(source, " ");
    final Vec result;
    if (CharSeqTools.indexOf(source, ":") >= 0) {
      result = new SparseVec(CharSeqTools.parseInt(parts[0]));
      final CharSequence[] idx2val = new CharSequence[2];
      for (int i = 1; i < parts.length; i++) {
        CharSeqTools.split(parts[i], ':', idx2val);
        result.set(CharSeqTools.parseInt(idx2val[0]), CharSeqTools.parseDouble(idx2val[1]));
      }
    }
    else {
      result = new ArrayVec(CharSeqTools.parseInt(parts[0]));
      for (int i = 1; i < parts.length; i++) {
        result.set(i - 1, CharSeqTools.parseDouble(parts[i]));
      }
    }
    return result;
  }

  @Override
  public CharSequence convertTo(final Vec v) {
    final NumberFormat prettyPrint = NumberFormat.getInstance(Locale.US);
    prettyPrint.setMaximumFractionDigits(5);
    prettyPrint.setMinimumFractionDigits(0);
    prettyPrint.setRoundingMode(RoundingMode.HALF_UP);
    prettyPrint.setGroupingUsed(false);

    final StringBuilder builder = new StringBuilder();
    builder.append(v.dim());
    if (v instanceof SparseVec) {
      final VecIterator it = v.nonZeroes();
      while(it.advance()) {
        builder.append(" ");
        builder.append(it.index()).append(":").append(prettyPrint.format(it.value()));
      }
    }
    else {
      for (int i = 0; i < v.dim(); i++) {
        builder.append(" ");
        builder.append(prettyPrint.format(v.get(i)));
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
