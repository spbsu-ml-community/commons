package com.spbsu.commons.math.cbio;

import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.seq.CharBufferSeq;
import com.spbsu.commons.seq.CharSeqBuilder;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by vkokarev on 06.10.14.
 */
public class Vec2CharBuffSeqConversionPack implements ConversionPack<Vec,CharBufferSeq> {
  public static class Vec2CharBuffSeqConverter implements TypeConverter<Vec, CharBufferSeq> {
    @Override
    public CharBufferSeq convert(final Vec from) {
      final NumberFormat prettyPring = NumberFormat.getInstance(Locale.US);
      prettyPring.setMaximumFractionDigits(5);
      prettyPring.setMinimumFractionDigits(0);
      prettyPring.setRoundingMode(RoundingMode.HALF_UP);

      final CharSeqBuilder builder = new CharSeqBuilder();
      builder.append(from.dim());
      for (int i = 0; i < from.dim(); i++) {
        builder.append(" ").append(prettyPring.format(from.get(i)));
      }
      return new CharBufferSeq(builder);
    }
  }

  public static class CharBuffSeq2VecConverter implements TypeConverter<CharBufferSeq, Vec> {
    @Override
    public Vec convert(final CharBufferSeq from) {
      final StringTokenizer tokenizer = new StringTokenizer(from.toString(), " ");
      final int size = Integer.parseInt(tokenizer.nextToken());
      final Vec result = new ArrayVec(size);
      for (int i = 0; tokenizer.hasMoreTokens(); i++) {
        final String token = tokenizer.nextToken();
        result.set(i, Double.parseDouble(token));
      }
      return result;
    }
  }

  @Override
  public Class<? extends TypeConverter<Vec, CharBufferSeq>> to() {
    return Vec2CharBuffSeqConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharBufferSeq, Vec>> from() {
    return CharBuffSeq2VecConverter.class;
  }
}
