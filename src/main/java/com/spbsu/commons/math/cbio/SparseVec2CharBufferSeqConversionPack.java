package com.spbsu.commons.math.cbio;

import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.io.SparseVec2CharSequenceConversionPack;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.seq.CharBufferSeq;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * Created by vkokarev on 07.10.14.
 */
public class SparseVec2CharBufferSeqConversionPack  implements ConversionPack<SparseVec,CharBufferSeq> {
  public static class SparseVec2CharBufferSeqConverter implements TypeConverter<SparseVec, CharBufferSeq> {
    private static final SparseVec2CharSequenceConversionPack.SparseVec2CharSequenceConverter sparseVec2CharSeqConverter = new SparseVec2CharSequenceConversionPack.SparseVec2CharSequenceConverter();
    @Override
    public CharBufferSeq convert(final SparseVec from) {
      return new CharBufferSeq(sparseVec2CharSeqConverter.convert(from));
    }
  }

  public static class CharBufferSeq2SparseVecConverter implements TypeConverter<CharBufferSeq, SparseVec> {
    @Override
    public SparseVec convert(final CharBufferSeq from) {
      final CharBufferSeq.Tokenizer tokenizer = from.getTokenizer(" ");
      final int dim;
      final int nzCount;
      final CharSequence[] split = new CharSequence[2];
      {
        CharSeqTools.split(tokenizer.nextToken(), ':', split);
        dim = CharSeqTools.parseInt(split[0]);
        nzCount = CharSeqTools.parseInt(split[1]);
      }
      final int[] indices = new int[nzCount];
      final double[] values = new double[nzCount];
      int i = 0;
      while (tokenizer.hasMoreElements()) {
        CharSeqTools.split(tokenizer.nextToken(), ':', split);
        indices[i] = CharSeqTools.parseInt(split[0]);
        values[i] = CharSeqTools.parseDouble(split[1]);
        ++i;
      }
      return new SparseVec(dim, indices, values);
    }
  }

  @Override
  public Class<? extends TypeConverter<SparseVec, CharBufferSeq>> to() {
    return SparseVec2CharBufferSeqConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharBufferSeq, SparseVec>> from() {
    return CharBufferSeq2SparseVecConverter.class;
  }
}
