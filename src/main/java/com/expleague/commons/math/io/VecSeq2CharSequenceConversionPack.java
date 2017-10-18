package com.expleague.commons.math.io;

import com.expleague.commons.func.types.ConversionPack;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.VecSeq;
import com.expleague.commons.func.types.TypeConverter;
import com.expleague.commons.math.vectors.impl.vectors.SparseVec;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by vkokarev on 22.07.14.
 */
public class VecSeq2CharSequenceConversionPack implements ConversionPack<VecSeq, CharSequence> {
  private static final String TYPE_VALUE_DELIMETER = "?";
  private static final String VEC_DELIMETER = "|";

  public static class VecSeq2CharSequenceConverter implements TypeConverter<VecSeq, CharSequence> {
    private static final SparseVec2CharSequenceConversionPack.SparseVec2CharSequenceConverter sparseVec2StrConverter = new SparseVec2CharSequenceConversionPack.SparseVec2CharSequenceConverter();
    private static final Vec2CharSequenceConverter vec2StrConverter = new Vec2CharSequenceConverter();


    /**
     * NO RLE SUPPORT
     * @param vecSeq
     * @return
     */
    @Override
    public CharSequence convert(final VecSeq vecSeq) {
      final StringBuilder sb = new StringBuilder();
      boolean isSimilar = false;
      int offset = 0;
      VecType tag = vecSeq.at(0) instanceof SparseVec ? VecType.SPARSE : VecType.COMMON;
      final int len = vecSeq.at(0).dim();
      for (int i = 0; i < vecSeq.length(); ++i) {
        if (vecSeq.at(i).dim() != len
            || (tag.equals(VecType.SPARSE) && !(vecSeq.at(i) instanceof SparseVec))) {
          isSimilar = false;
          break;
        }
      }

      sb.append(isSimilar)
              .append(VEC_DELIMETER)
              .append(false)
              .append(VEC_DELIMETER);
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
            serializedVec = vec2StrConverter.convertTo(vec);
            break;
          case SPARSE:
            serializedVec = sparseVec2StrConverter.convert((SparseVec) vec);
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
    private static final ArrayVec2CharSequenceConversionPack.ArrayCharSequence2VecConverter str2VecConverter = new ArrayVec2CharSequenceConversionPack.ArrayCharSequence2VecConverter();

    @Override
    public VecSeq convert(final CharSequence from) {
      final StringTokenizer tokenizer = new StringTokenizer(from.toString(), VEC_DELIMETER);
      final boolean isSimilar = Boolean.valueOf(tokenizer.nextToken());
      final boolean isRLE = Boolean.valueOf(tokenizer.nextToken());
      VecType type = null;
      CharSequence len = "";
      if (isSimilar) {
        type = VecType.getByName(tokenizer.nextToken());
        len = tokenizer.nextToken();
      }
      final List<Vec> vecs = new ArrayList<>(tokenizer.countTokens());

      while (tokenizer.hasMoreTokens()) {
        final String token = tokenizer.nextToken();
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
