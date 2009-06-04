package com.spbsu.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 19.09.2006
 * Time: 18:59:41
 * To change this template use File | Settings | File Templates.
 */
public class LowCapacitySet<T> implements Set<T> {
  Set<T> currentContainer = EmptySet.emptySet();

  public int size() {
    return currentContainer.size();
  }

  public boolean isEmpty() {
    return currentContainer.isEmpty();
  }

  public boolean contains(final Object o) {
    return currentContainer.contains(o);
  }

  public Iterator<T> iterator() {
    return currentContainer.iterator();
  }

  public Object[] toArray() {
    return currentContainer.toArray();
  }

  public <T> T[] toArray(final T[] a) {
    return currentContainer.toArray(a);
  }

  public boolean add(final T t) {
    final int size = currentContainer.size();
    if(currentContainer instanceof HashSet) return currentContainer.add(t);
    else if(size == 1){
      if(t.equals(currentContainer.iterator().next())) return false;
      final Set<T> old = currentContainer;
      currentContainer = new HashSet<T>(old);
      return currentContainer.add(t);
    }
    else {
      // empty
      currentContainer = SingletonSet.singleton(t);
      return true;
    }
  }

  public boolean remove(final Object o) {
    final int size = currentContainer.size();
    if(size > 1) return currentContainer.remove(o);
    else //noinspection SuspiciousMethodCalls
      if(size == 1 && currentContainer.contains(o)){
      currentContainer = EmptySet.emptySet();
      return true;
    }
    return false;
  }

  public boolean containsAll(final Collection<?> c) {
    return currentContainer.containsAll(c);
  }

  public boolean addAll(final Collection<? extends T> c) {
    if (currentContainer instanceof HashSet)
      return currentContainer.addAll(c);
    if(c.size() > 1){
      currentContainer = new HashSet<T>(currentContainer);
      return currentContainer.addAll(c);
    }
    else {
      for (final T t : c) {
        add(t);
      }
      return true;
    }
  }

  public boolean retainAll(final Collection<?> c) {
    if(currentContainer instanceof HashSet)
      return currentContainer.retainAll(c);
    else if(currentContainer.size() == 1){
      if(c.contains(currentContainer.iterator().next())) return false;
      currentContainer = EmptySet.emptySet();
      return true;
    }
    return false;
  }

  public boolean removeAll(final Collection<?> c) {
    if(currentContainer instanceof HashSet)
      return currentContainer.removeAll(c);
    else if(currentContainer.size() == 1){
      if(!c.contains(currentContainer.iterator().next())) return false;
      currentContainer = EmptySet.emptySet();
      return true;
    }
    return false;
  }

  public void clear() {
    currentContainer = EmptySet.emptySet();
  }
}
