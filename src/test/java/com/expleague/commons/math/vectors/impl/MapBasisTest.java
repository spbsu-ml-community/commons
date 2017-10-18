package com.expleague.commons.math.vectors.impl;

import com.expleague.commons.math.vectors.GenericBasis;
import com.expleague.commons.math.vectors.impl.basis.MapBasis;
import gnu.trove.map.hash.TObjectIntHashMap;
import junit.framework.TestCase;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * ksen | 22:06 28.02.2013 | commons
 */

public class MapBasisTest extends TestCase {
  private MapBasis<String> basis = new MapBasis<String>();

  public void testConstructors() {
    basis = new MapBasis<String>();
    assertTrue(basis.size() == 0);
    assertTrue(basis.getInverted().size() == 0);

    basis = new MapBasis<String>(100);
    assertTrue(basis.size() == 0);
    assertTrue(basis.getInverted().size() == 0);

    basis = new MapBasis<String>(new String[]{"First", "Second", "Third"});
    assertTrue(basis.size() == 3);
    assertTrue(basis.getInverted().size() == 3);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.fromIndex(2).equals("Third"));
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.toIndex("Third") == 2);

    final Collection<String> collection = new LinkedList<String>();
    collection.add("First");
    collection.add("Second");
    collection.add("Third");
    basis = new MapBasis<String>(collection);
    assertTrue(basis.size() == 3);
    assertTrue(basis.getInverted().size() == 3);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.fromIndex(2).equals("Third"));
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.toIndex("Third") == 2);

    final GenericBasis<String> genericBasis = new MapBasis<String>();
    genericBasis.add("First");
    genericBasis.add("Second");
    genericBasis.add("Third");
    basis = new MapBasis<String>(genericBasis);
    assertTrue(basis.size() == 3);
    assertTrue(basis.getInverted().size() == 3);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.fromIndex(2).equals("Third"));
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.toIndex("Third") == 2);
  }

  public void testToIndex(){
    basis.add("First");
    basis.add("Second");
    basis.add("Third");

    assertTrue(basis.size() == 3);

    basis.add("Forth");
    basis.add("Fifth");

    assertTrue(basis.size() == 5);
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Fifth") == 4);
    assertTrue(basis.size() == 5);
  }

  public void testToIndex2(){
    basis.add("First");
    basis.add("Second");
    basis.add("Third");
    basis.add("Forth");
    basis.add("Fifth");

    assertTrue(basis.size() == 5);
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Fifth") == 4);
    assertTrue(basis.size() == 5);
  }

  public void testFromIndex() {
    basis.add("A");
    basis.add("B");
    basis.add("C");

    assertTrue(basis.fromIndex(0).equals("A"));
    assertTrue(basis.fromIndex(1).equals("B"));
    assertTrue(basis.fromIndex(2).equals("C"));
  }

  public void testRemove() {
    basis.add("First");
    basis.add("Second");
    basis.add("Third");
    basis.add("Forth");
    basis.add("Fifth");

    assertTrue(basis.remove(2).equals("Third") & basis.size() == 4);
    assertTrue(basis.fromIndex(3).equals("Fifth"));
    assertTrue(basis.fromIndex(2).equals("Forth"));
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.toIndex("Fifth") == 3);
    assertTrue(basis.toIndex("Forth") == 2);
    assertTrue(basis.toIndex("Second") == 1);

    assertTrue(basis.remove(1).equals("Second") & basis.size() == 3);
    assertTrue(basis.fromIndex(2).equals("Fifth"));
    assertTrue(basis.fromIndex(1).equals("Forth"));
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.toIndex("Fifth") == 2);
    assertTrue(basis.toIndex("Forth") == 1);
    assertTrue(basis.toIndex("First") == 0);

    assertTrue(basis.remove("First") == 0);
    basis.add("A");
    basis.add("B");
    basis.add("C");
    assertTrue(basis.remove("B") == 3);
    assertTrue(basis.fromIndex(3).equals("C"));
  }

  public void testNull() {
    basis.add("First");
    basis.add("Second");

    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.fromIndex(1).equals("Second"));

    basis.add("Third");
    assertTrue(basis.size() > basis.toIndex("Third"));
    assertTrue(basis.size() - 1 == basis.toIndex("Third"));
  }

  public void testAdd() {
    assertTrue(basis.add("A") == 0);
    assertTrue(basis.add("B") == 1);
    assertTrue(basis.add("C") == 2);
    assertTrue(basis.add("D") == 3);

    assertTrue(basis.add("A") == 0);
    assertTrue(basis.add("B") == 1);
    assertTrue(basis.add("C") == 2);
    assertTrue(basis.add("D") == 3);

    assertTrue(basis.add("E") == 4);
    assertTrue(basis.add("F") == 5);

    assertTrue(basis.add("") == 6);
    assertTrue(basis.size() == 7);
  }

  public void testGetMap() {
    basis.add("A");
    basis.add("B");
    basis.add("C");

    final TObjectIntHashMap<String> map = basis.getMap();
    assertTrue(map.size() == 3);
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
  }

  public void testGetInverted() {
    basis.add("A");
    basis.add("B");
    basis.add("C");

    final List<String> list = basis.getInverted();
    assertTrue(list.get(0).equals("A"));
    assertTrue(list.get(1).equals("B"));
    assertTrue(list.get(2).equals("C"));
  }

}
