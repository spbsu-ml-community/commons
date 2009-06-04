package util;

import com.spbsu.util.CompositeCharSequence;
import junit.framework.TestCase;

/**
 * User: Igor Kuralenok
 * Date: 16.05.2006
 * Time: 18:19:24
 */
public class CharSequenceTest extends TestCase {
  public void testEmpty(){
    final CompositeCharSequence seq = new CompositeCharSequence(new CharSequence[]{""});
    seq.toString();
  }

  public void testCompositeSubSequence(){
    final CompositeCharSequence seq = new CompositeCharSequence(new CharSequence[]{"ss", "bbb"});
    assertTrue(seq.subSequence(4) instanceof String);
    assertTrue(seq.subSequence(1) instanceof CompositeCharSequence);
    assertEquals(2, ((CompositeCharSequence) seq.subSequence(1)).getFragmentsCount());
  }

  public void testCompaction(){
    final CompositeCharSequence seq1 = new CompositeCharSequence(new CharSequence[]{"ss", "bbb"});
    final CompositeCharSequence seq2 = new CompositeCharSequence(new CharSequence[]{"ss", "bbb"});
    final CompositeCharSequence compact = new CompositeCharSequence(new CharSequence[]{seq1, seq2});
    assertEquals(4, compact.getFragmentsCount());
    assertEquals("ssbbbssbbb", compact.toString());
  }

  public void testCopyToArray(){
    final CompositeCharSequence seq1 = new CompositeCharSequence(new CharSequence[]{"ss", "bbb"});
    final CompositeCharSequence seq2 = new CompositeCharSequence(new CharSequence[]{"ss", "bbb"});
    final CompositeCharSequence compact = new CompositeCharSequence(new CharSequence[]{seq1, seq2});
    assertEquals("sbbbs", compact.subSequence(1, 6).toString());
    assertEquals("ssb", compact.subSequence(0, 3).toString());
    assertEquals("bs", compact.subSequence(4, 6).toString());
  }
}
