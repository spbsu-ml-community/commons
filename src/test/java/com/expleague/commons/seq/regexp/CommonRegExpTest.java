package com.expleague.commons.seq.regexp;

import com.expleague.commons.JUnitIOCapture;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.seq.regexp.converters.PatternVecConverter;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * User: Manokk
 * Date: 21.09.11
 * Time: 21:23
 */
public class CommonRegExpTest extends JUnitIOCapture {
  final Alphabet<Character> A = Alphabet.CHARACTER_ALPHABET;

  @Test
  public void testCharAlphabet() {
     for(int c = 0; c < A.size(); c++){
       final SimpleRegExp.Condition<Character> condition = A.get(c);
       final char ch = condition.toString().charAt(0);
       System.out.println(condition);
       assertEquals(c, A.getOrder(condition));
       assertTrue(condition.is(ch));
       assertEquals(A.getByT(ch), condition);
     }
    assertEquals(A.get(A.size()), SimpleRegExp.Condition.ANY);
    assertEquals(A.getOrder(SimpleRegExp.Condition.ANY), A.size());
  }

  @Test
  public void testConverters() {
    final Pattern<Character> pattern = new Pattern<Character>(A);
    final PatternVecConverter<Character> conv = new PatternVecConverter<Character>(A);

    pattern.add(A.getByT('l'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('o'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('n'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('g'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.getByT('c'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.getByT('t'), Pattern.Modifier.NONE);
    pattern.add(A.getByT('z'), Pattern.Modifier.NONE);

    final Vec codedPattern = conv.convertTo(pattern);
    System.out.println(pattern.toString() + " -> " + conv.convertFrom(codedPattern).toString());
    assertEquals(conv.convertFrom(codedPattern).toString(), pattern.toString());
  }

}