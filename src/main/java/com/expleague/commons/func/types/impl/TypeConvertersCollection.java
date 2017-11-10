package com.expleague.commons.func.types.impl;

import com.expleague.commons.func.Converter;
import com.expleague.commons.func.Factory;
import com.expleague.commons.func.types.ConversionPack;
import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.system.RuntimeUtils;
import org.jetbrains.annotations.NotNull;


import com.expleague.commons.func.types.ConversionDependant;
import com.expleague.commons.func.types.TypeConverter;
import com.expleague.commons.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * User: solar
 * Date: 24.06.13
 * Time: 12:15
 */
@SuppressWarnings("unchecked")
public class TypeConvertersCollection implements ConversionRepository {
  private static final Log LOG = LogFactory.getLog(TypeConvertersCollection.class);
  private final ConversionRepository base;
  private final Predicate<TypeConverter> customize;
  private Map<Pair<Class, Class>, Factory<TypeConverter>> factories = new HashMap<>();
  private final Map<Pair<Class, Class>, TypeConverter> instances = new HashMap<>();
  private final Map<Pair<Class, Class>, TypeConverter> cache = new HashMap<>();

  public TypeConvertersCollection(final Object... converters) {
    this(null, converters);
  }
  /**
   * Accepts following variants of converters setup:
   * java.lang.String -- name of package
   */

  public TypeConvertersCollection(final ConversionRepository base, final Object... converters) {
    this.base = base != null ? base.customize(typeConverter -> {
      if (typeConverter instanceof ConversionDependant)
        ((ConversionDependant) typeConverter).setConversionRepository(TypeConvertersCollection.this);
      return true;
    }) : null;
    this.customize = base instanceof TypeConvertersCollection ? ((TypeConvertersCollection)base).customize : null;
    for (final Object convId : converters) {
      try {
        if (convId instanceof String) {
          final String pack = (String) convId;
          final String[] resources = RuntimeUtils.packageResourcesList(pack);
          final Set<Class> registered = new HashSet<>();
          for (final String resource : resources) {
            if (resource.endsWith(".class")) {
              final Class<?> converterClass = Class.forName(resource.substring(0, resource.length() - ".class".length()).replace('/', '.'));
              if (!registered.contains(converterClass) && converterClass.getEnclosingClass() == null) {
                // only top level classes
                if (register(converterClass)) {
                  registered.add(converterClass);
                } else {
//                  LOG.warn("Cant register converter " + converterClass);
                }
              } else {
                registered.add(converterClass);
              }
            }
          }
        }
        if (convId instanceof Class) {
          if (!register((Class)convId))
            throw new IllegalArgumentException("Unable to register class" + ((Class)convId).getName());
        }
        if (convId instanceof ConversionPack) {
          register(((ConversionPack) convId).from());
          register(((ConversionPack) convId).to());
        }
      } catch (Exception e) {
        LOG.debug("Exception during smart resource loading, skip it", e);
      }
    }

    createInstances(factories, customize);
  }

  private TypeConvertersCollection(final ConversionRepository base, final Map<Pair<Class, Class>, Factory<TypeConverter>> factories, final Predicate<TypeConverter> filter){
    this.base = base != null ? base.customize(typeConverter -> {
      if (typeConverter instanceof ConversionDependant)
        ((ConversionDependant) typeConverter).setConversionRepository(TypeConvertersCollection.this);
      return true;
    }) : null;
    this.factories = factories;
    this.customize = filter;

    createInstances(factories, filter);
  }

  private void createInstances(final Map<Pair<Class, Class>, Factory<TypeConverter>> factories, final Predicate<TypeConverter> filter) {
    for (final Map.Entry<Pair<Class, Class>, Factory<TypeConverter>> entry : factories.entrySet()) {
      if (entry.getValue() == null)
        continue;
      final TypeConverter converter = entry.getValue().create();
      if (converter instanceof ConversionDependant)
        ((ConversionDependant) converter).setConversionRepository(this);
      if (filter == null || filter.test(converter))
        instances.put(entry.getKey(), converter);
    }
  }

  @Override
  public <F, T> T convert(final F instance, final Class<T> destClass) {
    return ((TypeConverter<F,T>)converter(instance.getClass(), destClass)).convert(instance);
  }

  @NotNull
  @Override
  public synchronized <U,V> TypeConverter<U,V> converter(final Class<U> from, final Class<V> to) {
    if (to.isAssignableFrom(from))
      return new TypeConverter<U, V>() {
        @Override
        public V convert(final U from) {
          return (V)from;
        }
      };
    final Pair<Class, Class> key = Pair.create((Class)from, (Class)to);
    TypeConverter<U, V> converter = (TypeConverter<U, V>)cache.get(key);
    if (converter == null) { // trying to fall back by inheritance
      Pair<Class, Class> bestMatch = null;
      for (final Pair p : instances.keySet()) {
        final Pair<Class, Class> candidate = (Pair<Class, Class>)p;
        if (candidate.first.isAssignableFrom(key.first) && key.second.isAssignableFrom(candidate.second)) { // match!
          if (bestMatch == null
              || (bestMatch.first.isAssignableFrom(candidate.first)
                  && candidate.second.isAssignableFrom(bestMatch.second)))
            bestMatch = candidate;
        }
      }
      if (bestMatch != null)
        converter = (TypeConverter<U, V>)instances.get(bestMatch);
      else if (base != null)
        converter = base.converter(from, to);
      if (converter == null) {
        if (from.isArray() && from.getComponentType() != Object.class) {
          return (TypeConverter<U, V>)converter(Object[].class, to);
        }
        else if (to.isArray() && to.getComponentType() != Object.class) {
          return (TypeConverter<U, V>)converter(from, Object[].class);
        }
        throw new RuntimeException("Unable to find proper converter from " + from + " to " + to);
      }
    }
    cache.put(key, converter);
    return converter;
  }

  @Override
  public <F, T> Class<? super F> conversionType(final Class<F> fromC, final Class<T> toC) {
    final TypeConverter<F, T> converter = converter(fromC, toC);
    final Class[] parameters;
    if (converter instanceof MyTypeConverterFactory.MyTypeConverter) {
      final Converter realConverter = ((MyTypeConverterFactory.MyTypeConverter) converter).converter;
      parameters = RuntimeUtils.findTypeParameters(realConverter.getClass(), Converter.class);
    }
    else
      parameters = RuntimeUtils.findTypeParameters(converter.getClass(), TypeConverter.class);
    return ((Class<? super F>)(parameters[0] != null ? parameters[0] : fromC));
  }


  @Override
  public ConversionRepository customize(final Predicate<TypeConverter> todo) {
    return new TypeConvertersCollection(base != null ? base.customize(todo) : null, factories, customize != null ? customize.and(todo) : todo);
  }

  private boolean register(final Class<?> converterClass) throws NoSuchMethodException {
    try {
      converterClass.getConstructor();
    }
    catch(NoSuchMethodException e) {
      return false;
    }
    if (converterClass.isInterface() || Modifier.isAbstract(converterClass.getModifiers())) // Interface or abstract class
      return false;
    if (((converterClass.getConstructor().getModifiers() & Modifier.PUBLIC) == 0)
       || ((converterClass.getModifiers() & Modifier.PUBLIC) == 0)) // has no default public constructor or not public
      return false;
    if (converterClass.getTypeParameters().length > 0) // TODO: implement pairs, etc. for closure functionality
      return false;
    if (TypeConverter.class.isAssignableFrom(converterClass)) {
      final Class[] params = RuntimeUtils.findTypeParameters(converterClass, TypeConverter.class);
      //noinspection SimplifiableIfStatement
      if (params.length != 2 || params[0] == null || params[1] == null)
        return false;
      return registerInner(Pair.create(params[0], params[1]), new Factory<TypeConverter>() {
        @Override
        public TypeConverter create() {
          try {
            return (TypeConverter) converterClass.newInstance();
          } catch (InstantiationException e) {
            LOG.warn("Unable to create converter ", e);
            return null;
          } catch (IllegalAccessException e) {
            throw new RuntimeException("Should never happen!", e);
          }
        }
      });
    }
    if (Converter.class.isAssignableFrom(converterClass)) {
      final Class[] params = RuntimeUtils.findTypeParameters(converterClass, Converter.class);
      //noinspection SimplifiableIfStatement
      if (params.length != 2 || params[0] == null || params[1] == null)
        return false;
      return registerInner(Pair.create(params[0], params[1]), new MyTypeConverterFactory(converterClass, false)) &&
             registerInner(Pair.create(params[1], params[0]), new MyTypeConverterFactory(converterClass, true));
    }
    if (ConversionPack.class.isAssignableFrom(converterClass)) {
      try {
        final ConversionPack instance =(ConversionPack) converterClass.newInstance();
        return register(instance.to())
                && register(instance.from());
      } catch (InstantiationException | IllegalAccessException e) {
        LOG.warn("Unable to init conversion pack", e);
      }
    }

    return false;
  }

  private boolean registerInner(final Pair<Class,Class> key, final Factory<TypeConverter> converter) {
    if (factories.containsKey(key)) {
      LOG.warn("Conflict found for types" + key.first.getName() + " -> " + key.second.getName());
      factories.put(key, null);
      return false;
    }
    else {
      factories.put(key, converter);
      return true;
    }
  }

  private static class MyTypeConverterFactory implements Factory<TypeConverter> {
    private static class MyTypeConverter<From,To> implements TypeConverter<From,To>, ConversionDependant {
      private final Converter converter;
      private boolean from;

      public MyTypeConverter(Converter converter, boolean from) {
        this.converter = converter;
        this.from = from;
      }

      @SuppressWarnings("unchecked")
      @Override
      public To convert(final From from) {
        if (this.from)
          return (To)converter.convertFrom(from);
        return (To)converter.convertTo(from);
      }

      @Override
      public void setConversionRepository(ConversionRepository repository) {
        if (converter instanceof ConversionDependant)
          ((ConversionDependant) converter).setConversionRepository(repository);
      }

      @Override
      public String toString() {
        return converter.toString() + " " + (from ? "from" : "to");
      }
    }
    private final Class<?> converterClass;
    private final boolean from;

    public MyTypeConverterFactory(Class<?> converterClass, boolean from) {
      this.converterClass = converterClass;
      this.from = from;
    }

    @Override
    public TypeConverter create() {
      try {
        final Converter converter = (Converter) converterClass.newInstance();
        return new MyTypeConverter(converter, from);
      } catch (InstantiationException e) {
        LOG.warn("Unable to create converter ", e);
        return null;
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Should never happen!", e);
      }
    }
  }
}
