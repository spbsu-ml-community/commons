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
       final SimpleRegExp.Condition<Character> condition = A.condition(c);
       final char ch = condition.toString().charAt(0);
       System.out.println(condition);
       assertEquals(c, A.indexCondition(condition));
       assertTrue(condition.is(ch));
       assertEquals(A.conditionByT(ch), condition);
     }
    assertEquals(A.condition(A.size()), SimpleRegExp.Condition.ANY);
    assertEquals(A.indexCondition(SimpleRegExp.Condition.ANY), A.size());
  }

  @Test
  public void testConverters() {
    final Pattern<Character> pattern = new Pattern<Character>(A);
    final PatternVecConverter<Character> conv = new PatternVecConverter<Character>(A);

    pattern.add(A.conditionByT('l'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('o'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('n'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('g'), Pattern.Modifier.NONE);
    pattern.add(SimpleRegExp.Condition.ANY, Pattern.Modifier.QUESTION);
    pattern.add(A.conditionByT('c'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('a'), Pattern.Modifier.STAR);
    pattern.add(A.conditionByT('t'), Pattern.Modifier.NONE);
    pattern.add(A.conditionByT('z'), Pattern.Modifier.NONE);

    final Vec codedPattern = conv.convertTo(pattern);
    System.out.println(pattern.toString() + " -> " + conv.convertFrom(codedPattern).toString());
    assertEquals(conv.convertFrom(codedPattern).toString(), pattern.toString());
  }

}