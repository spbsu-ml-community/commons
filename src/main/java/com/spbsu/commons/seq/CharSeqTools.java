package com.spbsu.commons.seq;


import com.spbsu.commons.func.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;


import com.spbsu.commons.func.Processor;
import com.spbsu.commons.seq.trash.FloatingDecimal;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.strategy.HashingStrategy;

import javax.xml.bind.DatatypeConverter;

/**
 * User: terry
 * Date: 10.10.2009
 * Time: 23:26:28
 */
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

  public static CharSequence[] split(final CharSequence sequence, final char separator) {
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

  public static CharSequence[] split(final CharSequence sequence, final char separator, final CharSequence[] result) {
    final int index = trySplit(sequence, separator, result);
    if (index < result.length)
      throw new IllegalArgumentException("Too little parts found in input");
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

  public static boolean isImmutable(final CharSequence next) {
    if(next instanceof String)
      return true;
    if (next instanceof CharSeq)
      return ((CharSeq)next).isImmutable();
    return false;
  }

  public static List<CharSequence> discloseComposites(final List<CharSequence> fragments) {
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

  private static long mapNextLine(final Reader reader, final List<CharBuffer> buffers, final int bufferSize) throws IOException {
    char[] buffer = new char[bufferSize];
    int letter;
    long read = 0;
    int position = -1;
    while ((letter = reader.read()) >= 0) {
      ++read;
      ++position;
      if (letter == '\n') {
        buffers.add(CharBuffer.wrap(buffer, 0, position));
        return read;
      } else if (read % bufferSize == 0) {
        buffers.add(CharBuffer.wrap(buffer, 0, position));
        buffer = new char[bufferSize];
        position = 0;
      }
      buffer[position] = (char) letter;
    }
    if (position >= 0)
      buffers.add(CharBuffer.wrap(buffer, 0, position));
    return read;
  }

  private static long mapNextLine(final FileChannel fc, final List<CharBuffer> buffers, final Charset charset, final long startOffset, final long bufferSize) throws IOException {
    final long channelSize = fc.size();
    if (startOffset >= channelSize)
      return -1;
    long totalOffset = startOffset;
    long currentOffset = 0;
    MappedByteBuffer lastMappedMem = null;
    for(;;) {
      if (lastMappedMem != null) {
        lastMappedMem.clear();
        buffers.add(charset.decode(lastMappedMem.asReadOnlyBuffer()));
        totalOffset += currentOffset;
        currentOffset = 0;
      }
      if (totalOffset < channelSize) {
        lastMappedMem = fc.map(FileChannel.MapMode.READ_ONLY, totalOffset, (bufferSize < channelSize - totalOffset) ? bufferSize : channelSize - totalOffset);
        totalOffset += currentOffset;
        currentOffset = 0;
        while (lastMappedMem.hasRemaining()) {
          if (lastMappedMem.get() == '\n') {
            buffers.add(charset.decode(fc.map(FileChannel.MapMode.READ_ONLY, totalOffset, currentOffset)));
            return totalOffset + currentOffset - startOffset + 1;
          }
          ++currentOffset;
        }
      } else {
        return totalOffset - startOffset;
      }
    }
  }

  public static void processAndSplitLinesNIO(@NotNull final Reader in, @NotNull final Processor<CharBufferSeq[]> seqProcessor, @Nullable final String delimeters, final int splitDepth) throws IOException {
      for (;;) {
        final List<CharBuffer> buffers = new ArrayList<>();
        final long read = mapNextLine(in, buffers, 1 << 10);
        if (read > 0) {
          final CharBufferSeq cbs = new CharBufferSeq(buffers);
          final CharBufferSeq.Tokenizer tokenizer = cbs.getTokenizer(delimeters);
          final List<CharBufferSeq> result = new ArrayList<>();
          long len = 0;
          for (int i = 0; i < splitDepth; ++i) {
            if (tokenizer.hasMoreElements()) {
              final CharSequence value = tokenizer.nextElement();
              len += value.length();
              result.add(new CharBufferSeq(value));
            }
          }
          final CharBufferSeq value = new CharBufferSeq(cbs, (int) len + result.size());
          if (value.commonSize() > 0) {
            result.add(value);
          }
          seqProcessor.process(result.toArray(new CharBufferSeq[result.size()]));
        } else {
          return;
        }
      }
  }

  public static void processAndSplitLines(@NotNull final Reader in, @NotNull final Processor<CharSequence[]> seqProcessor, @Nullable final String delimeters, final boolean trim) throws IOException {
    final char[] buffer = new char[4096*4];
    final List<CharSequence> parts = new ArrayList<>();
    CharSeqBuilder line = new CharSeqBuilder();
    int read;
    try (Reader input = in) {
      boolean skipCaretReturn = false;
      while ((read = input.read(buffer)) >= 0) {
        int offset = 0;
        int index = 0;

        while (index < read) {
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
  }

  public static void processLines(final Reader input, final Processor<CharSequence> seqProcessor) throws IOException {
    processAndSplitLines(input, new Processor<CharSequence[]>() {
      @Override
      public void process(final CharSequence[] arg) {
        seqProcessor.process(arg[0]);
      }
    }, null, false);
  }

  public static void processLines(final Reader input, final Action<CharSequence> seqProcessor) throws IOException {
    processAndSplitLines(input, new Processor<CharSequence[]>() {
      @Override
      public void process(final CharSequence[] arg) {
        seqProcessor.invoke(arg[0]);
      }
    }, null, false);
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
      public Class<T> elementType() {
        return componentType;
      }
    };
  }

  public static <T extends Comparable<T>> Comparator<Seq<T>> lexicographicalComparator(final Class<T> clazz){
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
      if(suffix.charAt(sLength - index - 1) != seq.charAt(sLength - index - 1))
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
    if (sequence instanceof String) {
      int last = 0;
      int index = 0;
      for (int i = 0; i < sequence.length() && index < result.length - 1; i++) {
        if (sequence.charAt(i) == separator) {
          result[index++] = new CharSeqAdapter(sequence, last, i);
          last = i + 1;
        }
      }
      result[index++] = new CharSeqAdapter(sequence, last, sequence.length());
      return index;
    }
    else {
      int last = 0;
      int index = 0;
      for (int i = 0; i < sequence.length() && index < result.length - 1; i++) {
        if (sequence.charAt(i) == separator) {
          result[index++] = sequence.subSequence(last, i);
          last = i + 1;
        }
      }
      result[index++] = sequence.subSequence(last, sequence.length());
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
}
