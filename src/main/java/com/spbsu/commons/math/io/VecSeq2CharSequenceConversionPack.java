package com.spbsu.commons.math.io;

import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.VecSeq;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by vkokarev on 22.07.14.
 */
public class VecSeq2CharSequenceConversionPack implements ConversionPack<VecSeq, CharSequence> {
  private static final String TYPE_VALUE_DELIMETER = "?";
  private static final String VEC_DELIMETER = "|";

  public static class VecSeq2CharSequenceConverter implements TypeConverter<VecSeq, CharSequence> {
    private static final SparseVec2CharSequenceConversionPack.SparseVec2CharSequenceConverter sparseVec2StrConverter = new SparseVec2CharSequenceConversionPack.SparseVec2CharSequenceConverter();
    private static final Vec2CharSequenceConversionPack.Vec2CharSequenceConverter vec2StrConverter = new Vec2CharSequenceConversionPack.Vec2CharSequenceConverter();

    @Override
    public CharSequence convert(final VecSeq vecSeq) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < vecSeq.length(); ++i) {
        final Vec vec = vecSeq.at(i);
        final CharSequence serializedVec;
        final VecType tag;
        if (vec instanceof SparseVec) {
          tag = VecType.SPARSE;
          serializedVec = sparseVec2StrConverter.convert((SparseVec)vec);
        } else {
          tag = VecType.COMMON;
          serializedVec = vec2StrConverter.convert(vec);
        }
        if (i != 0) {
          sb.append(VEC_DELIMETER);
        }
        sb.append(tag.getName())
            .append(TYPE_VALUE_DELIMETER)
            .append(serializedVec);
      }
      return sb;
    }
  }

  public static class CharSequence2VecSeqConverter implements TypeConverter<CharSequence, VecSeq> {
    private static final SparseVec2CharSequenceConversionPack.CharSequence2SparseVecConverter sparseStr2VecConverter = new SparseVec2CharSequenceConversionPack.CharSequence2SparseVecConverter();
    private static final Vec2CharSequenceConversionPack.CharSequence2VecConverter str2VecConverter = new Vec2CharSequenceConversionPack.CharSequence2VecConverter();

    @Override
    public VecSeq convert(final CharSequence from) {
      final CharSequence[] serializedVecs = CharSeqTools.split(from.toString().trim(), VEC_DELIMETER);
      final List<Vec> vecs = new ArrayList<>(serializedVecs.length);

      for (final CharSequence serializedVec : serializedVecs) {
        CharSequence[] parts = CharSeqTools.split(serializedVec, TYPE_VALUE_DELIMETER);
        final VecType type = VecType.getByName(String.valueOf(parts[0]));
        final CharSequence vecStr = parts[1];
        final Vec vec;
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
        vecs.add(vec);
      }
      return new VecSeq(vecs.toArray(new Vec[vecs.size()]));
    }
  }

  @Override
  public Class<? extends TypeConverter<VecSeq, CharSequence>> to() {
    return VecSeq2CharSequenceConverter.class;
  }

  @Override
  public Class<? extends TypeConverter<CharSequence, VecSeq>> from() {
    return CharSequence2VecSeqConverter.class;
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
      if (COMMON.getName().equals(name)) {
        return COMMON;
      } else if (SPARSE.getName().equals(name)) {
        return SPARSE;
      }
      throw new IllegalArgumentException("No enum constant " + name);
    }
  }
}
