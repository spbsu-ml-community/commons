package com.expleague.commons.cache;

import com.expleague.commons.JUnitIOCapture;
import com.expleague.commons.util.Holder;
import com.expleague.commons.util.cache.CacheStrategy;
import com.expleague.commons.util.cache.impl.FixedSizeCache;
import com.expleague.commons.util.cache.impl.LRUStrategy;
import com.expleague.commons.util.logging.Interval;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class CacheTest extends JUnitIOCapture {
  public static final String KEY1 = "key1";
  public static final String KEY2 = "key2";
  public static final String KEY3 = "key3";
  public static final String KEY4 = "key4";

  @Test
  public void testLRU1() {
    final LRUStrategy strategy = new LRUStrategy(3);
    assertEquals(0, strategy.getStorePosition());
    strategy.registerAccess(0);
    assertEquals(1, strategy.getStorePosition());
    strategy.registerAccess(1);
    assertEquals(2, strategy.getStorePosition());
    strategy.registerAccess(2);
    assertEquals(0, strategy.getStorePosition());
  }

  @Test
  public void testLRU2() {
    final LRUStrategy strategy = new LRUStrategy(3);
    assertEquals(0, strategy.getStorePosition());
    strategy.registerAccess(0);
    assertEquals(1, strategy.getStorePosition());
    strategy.registerAccess(1);
    strategy.registerAccess(0);
    assertEquals(2, strategy.getStorePosition());
    strategy.registerAccess(2);
    assertEquals(1, strategy.getStorePosition());
  }

  @Test
  public void testLRU3() {
    final LRUStrategy strategy = new LRUStrategy(3);
    assertEquals(0, strategy.getStorePosition());
    strategy.registerAccess(0);
    assertEquals(1, strategy.getStorePosition());
    strategy.registerAccess(1);
    assertEquals(2, strategy.getStorePosition());
    strategy.registerAccess(2);
    strategy.registerAccess(0);
    assertEquals(1, strategy.getStorePosition());
  }

  @Test
  public void testLRU4() {
    final LRUStrategy strategy = new LRUStrategy(3);
    assertEquals(0, strategy.getStorePosition());
    strategy.registerAccess(0);
    assertEquals(1, strategy.getStorePosition());
    strategy.registerAccess(1);
    assertEquals(2, strategy.getStorePosition());
    strategy.registerAccess(2);
    strategy.registerAccess(0);
    strategy.removePosition(0);
    assertEquals(0, strategy.getStorePosition());
  }

  @Test
  public void testLRU5() {
    final LRUStrategy strategy = new LRUStrategy(100);
    final Random position = new Random();
    for (int i = 0; i < 10000; i++) {
      strategy.registerAccess(Math.abs(position.nextInt()) % 100);
    }
    final Set<Integer> known = new HashSet<Integer>();
    for (int i = 0; i < 100; i++) {
      final int storePosition = strategy.getStorePosition();
      known.add(storePosition);
      strategy.registerAccess(storePosition);
    }
    assertEquals(100, known.size());
  }

  @Test
  public void testLRU6() {
    final LRUStrategy strategy = new LRUStrategy(100);
    final Random position = new Random();
    for (int i = 0; i < 10000; i++) {
      strategy.registerAccess(Math.abs(position.nextInt()) % 100);
      strategy.removePosition(Math.abs(position.nextInt()) % 100);
    }
    final Set<Integer> known = new HashSet<Integer>();
    for (int i = 0; i < 100; i++) {
      final int storePosition = strategy.getStorePosition();
      known.add(storePosition);
      strategy.registerAccess(storePosition);
    }
    assertEquals(100, known.size());
  }

  @Test
  public void testCache1() {
    final FixedSizeCache<String, Object> cache = new FixedSizeCache<String, Object>(3, CacheStrategy.Type.LRU);
    assertNull(cache.get(KEY1));
    cache.put(KEY1, new Object());
    cache.put(KEY2, new Object());
    cache.put(KEY3, new Object());
    cache.put(KEY4, new Object());
    System.gc();
    assertNull(cache.get(KEY1));
    assertNotNull(cache.get(KEY2));
    assertNotNull(cache.get(KEY3));
    assertNotNull(cache.get(KEY4));
  }

  @Test
  public void testCache2() {
    final FixedSizeCache<String, Object> cache = new FixedSizeCache<String, Object>(3, CacheStrategy.Type.LRU);
    cache.put(KEY1, new Object());
    cache.put(KEY2, new Object());
    cache.put(KEY3, new Object());
    cache.clear(KEY2);
    cache.put(KEY4, new Object());
    System.gc();
    assertNotNull(cache.get(KEY1));
    assertNull(cache.get(KEY2));
    assertNotNull(cache.get(KEY3));
    assertNotNull(cache.get(KEY4));
  }

  @Test
  public void testCache3() {
    final FixedSizeCache<String, String> cache = new FixedSizeCache<String, String>(3, CacheStrategy.Type.LRU);
    final Function<String, String> computable = argument -> argument;
    assertNotNull(cache.get(KEY1, computable));
    assertNotNull(cache.get(KEY1));
    cache.put(KEY1, "qqq");
    assertEquals("qqq", cache.get(KEY1));
    assertEquals("qqq", cache.get(KEY1, computable));
    assertNotNull(cache.get(KEY2, computable));
    assertNotNull(cache.get(KEY1, computable));
  }

  @Test
  public void testDoublePut() {
    final FixedSizeCache<String, Object> cache = new FixedSizeCache<String, Object>(1, CacheStrategy.Type.LRU);
    cache.put(KEY1, new Object());
    cache.put(KEY1, new Object());
    cache.put(KEY1, new Object());
    cache.put(KEY1, new Object());
    cache.put(KEY1, new Object());
    cache.put(KEY1, new Object());
    assertTrue(cache.checkEqualSizes());
  }

  @Test
  public void testStress1() {
    final FixedSizeCache<Integer, Object> cache = new FixedSizeCache<Integer, Object>(1000, CacheStrategy.Type.LRU);
    int count = 100000;
    for (int i = 0; i < 10000; i++) {
      cache.put(i, new Object());
    }
    System.gc();
    Interval.start();
    while (count-- > 0) {
      final int key = (int) (Math.random() * 10000);
      if (cache.get(key) == null) cache.put(key, new Object());
      if (count % 10000 == 0) System.gc();
    }
    System.out.println("Successful accesses: " + cache.getStrategy().getAccessCount());
    System.out.println("Missed: " + cache.getStrategy().getCacheMisses());
    Interval.stopAndPrint();
  }

  @Test
  public void testStressPut() throws Exception {
    final FixedSizeCache<Integer, Object> cache = new FixedSizeCache<Integer, Object>(1000, CacheStrategy.Type.LRU);
    final int count = 1 * 1000 * 1000;
    Interval.start();
    for (int i = 0; i < count; i++) {
      cache.put(i, "i");
    }
    Interval.stopAndPrint();
  }

  @Test
  public void testPerformanceUniform() {
    final FixedSizeCache<Integer, Integer> cache = new FixedSizeCache<Integer, Integer>(1000, CacheStrategy.Type.LRU);
    final long time = System.currentTimeMillis();
    final Holder<Integer> missCount = new Holder<Integer>(0);
    for (int i = 0; i < 1000000; i++) {
      final int key = (int) (Math.random() * 10000);
      cache.get(key, argument -> {
        missCount.setValue(missCount.getValue() + 1);
        return 1;
      });
    }
    System.out.println("Total 1000000 tries, " + missCount + " misses");
    System.out.println("Time: " + (System.currentTimeMillis() - time));
  }

  @Test
  public void testPerformanceNormal() {
    final FixedSizeCache<Integer, Integer> cache = new FixedSizeCache<Integer, Integer>(1000, CacheStrategy.Type.LRU);
    final long time = System.currentTimeMillis();
    final Random rnd = new Random();
    final Holder<Integer> missCount = new Holder<Integer>(0);
    int maxKey = 0;

    for (int i = 0; i < 1000000; i++) {
      final double r = rnd.nextGaussian();
      final int key = (int) (r * r * 10000);
      maxKey = Math.max(maxKey, key);
      cache.get(key, argument -> {
        missCount.setValue(missCount.getValue() + 1);
        return 1;
      });
    }
    System.out.println("Total 1000000 tries, " + missCount + " misses. Maximal key: " + maxKey);
    System.out.println("Time: " + (System.currentTimeMillis() - time));
  }

  @Test
  public void testDoublePutAndGC() {
    final FixedSizeCache<String, Object> cache = new FixedSizeCache<>(1, CacheStrategy.Type.LRU);
    cache.put(KEY1, new Object());
    cache.put(KEY1, new Object());
    System.gc();
    assertNotNull(cache.get(KEY1));
  }
}
