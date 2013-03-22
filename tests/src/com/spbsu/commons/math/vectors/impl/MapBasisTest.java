package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.MapBasis;
import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * ksen | 22:06 28.02.2013 | commons
 */

public class MapBasisTest extends TestCase {
  private MapBasis<String> basis = new MapBasis<String>();

  public void testToIndex(){
    basis.toIndex("First");
    basis.toIndex("Second");
    basis.toIndex("Third");

    assertTrue(basis.size() == 3);

    basis.toIndex("Forth");
    basis.toIndex("Fifth");

    assertTrue(basis.size() == 5);
    assertTrue(basis.toIndex("First") == 0);
    assertTrue(basis.toIndex("Fifth") == 4);
    assertTrue(basis.size() == 5);
  }

  public void testToIndex2(){
    basis.toIndex("First");
    basis.toIndex("Second");
    basis.toIndex("Third");
    basis.toIndex("Forth");
    basis.toIndex("Fifth");

    assertTrue(basis.size() == 5);
    assertTrue(basis.toIndex("First") == 5);
    assertTrue(basis.toIndex("Fifth") == 4);
    assertTrue(basis.size() == 5);
  }

  public void testFromIndex() {
    basis.toIndex("First");
    basis.toIndex("Second");
    basis.toIndex("Third");

    assertTrue(basis.fromIndex(0).equals("First"));
  }
}
