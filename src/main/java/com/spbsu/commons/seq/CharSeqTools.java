package com.spbsu.commons.seq;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.spbsu.commons.func.Processor;
import com.spbsu.commons.seq.trash.FloatingDecimal;
import gnu.trove.strategy.HashingStrategy;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * User: terry
 * Date: 10.10.2009
 * Time: 23:26:28
 */
public class CharSeqTools {
  public static final String EMPTY = "";
  public static final HashingStrategy<CharSequence> STRATEGY = new HashingStrategy<CharSequence>() {
    public int computeHashCode(final CharSequence cs) {
      return cs.hashCode();
    }

    public boolean equals(final CharSequence cs1, final CharSequence cs2) {
      return CharSeqTools.equals(cs1, cs2);
    }
  };
  
  public static boolean isWhitespace(char ch) {
    return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
  }

  public static boolean equals(CharSequence text, CharSequence other) {
    if (text == other) {
      return true;
    }
    final int length = text.length();
    if (length != other.length()) {
      return false;
    }
    int index = 0;
    while (index < length) {
      if (text.charAt(index) != other.charAt(index)) {
        return false;
      }
      index++;
    }
    return true;
  }

  public static boolean isNullOrEmpty(CharSequence charSequence) {
    return charSequence == null || charSequence.length() == 0;
  }

  public static CharSequence toLowerCase(CharSequence word) {
    char[] result = null;
    final int length = word.length();
    for (int i = 0; i < length; i++) {
      if (Character.isUpperCase(word.charAt(i))) {
        result = new char[length];
        break;
      }
    }
    if (result == null) {
      return word;
    }
    for (int i = 0; i < length; i++) {
      result[i] = Character.toLowerCase(word.charAt(i));
    }
    return new CharSeqArray(result, 0, result.length);
  }

  public static CharSequence toUpperCase(CharSequence word) {
    char[] result = null;
    final int length = word.length();
    for (int i = 0; i < length; i++) {
      if (Character.isLowerCase(word.charAt(i))) {
        result = new char[length];
        break;
      }
    }
    if (result == null) {
      return word;
    }
    for (int i = 0; i < length; i++) {
      result[i] = Character.toUpperCase(word.charAt(i));
    }
    return new CharSeqArray(result, 0, result.length);
  }

  public static CharSequence trim(final CharSequence word) {
    final int initialLength = word.length();
    int len = initialLength;
    int st = 0;

    while ((st < len) && (word.charAt(st) <= ' ')) {
      st++;
    }
    while ((st < len) && (word.charAt(len - 1) <= ' ')) {
      len--;
    }
    return ((st > 0) || (len < initialLength)) ? word.subSequence(st, len) : word;
  }

  public static CharSequence concatWithDelimeter(final CharSequence delimeter, final CharSequence... texts) {
    return concatWithDelimeter(delimeter, Arrays.asList(texts));
  }

  public static CharSequence concatWithDelimeter(final CharSequence delimeter, final List<? extends CharSequence> texts) {
    CharSeqBuilder result = new CharSeqBuilder(texts.size() * 2);
    for (int i = 0; i < texts.size(); i++) {
      if (i > 0)
        result.append(delimeter);
      result.append(texts.get(i));
    }

    return result.toString();
  }

  public static CharSequence concat(final CharSequence... texts) {
    return new CharSeqComposite(texts);
  }

  public static CharSequence[] split(CharSequence sequence, char separator) {
    final List<CharSequence> result = new ArrayList<CharSequence>(10);
    int last = 0;
    for (int i = 0; i < sequence.length(); i++) {
      if (sequence.charAt(i) == separator) {
        result.add(sequence.subSequence(last, i));
        last = i + 1;
      }
    }
    result.add(sequence.subSequence(last, sequence.length()));
    return result.toArray(new CharSequence[result.size()]);
  }

  public static CharSequence[] split(CharSequence sequence, CharSequence separator) {
    final List<CharSequence> result = new ArrayList<CharSequence>(10);
    int last = 0;
    for (int i = 0; i < sequence.length(); i++) {
      boolean accept = separator.length() <= sequence.length() - i;
      for (int j = 0; j < separator.length() && accept; j++) { // need to change to something faster
        if (sequence.charAt(i + j) != separator.charAt(j))
          accept = false;
      }
      if (accept) {
        result.add(sequence.subSequence(last, i));
        last = i + separator.length();
      }
    }
    result.add(sequence.subSequence(last, sequence.length()));
    return result.toArray(new CharSequence[result.size()]);
  }

  public static CharSequence cut(CharSequence from, int index, char sep) {
    final int start = index;
    while (from.length() > index && from.charAt(index) != sep)
      index++;
    return from.subSequence(start, index);
  }

  public static CharSequence cutBetween(CharSequence sequence, int startIndex, char fromSymbol, char toSymbol) {
    final int startPos = skipTo(sequence, startIndex, fromSymbol) + 1;
    if (startPos >= sequence.length()) {
      return EMPTY;
    }
    int index = startPos;
    int depth = 0;
    while (index < sequence.length()) {
      char currentSymbol = sequence.charAt(index);
      if (currentSymbol == fromSymbol) {
        depth++;
      } else if (currentSymbol == toSymbol) {
        if (depth == 0) {
          break;
        } else {
          depth--;
        }
      }
      index++;
    }
    return sequence.subSequence(startPos, index);
  }

  public static int skipTo(CharSequence from, int index, char sep) {
    while (from.length() > index && from.charAt(index) != sep)
      index++;
    return index;
  }

  public static boolean startsWith(CharSequence seq, CharSequence prefix) {
    if (seq.length() < prefix.length())
      return false;
    int index = 0;
    while(index < prefix.length()) {
      if(prefix.charAt(index) != seq.charAt(index))
        return false;
      index++;
    }

    return true;
  }

  public static boolean isImmutable(final CharSequence next) {
    if(next instanceof String)
      return true;
    if (next instanceof CharSeq)
      return ((CharSeq)next).isImmutable();
    return false;
  }

  public static List<CharSequence> discloseComposites(List<CharSequence> fragments) {
    int fragmentsCount = fragments.size();
    for (final CharSequence fragment : fragments) {
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        fragmentsCount += charSeqComposite.fragmentsCount() - 1;
      }
    }
    if (fragmentsCount == fragments.size()) {
      return fragments;
    }

    final List<CharSequence> compacted = new ArrayList<CharSequence>(fragmentsCount);
    for (final CharSequence fragment : fragments) {
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        for (int j = 0; j < charSeqComposite.fragmentsCount(); j++) {
          compacted.add(charSeqComposite.fragment(j));
        }
      }
      else compacted.add(fragment);
    }
    return compacted;
  }

  public static void processLines(Reader input, Processor<CharSequence> seqProcessor) throws IOException {
    final char[] buffer = new char[4096*4];
    CharSeqBuilder line = new CharSeqBuilder();
    int read;
    boolean skipCaretReturn = false;
    while ((read = input.read(buffer)) >= 0) {
      int offset = 0;
      int index = 0;

      while (index < read){
        if (skipCaretReturn && buffer[offset] == '\r') { // skip '\r'
          offset = ++index;
          skipCaretReturn = false;
        }

        while (index < read && buffer[index] != '\n')
          index++;

        if (index < read) {
          line.append(buffer, offset, index);
          seqProcessor.process(line);
          line.clear();
          offset = ++index; // skip '\n'
          skipCaretReturn = true;
        }
      }
      if (offset < read)
        line.append(buffer, offset, read);
    }
    if (line.length() > 0)
      seqProcessor.process(line);
  }

  public static JsonParser parseJSON(final CharSequence part) throws IOException {
    return new JsonFactory().createParser(new CharSeqReader(part));
  }

  public static float parseFloat(CharSequence in) {
    return FloatingDecimal.readJavaFormatString(in).floatValue();
  }

  public static double parseDouble(CharSequence in) {
    return FloatingDecimal.readJavaFormatString(in).doubleValue();
  }

  public static int parseInt(final CharSequence part) {
    int result = 0;
    boolean negative = false;
    int offset = 0;
    if (part.charAt(0) == '-') {
      offset++;
      negative = true;
    }
    while (offset < part.length()) {
      int nextCh = part.charAt(offset++) - '0';
      if (nextCh < 0 || nextCh > 9)
      result *= 10;
      result += nextCh;
    }
    return result * (negative ? -1 : 1);
  }

  public static class LexicographicalComparator implements Comparator<CharSequence> {
    public int compare(CharSequence a, CharSequence b) {
      int index = 0;
      while (a.length() > index && b.length() > index) {
        char aCh = a.charAt(index);
        char bCh = b.charAt(index);
        if (aCh != bCh)
          return aCh - bCh;
        index++;
      }
      return Integer.compare(a.length(), b.length());
    }
  }
}
