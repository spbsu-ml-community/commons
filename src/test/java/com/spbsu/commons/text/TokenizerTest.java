package com.spbsu.commons.text;

import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.CharSeqArray;
import com.spbsu.commons.text.lexical.AllocatingTokenizer;
import com.spbsu.commons.text.lexical.NoStopWordsTokenizer;
import com.spbsu.commons.text.lexical.Tokenizer;
import com.spbsu.commons.text.lexical.WordsTokenizer;
import junit.framework.TestCase;

import java.lang.ref.WeakReference;
import java.util.HashSet;

/**
 * @author vp
 */
public class TokenizerTest extends TestCase {
  public void testNonAllocatingTokenizer() throws Exception {
    CharSeqArray sequence = (CharSeqArray) CharSeq.createArrayBasedSequence("test text tezt teqt");
    char[] array = sequence.toCharArray();
    final WeakReference<char[]> weakRef = new WeakReference<char[]>(array);
    final HashSet<CharSequence> set = new HashSet<CharSequence>();
    Tokenizer tok = new WordsTokenizer(sequence);
    while (tok.hasNext()) {
      set.add(tok.next());
    }
    sequence = null;
    tok = null;
    array = null;
    System.gc();
    System.gc();
    assertTrue(weakRef.get() != null);
  }

  public void testAllocatingTokenizer() throws Exception {
    CharSeqArray sequence = (CharSeqArray) CharSeq.createArrayBasedSequence("test text tezt teqt");
    char[] array = sequence.toCharArray();
    final WeakReference<char[]> weakRef = new WeakReference<char[]>(array);
    final HashSet<CharSequence> set = new HashSet<CharSequence>();
    Tokenizer tok = new AllocatingTokenizer(new WordsTokenizer(sequence));
    while (tok.hasNext()) {
      set.add(tok.next());
    }
    sequence = null;
    tok = null;
    array = null;
    System.gc();
    System.gc();
    assertTrue(weakRef.get() == null);
  }

  public void testNoStopWordsTokenizer() throws Exception {
    final HashSet<CharSequence> stop = new HashSet<CharSequence>();
    stop.add("a");
    final NoStopWordsTokenizer tok = new NoStopWordsTokenizer(new WordsTokenizer("a b c d"), stop);
    assertTrue(tok.hasNext());
    assertTrue(tok.hasNext());
    assertTrue(tok.hasNext());
    assertTrue(tok.hasNext());
    assertEquals("b", tok.next());
    assertTrue(tok.hasNext());
    assertEquals("c", tok.next());
    assertTrue(tok.hasNext());
    assertEquals("d", tok.next());
    assertFalse(tok.hasNext());
    assertFalse(tok.hasNext());
  }

  public void testNoStopWordsTokenizer2() throws Exception {
    final HashSet<CharSequence> stop = new HashSet<CharSequence>();
    stop.add("a");
    stop.add("c");
    final NoStopWordsTokenizer tok = new NoStopWordsTokenizer(new WordsTokenizer("a b c d"), stop);
    assertTrue(tok.hasNext());
    assertEquals("b", tok.next());
    assertTrue(tok.hasNext());
    assertEquals("d", tok.next());
    assertFalse(tok.hasNext());
  }

  public void testNoStopWordsTokenizer3() throws Exception {
    final HashSet<CharSequence> stop = new HashSet<CharSequence>();
    final NoStopWordsTokenizer tok = new NoStopWordsTokenizer(new WordsTokenizer("a b c d"), stop);
    assertTrue(tok.hasNext());
    assertEquals("a", tok.next());
    assertTrue(tok.hasNext());
    assertEquals("b", tok.next());
    assertTrue(tok.hasNext());
    assertEquals("c", tok.next());
    assertTrue(tok.hasNext());
    assertEquals("d", tok.next());
    assertFalse(tok.hasNext());
  }

  public void testNoStopWordsTokenizer4() throws Exception {
    final HashSet<CharSequence> stop = new HashSet<CharSequence>();
    stop.add("a");
    stop.add("b");
    stop.add("c");
    stop.add("d");
    final NoStopWordsTokenizer tok = new NoStopWordsTokenizer(new WordsTokenizer("a b c d"), stop);
    assertFalse(tok.hasNext());
  }

}
