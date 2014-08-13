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
      boolean isSimilar = false;
      int offset = 0;
      VecType tag = vecSeq.at(0) instanceof SparseVec ? VecType.SPARSE : VecType.COMMON;
      final int len = vecSeq.at(0).dim();
      for (int i = 0; i < vecSeq.length(); ++i) {
          if (vecSeq.at(i).dim() != len
              || (tag.equals(VecType.SPARSE) && ! (vecSeq.at(i) instanceof SparseVec))) {
            isSimilar = false;
            break;
          }
      }
      sb.append(isSimilar).append(VEC_DELIMETER);
      if (isSimilar) {
        sb.append(tag.getName())
            .append(VEC_DELIMETER)
            .append(len)
            .append(VEC_DELIMETER);

        offset = String.valueOf(len).length();
      }
      for (int i = 0; i < vecSeq.length(); ++i) {
        final Vec vec = vecSeq.at(i);
        CharSequence serializedVec;
        if (!isSimilar) {
          tag = vec instanceof SparseVec ? VecType.SPARSE : VecType.COMMON;
        }
        switch (tag) {
          case COMMON:
            serializedVec = vec2StrConverter.convert(vec);
            break;
          case SPARSE:
            serializedVec = sparseVec2StrConverter.convert((SparseVec)vec);
            break;
          default:
            throw new IllegalArgumentException("unexpected tag");
        }
        if (i != 0)
          sb.append(VEC_DELIMETER);

        if (isSimilar)
          serializedVec = serializedVec.subSequence(offset, serializedVec.length());
        else {
          sb.append(tag.getName())
              .append(TYPE_VALUE_DELIMETER);
        }
        sb.append(serializedVec);
      }
      return sb;
    }
  }

  public static class CharSequence2VecSeqConverter implements TypeConverter<CharSequence, VecSeq> {
    private static final SparseVec2CharSequenceConversionPack.CharSequence2SparseVecConverter sparseStr2VecConverter = new SparseVec2CharSequenceConversionPack.CharSequence2SparseVecConverter();
    private static final Vec2CharSequenceConversionPack.CharSequence2VecConverter str2VecConverter = new Vec2CharSequenceConversionPack.CharSequence2VecConverter();

    @Override
    public VecSeq convert(final CharSequence from) {
      final CharSequence[] parts = CharSeqTools.split(from, VEC_DELIMETER);
      final boolean isSimilar = Boolean.parseBoolean(parts[0].toString());
      VecType type = null;
      CharSequence len = null;
      int offset = 1;
      if (isSimilar) {
        type = VecType.getByName(String.valueOf(parts[1]));
        len = parts[2].toString();
        offset += 2;
      }
      final List<Vec> vecs = new ArrayList<>(parts.length - offset);

      for (int i = offset; i < parts.length; ++i) {
        final CharSequence serializedVec = parts[i];
        final CharSequence[] vecParts;
        final CharSequence vecStr;
        final Vec vec;
        if (serializedVec.length() > 0) {
          if (!isSimilar) {
            vecParts = CharSeqTools.split(serializedVec, TYPE_VALUE_DELIMETER);
            type = VecType.getByName(String.valueOf(parts[0]));
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
        } else {
          vec = vecs.get(vecs.size() - 1);
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
