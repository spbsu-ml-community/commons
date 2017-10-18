package com.expleague.commons.func.converters;


import com.expleague.commons.func.types.ConversionPack;
import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.func.types.TypeConverter;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.util.Pair;
import com.expleague.commons.func.types.ConversionDependant;

public class Pair2CharSequenceConversionPack implements ConversionPack<Pair, CharSequence> {
  public static class To implements TypeConverter<Pair, CharSequence>, ConversionDependant {
    private ConversionRepository owner;

    @Override
    public void setConversionRepository(final ConversionRepository repository) {
      this.owner = repository;
    }

    @Override
    public CharSequence convert(final Pair from) {
      final Class convType1 = owner.conversionType(from.first.getClass(), CharSequence.class);
      final Class convType2 = owner.conversionType(from.second.getClass(), CharSequence.class);
      final CharSequence firstConversion = owner.convert(from.first, CharSequence.class);
      return CharSeqTools.concat("(",
          Integer.toString(firstConversion.length()), ",",
          convType1.getName(), ",",
          convType2.getName(), ",",
          firstConversion, ",",
          owner.convert(from.second, CharSequence.class),
          ")");
    }
  }

  public static class From implements TypeConverter<CharSequence, Pair>, ConversionDependant {
    private ConversionRepository owner;

    @Override
    public void setConversionRepository(final ConversionRepository repository) {
      this.owner = repository;
    }

    @Override
    public final Pair convert(final CharSequence seq) {
      final CharSequence[] split = CharSeqTools.split(seq.subSequence(1, seq.length() - 1), ',', new CharSequence[4]);
      final int firstLength = CharSeqTools.parseInt(split[0]);
      final CharSequence first = split[3].subSequence(0, firstLength);
      final CharSequence second = split[3].subSequence(firstLength + 1, split[3].length());

      try {
        return Pair.create(owner.convert(first, Class.forName(split[1].toString())), owner.convert(second, Class.forName(split[2].toString())));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public Class<? extends TypeConverter<CharSequence, Pair>> from() {
    return From.class;
  }

  @Override
  public Class<? extends TypeConverter<Pair, CharSequence>> to() {
    return To.class;
  }
}
