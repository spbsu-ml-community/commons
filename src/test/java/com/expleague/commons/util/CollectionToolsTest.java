package com.expleague.commons.util;

import com.expleague.commons.JUnitIOCapture;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.util.logging.Interval;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertTrue;

/**
 * @author vp
 */
public class CollectionToolsTest extends JUnitIOCapture {
  @Test
  public void testFastIterator() throws Exception {
    final ArrayList<Object> list = new ArrayList<Object>();
    final int count = 5 * 1000;
    for (int i = 0; i < count; i++) {
      final Object o = new Object();
      for (int j = 0; j < 1000; j++) {
        list.add(o);
      }
    }
    final Collection<Object> collection = Collections.unmodifiableCollection(list);
    final ArrayList<Object> iterable = new ArrayList<Object>(collection);
    final List<Object> linked = new LinkedList<Object>(collection);

    final Iterator<Object> it = iterable.iterator();
    Interval.start();
    while (it.hasNext()) {
      it.next();
    }
    Interval.stopAndPrint();

    final Iterator<Object> fast = CollectionTools.fastIterator(iterable);

    Interval.start();
    while (fast.hasNext()) {
      fast.next();
    }
    Interval.stopAndPrint();

    final Iterator<Object> linkedIt = linked.iterator();
    Interval.start();
    while (linkedIt.hasNext()) {
      linkedIt.next();
    }
    Interval.stopAndPrint();

    final Iterator<Object> fastLinked = CollectionTools.fastIterator(linked);

    Interval.start();
    while (fastLinked.hasNext()) {
      fastLinked.next();
    }
    Interval.stopAndPrint();
  }

  @Test
  public void testParallelSort() {
    final FastRandom rng = new FastRandom(0);
    final double[] array = new double[100000];
    for (int i = 0; i < array.length; i++) {
      array[i] = rng.nextDouble();
    }
    ArrayTools.parallelSort(array, ArrayTools.sequence(0, array.length));
    for (int i = 1; i < array.length; i++) {
      assertTrue(array[i] >= array[i - 1]);
    }
  }
}
