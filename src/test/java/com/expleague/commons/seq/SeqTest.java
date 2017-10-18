package com.expleague.commons.seq;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.util.ArrayTools;
import junit.framework.TestCase;

/**
 * User: qdeee
 * Date: 08.09.14
 */
public class SeqTest extends TestCase {
  public void testConcat1() throws Exception {
    final Vec vec1 = new ArrayVec(1, 2);
    final Vec vec2 = new ArrayVec(5, 6, 7);
    final Seq<Double> concat = ArrayTools.concat(vec1, vec2);
    assertEquals(new ArrayVec(1, 2, 5, 6, 7), concat);
  }

  public void testConcat2() throws Exception {
    final CharSeqChar seq1 = new CharSeqChar('a');
    final CharSeqChar seq2 = new CharSeqChar('b');
    final CharSeqChar seq3 = new CharSeqChar('c');
    final Seq<Character> concat = ArrayTools.concat(seq1, seq2, seq3);
    final ArraySeq<Character> characterArraySeq = new ArraySeq<>(new Character[]{'a', 'b', 'c'});
    assertEquals(characterArraySeq, concat);
  }
}
