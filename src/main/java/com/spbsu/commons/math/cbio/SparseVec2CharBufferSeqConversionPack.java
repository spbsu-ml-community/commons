package com.spbsu.commons.math.cbio;

import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.io.SparseVec2CharSequenceConversionPack;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.seq.CharBufferSeq;
import com.spbsu.commons.seq.CharSeqTools;

import java.util.StringTokenizer;

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
      final StringTokenizer tokenizer = new StringTokenizer(from.toString(), " ");

      String token = tokenizer.nextToken();
      int pos = token.indexOf(':');
      String index = token.substring(0, pos);
      String value = token.substring(pos + 1);

      final int dim = Integer.parseInt(index);
      final int nzCount = Integer.parseInt(value);
      final int[] indices = new int[nzCount];
      final double[] values = new double[nzCount];
      for (int i = 0; tokenizer.hasMoreTokens(); i++) {
        token = tokenizer.nextToken();
        pos = token.indexOf(':');
        index = token.substring(0, pos);
        value = token.substring(pos + 1);
        indices[i] = Integer.parseInt(index);
        values[i] = Double.parseDouble(value);
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
