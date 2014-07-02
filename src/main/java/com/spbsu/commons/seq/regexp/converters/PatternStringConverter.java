package com.spbsu.commons.seq.regexp.converters;


import com.spbsu.commons.func.Converter;
import com.spbsu.commons.seq.regexp.Alphabet;
import com.spbsu.commons.seq.regexp.Matcher;
import com.spbsu.commons.seq.regexp.Pattern;
import com.spbsu.commons.seq.regexp.SimpleRegExp;

/**
 * User: solar
 * Date: 07.12.11
 * Time: 13:50
 */
public class PatternStringConverter implements Converter<Pattern<Character>,String> {
  private final Alphabet<Character> alpha;

  public PatternStringConverter(Alphabet<Character> alpha) {
    this.alpha = alpha;
  }

  @Override
  public Pattern<Character> convertFrom(String str) {
    Pattern<Character> result = new Pattern<Character>(alpha);
    for (int i = 0; i < str.length(); i+=2) {
      final SimpleRegExp.Condition chCondition = str.charAt(i) == '.' ? Matcher.Condition.ANY : alpha.getByT(str.charAt(i));
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

  private static final String[] SIGNS = new String[]{"", "?", "*", ""};
  @Override
  public String convertTo(Pattern pattern) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < pattern.size(); i++) {
      sb.append(alpha.getT(pattern.condition(i)).toString()).append(SIGNS[pattern.modifier(i).ordinal()]);
    }
    return sb.toString();
  }
}
