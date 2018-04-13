package com.expleague.commons.seq.regexp;

import com.expleague.commons.JUnitIOCapture;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.util.logging.Interval;
import com.expleague.commons.seq.regexp.converters.PatternStringConverter;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: Manokk
 * Date: 13.09.11
 * Time: 17:42
 */
public class SimpleRegExpTest extends JUnitIOCapture {
  private final Alphabet<Character> A = Alphabet.CHARACTER_ALPHABET;
  private Pattern<Character> pattern = new Pattern<>(Alphabet.CHARACTER_ALPHABET);

  private String string;
  private TestMatchVisitor mv;

  private class TestMatchVisitor implements SimpleRegExp.MatchVisitor {
    private final List<String> matches = new LinkedList<>();
    @Override
    public boolean found(final int start, final int end) {
      matches.add(string.substring(start, end));
      //System.out.println("-- Match found (" + start + "," + end + ") = " + matchBuilder.toString());
      return true;
    }

    public boolean contains(final String s) {
      return matches.contains(s);
    }

    public int occurrences() {
      return matches.size();
    }
  }

  private void match() {
    final SimpleRegExp matcher = new SimpleRegExp(pattern);
    mv = new TestMatchVisitor();
    matcher.match(CharSeq.create(string), mv);
  }

  private String randomString(final int size) {
    final StringBuilder sb = new StringBuilder();
    final Random r = new Random();
    for (int i = 0; i < size; i++) {
      sb.append((char) (('z' - 'a' + 1) * r.nextDouble() + 'a'));
    }
    return sb.toString();
  }

  // TODO lyadzhin: FIX ME: randomly fails @ teamcity
  /*public void testSubstringMatching() {
    long time = 0;
    for (int t = 0; t < 50; t++) {
      Interval.start();

      pattern.clear();
      String s = randomString(5000);
      final Random rand = new Random();
      final int offset = rand.nextInt(s.length());
      for (int i = offset; i < s.length(); i++) {
        pattern.add(A.conditionByT(s.charAt(i)), Pattern.Modifier.NONE);
      }
      string = s;
      match();

      assertTrue(mv.contains(s.substring(offset)));
      assertEquals(1, mv.occurrences());

      final String regularString = randomString(50);
      s = "";
      for (int i = 0; i < 17; i++) {
        s += randomString(rand.nextInt(1000)) + regularString + randomString(rand.nextInt(3));
      }
      pattern.clear();

      for (int i = 0; i < regularString.length(); i++) {
        pattern.add(A.conditionByT(regularString.charAt(i)), Pattern.Modifier.NONE);
        assertEquals(A.conditionByT(s.charAt(i)).toString().charAt(0), s.charAt(i));
      }
      string = s;
      match();

      assertTrue(mv.contains(regularString));
      assertEquals(17, mv.occurrences());

      time += Interval.time();
      Interval.stopAndPrint(" -- substring found test");
    }
    time /= 100;
    System.out.println("Average time = " + time + " ms");
  }*/

  @Test
  public void testZeroOrOne() {
    pattern.clear();
    pattern.add(A.conditionByT('a'), Pattern.Modifier.NONE);
    //noinspection unchecked
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.conditionByT('b'), Pattern.Modifier.NONE);

    string = "axaba1bavbaxxb";
    match();

    assertTrue(mv.contains("ab"));
    assertTrue(mv.contains("a1b"));
    assertTrue(mv.contains("avb"));
    assertFalse(mv.contains("axxb"));
    assertFalse(mv.contains("ax"));

    for (final String m : mv.matches) {
      assertTrue(m.charAt(0) == 'a');
      assertTrue(m.charAt(m.length() - 1) == 'b');
    }

    pattern.clear();
    pattern.add(A.conditionByT('x'), Pattern.Modifier.NONE);
    //noinspection unchecked
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.conditionByT('y'), Pattern.Modifier.QUESTION);
    pattern.add(A.conditionByT('z'), Pattern.Modifier.QUESTION);

    string = "xyzx1yz";
    match();

//    assertTrue(mv.contains("x"));
//    assertTrue(mv.contains("x1"));
//    assertTrue(mv.contains("xy"));
    assertTrue(mv.contains("xyz"));
    assertTrue(mv.contains("x1yz"));
    assertFalse(mv.contains("xyzx1yz"));
  }

  @Test
  public void testZeroOrMore() {
    pattern.clear();
    pattern.add(A.conditionByT('l'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('o'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('n'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('g'), Pattern.Modifier.NONE);
    //noinspection unchecked
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('c'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('t'), Pattern.Modifier.NONE);

    string = "loooongcaaaatlong   catlngct";
    match();

//    assertTrue(mv.contains("lngct"));
//    assertTrue(mv.contains("long   cat"));
//    assertTrue(mv.contains("loooongcaaaat"));
    assertTrue(mv.contains("loooongcaaaatlong   catlngct"));
//    assertTrue(mv.contains("long   catlngct"));

    pattern.clear();
    pattern.add(A.conditionByT('x'), Pattern.Modifier.STAR);
    //noinspection unchecked
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('y'), Pattern.Modifier.STAR);

    string = "xcdsv43f3vdfyxyx1y";
    match();

//    assertTrue(mv.contains("v43f"));
//    assertTrue(mv.contains("x"));
//    assertTrue(mv.contains("y"));
//    assertTrue(mv.contains("xy"));
//    assertTrue(mv.contains("x1y"));
    assertTrue(mv.contains("xcdsv43f3vdfyxyx1y"));
    assertFalse(mv.contains(""));

    pattern.clear();
    pattern.add(A.conditionByT('a'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('b'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('v'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('c'), Pattern.Modifier.NONE);

    string = "acabvcabbbbbcabbkcasvvccavvvvcavcabc";
    match();
    assertTrue(mv.contains("ac"));
    assertTrue(mv.contains("abc"));
    assertTrue(mv.contains("avc"));
    assertTrue(mv.contains("abvc"));
    assertTrue(mv.contains("abbbbbc"));
    assertTrue(mv.contains("avvvvc"));
    assertFalse(mv.contains("abbkc"));
    assertFalse(mv.contains("asvvc"));

    for (final String m : mv.matches) {
      assertTrue(m.charAt(0) == 'a');
      assertTrue(m.charAt(m.length() - 1) == 'c');
    }

    pattern.clear();
    //noinspection unchecked
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);

    final String s = randomString(100);
    string = s;
    match();

    assertTrue(mv.contains(s));
    assertEquals(1, mv.occurrences());
//    assertTrue(mv.contains(s.substring(0, new Random().nextInt(s.length()))));

    pattern.clear();
    pattern.add(A.conditionByT('g'), Pattern.Modifier.NONE);
    //noinspection unchecked
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('o'), Pattern.Modifier.NONE);

    string = "omgcjbjbjhbhjbhblkjlkomggo";
    match();

    assertTrue(mv.contains("gcjbjbjhbhjbhblkjlkomggo"));

    for (final String m : mv.matches) {
      assertTrue(m.charAt(0) == 'g');
      assertTrue(m.charAt(m.length() - 1) == 'o');
    }
  }

  @Test
  public void testSomeCases() {
    pattern.clear();
    pattern.add(A.conditionByT('z'), Pattern.Modifier.QUESTION);
    pattern.add(A.conditionByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('c'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('b'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('m'), Pattern.Modifier.QUESTION);

    string = "zbmbmbgbfbabdbkbiblbw";
    match();

    assertTrue(mv.contains("zbm"));
    assertTrue(mv.contains("b"));
    assertTrue(mv.contains("bm"));
    assertFalse(mv.contains("bg"));
    assertFalse(mv.contains("bf"));
    assertFalse(mv.contains("bw"));
    assertFalse(mv.contains("bl"));

    pattern.clear();
    pattern.add(A.conditionByT('p'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('x'), Pattern.Modifier.QUESTION);

    string = "zbmbmbgbfbabdbkpppxkxpkwpkxpwkxpwekfnjrgnrrejnerkgnrgekgerjgnjkegnkebiblbw";
    match();
    assertTrue(mv.contains("pppx"));
    assertFalse(mv.contains(""));
    assertEquals(8, mv.occurrences());

    pattern.clear();
    pattern.add(A.conditionByT('f'), Pattern.Modifier.STAR);

    string = "nkjhjuhnfinniunfiuniuniufbhfhjbhffjhbjhf";
    match();
    assertTrue(mv.contains("f"));
    assertTrue(mv.contains("ff"));
    assertFalse(mv.contains(""));
    assertEquals(6, mv.occurrences());
  }

  @Test
  public void testFolding1() {
    string = "bbm";
    pattern = SimpleRegExp.create("b*m").pattern();
    match();
    assertEquals(1, mv.occurrences());
  }

  @Test
  public void testFolding2() {
    string = "bbmbmm";
    pattern.clear();
    pattern = SimpleRegExp.create("b*m?.m").pattern();
    match();
    assertTrue(mv.contains("bbmbm"));
    assertTrue(mv.contains("bmm"));
    assertEquals(2, mv.occurrences());
  }

  @Test
  public void testFolding3() {
    string = "bbmbmm";
    pattern.clear();
    pattern = SimpleRegExp.create("b*m?.m?m").pattern();
    match();
    assertTrue(mv.contains("bbmbmm"));
    assertEquals(1, mv.occurrences());
  }

  @Test
  public void testDots() {
    string = "bb";
    pattern.clear();
    pattern = SimpleRegExp.create("b..?").pattern();
    match();
    final TestMatchVisitor old = mv;
    mv = new TestMatchVisitor();
    pattern = SimpleRegExp.create("b.?.").pattern();
    match();
    assertEquals(old.occurrences(), mv.occurrences());
  }

  @Test
  public void testPerformance() {
    pattern = SimpleRegExp.create("z?a*c*bm?").pattern();

    long time = 0;
    final int count = 200;
    for (int i = 0; i < count; i++) {
      final String str = randomString(3000);
      string = str;
      Interval.start();
      match();
      if (i > 10)
        time += Interval.time();
      Interval.stopAndPrint(" - string(" + str.length() + ") ");
    }
    System.out.println("-- Average time = " + (time / (count - 10)) + " ms");

    final int eLength = 1000;
    pattern.clear();
    pattern.add(A.conditionByT('e'), Pattern.Modifier.STAR);

    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < eLength; i++) {
      sb.append('e');
    }

    string = sb.toString();
    Interval.start();
    match();
    Interval.stopAndPrint(" - matching a string of length " + eLength + " by (e*)");

//  now it's GREEDY
//    String str = "e";
//    for (int i = 0; i < eLength; i++) {
//      assertTrue(mv.contains(str));
//      str = str + "e";
//    }
    assertTrue(mv.contains(sb.toString()));
    assertFalse(mv.contains("eee"));
  }

  @Test
  public void testStringConverter() {
    final PatternStringConverter converter = new PatternStringConverter(A);
    final String pStr = "a*b";
    pattern = converter.convertFrom(pStr);
    string = "aaaabbbbbbbbbbsddhsjhdgfhjgdjshkfb";
    match();
    assertEquals(11, mv.occurrences());
    assertEquals(pStr, converter.convertTo(pattern));
  }
}