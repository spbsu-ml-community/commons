package com.spbsu.commons.math.io;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;


import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: solar
 * Date: 25.02.14
 * Time: 10:38
 */
public class Mx2CharSequenceConversionPack implements ConversionPack<Mx,CharSequence> {
  public static class Mx2CharSequenceConverter implements TypeConverter<Mx, CharSequence> {
    @Override
    public CharSequence convert(Mx from) {
      final NumberFormat prettyPrint = NumberFormat.getInstance(Locale.US);
      prettyPrint.setMaximumFractionDigits(5);
      prettyPrint.setMinimumFractionDigits(0);
      prettyPrint.setRoundingMode(RoundingMode.HALF_UP);
      prettyPrint.setGroupingUsed(false);

      final StringBuilder builder = new StringBuilder();
      builder.append(from.rows()).append(" ").append(from.columns());
      for (int i = 0; i < from.rows(); i++) {
        builder.append("\n");
        for (int j = 0; j < from.columns(); j++) {
          if (j > 0)
            builder.append(" ");
          builder.append(prettyPrint.format(from.get(i, j)));
        }
      }
      return builder;
    }
  }

  public static class CharSequence2MxConverter implements TypeConverter<CharSequence, Mx> {
    @Override
    public Mx convert(CharSequence from) {
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
