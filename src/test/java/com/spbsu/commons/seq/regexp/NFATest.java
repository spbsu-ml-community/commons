package com.spbsu.commons.seq.regexp;


import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.seq.regexp.converters.PatternStringConverter;
import com.spbsu.commons.util.Holder;
import com.spbsu.commons.util.logging.Interval;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TLongProcedure;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: solar
 */
public class NFATest extends TestCase {
  private final Alphabet<Character> A = Alphabet.CHARACTER_ALPHABET;
  private Pattern<Character> pattern = new Pattern<Character>(Alphabet.CHARACTER_ALPHABET);

  private String string;
  private TestMatchVisitor mv;

  private class TestMatchVisitor implements SimpleRegExp.MatchVisitor {
    TLongArrayList matches = new TLongArrayList(100500);
    private List<String> matchesStr = null;
    public boolean found(final int start, final int end) {
      matches.add(((long)start << 32) | end);
      //System.out.println("-- Match found (" + start + "," + end + ") = " + matchBuilder.toString());
      return true;
    }

    public boolean contains(final String s) {
      if (matchesStr == null) {
        matchesStr = new ArrayList<String>(matches.size());
        matches.forEach(new TLongProcedure() {
          public boolean execute(final long l) {
            matchesStr.add(string.substring((int)(l >> 32), (int)l));
            return true;
          }
        });
      }
      return matchesStr.contains(s);
    }

    public int occurrences() {
      return matches.size();
    }
  }

  private void match() {
    final Matcher<Character> matcher = new PatternCompiler().compile(pattern);
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

  public void testSubstringMatching() {
    {
      final String str = "asc";
      pattern = parseRegExp(str);
      string = "bd" + str;
      match();
      assertEquals(1, mv.occurrences());
      assertTrue(mv.contains(str));
    }
    {
      final String str = "asc";
      pattern = parseRegExp(str);
      string = "a" + str;
      match();
      assertEquals(1, mv.occurrences());
      assertTrue(mv.contains(str));
    }
    {
      final String str = "asc";
      pattern = parseRegExp(str);
      string = str;
      match();
      assertEquals(1, mv.occurrences());
      assertTrue(mv.contains(str));
    }
  }

  public void testRandomSubstringMatching() {
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
//      assertEquals(1, mv.occurrences());

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
//    pattern = create("ab?");
//    string = "ac";
//    found();
//    assertEquals(1, mv.occurrences());
//    assertTrue(mv.contains("a"));

    pattern = parseRegExp("a.?b");
    string = "axabacbavbaxxb";
    match();

    assertTrue(mv.contains("ab"));
    assertTrue(mv.contains("acb"));
    assertTrue(mv.contains("avb"));
    assertFalse(mv.contains("axxb"));
    assertFalse(mv.contains("ax"));

    for (final String m : mv.matchesStr) {
      assertTrue(m.charAt(0) == 'a');
      assertTrue(m.charAt(m.length() - 1) == 'b');
    }

    pattern.clear();
    pattern.add(A.getByT('x'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('y'), Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('z'), Pattern.Modifier.QUESTION);

    string = "xyzxcyz";
    match();

//    assertTrue(mv.contains("x"));
//    assertTrue(mv.contains("x1"));
//    assertTrue(mv.contains("xy"));
    assertTrue(mv.contains("xyz"));
    assertTrue(mv.contains("xcyz"));
    assertFalse(mv.contains("xyzxcyz"));
  }

  public void testZeroOrMore() {
    pattern = parseRegExp("ab*c");
    string = "abbbbbbbbbbbc";
    match();
    assertEquals(1, mv.occurrences());
    assertTrue(mv.contains(string));

    pattern.clear();
    pattern.add(A.getByT('l'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('o'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('n'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('g'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.getByT('c'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('t'), Pattern.Modifier.NONE);

    string = "loooongcaaaatlongsukacatlngct";
    match();

//    assertTrue(mv.contains("lngct"));
//    assertTrue(mv.contains("long   cat"));
//    assertTrue(mv.contains("loooongcaaaat"));
    assertTrue(mv.contains(string));
//    assertTrue(mv.contains("long   catlngct"));

    pattern.clear();
    pattern.add(A.getByT('x'), Pattern.Modifier.STAR);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.getByT('y'), Pattern.Modifier.STAR);

    string = "xcdsvnmfnvdfyxyxky";
    match();

//    assertTrue(mv.contains("v43f"));
//    assertTrue(mv.contains("x"));
//    assertTrue(mv.contains("y"));
//    assertTrue(mv.contains("xy"));
//    assertTrue(mv.contains("x1y"));
    assertTrue(mv.contains("xcdsvnmfnvdfyxyxky"));
    assertFalse(mv.contains(""));

    pattern.clear();
    pattern = parseRegExp("ab*v*c");

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

    for (final String m : mv.matchesStr) {
      assertTrue(m.charAt(0) == 'a');
      assertTrue(m.charAt(m.length() - 1) == 'c');
    }

//    assertTrue(mv.contains(s.substring(0, new Random().nextInt(s.length()))));

    pattern.clear();
    pattern.add(A.getByT('g'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.STAR);
    pattern.add(A.getByT('o'), Pattern.Modifier.NONE);

    string = "omgcjbjbjhbhjbhblkjlkomggo";
    match();

    assertTrue(mv.contains("gcjbjbjhbhjbhblkjlkomggo"));

    for (final String m : mv.matchesStr) {
      assertTrue(m.charAt(0) == 'g');
      assertTrue(m.charAt(m.length() - 1) == 'o');
    }
  }

  public void testWildcard() {
    pattern = parseRegExp(".*");
    final String s = randomString(100);
    string = s;
    match();

    assertTrue(mv.contains(s));
    assertEquals(1, mv.occurrences());
  }

  public void testSomeCases() {
    pattern.clear();
    pattern = parseRegExp("z?a*c*bm?");

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
    final TestMatchVisitor old = mv;
    mv = new TestMatchVisitor();
    pattern = parseRegExp("b.?.");
    match();
    assertEquals(old.occurrences(), mv.occurrences());
  }

  public void testPerformance() {
    final String patternStr = "z?a*c*bm?";
    pattern = parseRegExp(patternStr);
    final java.util.regex.Pattern stdMatch = java.util.regex.Pattern.compile(patternStr);
    final NFA<Character> nfa = new PatternCompiler().compile(pattern);
    long time = 0;
    long time1 = 0;
    for (int i = 0; i < 110; i++) {
      final String s = randomString(300000);
      final Seq<Character> seq = CharSeq.create(s);

      final Holder<Integer> counter = new Holder<Integer>(0);
      final Matcher.MatchVisitor visitor = new Matcher.MatchVisitor() {
        public boolean found(final int start, final int end) {
//          System.out.println(start + " " + end);
//          counter.setValue(counter.getValue() + 1);
          return true;
        }
      };

      final int cnt = 0;
      Interval.start();
      final java.util.regex.Matcher matcher = stdMatch.matcher(s);
      for (int t = 0; t < 1; t++) {
        final int start = 0;
        matcher.reset();
        while (matcher.find()) {
//          System.out.println(matcher.start() + " " + matcher.end());
//          cnt++;
//          break;
        }
      }
      if (i > 10)
        time1 += Interval.time();
      Interval.stopAndPrint(" - std string(300000) found " + cnt);
      Interval.start();
      for (int t = 0; t < 1; t++) {
        nfa.match(seq, visitor);
      }
      if (i > 10)
        time += Interval.time();
      Interval.stopAndPrint(" - string(300000) found " + counter.getValue());
    }
    System.out.println("-- Average time = " + (time / 100.) + " ms");
    System.out.println("-- Average time = " + (time1 / 100.) + " ms");

    final int eLength = 1000;
    pattern.clear();
    pattern.add(A.getByT('e'), Pattern.Modifier.STAR);

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

  public void testStringConverter() {
    final PatternStringConverter converter = new PatternStringConverter(A);
    final String pStr = "a*b";
    pattern = converter.convertFrom(pStr);
    string = "aaaabbbbbbbbbbsddhsjhdgfhjgdjshkfb";
    match();
    assertEquals(11, mv.occurrences());
    assertEquals(pStr, converter.convertTo(pattern));
  }

  public Pattern<Character> parseRegExp(final String str) {
    final Pattern<Character> result = new Pattern<Character>(Alphabet.CHARACTER_ALPHABET);
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