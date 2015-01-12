package com.spbsu.commons.math.io;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;


import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.seq.CharSeqBuilder;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: solar
 * Date: 25.02.14
 * Time: 10:38
 */
public class SparseVec2CharSequenceConversionPack implements ConversionPack<SparseVec,CharSequence> {
  public static class SparseVec2CharSequenceConverter implements TypeConverter<SparseVec, CharSequence> {
    @Override
    public CharSequence convert(final SparseVec from) {
      final NumberFormat prettyPrint = NumberFormat.getInstance(Locale.US);
      prettyPrint.setMaximumFractionDigits(5);
      prettyPrint.setMinimumFractionDigits(0);
      prettyPrint.setRoundingMode(RoundingMode.HALF_UP);
      prettyPrint.setGroupingUsed(false);

      final CharSeqBuilder builder = new CharSeqBuilder();
      builder.append(from.dim()).append(':').append(from.indices.size());
      final VecIterator nzIt = from.nonZeroes();
      while (nzIt.advance()) {
        builder.append(' ').append(nzIt.index()).append(':').append(prettyPrint.format(nzIt.value()));
      }
      return builder;
    }
  }

  public static class CharSequence2SparseVecConverter implements TypeConverter<CharSequence, SparseVec> {
    @Override
    public SparseVec convert(final CharSequence from) {
      final CharSequence[] parts = CharSeqTools.split(from.toString().trim(), ' ');
      final int dim;
      final int nzCount;
      final CharSequence[] split = new CharSequence[2];
      {
        CharSeqTools.split(parts[0], ':', split);
        dim = CharSeqTools.parseInt(split[0]);
        nzCount = CharSeqTools.parseInt(split[1]);
      }
      final int[] indices = new int[nzCount];
      final double[] values = new double[nzCount];
      for (int i = 1; i < parts.length; i++) {
        CharSeqTools.split(parts[i], ':', split);
        indices[i - 1] = CharSeqTools.parseInt(split[0]);
        values[i - 1] = CharSeqTools.parseDouble(split[1]);
      }
      return new SparseVec(dim, indices, values);
    }
  }

  @Override
  public Class<? extends TypeConverter<SparseVec, CharSequence>> to() {
    return SparseVec2CharSequenceConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharSequence, SparseVec>> from() {
    return CharSequence2SparseVecConverter.class;
  }
}
