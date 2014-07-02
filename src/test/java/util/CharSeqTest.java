package util;

import com.spbsu.commons.seq.CharSeqComposite;
import junit.framework.TestCase;

/**
 * User: Igor Kuralenok
 * Date: 16.05.2006
 */
public class CharSeqTest extends TestCase {
  public void testEmpty(){
    final CharSeqComposite seq = new CharSeqComposite(new CharSequence[]{""});
    seq.toString();
  }

  public void testCompositeSubSequence(){
    final CharSeqComposite seq = new CharSeqComposite(new CharSequence[]{"ss", "bbb"});
    assertFalse(seq.subSequence(4) instanceof CharSeqComposite);
    assertTrue(seq.subSequence(1) instanceof CharSeqComposite);
    assertEquals(2, ((CharSeqComposite) seq.subSequence(1)).fragmentsCount());
  }

  public void testCompaction(){
    final CharSeqComposite seq1 = new CharSeqComposite(new CharSequence[]{"ss", "bbb"});
    final CharSeqComposite seq2 = new CharSeqComposite(new CharSequence[]{"ss", "bbb"});
    final CharSeqComposite compact = new CharSeqComposite(new CharSequence[]{seq1, seq2});
    assertEquals(4, compact.fragmentsCount());
    assertEquals("ssbbbssbbb", compact.toString());
  }

  public void testCopyToArray(){
    final CharSeqComposite seq1 = new CharSeqComposite(new CharSequence[]{"ss", "bbb"});
    final CharSeqComposite seq2 = new CharSeqComposite(new CharSequence[]{"ss", "bbb"});
    final CharSeqComposite compact = new CharSeqComposite(new CharSequence[]{seq1, seq2});
    assertEquals("sbbbs", compact.subSequence(1, 6).toString());
    assertEquals("ssb", compact.subSequence(0, 3).toString());
    assertEquals("bs", compact.subSequence(4, 6).toString());
  }
}
