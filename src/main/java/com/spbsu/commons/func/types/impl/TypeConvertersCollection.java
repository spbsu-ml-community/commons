package com.spbsu.commons.func.types.impl;

import com.spbsu.commons.filters.AndFilter;
import com.spbsu.commons.filters.Filter;
import com.spbsu.commons.func.Converter;
import com.spbsu.commons.func.Factory;
import com.spbsu.commons.func.types.ConversionDependant;
import com.spbsu.commons.func.types.ConversionPack;
import com.spbsu.commons.func.types.ConversionRepository;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.system.RuntimeUtils;
import com.spbsu.commons.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: solar
 * Date: 24.06.13
 * Time: 12:15
 */
public class TypeConvertersCollection implements ConversionRepository {
  private static Log LOG = LogFactory.getLog(TypeConvertersCollection.class);
  private ConversionRepository base;
  private Filter<TypeConverter> customize;
  private Map<Pair<Class, Class>, Factory<TypeConverter>> factories = new HashMap<Pair<Class, Class>, Factory<TypeConverter>>();
  private Map<Pair<Class, Class>, TypeConverter> instances = new HashMap<Pair<Class, Class>, TypeConverter>();
  private Map<Pair<Class, Class>, TypeConverter> cache = new HashMap<Pair<Class, Class>, TypeConverter>();

  public TypeConvertersCollection(final Object... converters) {
    this(null, converters);
  }
  /**
   * Accepts following variants of converters setup:
   * java.lang.String -- name of package
   */

  public TypeConvertersCollection(ConversionRepository base, final Object... converters) {
    this.base = base;
    this.customize = base instanceof TypeConvertersCollection ? ((TypeConvertersCollection)base).customize : null;
    for (Object convId : converters) {
      try {
        if (convId instanceof String) {
          String pack = (String) convId;
          String[] resources = RuntimeUtils.packageResourcesList(pack);
          Set<Class> registered = new HashSet<Class>();
          for (String resource : resources) {
            if (resource.endsWith(".class")) {
              final Class<?> converterClass = Class.forName(resource.substring(0, resource.length() - ".class".length()).replace('/', '.'));
              if (!registered.contains(converterClass) && converterClass.getEnclosingClass() == null) // only top level classes
                register(converterClass);
              registered.add(converterClass);
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

  private TypeConvertersCollection(ConversionRepository base, Map<Pair<Class, Class>, Factory<TypeConverter>> factories, Filter<TypeConverter> filter){
    this.base = base;
    this.factories = factories;
    this.customize = filter;

    createInstances(factories, filter);
  }

  private void createInstances(Map<Pair<Class, Class>, Factory<TypeConverter>> factories, Filter<TypeConverter> filter) {
    for (Map.Entry<Pair<Class, Class>, Factory<TypeConverter>> entry : factories.entrySet()) {
      if (entry.getValue() == null)
        continue;
      TypeConverter converter = entry.getValue().create();
      if (converter instanceof ConversionDependant)
        ((ConversionDependant) converter).setConversionRepository(this);
      if (filter == null || filter.accept(converter))
        instances.put(entry.getKey(), converter);
    }
  }

  @Override
  public <F, T> T convert(final F instance, final Class<T> destClass) {
    return ((TypeConverter<F,T>)converter(instance.getClass(), destClass)).convert(instance);
  }

  public synchronized <U,V> TypeConverter<U,V> converter(Class<U> from, Class<V> to) {
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
      Pair<Class<?>, Class<?>> bestMatch = null;
      for (Pair p : instances.keySet()) {
        Pair<Class<?>, Class<?>> candidate = (Pair<Class<?>, Class<?>>)p;
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
      if (converter == null)
        throw new RuntimeException("Unable to find proper converter from " + from + " to " + to);
    }
    cache.put(key, converter);
    return converter;
  }

  @Override
  public <F, T> Class<? super F> conversionType(final Class<F> fromC, final Class<T> toC) {
    final TypeConverter<F, T> converter = converter(fromC, toC);
    final Class[] parameters = RuntimeUtils.findTypeParameters(converter.getClass(), TypeConverter.class);
    return ((Class<? super F>)(parameters[0] != null ? parameters[0] : fromC));
  }


  @Override
  public ConversionRepository customize(final Filter<TypeConverter> todo) {
    return new TypeConvertersCollection(base != null ? base.customize(todo) : null, factories, customize != null ? new AndFilter<TypeConverter>(customize, todo) : todo);
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
      if (params.length != 2 || params[0] == null || params[1] == null)
        return false;
      return registerInner(Pair.create(params[0], params[1]), new Factory<TypeConverter>() {
               @Override
               public TypeConverter create() {
                 try {
                   final Converter converter = (Converter) converterClass.newInstance();
                   return new TypeConverter() {
                     @Override
                     public Object convert(final Object from) {
                       return converter.convertTo(from);
                     }
                   };
                 } catch (InstantiationException e) {
                   LOG.warn("Unable to create converter ", e);
                   return null;
                 } catch (IllegalAccessException e) {
                   throw new RuntimeException("Should never happen!", e);
                 }
               }
             }) &&
             registerInner(Pair.create(params[1], params[0]), new Factory<TypeConverter>() {
               @Override
               public TypeConverter create() {
                 try {
                   final Converter converter = (Converter) converterClass.newInstance();
                   return new TypeConverter() {
                     @Override
                     public Object convert(final Object from) {
                       return converter.convertFrom(from);
                     }
                   };
                 } catch (InstantiationException e) {
                   LOG.warn("Unable to create converter ", e);
                   return null;
                 } catch (IllegalAccessException e) {
                   throw new RuntimeException("Should never happen!", e);
                 }
               }
             });
    }
    if (ConversionPack.class.isAssignableFrom(converterClass)) {
      try {
        final ConversionPack instance =(ConversionPack) converterClass.newInstance();
        register(instance.to());
        register(instance.from());
      } catch (InstantiationException | IllegalAccessException e) {
        LOG.warn("Unable to init conversion pack", e);
      }
    }

    return false;
  }

  private boolean registerInner(Pair<Class,Class> key, Factory<TypeConverter> converter) {
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
}
