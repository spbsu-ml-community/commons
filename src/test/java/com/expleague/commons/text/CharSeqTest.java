package com.expleague.commons.text;

import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqComposite;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * User: Igor Kuralenok
 * Date: 16.05.2006
 * Time: 18:19:24
 */
public class CharSeqTest extends TestCase {
  public void testEmpty(){
    final CharSeqComposite seq = new CharSeqComposite(new CharSequence[]{""});
    seq.toString();
  }

  public void testEquals() throws Exception {
    assertEquals(CharSeq.createArrayBasedSequence("text"), CharSeq.createArrayBasedSequence("text"));
    assertEquals(CharSeq.createArrayBasedSequence("text"), CharSeq.createArrayBasedSequence("-text-").subSequence(1, 5));
    assertEquals(CharSeq.createArrayBasedSequence("text"), new CharSeqComposite("te", "xt"));
    assertEquals(new CharSeqComposite("te", "xt"), CharSeq.createArrayBasedSequence("text"));
    assertEquals(new CharSeqComposite("te", "xt"), new CharSeqComposite("t", "ext"));
    assertEquals(new CharSeqComposite("te", "xt"), "text");
    assertEquals(CharSeq.createArrayBasedSequence("text"), "text");
  }

  public void testCompositeSubSequence(){
    final FastRandom rng = new FastRandom();

    final StringBuilder randString = new StringBuilder();
    for (int i = 0; i < CharSeqComposite.MAXIMUM_COPY_FRAGMENT_LENGTH; i++) {
      randString.append((char)rng.nextByte());
    }
    final CharSeqComposite seq = new CharSeqComposite(randString.toString(), "sss");
    assertFalse(seq.subSequence(CharSeqComposite.MAXIMUM_COPY_FRAGMENT_LENGTH) instanceof CharSeqComposite);
    assertTrue(seq.subSequence(1) instanceof CharSeqComposite);
    assertEquals(2, ((CharSeqComposite) seq.subSequence(1)).fragmentsCount());
  }

  public void testCompaction(){
    final CharSeqComposite seq1 = new CharSeqComposite("ss", "bbb");
    final CharSeqComposite seq2 = new CharSeqComposite("ss", "bbb");
    final CharSeqComposite compact = new CharSeqComposite(seq1, seq2);
    assertEquals(4, compact.fragmentsCount());
    assertEquals("ssbbbssbbb", compact.toString());
  }

  public void testCopyToArray(){
    final CharSeqComposite seq1 = new CharSeqComposite("ss", "bbb");
    final CharSeqComposite seq2 = new CharSeqComposite("ss", "bbb");
    final CharSeqComposite compact = new CharSeqComposite(seq1, seq2);
    assertEquals("sbbbs", compact.subSequence(1, 6).toString());
    assertEquals("ssb", compact.subSequence(0, 3).toString());
    assertEquals("bs", compact.subSequence(4, 6).toString());
  }

  public void testToString() throws Exception {
    final CharSeqComposite seq1 = new CharSeqComposite("ss", new CharSeqComposite("", "as"),
        new CharSeqComposite("", "sd", "", ""), new CharSeqComposite(""));
    Assert.assertEquals("ssassd", seq1.toString());
  }
}
