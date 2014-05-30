package com.spbsu.commons.regexp;


import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.ArrayVec;
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
    DictExpansion de = new DictExpansion(alpha, 200, 1e-3);
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
    List<Character> alpha = new ArrayList<Character>();
    for (char a = 'a'; a <= 'c'; a++)
      alpha.add(a);
    FastRandom rnd = new FastRandom();
    DictExpansion de = new DictExpansion(alpha, reference.size(), 1e-5);
    Vec probabs = new ArrayVec(reference.size());
    VecTools.fill(probabs, 1.);
    VecTools.normalizeL1(probabs);
    for (int i = 0; i < 10000; i++) {
      int len = rnd.nextInt(30);
      StringBuilder builder = new StringBuilder(len);
      for (int c = 0; c < len; c++)
        builder.append(reference.next(probabs, rnd));
      de.accept(builder);
    }
    assertEquals(reference.alphabet().toString(), de.result().alphabet().toString());
  }

  public void testRestoreLong() throws Exception {
    ListDictionary reference = new ListDictionary("a", "b", "c", "r", "d", "cc", "aa", "bb", "abracadabra");
    ListDictionary start = new ListDictionary("a", "b", "c", "r", "d");
    DictExpansion de = new DictExpansion(start, reference.size(), 1e-3);
    FastRandom rng = new FastRandom();
    Vec probabs = new ArrayVec(reference.size());
    VecTools.fill(probabs, 1.);
    VecTools.normalizeL1(probabs);
    for (int i = 0; i < 100000; i++) {
      int len = rng.nextInt(100);
      StringBuilder builder = new StringBuilder(len);
      for (int c = 0; c < len; c++)
        builder.append(reference.next(probabs, rng));
      de.accept(builder);
    }
    assertEquals(reference.alphabet().toString(), de.result().alphabet().toString());
  }
}
