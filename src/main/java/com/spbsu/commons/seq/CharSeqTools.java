package com.spbsu.commons.seq;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spbsu.commons.func.Processor;
import com.spbsu.commons.seq.trash.FloatingDecimal;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.strategy.HashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
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

  public static byte hexCharToByte(final char c) {
    if (c >= 0x30 && c <= 0x39) // 0..9
      return (byte) (c - 0x30);
    if (c >= 0x61 && c <= 0x7A) // a..z
      return (byte) ((c - 0x61) + 10);
    if (c >= 0x41 && c <= 0x5A) // A..Z
      return (byte) ((c - 0x41) + 10);
    throw new IllegalArgumentException("Not a hex char: " + c);
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

  @SafeVarargs
  public static <X> Seq<X> concat(final Seq<X>... texts) {
    if (texts.length == 0)
      throw new IllegalArgumentException();
    final Seq<X> first = texts[0];
    if (char.class.isAssignableFrom(first.elementType()) || Character.class.isAssignableFrom(first.elementType())) {
      return (Seq<X>)new CharSeqComposite((CharSequence[])ArrayTools.repack(texts, CharSequence.class));
    }
    int size = 0;
    for (int i = 0; i < texts.length; i++) {
      size += texts[i].length();
    }
    final Object join = Array.newInstance(first.elementType(), size);
    int index = 0;
    for (int i = 0; i < texts.length; i++) {
      size += texts[i].length();
      for (int j = 0; j < texts[i].length(); j++) {
        Array.set(join, index++, texts[i].at(j));
      }
    }
    return create(join);
  }

  public static CharSequence concat(final CharSequence... texts) {
    return new CharSeqComposite((CharSequence[])ArrayTools.repack(texts, CharSequence.class));
  }

  public static CharSequence[] split(CharSequence sequence, char separator) {
    final List<CharSequence> result = new ArrayList<CharSequence>(10);
    int last = 0;
    final int length = sequence.length();
    for (int i = 0; i < length; i++) {
      if (sequence.charAt(i) == separator) {
        result.add(sequence.subSequence(last, i));
        last = i + 1;
      }
    }
    result.add(sequence.subSequence(last, length));
    return result.toArray(new CharSequence[result.size()]);
  }

  public static int split(CharSequence sequence, char separator, CharSequence[] result) {
    int last = 0;
    int index = 0;
    for (int i = 0; i < sequence.length(); i++) {
      if (sequence.charAt(i) == separator) {
        result[index++] = sequence.subSequence(last, i);
        last = i + 1;
      }
    }
    result[index++] = sequence.subSequence(last, sequence.length());
    return index;
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

  public static <T> boolean startsWith(Seq<T> seq, Seq<T> prefix) {
    if (seq.length() < prefix.length())
      return false;
    int index = 0;
    while(index < prefix.length()) {
      if(!prefix.at(index).equals(seq.at(index)))
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

  public static void processAndSplitLines(@NotNull final Reader input, @NotNull final Processor<CharSequence[]> seqProcessor, @Nullable final String delimeters, final boolean trim) throws IOException {
    final char[] buffer = new char[4096*4];
    final List<CharSequence> parts = new ArrayList<>();
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

        while (index < read && buffer[index] != '\n') {
          if (delimeters != null && delimeters.indexOf(buffer[index]) >= 0) {
            line.append(buffer, offset, index++);
            offset = index;
            parts.add((trim ? trim(line) : line).toString());
            line = new CharSeqBuilder();
          }
          index++;
        }

        if (index < read) {
          line.append(buffer, offset, index);
          parts.add((trim ? trim(line) : line).toString());
          seqProcessor.process(parts.toArray(new CharSequence[parts.size()]));
          line.clear();
          parts.clear();
          offset = ++index; // skip '\n'
          skipCaretReturn = true;
        }
      }
      if (offset < read)
        line.append(buffer, offset, read);
    }
    if (line.length() > 0) {
      parts.add((trim ? trim(line) : line).toString());
      seqProcessor.process(parts.toArray(new CharSequence[parts.size()]));
    }
  }

  public static void processLines(final Reader input, final Processor<CharSequence> seqProcessor) throws IOException {
    processAndSplitLines(input, new Processor<CharSequence[]>() {
      @Override
      public void process(CharSequence[] arg) {
        seqProcessor.process(arg[0]);
      }
    }, null, false);
  }


  public static JsonParser parseJSON(final CharSequence part) throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.getFactory().enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    return objectMapper.getFactory().createParser(new CharSeqReader(part));
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
        throw new IllegalArgumentException("Can not parse integer: " + part);
      result *= 10;
      result += nextCh;
    }
    return result * (negative ? -1 : 1);
  }

  public static <T> Seq<T> create(final T... symbols) {
    return create((Object)symbols);
  }

  @SuppressWarnings("unchecked")
  public static <T> Seq<T> create(final Object symbols) {
    if (!symbols.getClass().isArray())
      throw new IllegalArgumentException();

    final double length = Array.getLength(symbols);
    final Class<?> componentType = Object[].class.equals(symbols.getClass()) ? Array.get(symbols, 0).getClass() : symbols.getClass().getComponentType();
    if (length == 0)
      return emptySeq((Class<T>) componentType);
    if (char.class.isAssignableFrom(componentType)) {
      return length == 1 ? (Seq<T>)new CharSeqChar(Array.getChar(symbols, 0)) : (Seq<T>)new CharSeqArray((char[])symbols);
    }
    else if (Character.class.isAssignableFrom(componentType)){
      return length == 1 ?
             (Seq<T>)new CharSeqChar((Character)Array.get(symbols, 0)) :
             (Seq<T>)new CharSeqArray((char[])ArrayTools.repack((Object[])symbols, char.class));
    }
    else if (int.class.isAssignableFrom(componentType)) {
      return (Seq<T>)new IntSeq((int[])symbols);
    }
    else if (Integer.class.isAssignableFrom(componentType)) {
      return (Seq<T>)new IntSeq((int[])ArrayTools.repack((Object[])symbols, int.class));
    }
    else if (byte.class.isAssignableFrom(componentType)) {
      return (Seq<T>)new ByteSeq((byte[])symbols);
    }
    else if (Byte.class.isAssignableFrom(componentType)) {
      return (Seq<T>)new ByteSeq((byte[])ArrayTools.repack((Object[])symbols, byte.class));
    }
    return (Seq<T>)new ArraySeq((Object[])symbols);
  }

  public static <T> Seq.Stub<T> emptySeq(final Class<T> componentType) {
    return new Seq.Stub<T>() {
      @Override
      public T at(final int i) {
        throw new ArrayIndexOutOfBoundsException("Empty sequence");
      }
      @Override
      public int length() {
        return 0;
      }
      @Override
      public boolean isImmutable() {
        return true;
      }

      @Override
      public Class<T> elementType() {
        return componentType;
      }
    };
  }

  public static <T extends Comparable<T>> Comparator<Seq<T>> lexicographicalComparator(Class<T> clazz){
    if (char.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
      return new Comparator<Seq<T>>() {
        @Override
        public int compare(final Seq<T> aa, final Seq<T> bb) {
          final CharSeq a = (CharSeq)(Seq)aa;
          final CharSeq b = (CharSeq)(Seq)bb;
          final int minLength = Math.min(a.length(), b.length());
          int index = 0;
          while (minLength > index) {
            final char aCh = a.charAt(index);
            final char bCh = b.charAt(index);
            if (aCh != bCh)
              return aCh - bCh;
            index++;
          }
          return Integer.compare(a.length(), b.length());
        }
      };
    }
    else if (byte.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
      return new Comparator<Seq<T>>() {
        @Override
        public int compare(final Seq<T> aa, final Seq<T> bb) {
          final ByteSeq a = (ByteSeq)(Seq)aa;
          final ByteSeq b = (ByteSeq)(Seq)bb;
          final int minLength = Math.min(a.length(), b.length());
          int index = 0;
          while (minLength > index) {
            final byte aCh = a.byteAt(index);
            final byte bCh = b.byteAt(index);
            if (aCh != bCh)
              return aCh - bCh;
            index++;
          }
          return Integer.compare(a.length(), b.length());
        }
      };
    }
    else if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
      return new Comparator<Seq<T>>() {
        @Override
        public int compare(final Seq<T> aa, final Seq<T> bb) {
          final IntSeq a = (IntSeq)(Seq)aa;
          final IntSeq b = (IntSeq)(Seq)bb;
          final int minLength = Math.min(a.length(), b.length());
          int index = 0;
          while (minLength > index) {
            final int aCh = a.intAt(index);
            final int bCh = b.intAt(index);
            if (aCh != bCh)
              return aCh - bCh;
            index++;
          }
          return Integer.compare(a.length(), b.length());
        }
      };
    }
    return new Comparator<Seq<T>>() {
      @Override
      public int compare(final Seq<T> a, final Seq<T> b) {
        final int minLength = Math.min(a.length(), b.length());
        int index = 0;
        while (minLength > index) {
          final T aCh = a.at(index);
          final T bCh = b.at(index);
          if (!aCh.equals(bCh)) {
            return aCh.compareTo(bCh);
          }
          index++;
        }
        return Integer.compare(a.length(), b.length());
      }
    };
  }

  public static Object toArray(final Seq values) {
    final Object result;
    if (Integer.class.isAssignableFrom(values.elementType()))
      result = new int[values.length()];
    else if (Character.class.isAssignableFrom(values.elementType()))
      result = new char[values.length()];
    else if (Byte.class.isAssignableFrom(values.elementType()))
      result = new byte[values.length()];
    else
      result = Array.newInstance(values.elementType());
    for (int i = 0; i < values.length(); i++) {
      Array.set(result, i, values.at(i));
    }
    return result;
  }
}
