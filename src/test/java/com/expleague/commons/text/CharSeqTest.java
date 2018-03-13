package com.expleague.commons.text;

import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqComposite;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.ReaderChopper;
import com.expleague.commons.util.logging.Interval;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import javax.xml.stream.util.StreamReaderDelegate;
import java.io.IOException;
import java.io.Reader;

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

  public void testCompact1() {
    String test = "steckschlüssel satz";
    CharSeq seq = CharSeq.compact(CharSeq.create(test));
    Assert.assertEquals(CharSeq.create(test), seq);
  }

  public void testReaderBuffersRotation() throws IOException {
    Interval.start();
    FastRandom rng = new FastRandom();
    Reader reader = rng.base64Stream(100 * 1024 * 1024);
    ReaderChopper chopper = new ReaderChopper(reader);
    CharSeq next;
    while ((next = chopper.chop('A')) != null) {
      ; // nop
    }
    Interval.stopAndPrint();
  }

  public void testCH2U() {
    Assert.assertEquals(CharSeq.create("hello_world"), CharSeqTools.fromCamelHumpsToUnderscore("HelloWorld"));
    Assert.assertEquals(CharSeq.create("char_seq_test"), CharSeqTools.fromCamelHumpsToUnderscore(getClass().getSimpleName()));
    Assert.assertEquals(CharSeq.create(getClass().getSimpleName()), CharSeqTools.fromUnderscoreToCamelHumps(CharSeqTools.fromCamelHumpsToUnderscore(getClass().getSimpleName()), true));
  }
}
