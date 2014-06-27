package com.spbsu.commons.regexp;


import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.DictExpansion;
import com.spbsu.commons.seq.ListDictionary;
import junit.framework.TestCase;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 05.06.12
 * Time: 15:31
 */
public class DictExpansionTest extends TestCase {
  public void testIndependent() throws Exception {
    List<Character> alpha = new ArrayList<Character>();
    for (char a = 'a'; a <= 'z'; a++)
      alpha.add(a);
    Random rnd = new FastRandom();
    DictExpansion de = new DictExpansion(alpha, alpha.size() + 100);
    for (int i = 0; i < 10000; i++) {
      int len = rnd.nextInt(300);
      StringBuilder builder = new StringBuilder(len);
      for (int c = 0; c < len; c++)
        builder.append((char)('a' + rnd.nextInt('z' - 'a' + 1)));
      de.accept(builder);
    }
    assertEquals('z' - 'a' + 1, de.result().size());
  }

  public void testRestore() throws Exception {
    ListDictionary reference = new ListDictionary("a", "b", "c", "cc", "aa", "bb");
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
        List<Character> alpha = new ArrayList<Character>();
      for (char a = 'a'; a <= 'c'; a++) {
        alpha.add(a);
      }
      FastRandom rnd = new FastRandom();
      DictExpansion de = new DictExpansion(alpha, reference.size());
      Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 10000; j++) {
        int len = rnd.nextInt(30);
        StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.next(probabs, rnd));
        de.accept(builder);
  //      System.out.println(builder);
      }
      equalsAtLeastOnce = reference.alphabet().toString().equals(de.result().alphabet().toString());
    }
    assertTrue(equalsAtLeastOnce);
  }

  public void testRestoreLong() throws Exception {
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
      ListDictionary reference = new ListDictionary("a", "b", "c", "r", "d", "cc", "aa", "bb", "rabracadabra");
      ListDictionary start = new ListDictionary("a", "b", "c", "r", "d");
      DictExpansion de = new DictExpansion(start, reference.size());
      FastRandom rng = new FastRandom();
      Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 10000; j++) {
        int len = rng.nextInt(100);
        StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.next(probabs, rng));
        de.accept(builder);
      }
      equalsAtLeastOnce = reference.alphabet().toString().equals(de.result().alphabet().toString());
    }
    assertTrue(equalsAtLeastOnce);
  }
}
