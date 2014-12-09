package com.spbsu.commons.math.cbio;

import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.io.SparseVec2CharSequenceConversionPack;
import com.spbsu.commons.math.io.ArrayVec2CharSequenceConversionPack;
import com.spbsu.commons.math.io.VecSeq2CharSequenceConversionPack;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.seq.CharBufferSeq;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.VecSeq;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkokarev on 06.10.14.
 */
public class VecSeq2CharBuffSeqConversionPack implements ConversionPack<VecSeq, CharBufferSeq> {
  private static final String TYPE_VALUE_DELIMETER = "?";
  private static final String VEC_DELIMETER = "|";

  public static class VecSeq2CharBufferSeqConverter implements TypeConverter<VecSeq, CharBufferSeq> {
    private static final VecSeq2CharSequenceConversionPack.VecSeq2CharSequenceConverter vecSeq2CharSeq = new VecSeq2CharSequenceConversionPack.VecSeq2CharSequenceConverter();
    /**
     * NO RLE SUPPORT
     * @param vecSeq
     * @return
     */
    @Override
    public CharBufferSeq convert(final VecSeq vecSeq) {
      return new CharBufferSeq(vecSeq2CharSeq.convert(vecSeq));
    }
  }

  public static class CharBufferSeq2VecSeqConverter implements TypeConverter<CharBufferSeq, VecSeq> {
    private static final SparseVec2CharSequenceConversionPack.CharSequence2SparseVecConverter sparseStr2VecConverter = new SparseVec2CharSequenceConversionPack.CharSequence2SparseVecConverter();
    private static final ArrayVec2CharSequenceConversionPack.ArrayCharSequence2VecConverter str2VecConverter = new ArrayVec2CharSequenceConversionPack.ArrayCharSequence2VecConverter();

    @Override
    public VecSeq convert(final CharBufferSeq from) {
      final CharBufferSeq.Tokenizer tokenizer = from.getTokenizer(VEC_DELIMETER);
      final boolean isSimilar = Boolean.valueOf(tokenizer.nextToken().toString());
      final boolean isRLE = Boolean.valueOf(tokenizer.nextToken().toString());
      VecType type = null;
      CharSequence len = "";
      if (isSimilar) {
        type = VecType.getByName(tokenizer.nextToken());
        len = tokenizer.nextToken();
      }
      final List<Vec> vecs = new ArrayList<>();

      while (tokenizer.hasMoreElements()) {
        final String token = tokenizer.nextToken().toString();
        final CharSequence serializedVec;
        final Integer times;

        if (isRLE) {
          final String[] parts = token.split(" ", 2);
          times = Integer.valueOf(parts[0]);
          serializedVec = parts[1];
        } else {
          times = 1;
          serializedVec = token;
        }

        final CharSequence[] vecParts;
        final CharSequence vecStr;
        final Vec vec;

        if (!isSimilar) {
          vecParts = CharSeqTools.split(serializedVec, TYPE_VALUE_DELIMETER);
          type = VecType.getByName(String.valueOf(vecParts[0]));
          vecStr = vecParts[1];
        } else {
          vecStr = CharSeqTools.concatWithDelimeter("", len, serializedVec);
        }
        switch (type) {
          case COMMON:
            vec = str2VecConverter.convert(vecStr);
            break;
          case SPARSE:
            vec = sparseStr2VecConverter.convert(vecStr);
            break;
          default:
            throw new RuntimeException("unexpected type");
        }

        for (int i = 0; i < times; ++i) {
          vecs.add(vec);
        }
      }
      return new VecSeq(vecs.toArray(new Vec[vecs.size()]));
    }
  }

  @Override
  public Class<? extends TypeConverter<VecSeq, CharBufferSeq>> to() {
    return VecSeq2CharBufferSeqConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharBufferSeq, VecSeq>> from() {
    return CharBufferSeq2VecSeqConverter.class;
  }

  private enum VecType {
    COMMON("c"),
    SPARSE("s");

    private final CharSequence name;

    private VecType(final CharSequence name) {
      this.name = name;
    }

    public CharSequence getName() {
      return name;
    }

    public static VecType getByName(final CharSequence name) {
      if (COMMON.getName().equals(name.toString())) {
        return COMMON;
      } else if (SPARSE.getName().equals(name.toString())) {
        return SPARSE;
      }
      throw new IllegalArgumentException("No enum constant " + name);
    }
  }
}