package com.spbsu.commons.math.io;

import java.text.NumberFormat;


import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.mx.SparseMx;
import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: solar
 * Date: 25.02.14
 * Time: 10:38
 */
public class Mx2CharSequenceConversionPack implements ConversionPack<Mx, CharSequence> {
  public static class Mx2CharSequenceConverter implements TypeConverter<Mx, CharSequence> {
    @Override
    public CharSequence convert(final Mx from) {
      final StringBuilder builder = new StringBuilder();
      final NumberFormat prettyPrint = MathTools.numberFormatter();
      if (from.vec() instanceof SparseVec) {
        builder.append(from.rows()).append(" ").append(from.columns());
        for (int i = 0; i < from.rows(); i++) {
          Vec row = from.row(i);
          final VecIterator it = row.nonZeroes();
          if (!row.nonZeroes().advance())
            continue;
          builder.append(i);
          while (it.advance()) {
            builder.append(" ");
            builder.append(it.index()).append(":").append(prettyPrint.format(it.value()));
          }
          builder.append("\n");
        }
      }
      else {
        builder.append(from.rows()).append(" ").append(from.columns());
        for (int i = 0; i < from.rows(); i++) {
          builder.append("\n");
          for (int j = 0; j < from.columns(); j++) {
            if (j > 0) {
              builder.append(" ");
            }
            builder.append(prettyPrint.format(from.get(i, j)));
          }
        }
      }
      return builder;
    }
  }

  public static class CharSequence2MxConverter implements TypeConverter<CharSequence, Mx> {
    @Override
    public Mx convert(final CharSequence from) {
      final CharSequence[] rows = CharSeqTools.split(from, '\n');
      final CharSequence[] sizes = CharSeqTools.split(rows[0].toString().trim(), ' ');
      final Mx result = new VecBasedMx(Integer.parseInt(sizes[0].toString()), Integer.parseInt(sizes[1].toString()));
      for (int i = 1; i < result.rows() + 1; i++) {
        final CharSequence[] cols = CharSeqTools.split(rows[i].toString().trim(), ' ');
        for (int j = 0; j < result.columns(); j++) {
          result.set(i - 1, j, Double.parseDouble(cols[j].toString()));
        }
      }
      return result;
    }
  }

  @Override
  public Class<? extends TypeConverter<Mx, CharSequence>> to() {
    return Mx2CharSequenceConversionPack.Mx2CharSequenceConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharSequence, Mx>> from() {
    return CharSequence2MxConverter.class;
  }
}
