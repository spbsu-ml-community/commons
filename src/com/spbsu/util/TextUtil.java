package com.spbsu.util;

import com.spbsu.util.charset.TextDecoder;
import com.spbsu.util.charset.bigram.BigramsTable;
import com.spbsu.util.charset.bigram.BigramsTextAnalyzer;
import com.spbsu.util.charset.bigram.BigramsTextDecoder;
import com.spbsu.util.charset.bigram.CharFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author solar
 * @author lyadzhin
 */
public class TextUtil {
  private static final Logger log = Logger.create(TextUtil.class);

  public static final String UTF8 = "UTF8";
  public static final String UTF16 = "UTF-16";
  public static final String UTF16BE = "UTF-16BE";
  public static final String UTF16LE = "UTF-16LE";
  public static final String KOI8_R = "KOI8_R";
  public static final String CP866 = "Cp866";
  public static final String CP855 = "Cp855";
  public static final String WINDOWS_1251 = "windows-1251";
  public static final String MAC_CYRILLIC = "MacCyrillic";

  public static final String[] CYRILLIC_CHARSETS = {
          UTF8, KOI8_R, CP866, CP855, WINDOWS_1251, MAC_CYRILLIC
          //, UTF16, UTF16BE, UTF16LE
  };

  private static final TextDecoder cyrillicTextDecoder;

  static {
    final InputStream is = TextUtil.class.getResourceAsStream("charset/bigram/CYRILLIC_CASE_INSENSITIVE.properties");
    final Properties properties = new Properties();
    try {
      properties.load(is);
    } catch (IOException e) {
      log.error(e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        log.error(e);
      }
    }
    final BigramsTable cyrBigramsTable = BigramsTable.create(properties);
    final BigramsTextAnalyzer textAnalyzer = new BigramsTextAnalyzer();
    textAnalyzer.setIgnoreCase(true);
    textAnalyzer.setCharFilter(CharFilter.NOT_ASCII_FILTER);
    textAnalyzer.setMaxAnalysisLength(500);
    final List<Charset> cyrillicCharsets = new ArrayList<Charset>();
    for (String charset : CYRILLIC_CHARSETS) {
      cyrillicCharsets.add(Charset.forName(charset));
    }
    cyrillicTextDecoder = new BigramsTextDecoder(cyrBigramsTable, cyrillicCharsets);
  }

  public static CharSequence decodeCyrillicText(@NotNull byte[] bytes) {
    return cyrillicTextDecoder.decodeText(bytes);
  }

  public static CharSequence decodeCyrillicText(@NotNull CharSequence text) {
    return cyrillicTextDecoder.decodeText(text);
  }

  public static CharSequence removeIllegalXMLCharacters(@Nullable CharSequence charSequence) {
    if (charSequence == null) {
      return null;
    }
    final int length = charSequence.length();
    if (length == 0) {
      return "";
    }
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      final char c = charSequence.charAt(i);
      if (isLegalXMLCharacter(c)) {
        sb.append(c);
      }
    }
    return sb;
  }

  public static boolean isLegalXMLCharacter(char c) {
    return (c == 0x9) || (c == 0xA) || (c == 0xD) || (c >= 0x20 || c <= 0xD7FF) ||
            (c >= 0xE000 || c <= 0xFFFD) || (c >= 0x10000 || c <= 0x10FFFF);
  }

  public static boolean isWhitespace(char ch) {
    return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
  }

  public static boolean equals(CharSequence text, CharSequence text1) {
    if (text == text1) return true;
    final int length = text.length();
    if (length != text1.length()) return false;
    int index = 0;
    while (index < length) {
      if (text.charAt(index) != text1.charAt(index)) return false;
      index++;
    }
    return true;
  }

  public static byte[] getBytes(CharSequence sequence, String encName) {
    try {
      return sequence.toString().getBytes(encName);
    }
    catch (UnsupportedEncodingException e) {
      log.error(e);
    }
    return null;
  }

  public static double textDistanceByShingles(CharSequence one, CharSequence two) {
    final long[] shingles1 = getShingles(one, 10);
    final long[] shingles2 = getShingles(two, 10);
    for (int i = 0; i < shingles1.length; i++) {
      if (shingles1[i] != shingles2[i])
        return (1 - (i / 10.0));
    }
    return 0;
  }

  public static long[] getShingles(CharSequence text, int count) {
    final Holder<CharSequence> wordHolder = new Holder<CharSequence>();
    final Stack<Holder<Long>> shingles = new Stack<Holder<Long>>();
    final List<Pair<Long, Integer>> maxShingles = new LinkedList<Pair<Long, Integer>>();
    int position = 0;
    while (text.length() > 0) {
      text = nextWord(text, wordHolder);
      final long textHash = hash(wordHolder.value);
      for (Holder<Long> shingle : shingles) {
        shingle.setValue(shingle.value * 32 + textHash);
      }
      shingles.push(new Holder<Long>(textHash));
      if (shingles.size() >= count) {
        final Holder<Long> fullShingle = shingles.remove(0);
        final Long newShingle = fullShingle.value;

        final ListIterator<Pair<Long, Integer>> iterator = maxShingles.listIterator();
        boolean inserted = false;
        while (iterator.hasNext()) {
          Long shingle = iterator.next().getFirst();
          if (shingle > newShingle) {
            maxShingles.add(iterator.previousIndex(), new Pair<Long, Integer>(newShingle, position));
            inserted = true;
            break;
          }
        }
        if (!inserted) maxShingles.add(maxShingles.size(), new Pair<Long, Integer>(newShingle, position));
        if (maxShingles.size() > count) maxShingles.remove(0);
      }
      position++;
    }
    final long[] result = new long[count];
    int index = maxShingles.size();
    for (Pair<Long, Integer> shingle : maxShingles) {
      result[--index] = shingle.getFirst();
    }
    return result;
  }

  public static long hash(CharSequence seq) {
    long hash = 0;

    for (int i = 0; i < seq.length(); i++) {
      hash = 31 * hash + seq.charAt(i);
    }
    return hash;
  }

  public static CharSequence nextWord(CharSequence sequence, Holder<CharSequence> wordHolder) {
    int offset = 0;
    while (offset < sequence.length() && !Character.isLetterOrDigit(sequence.charAt(offset))) offset++;
    final int wordStart = offset;
    while (offset < sequence.length()
            && (sequence.charAt(offset) == '-'
            || sequence.charAt(offset) == '_'
            || Character.isLetterOrDigit(sequence.charAt(offset)))) offset++;
    wordHolder.setValue(sequence.subSequence(wordStart, offset));
    return sequence.subSequence(offset, sequence.length());
  }

  public static CharSequence toLowerCase(CharSequence word) {
    char[] result = null;
    final int length = word.length();
    for (int i = 0; i < length; i++) {
      if (Character.isUpperCase(word.charAt(i))) {
        result = new char[word.length()];
        break;
      }
    }
    if (result == null) return word;
    for (int i = 0; i < length; i++) {
      result[i] = Character.toLowerCase(word.charAt(i));
    }
    return new CharArrayCharSequence(result, 0, result.length);
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

  private TextUtil() {
  }

  public static int getWordCount(final CharSequence s) {
    if (s == null) return 0;

    int wordCount = 0;
    final Holder<CharSequence> wordHolder = new Holder<CharSequence>();
    for (CharSequence text = s; text.length() > 0; text = nextWord(text, wordHolder), wordCount++);
    return wordCount;
  }

  public static CharSequence[] getSentences(final CharSequence input) {
    if (input == null) return StringUtils.EMPTY;

    final ArrayList<CharSequence> sentences = new ArrayList<CharSequence>();
    final CharSequenceTokenizer tok = new CharSequenceTokenizer(input, ".!?", true);
    while (tok.hasMoreTokens()) {
      final StringBuilder builder = new StringBuilder();
      final CharSequence sentence = tok.nextToken();
      builder.append(sentence);
      if (tok.hasMoreTokens()) {
        final CharSequence delimeter = tok.nextToken();
        builder.append(delimeter);
      }
      sentences.add(builder);
    }
    return sentences.toArray(new CharSequence[sentences.size()]);
  }

  public static CharSequence join(String separator, String[] strings) {
    final StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (String string : strings) {
      if(!first)
        builder.append(separator);
      else
        first = false;
      builder.append(string);
    }
    return builder;
  }
}
