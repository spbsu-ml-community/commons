package com.spbsu.commons.seq;


import com.spbsu.commons.func.Action;
import com.spbsu.commons.func.Processor;
import com.spbsu.commons.io.StreamTools;
import com.spbsu.commons.seq.trash.FloatingDecimal;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.strategy.HashingStrategy;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * User: terry
 * Date: 10.10.2009
 * Time: 23:26:28
 */
@SuppressWarnings("UnusedDeclaration")
public class CharSeqTools {
  public static final String EMPTY = "";
  public static final HashingStrategy<CharSequence> STRATEGY = new HashingStrategy<CharSequence>() {
    @Override
    public int computeHashCode(final CharSequence cs) {
      return cs.hashCode();
    }

    @Override
    public boolean equals(final CharSequence cs1, final CharSequence cs2) {
      return CharSeqTools.equals(cs1, cs2);
    }
  };

  public static final NumberFormat prettyPrint = NumberFormat.getInstance(Locale.US);

  static {
    CharSeqTools.prettyPrint.setMaximumFractionDigits(4);
    CharSeqTools.prettyPrint.setMinimumFractionDigits(0);
    CharSeqTools.prettyPrint.setRoundingMode(RoundingMode.HALF_UP);
    CharSeqTools.prettyPrint.setGroupingUsed(false);
  }

  public static boolean isWhitespace(final char ch) {
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

  public static boolean equals(final CharSequence text, final CharSequence other) {
    if (text == other) {
      return true;
    }
    if (text == null || other == null)
      return false;

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

  public static CharSequence toLowerCase(final CharSequence word) {
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

  public static CharSequence toUpperCase(final CharSequence word) {
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
    final CharSeqBuilder result = new CharSeqBuilder(texts.size() * 2);
    for (int i = 0; i < texts.size(); i++) {
      if (i > 0)
        result.append(delimeter);
      result.append(texts.get(i));
    }

    return result.build();
  }

  @SafeVarargs
  public static <X> Seq<X> concat(final Seq<X>... texts) {
    if (texts.length == 0)
      throw new IllegalArgumentException();
    final Seq<X> first = texts[0];
    if (char.class.isAssignableFrom(first.elementType()) || Character.class.isAssignableFrom(first.elementType())) {
      //noinspection unchecked
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

  public static CharSequence[] split(final CharSequence sequence, final char separator) {
    final List<CharSequence> result = new ArrayList<>(10);
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

  public static CharSequence[] split(final CharSequence sequence, final char separator, final CharSequence[] result) {
    final int index = trySplit(sequence, separator, result);
    if (index < result.length)
      throw new IllegalArgumentException("Too little parts found in input: " + sequence);
    return result;
  }

  public static CharSequence[] split(final CharSequence sequence, final CharSequence separator) {
    final List<CharSequence> result = new ArrayList<>(10);
    int prev = 0;
    while (true) {
      final int next = indexOf(sequence, prev, separator);
      if (next >= 0) {
        result.add(sequence.subSequence(prev, next));
        prev = next + separator.length();
      }
      else {
        result.add(sequence.subSequence(prev, sequence.length()));
        break;
      }
    }
    return result.toArray(new CharSequence[result.size()]);
  }

  public static Stream<CharSequence> split(final CharSequence input, CharSequence separator, boolean parallel) {
    CharSequence next;
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CharSequence>() {
      int next = 0;
      int prev = 0;
      @Override
      public boolean hasNext() {
        if (prev >= next && prev < input.length()) {
          next = indexOf(input, prev, separator);
          if (next < 0)
            next = input.length();
        }
        return prev < next;
      }

      @Override
      public CharSequence next() {
        if (!hasNext())
          throw new NoSuchElementException();
        final CharSequence result = input.subSequence(prev, next);
        prev = next + separator.length();
        return result;
      }
    }, Spliterator.IMMUTABLE), parallel);
  }

  public static CharSequence cut(final CharSequence from, int index, final char sep) {
    final int start = index;
    while (from.length() > index && from.charAt(index) != sep)
      index++;
    return from.subSequence(start, index);
  }

  public static CharSequence cutBetween(final CharSequence sequence, final int startIndex, final char fromSymbol, final char toSymbol) {
    final int startPos = skipTo(sequence, startIndex, fromSymbol) + 1;
    if (startPos >= sequence.length()) {
      return EMPTY;
    }
    int index = startPos;
    int depth = 0;
    while (index < sequence.length()) {
      final char currentSymbol = sequence.charAt(index);
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

  public static int skipTo(final CharSequence from, int index, final char sep) {
    while (from.length() > index && from.charAt(index) != sep)
      index++;
    return index;
  }

  public static <T> boolean startsWith(final Seq<T> seq, final Seq<T> prefix) {
    if (seq instanceof CharSeq && prefix instanceof CharSeq)
      return startsWith((CharSequence) seq, (CharSequence) prefix);
    if (seq.length() < prefix.length())
      return false;
    int index = 0;
    while(index < prefix.length()) {
      if (!prefix.at(index).equals(seq.at(index)))
        return false;
      index++;
    }

    return true;
  }

  public static boolean startsWith(final CharSequence seq, final CharSequence prefix) {
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

  private static final String[] itoaCache = new String[1000];
  static {
    for (int i = 0; i < itoaCache.length; i++)
      itoaCache[i] = Integer.toString(i);
  }
  public static String itoa(int i) {
    if (i < itoaCache.length)
      return itoaCache[i];
    return Integer.toString(i);
  }

  public static boolean isImmutable(final CharSequence next) {
    return next instanceof String || next instanceof CharSeq && ((CharSeq) next).isImmutable();
  }

  public static List<CharSequence> discloseComposites(final List<CharSequence> fragments) {
    int fragmentsCount = fragments.size();
    boolean hasComposited = false;
    for (final CharSequence fragment : fragments) {
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        fragmentsCount += charSeqComposite.fragmentsCount() - 1;
        hasComposited = true;
      }
    }
    if (!hasComposited) {
      return fragments;
    }

    final List<CharSequence> compacted = new ArrayList<>(fragmentsCount);
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

  public static void processLines(final Reader input, final Processor<CharSequence> seqProcessor) throws IOException {
    final ReaderChopper chopper = new ReaderChopper(input);
    CharSequence next;
    while ((next = chopper.chop('\n')) != null) {
      seqProcessor.process(next);
      chopper.eat('\r');
    }
  }

  public static int processLines(final Reader input, final Action<CharSequence> seqProcessor) throws IOException {
    int count = 0;
    final ReaderChopper chopper = new ReaderChopper(input);
    CharSequence next;
    while ((next = chopper.chop('\n')) != null) {
      seqProcessor.invoke(next);
      chopper.eat('\r');
      count++;
    }
    return count;
  }

  public static Stream<CharSeq> lines(final Reader input, boolean parallel) {
    final ReaderChopper chopper = new ReaderChopper(input);
    CharSequence next;
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CharSeq>() {
      CharSeq next;
      @Override
      public boolean hasNext() {
        try {
          next = chopper.chop('\n');
          if (next != null) {
            if (next.length() > 0 && next.at(next.length() - 1) == '\r')
              next = next.subSequence(0, next.length() - 1);
            return true;
          }
          return false;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public CharSeq next() {
        return next;
      }
    }, Spliterator.IMMUTABLE), parallel).onClose(() -> StreamTools.close(input));
  }

  public static float parseFloat(final CharSequence in) {
    return FloatingDecimal.readJavaFormatString(in).floatValue();
  }

  public static double parseDouble(final CharSequence in) {
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
      final int nextCh = part.charAt(offset++) - '0';
      if (nextCh < 0 || nextCh > 9)
        throw new IllegalArgumentException("Can not parse integer: " + part);
      result *= 10;
      result += nextCh;
    }
    return result * (negative ? -1 : 1);
  }

  public static long parseLong(final CharSequence part) {
    long result = 0;
    boolean negative = false;
    int offset = 0;
    if (part.charAt(0) == '-') {
      offset++;
      negative = true;
    }
    while (offset < part.length()) {
      final int nextCh = part.charAt(offset++) - '0';
      if (nextCh < 0 || nextCh > 9)
        throw new IllegalArgumentException("Can not parse integer: " + part);
      result *= 10;
      result += nextCh;
    }
    return result * (negative ? -1l : 1l);
  }

  /**
   * creates Seq from both primitive and non-primitive arrays. In case of non primitive arrays of wrapped objects they will be repacked to primitives.
   * Until repack the operation is light.
   */
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
      public Class<? extends T> elementType() {
        return componentType;
      }
    };
  }

  public static <T extends Comparable<T>> Comparator<Seq<T>> lexicographicalComparator(final Class<? extends T> clazz){
    if (char.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
      return (aa, bb) -> {
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
      };
    }
    else if (byte.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
      return (aa, bb) -> {
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
      };
    }
    else if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
      return (aa, bb) -> {
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
      };
    }
    return (a, b) -> {
      final int minLength = Math.min(a.length(), b.length());
      int index = 0;
      while (minLength > index) {
        final T aCh = a.at(index);
        final T bCh = b.at(index);
        if (aCh != bCh && !aCh.equals(bCh)) {
          return aCh.compareTo(bCh);
        }
        index++;
      }
      return Integer.compare(a.length(), b.length());
    };
  }

  public static Object toArray(final Seq values) {
    if (values instanceof CharSeq)
      return ((CharSeq) values).toCharArray();
    final Object result;
    if (Integer.class.isAssignableFrom(values.elementType()))
      result = new int[values.length()];
    else if (Character.class.isAssignableFrom(values.elementType()))
      result = new char[values.length()];
    else if (Byte.class.isAssignableFrom(values.elementType()))
      result = new byte[values.length()];
    else
      result = Array.newInstance(values.elementType(), values.length());
    for (int i = 0; i < values.length(); i++) {
      Array.set(result, i, values.at(i));
    }
    return result;
  }

  public static CharSequence replace(final CharSequence seq, final CharSequence from, final CharSequence to) {
    return concatWithDelimeter(to, split(seq, from));
  }

  public static boolean endsWith(final CharSequence seq, final CharSequence suffix) {
    final int sLength = suffix.length();
    final int length = seq.length();
    if (length < sLength)
      return false;
    int index = 0;
    while(index < sLength) {
      if(suffix.charAt(sLength - index - 1) != seq.charAt(length - index - 1))
        return false;
      index++;
    }

    return true;
  }

  public static boolean isAlpha(final CharSequence suffix) {
    if (suffix.length() == 0)
      return false;
    int index = 0;
    while(index < suffix.length()) {
      if (!Character.isAlphabetic(suffix.charAt(index++)))
        return false;
    }
    return true;
  }

  public static boolean isNumeric(final CharSequence arg) {
    if (arg.length() == 0)
      return false;
    int index = 0;
    while(index < arg.length()) {
      if (!Character.isDigit(arg.charAt(index++)))
        return false;
    }
    return true;
  }

  public static int indexOf(final CharSequence value, final CharSequence toFind) {
    return indexOf(value, 0, toFind);
  }

  public static boolean parseBoolean(final CharSequence sequence) {
    return equals(sequence, "true") || equals(sequence, "yes") || equals(sequence, "1");
  }

  /**
   * Removes leading occurrences of specified char from specified sequence.
   */
  public static CharSequence removeLeading(final CharSequence s, final char c) {
    final int index = countLeadingOccurrences(s, c);
    return s.subSequence(index, s.length());
  }

  /**
   * Counts number of occurrences of specified char at the beginning of specified sequence.
   */
  public static int countLeadingOccurrences(final CharSequence s, final char c) {
    int i = 0;
    while (i < s.length() && s.charAt(i) == c)
      i++;
    return i;
  }

  public static int getTotalLength(final Collection<? extends CharSequence> sequences) {
    int length = 0;
    for (final CharSequence sequence: sequences)
      length += sequence.length();
    return length;
  }

  public static int trySplit(final CharSequence sequence, final char separator, final CharSequence[] result) {
    final int length = sequence.length();
    if (sequence instanceof String) {
      int last = 0;
      int index = 0;
      for (int i = 0; i < length && index < result.length - 1; i++) {
        if (sequence.charAt(i) == separator) {
          result[index++] = new CharSeqAdapter(sequence, last, i);
          last = i + 1;
        }
      }
      result[index++] = new CharSeqAdapter(sequence, last, length);
      return index;
    }
    else {
      int last = 0;
      int index = 0;
      for (int i = 0; i < length && index < result.length - 1; i++) {
        if (sequence.charAt(i) == separator) {
          result[index++] = sequence.subSequence(last, i);
          last = i + 1;
        }
      }
      result[index++] = sequence.subSequence(last, length);
      return index;
    }
  }

  // TODO: optimize for n/m complexity
  public static int indexOf(final CharSequence value, final int start, final CharSequence looking4) {
    final int looking4length = looking4.length();
    if (value.length() < looking4length)
      return -1;
    if (looking4length == 0)
      return 0;
    int index = start;
    final int lastIndex = value.length() - looking4length;
    while (index <= lastIndex) {
      int i = 0;
      while (i < looking4length && value.charAt(index + i) == looking4.charAt(i)) {
        i++;
      }
      if (i == looking4length)
        return index;
      index++;
    }
    return -1;
  }

  public static byte[] parseBase64(final CharSequence in) {
    return DatatypeConverter.parseBase64Binary(in.toString());
  }

  public static CharSequence toBase64(final byte[] bytes) {
    return DatatypeConverter.printBase64Binary(bytes);
  }

  public static int count(CharSequence sequence, int off, int len, char ch) {
    if (len > sequence.length())
      throw new ArrayIndexOutOfBoundsException();
    int result = 0;
    while (off < len) {
      result += (sequence.charAt(off) == ch ? 1 : 0);
      off++;
    }
    return result;
  }

  public static CharSequence ppDouble(double v) {
    return prettyPrint.format(v);
  }

  public static Map<String, String> splitURLQuery(CharSequence urlQuery) {
    final Map<String, String> query_pairs = new LinkedHashMap<>();
    try {
      final CharSequence[] parts = split(urlQuery, '&');
      for (CharSequence part : parts) {
        int idx = indexOf(part, "=");
        query_pairs.put(URLDecoder.decode(part.subSequence(0, idx).toString(), "UTF-8"), URLDecoder.decode(part.subSequence(idx + 1, part.length()).toString(), "UTF-8"));
      }
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return query_pairs;
  }
}
