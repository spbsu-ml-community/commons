package com.spbsu.commons.math.vectors.impl;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * ksen | 15:54 01.03.2013 | commons
 */

public class ExpandableMapBasisTest extends TestCase {
  private ExpandableMapBasis<String> basis;

  public void testConstructors() {
    basis = new ExpandableMapBasis<>();
    assertTrue(basis.size() == 0);

    basis = new ExpandableMapBasis<>(100);
    assertTrue(basis.size() == 0);

    basis = new ExpandableMapBasis<>(new String[]{"First", "Second", "Third"});
    assertTrue(basis.size() == 3);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.fromIndex(2).equals("Third"));
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.toIndex("Third") == 2);

    basis = new ExpandableMapBasis<>(basis);
    assertTrue(basis.size() == 3);
    assertTrue(basis.fromIndex(0).equals("First"));
    assertTrue(basis.fromIndex(1).equals("Second"));
    assertTrue(basis.fromIndex(2).equals("Third"));
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Second") == 1);
    assertTrue(basis.toIndex("Third") == 2);
  }

  public void testFromIndex() {
    basis = new ExpandableMapBasis<>();
    for(int i = 0; i < 10000; i++)
      basis.toIndex("Element" + i);

    for(int i = 0; i < 10000; i++)
      assertTrue(basis.fromIndex(i).equals("Element" + i));

    for(int i = 10000; i < 30000; i++)
      basis.toIndex("Element" + i);

    for(int i = 10000; i < 30000; i++)
      assertTrue(basis.fromIndex(i).equals("Element" + i));
  }

  public void testToIndex() {
    basis = new ExpandableMapBasis<>();
    basis.toIndex("");
    assertTrue(basis.size() == 1);
    assertTrue(basis.fromIndex(0).equals(""));
    basis.toIndex("");
    assertTrue(basis.size() == 1);
    assertTrue(basis.fromIndex(0).equals(""));
    basis.toIndex("some");
    assertTrue(basis.size() == 2);
    assertTrue(basis.fromIndex(0).equals(""));
    assertTrue(basis.fromIndex(1).equals("some"));
    basis = new ExpandableMapBasis<>();
    basis.toIndex("Some");
    basis.toIndex("");
    assertTrue(basis.fromIndex(0).equals("Some"));
    assertTrue(basis.fromIndex(1).equals(""));
  }

  public void testGetBasis() {
    basis = new ExpandableMapBasis<>();
    for(int i = 0; i < 100; i++)
      basis.toIndex("s" + i);
    List<String> temp = basis.getBasis();
    for(int i = 0; i < 100; i++)
      assertTrue(temp.get(i).equals("s" + i));
  }

  public void testContainKey() {
    basis = new ExpandableMapBasis<>();
    basis.toIndex("First");
    assertTrue(basis.containKey("First"));
  }

}
