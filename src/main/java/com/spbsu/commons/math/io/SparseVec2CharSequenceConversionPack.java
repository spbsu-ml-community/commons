package com.spbsu.commons.math.io;

import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.vectors.Vec;
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
    public CharSequence convert(SparseVec from) {
      CharSeqBuilder builder = new CharSeqBuilder();
      builder.append(from.dim()).append(':').append(from.indices.size());
      VecIterator nzIt = from.nonZeroes();
      while (nzIt.advance()) {
        builder.append(' ').append(nzIt.index()).append(':').append(nzIt.value());
      }
      return builder;
    }
  }

  public static class CharSequence2SparseVecConverter implements TypeConverter<CharSequence, SparseVec> {
    @Override
    public SparseVec convert(CharSequence from) {
      final CharSequence[] parts = CharSeqTools.split(from.toString().trim(), ' ');
      int dim;
      int nzCount;
      final CharSequence[] split = new CharSequence[2];
      {
        CharSeqTools.split(parts[0], ':', split);
        dim = CharSeqTools.parseInt(split[0]);
        nzCount = CharSeqTools.parseInt(split[1]);
      }
      int[] indices = new int[nzCount];
      double[] values = new double[nzCount];
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
