package com.expleague.commons.func.types;

import com.expleague.commons.filters.Filter;
import com.expleague.commons.func.types.impl.TypeConvertersCollection;

/**
 * User: solar
 * Date: 21.06.13
 * Time: 13:54
 */
public interface ConversionRepository {
  <F,T> T convert(F instance, Class<T> destClass);
  <F,T> TypeConverter<F,T> converter(Class<F> fromC, Class<T> toC);
  <F,T> Class<? super F> conversionType(Class<F> fromC, Class<T> toC);
  /**
   * Filter provided can filter irrelevant converters, or customize them. Localization can be example of such a customization
   * @return new ConversionRepository
   */
  ConversionRepository customize(Filter<TypeConverter> todo);

  ConversionRepository ROOT = new TypeConvertersCollection("com.expleague.commons.func.converters");
}
