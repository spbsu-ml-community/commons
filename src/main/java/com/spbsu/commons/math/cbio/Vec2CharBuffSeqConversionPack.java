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
      final CharBufferSeq.Tokenizer tokenizer = from.getTokenizer(" ");
      final Vec result = new ArrayVec(Integer.parseInt(tokenizer.nextToken().toString()));
      int i = 0;
      while (tokenizer.hasMoreElements()) {
        result.set(i++, Double.parseDouble(tokenizer.nextToken().toString()));
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
