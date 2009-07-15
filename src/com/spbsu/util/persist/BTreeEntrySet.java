package com.spbsu.util.persist;

import com.spbsu.util.Logger;
import com.spbsu.util.persist.pages.BTreePage;
import com.spbsu.util.persist.pages.CompositeDataPage;
import com.spbsu.util.persist.pages.DataPage;
import com.spbsu.util.persist.pages.GranulatedDataPage;

import java.nio.ByteBuffer;
import java.util.*;

/**
* User: igorkuralenok
* Date: 17.04.2008
*/
class BTreeEntrySet<K, V> implements Set<Map.Entry<K, V>> {
  private static Logger LOG = Logger.create(BTreeEntrySet.class);
  private BTreeMap<K, V> owner;

  public BTreeEntrySet(BTreeMap<K, V> owner) {
    this.owner = owner;
  }

  public int size() {
    return owner.size();
  }

  public boolean isEmpty() {
    return owner.isEmpty();
  }

  public boolean contains(Object o) {
    throw new UnsupportedOperationException();
  }

  public Iterator<Map.Entry<K, V>> iterator() {
    return new Iterator<Map.Entry<K, V>>() {
      BTreePage currentPage = owner.getPage(0);
      int currentInPage = 0;
      K currentKey = null;
      V currentValue = null;

      public boolean hasNext() {
        if(currentKey != null) return true;
        moveToNext();
        return currentKey != null;
      }

      public Map.Entry<K, V> next() {
        try{
          if(currentKey == null){
            moveToNext();
            if(currentKey == null) throw new NoSuchElementException();
          }
          final K currentKey = this.currentKey;
          final V currentValue = this.currentValue;
          return new Map.Entry<K, V>() {
            public K getKey() {
              return currentKey;
            }
            public V getValue() {
              return currentValue;
            }
            public V setValue(V v) {
              throw new UnsupportedOperationException();
            }
          };
        }
        finally{
          currentKey = null;
          currentValue = null;
        }
      }

      private void moveToNext(){
        if(currentPage == null) return;
        if(currentPage instanceof DataPage){
          final DataPage dataPage = (DataPage) currentPage;
          final long address = (long)currentInPage << 24 | currentPage.id();
          final ByteBuffer buffer = dataPage.readData(address);
          if(buffer != null){
            currentKey = owner.keyConverter.convertTo(buffer);
            currentValue = owner.valueConverter.convertTo(buffer);
            currentInPage ++;
            return;
          }
          moveForward();
          currentInPage = 0;
        }
        while(currentPage != null && !(currentPage instanceof GranulatedDataPage) && !(currentPage instanceof CompositeDataPage))
          moveForward();
        moveToNext();
      }

      public void moveForward(){
        final int oldId = currentPage.id();
        currentPage = owner.getPage(oldId + 1);
        int index = 0;
        while(currentPage != null && oldId > 0 && oldId != currentPage.id() - ++index){
          currentPage = owner.getPage(oldId + index + 1);
          LOG.warning("Empty page found!");
        }

      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  public <T> T[] toArray(T[] ts) {
    throw new UnsupportedOperationException();
  }

  public boolean add(Map.Entry<K, V> kvEntry) {
    throw new UnsupportedOperationException();
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(Collection<? extends Map.Entry<K, V>> entries) {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  public void clear() {
    throw new UnsupportedOperationException();
  }
}
