package com.spbsu.commons.seq.regexp;


import com.spbsu.commons.func.Computable;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.io.codec.seq.DictExpansion;
import com.spbsu.commons.io.codec.seq.ListDictionary;
import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.CharSeqAdapter;
import com.spbsu.commons.util.ArrayTools;
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
    final List<Character> alpha = new ArrayList<Character>();
    for (char a = 'a'; a <= 'z'; a++)
      alpha.add(a);
    final Random rnd = new FastRandom();
    final DictExpansion de = new DictExpansion(alpha, alpha.size() + 100);
    for (int i = 0; i < 10000; i++) {
      final int len = rnd.nextInt(300);
      final StringBuilder builder = new StringBuilder(len);
      for (int c = 0; c < len; c++)
        builder.append((char)('a' + rnd.nextInt('z' - 'a' + 1)));
      de.accept(new CharSeqAdapter(builder));
    }
    assertEquals('z' - 'a' + 1, de.result().size());
  }

  public void testRestore() throws Exception {
    final ListDictionary reference = new ListDictionary("a", "b", "c", "cc", "aa", "bb");
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
        final List<Character> alpha = new ArrayList<Character>();
      for (char a = 'a'; a <= 'c'; a++) {
        alpha.add(a);
      }
      final FastRandom rnd = new FastRandom();
      final DictExpansion<Character> de = new DictExpansion<>(alpha, reference.size());
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 10000; j++) {
        final int len = rnd.nextInt(30);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.get(rnd.nextSimple(probabs)));
        de.accept(new CharSeqAdapter(builder));
  //      System.out.println(builder);
      }
      equalsAtLeastOnce = reference.alphabet().toString().equals(de.result().alphabet().toString());
    }
    assertTrue(equalsAtLeastOnce);
  }

  public void testRestoreLong() throws Exception {
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
      final ListDictionary<Character> reference = new ListDictionary<>(ArrayTools.map(
          new CharSequence[]{"a", "b", "c", "r", "d", "cc", "aa", "bb", "rabracadabra"},
          CharSeq.class, new Computable<CharSequence, CharSeq>() {
            @Override
            public CharSeq compute(final CharSequence argument) {
              return new CharSeqAdapter(argument);
            }
          }));
      final ListDictionary<Character> start = new ListDictionary<>(ArrayTools.map(
          new CharSequence[]{"a", "b", "c", "r", "d"},
          CharSeq.class, new Computable<CharSequence, CharSeq>() {
            @Override
            public CharSeq compute(final CharSequence argument) {
              return new CharSeqAdapter(argument);
            }
          }));
      final DictExpansion<Character> de = new DictExpansion<>(start, reference.size());
      final FastRandom rng = new FastRandom();
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 10000; j++) {
        final int len = rng.nextInt(100);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.get(rng.nextSimple(probabs)));
        de.accept(new CharSeqAdapter(builder));
      }
      equalsAtLeastOnce = reference.alphabet().toString().equals(de.result().alphabet().toString());
    }
    assertTrue(equalsAtLeastOnce);
  }
}
