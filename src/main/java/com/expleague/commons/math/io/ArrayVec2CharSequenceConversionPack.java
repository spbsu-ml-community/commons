package com.expleague.commons.math.io;

import java.text.NumberFormat;


import com.expleague.commons.func.types.ConversionPack;
import com.expleague.commons.func.types.TypeConverter;
import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.CharSeqBuilder;

/**
 * User: solar
 * Date: 25.02.14
 * Time: 10:38
 */
public class ArrayVec2CharSequenceConversionPack implements ConversionPack<ArrayVec, CharSequence> {
  public static class ArrayVec2CharSequenceConverter implements TypeConverter<ArrayVec, CharSequence> {
    @Override
    public CharSequence convert(final ArrayVec from) {
      final NumberFormat prettyPrint = MathTools.numberFormatter();

      final CharSeqBuilder builder = new CharSeqBuilder();
      builder.append(from.dim());
      for (int i = 0; i < from.dim(); i++) {
        builder.append(" ").append(prettyPrint.format(from.get(i)));
      }
      return builder.build();
    }
  }

  public static class ArrayCharSequence2VecConverter implements TypeConverter<CharSequence, ArrayVec> {
    @Override
    public ArrayVec convert(final CharSequence from) {
      final CharSequence[] parts = CharSeqTools.split(CharSeqTools.trim(from), ' ');
      final ArrayVec result = new ArrayVec(CharSeqTools.parseInt(parts[0]));
      for (int i = 1; i < parts.length; i++) {
        result.set(i - 1, CharSeqTools.parseDouble(parts[i]));
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
