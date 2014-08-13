package com.spbsu.commons.util;

import com.spbsu.commons.util.tree.IntArrayTree;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * User: qdeee
 * Date: 25.07.14
 */
public class TreeTest extends TestCase {
  private IntArrayTree tree;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tree = new IntArrayTree();
    tree.addTo(0);
    tree.addTo(0);
    tree.addTo(0);
    tree.addTo(1);
    final int childOfId3 = tree.addTo(3);
    tree.addTo(childOfId3);
  }

  public void testAdd() throws Exception {
    tree.addTo(2);

    final TIntArrayList expected1 = new TIntArrayList(new int[]{4, 5, 6, 7, 7, 7, 8, 8});
    assertEquals(tree.toString(), expected1, getPrivateFieldValue(tree, "children"));

    tree.addTo(1);
    final TIntArrayList expected2 = new TIntArrayList(new int[]{4, 6, 7, 8, 8, 8, 8, 9, 9});
    assertEquals(tree.toString(), expected2, getPrivateFieldValue(tree, "children"));

    tree.addTo(0);
    final TIntArrayList expected3 = new TIntArrayList(new int[]{5, 7, 8, 9, 9, 9, 9, 9, 10, 10});
    assertEquals(tree.toString(), expected3, getPrivateFieldValue(tree, "children"));

  }

  public void testGetParent() throws Exception {
    assertEquals(-1, tree.getParent(0));
    assertEquals(0, tree.getParent(3));
    assertEquals(3, tree.getParent(5));
    assertEquals(5, tree.getParent(6));
  }

  public void testIsDescendant() throws Exception {
    assertTrue(tree.isDescendant(6, 0));
    assertTrue(tree.isDescendant(4, 1));
    assertFalse(tree.isDescendant(2, 6));
    assertFalse(tree.isDescendant(0, 0));
  }

  public void testChildrenIterator() throws Exception {
    final TIntIterator childrenOfRoot = tree.getChildren(0);
    assertNotNull(childrenOfRoot);

    int id = 1;
    while (childrenOfRoot.hasNext()) {
      assertEquals(id++, childrenOfRoot.next());
    }

    final TIntIterator childrenOfId5 = tree.getChildren(3);
    assertNotNull(childrenOfId5);
    assertEquals(5, childrenOfId5.next());
    assertFalse(childrenOfId5.hasNext());

    assertNull(tree.getChildren(4));

  }

  private static <T> T getPrivateFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
    final Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    final Object o =  field.get(obj);
    return (T) o;
  }
}
