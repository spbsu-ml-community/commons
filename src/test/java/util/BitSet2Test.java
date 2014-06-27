//package util;
//
//import junit.framework.TestCase;
//
///**
// * @author vp
// */
//public class BitSet2Test extends TestCase {
//  public void testNextSetBitAnd() throws Exception {
//    final BitSet2 s1 = new BitSet2();
//    final BitSet2 s2 = new BitSet2();
//
//    s1.set(20);
//    s1.set(30);
//    s1.set(40);
//
//    s2.set(10);
//    s2.set(30);
//    s2.set(50);
//
//    assertEquals(30, s1.nextSetBitAnd(s2, 0));
//  }
//
//  public void testNextSetBitAnd2() throws Exception {
//    final BitSet2 s1 = new BitSet2();
//    final BitSet2 s2 = new BitSet2();
//
//    s1.set(20);
//    s1.set(30);
//    s1.set(40);
//
//    s2.set(10);
//    s2.set(20);
//    s2.set(30);
//
//    assertEquals(20, s1.nextSetBitAnd(s2, 0));
//    assertEquals(30, s1.nextSetBitAnd(s2, 21));
//  }
//
//  public void testNextSetBitAnd3() throws Exception {
//    final BitSet2 s1 = new BitSet2();
//    final BitSet2 s2 = new BitSet2();
//
//    s1.set(1);
//    s1.set(2);
//    s1.set(3);
//
//    s2.set(4);
//    s2.set(5);
//    s2.set(6);
//
//    assertEquals(-1, s1.nextSetBitAnd(s2, 0));
//  }
//
//  public void testNextSetBitAnd4() throws Exception {
//    final BitSet2 s1 = new BitSet2();
//    final BitSet2 s2 = new BitSet2();
//
//    s1.set(20);
//    s1.set(30);
//    s1.set(40);
//    s1.set(1223);
//    s1.set(8);
//    s1.set(324);
//
//    s2.set(10);
//    s2.set(30);
//    s2.set(50);
//    s2.set(8);
//    s2.set(124);
//
//    assertEquals(8, s1.nextSetBitAnd(s2, 0));
//    assertEquals(30, s1.nextSetBitAnd(s2, 9));
//    assertEquals(-1, s1.nextSetBitAnd(s2, 31));
//  }
//
//}
