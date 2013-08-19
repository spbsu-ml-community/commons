package com.spbsu.commons.func.types;


import com.spbsu.commons.filters.ClassFilter;
import com.spbsu.commons.filters.Filter;
import com.spbsu.commons.func.types.impl.TypeConvertersCollection;

/**
 * User: solar
 * Date: 21.06.13
 * Time: 13:54
 */
public class SerializationRepository<T> {
  public final ConversionRepository base;
  private final ConversionRepository baseShattered;
  private final Class<T> destination;

  public SerializationRepository(final ConversionRepository base, Class<T> destination) {
    this.base = base;
    this.baseShattered = base.customize(new ClassFilter<TypeConverter>(ShatteredTypeConverter.class));
    this.destination = destination;
  }

  public SerializationRepository(SerializationRepository<T> base, final Object... converters) {
    this.base = new TypeConvertersCollection(base.base, converters);
    this.baseShattered = this.base.customize(new ClassFilter<TypeConverter>(ShatteredTypeConverter.class));
    this.destination = base.destination;
  }

  public <F> T write(F instance) {
    return base.convert(instance, destination);
  }

  public <F> F read(T from, Class<F> type) {
    return base.convert(from, type);
  }

  public <F> T createFrom(F from) {
    return ((ShatteredTypeConverter<F,T>)baseShattered.converter(from.getClass(), destination)).createFrom(from);
  }
  public <F> void writeTo(F from, T to) {
    ((ShatteredTypeConverter<F,T>)baseShattered.converter(from.getClass(), destination)).writeTo(from, to);
  }

  public SerializationRepository<T> customize(Filter<TypeConverter> todo) {
    return new SerializationRepository<T>(base.customize(todo), destination);
  }
}
