package com.expleague.commons.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * User: alms
 * Date: 13.01.2009
 */
public class CollectionTools {

  public static <T, U, S extends Collection<U>> S map(final Function<T, U> converter, final Collection<T> inital, final S accum) {
    for (final T el : inital) {
      final U value = converter.apply(el);
      accum.add(value);
    }
    return accum;
  }

  public static <T, U> List<U> map(final Function<T, U> converter, final Collection<T> inital) {
    final List<U> accum = new ArrayList<>(inital.size());

    return map(converter, inital, accum);
  }

  public static <F, S> List<F> mapFirst(final Collection<Pair<F, S>> source) {
    return map(Pair::getFirst, source);
  }

  public static <F, S> List<S> mapSecond(final Collection<Pair<F, S>> inital) {
    return map(Pair::getSecond, inital);
  }

  public static <K, V, U> Map<K, U> transformValues(final Function<V, U> converter, final Map<K, V> inital) {
    final HashMap<K, U> result = new HashMap<K, U>(inital.size());
    for (final Map.Entry<K, V> entry : inital.entrySet()) {
      result.put(entry.getKey(), converter.apply(entry.getValue()));
    }
    return result;
  }

  public static <T, U> List<Pair<T, U>> merge(final List<T> first, final List<U> second) {
    final List<Pair<T, U>> result = new ArrayList<Pair<T, U>>(first.size());
    final Iterator<T> itR = first.iterator();
    final Iterator<U> itU = second.iterator();

    while (itR.hasNext() && itU.hasNext()) {
      result.add(Pair.create(itR.next(), itU.next()));
    }

    return result;
  }


  public static <T, U extends Collection<T>> U filter(final Collection<? extends T> col, final U accum, final Predicate<? super T> filter) {
    for (final T el : col) {
      if (filter.test(el)) {
        accum.add(el);
      }
    }

    return accum;
  }

  public static <T, U extends Collection<T>> int count(final U col, final Predicate<T> filter) {
    int num = 0;
    for (final T el : col) {
      if (filter.test(el)) {
        num++;
      }
    }
    return num;
  }

  public static <T> Set<T> merge(final Set<T>... sets) {
    int size = 0;
    for (final Set<T> set : sets) {
      size += set.size();
    }

    final Set<T> result = new HashSet<T>(size);
    for (final Set<T> set : sets) {
      result.addAll(set);
    }

    return result;
  }

  public static <T, U> Map<U, Integer> computeObjectCounts(final Collection<T> col, final Function<T, U> conv) {
    final Map<U, Integer> result = new HashMap<>();
    for (final T el : col) {
      final U convEl = conv.apply(el);
      Integer n = result.get(convEl);
      if (n == null) {
        n = 0;
      }
      result.put(convEl, n + 1);
    }

    return result;
  }

  /**
   * filter iterators collection (remove some elements from original collection)
   * @param iterator
   * @param filter
   * @param <T>
   */
  public static <T> void filterWithRemove(final Iterator<T> iterator, final Predicate<T> filter) {
    while (iterator.hasNext()) {
      if (!filter.test(iterator.next())) {
        iterator.remove();
      }
    }
  }

  public static <T> Iterator<T> filter(final Iterator<T> iterator, final Predicate<T> filter) {
    return new Iterator<T>() {
      T nextObject;

      @Override
      public boolean hasNext() {
        if (nextObject != null) {
          return true;
        }
        while (iterator.hasNext()) {
          final T obj = iterator.next();
          if (filter.test(obj)) {
            nextObject = obj;
            return true;
          }
        }
        return false;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        } else {
          final T obj = nextObject;
          nextObject = null;
          return obj;
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("not yet");
      }
    };
  }

  public static <T> Iterator<T> fastIterator(final List<T> list) {
    if (list instanceof ArrayList) {
      return new Iterator<T>() {
        private final int size = list.size();
        private int current = 0;

        @Override
        public boolean hasNext() {
          return current < size;
        }

        @Override
        public T next() {
          return list.get(current++);
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    else return list.iterator();
  }
}