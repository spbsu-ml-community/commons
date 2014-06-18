package com.spbsu.commons.util;

import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

import java.util.*;

/**
 * @author vp
 */
public class CollectionToolsTest extends TestCase {
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

  static class IntegerEntry extends RBTreeNodeBase {
    int v;

    IntegerEntry(int v) {
      this.v = v;
    }

    @Override
    public int compareTo(RBTreeNode rbTreeNode) {
      return v - ((IntegerEntry)rbTreeNode).v;
    }
  }

  public void testRBTree1() throws Exception {
    RedBlackTree<IntegerEntry> rbTree = new RedBlackTree<IntegerEntry>();
    TreeSet<Integer> treeSet = new TreeSet<Integer>();
    for (int i = 0; i < 100000; i++) {
      int val = (int)(Math.random() * 10000000);
      treeSet.add(val);
      rbTree.add(new IntegerEntry(val));
    }

    for (Integer v : treeSet) {
      final IntegerEntry entry = rbTree.first();
      assertEquals(v.intValue(), entry.v);
      assertTrue(rbTree.remove(entry));
    }
  }

  public void testRBTree2() throws Exception {
    RedBlackTree<IntegerEntry> rbTree = new RedBlackTree<IntegerEntry>();
    TreeSet<Integer> treeSet = new TreeSet<Integer>();
    for (int i = 0; i < 100000; i++) {
      treeSet.add(i);
      rbTree.add(new IntegerEntry(i));
    }

    for (Integer v : treeSet) {
      final IntegerEntry entry = rbTree.first();
      assertEquals(v.intValue(), entry.v);
      rbTree.remove(entry);
    }
  }

  public void testRBTree3() throws Exception {
    RedBlackTree<IntegerEntry> rbTree = new RedBlackTree<IntegerEntry>();
    for (int i = 0; i < 100000; i++) {
      int val = (int)(Math.random() * 10000000);
      rbTree.add(new IntegerEntry(val));
    }

    int current = Integer.MIN_VALUE;
    for (int i = 0; i < 1000000; i++) {
      final IntegerEntry integerEntry = rbTree.first();
      if(current > integerEntry.v)
        assertTrue(current <= integerEntry.v);
      rbTree.remove(integerEntry);
      current = integerEntry.v;
      integerEntry.v += (int)(Math.random() * 10000);
      rbTree.add(integerEntry);
    }
  }

  public void testParallelSort() {
    FastRandom rng = new FastRandom(0);
    double[] array = new double[100000];
    for (int i = 0; i < array.length; i++) {
      array[i] = rng.nextDouble();
    }
    ArrayTools.parallelSort(array, ArrayTools.sequence(0, array.length));
    for (int i = 1; i < array.length; i++) {
      assertTrue(array[i] > array[i - 1]);
    }
  }
}
