package com.spbsu.commons.math.io;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;


import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.seq.CharSeqBuilder;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: solar
 * Date: 25.02.14
 * Time: 10:38
 */
public class ArrayVec2CharSequenceConversionPack implements ConversionPack<ArrayVec,CharSequence> {
  public static class ArrayVec2CharSequenceConverter implements TypeConverter<ArrayVec, CharSequence> {
    @Override
    public CharSequence convert(ArrayVec from) {
      final NumberFormat prettyPrint = NumberFormat.getInstance(Locale.US);
      prettyPrint.setMaximumFractionDigits(5);
      prettyPrint.setMinimumFractionDigits(0);
      prettyPrint.setRoundingMode(RoundingMode.HALF_UP);
      prettyPrint.setGroupingUsed(false);

      final CharSeqBuilder builder = new CharSeqBuilder();
      builder.append(from.dim());
      for (int i = 0; i < from.dim(); i++) {
        builder.append(" ").append(prettyPrint.format(from.get(i)));
      }
      return builder;
    }
  }

  public static class ArrayCharSequence2VecConverter implements TypeConverter<CharSequence, ArrayVec> {
    @Override
    public ArrayVec convert(CharSequence from) {
      final CharSequence[] parts = CharSeqTools.split(from.toString().trim(), ' ');
      final ArrayVec result = new ArrayVec(Integer.parseInt(parts[0].toString()));
      for (int i = 1; i < parts.length; i++) {
        result.set(i - 1, Double.parseDouble(parts[i].toString()));
      }
      return result;
    }
  }

  @Override
  public Class<? extends TypeConverter<ArrayVec, CharSequence>> to() {
    return ArrayVec2CharSequenceConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharSequence, ArrayVec>> from() {
    return ArrayCharSequence2VecConverter.class;
  }
}
