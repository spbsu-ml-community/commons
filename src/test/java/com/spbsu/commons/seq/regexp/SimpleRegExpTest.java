package com.spbsu.commons.seq.regexp;

import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.regexp.converters.PatternStringConverter;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * User: Manokk
 * Date: 13.09.11
 * Time: 17:42
 */
public class SimpleRegExpTest extends TestCase {
  private final Alphabet<Character> A = Alphabet.CHARACTER_ALPHABET;
  private Pattern<Character> pattern = new Pattern<Character>(Alphabet.CHARACTER_ALPHABET);

  private String string;
  private TestMatchVisitor mv;

  private class TestMatchVisitor implements SimpleRegExp.MatchVisitor {
    private List<String> matches = new LinkedList<String>();
    public boolean found(int start, int end) {
      matches.add(string.substring(start, end));
      //System.out.println("-- Match found (" + start + "," + end + ") = " + matchBuilder.toString());
      return true;
    }

    public boolean contains(String s) {
      return matches.contains(s);
    }

    public int occurrences() {
      return matches.size();
    }
  }

  private void match() {
    SimpleRegExp<Character> matcher = new SimpleRegExp<Character>(pattern);
    mv = new TestMatchVisitor();
    matcher.match(CharSeq.create(string), mv);
  }

  private String randomString(int size) {
    final StringBuilder sb = new StringBuilder();
    final Random r = new Random();
    for (int i = 0; i < size; i++) {
      sb.append((char) (('z' - 'a' + 1) * r.nextDouble() + 'a'));
    }
    return sb.toString();
  }

  public void testSubstringMatching() {
    long time = 0;
    for (int t = 0; t < 50; t++) {
      Interval.start();

      pattern.clear();
      String s = randomString(5000);
      final Random rand = new Random();
      final int offset = rand.nextInt(s.length());
      for (int i = offset; i < s.length(); i++) {
        pattern.add(A.getByT(s.charAt(i)), Pattern.Modifier.NONE);
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
        pattern.add(A.getByT(regularString.charAt(i)), Pattern.Modifier.NONE);
        assertEquals(A.getByT(s.charAt(i)).toString().charAt(0), s.charAt(i));
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
  }

  public void testZeroOrOne() {
    pattern.clear();
    pattern.add(A.getByT('a'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('b'), Pattern.Modifier.NONE);

    string = "axaba1bavbaxxb";
    match();

    assertTrue(mv.contains("ab"));
    assertTrue(mv.contains("a1b"));
    assertTrue(mv.contains("avb"));
    assertFalse(mv.contains("axxb"));
    assertFalse(mv.contains("ax"));

    for (String m : mv.matches) {
      assertTrue(m.charAt(0) == 'a');
      assertTrue(m.charAt(m.length() - 1) == 'b');
    }

    pattern.clear();
    pattern.add(A.getByT('x'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('y'), Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('z'), Pattern.Modifier.QUESTION);

    string = "xyzx1yz";
    match();

//    assertTrue(mv.contains("x"));
//    assertTrue(mv.contains("x1"));
//    assertTrue(mv.contains("xy"));
    assertTrue(mv.contains("xyz"));
    assertTrue(mv.contains("x1yz"));
    assertFalse(mv.contains("xyzx1yz"));
  }

  public void testZeroOrMore() {
    pattern.clear();
    pattern.add(A.getByT('l'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('o'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('n'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('g'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.getByT('c'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('t'), Pattern.Modifier.NONE);

    string = "loooongcaaaatlong   catlngct";
    match();

//    assertTrue(mv.contains("lngct"));
//    assertTrue(mv.contains("long   cat"));
//    assertTrue(mv.contains("loooongcaaaat"));
    assertTrue(mv.contains("loooongcaaaatlong   catlngct"));
//    assertTrue(mv.contains("long   catlngct"));

    pattern.clear();
    pattern.add(A.getByT('x'), Pattern.Modifier.STAR);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.getByT('y'), Pattern.Modifier.STAR);

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
    pattern.add(A.getByT('a'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('b'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('v'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('c'), Pattern.Modifier.NONE);

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

    for (String m : mv.matches) {
      assertTrue(m.charAt(0) == 'a');
      assertTrue(m.charAt(m.length() - 1) == 'c');
    }

    pattern.clear();
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);

    String s = randomString(100);
    string = s;
    match();

    assertTrue(mv.contains(s));
    assertEquals(1, mv.occurrences());
//    assertTrue(mv.contains(s.substring(0, new Random().nextInt(s.length()))));

    pattern.clear();
    pattern.add(A.getByT('g'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.getByT('o'), Pattern.Modifier.NONE);

    string = "omgcjbjbjhbhjbhblkjlkomggo";
    match();

    assertTrue(mv.contains("gcjbjbjhbhjbhblkjlkomggo"));

    for (String m : mv.matches) {
      assertTrue(m.charAt(0) == 'g');
      assertTrue(m.charAt(m.length() - 1) == 'o');
    }
  }

  public void testSomeCases() {
    pattern.clear();
    pattern.add(A.getByT('z'), Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('c'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('b'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('m'), Pattern.Modifier.QUESTION);

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
    pattern.add(A.getByT('p'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('x'), Pattern.Modifier.QUESTION);

    string = "zbmbmbgbfbabdbkpppxkxpkwpkxpwkxpwekfnjrgnrrejnerkgnrgekgerjgnjkegnkebiblbw";
    match();
    assertTrue(mv.contains("pppx"));
    assertFalse(mv.contains(""));
    assertEquals(8, mv.occurrences());

    pattern.clear();
    pattern.add(A.getByT('f'), Pattern.Modifier.STAR);

    string = "nkjhjuhnfinniunfiuniuniufbhfhjbhffjhbjhf";
    match();
    assertTrue(mv.contains("f"));
    assertTrue(mv.contains("ff"));
    assertFalse(mv.contains(""));
    assertEquals(6, mv.occurrences());
  }

  public void testFolding1() {
    string = "bbm";
    pattern = parseRegExp("b*m");
    match();
    assertEquals(1, mv.occurrences());
  }
  public void testFolding2() {
    string = "bbmbmm";
    pattern.clear();
    pattern = parseRegExp("b*m?.m");
    match();
    assertTrue(mv.contains("bbmbm"));
    assertTrue(mv.contains("bmm"));
    assertEquals(2, mv.occurrences());
  }

  public void testFolding3() {
    string = "bbmbmm";
    pattern.clear();
    pattern = parseRegExp("b*m?.m?m");
    match();
    assertTrue(mv.contains("bbmbmm"));
    assertEquals(1, mv.occurrences());
  }

  public void testDots() {
    string = "bb";
    pattern.clear();
    pattern = parseRegExp("b..?");
    match();
    TestMatchVisitor old = mv;
    mv = new TestMatchVisitor();
    pattern = parseRegExp("b.?.");
    match();
    assertEquals(old.occurrences(), mv.occurrences());
  }

  public void testPerformance() {
    pattern = parseRegExp("z?a*c*bm?");

    long time = 0;
    final int count = 200;
    for (int i = 0; i < count; i++) {
      String str = randomString(3000);
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
    pattern.add(A.getByT('e'), Pattern.Modifier.STAR);

    StringBuilder sb = new StringBuilder();
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

  public void testStringConverter() {
    final PatternStringConverter converter = new PatternStringConverter(A);
    final String pStr = "a*b";
    pattern = converter.convertFrom(pStr);
    string = "aaaabbbbbbbbbbsddhsjhdgfhjgdjshkfb";
    match();
    assertEquals(11, mv.occurrences());
    assertEquals(pStr, converter.convertTo(pattern));
  }

  public Pattern<Character> parseRegExp(String str) {
    Pattern<Character> result = new Pattern<Character>(Alphabet.CHARACTER_ALPHABET);
    for (int i = 0; i < str.length(); i+=2) {
      final SimpleRegExp.Condition chCondition = str.charAt(i) == '.' ? SimpleRegExp.Condition.ANY : A.getByT(str.charAt(i));
      Pattern.Modifier mod = Pattern.Modifier.NONE;
      if (str.length() > i + 1) {
        switch(str.charAt(i + 1)) {
          case '*':
            mod = Pattern.Modifier.STAR;
            break;
          case '?':
            mod = Pattern.Modifier.QUESTION;
            break;
          default:
            i--;
        }
      }
      //noinspection unchecked
      result.add((SimpleRegExp.Condition<Character>)chCondition, mod);
    }
    return result;
  }

}