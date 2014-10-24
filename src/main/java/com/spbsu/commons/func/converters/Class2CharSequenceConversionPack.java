package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.types.ConversionDependant;
import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.ConversionRepository;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.util.Pair;

public class Class2CharSequenceConversionPack implements ConversionPack<Class, CharSequence> {
  public static class To implements TypeConverter<Class, CharSequence>{
    @Override
    public CharSequence convert(final Class from) {
      return from.getName();
    }
  }

  public static class From implements TypeConverter<CharSequence, Class> {
    @Override
    public final Class convert(CharSequence seq) {
      try {
        return Class.forName(seq.toString());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public Class<? extends TypeConverter<CharSequence, Class>> from() {
    return From.class;
  }

  @Override
  public Class<? extends TypeConverter<Class, CharSequence>> to() {
    return To.class;
  }
}
