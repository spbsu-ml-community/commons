package com.expleague.commons.text.charset;

import com.expleague.commons.text.charset.bigram.BigramsTextAnalyzer;
import com.expleague.commons.text.charset.bigram.BigramsTextDecoder;
import com.expleague.commons.text.charset.bigram.CharFilter;
import com.expleague.commons.text.charset.bigram.BigramsTable;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.Charset;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: terry
 * Date: 14.10.2009
 * Time: 13:11:15
 * To change this template use File | Settings | File Templates.
 */
public class TextDecoderTools {
    private static final Logger LOG = LoggerFactory.getLogger(TextDecoderTools.class);

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
    final InputStream is = TextDecoderTools.class.getResourceAsStream("/com/expleague/commons/text/charset/bigram/CYRILLIC_CASE_INSENSITIVE.properties");
    final Properties properties = new Properties();
    try {
      properties.load(is);
    } catch (IOException e) {
      LOG.error("", e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        LOG.error("", e);
      }
    }
    final BigramsTable cyrBigramsTable = BigramsTable.create(properties);
    final BigramsTextAnalyzer textAnalyzer = new BigramsTextAnalyzer();
    textAnalyzer.setIgnoreCase(true);
    textAnalyzer.setCharFilter(CharFilter.NOT_ASCII_FILTER);
    textAnalyzer.setMaxAnalysisLength(500);
    final List<Charset> cyrillicCharsets = new ArrayList<Charset>();
    for (final String charset : CYRILLIC_CHARSETS) {
      cyrillicCharsets.add(Charset.forName(charset));
    }
    cyrillicTextDecoder = new BigramsTextDecoder(cyrBigramsTable, cyrillicCharsets);
  }

  public static CharSequence decodeCyrillicText(@NotNull final byte[] bytes) {
    return cyrillicTextDecoder.decodeText(bytes);
  }

  public static CharSequence decodeCyrillicText(@NotNull final CharSequence text) {
    return cyrillicTextDecoder.decodeText(text);
  }

  public static byte[] getBytes(final CharSequence sequence, final String encName) {
    try {
      return sequence.toString().getBytes(encName);
    }
    catch (UnsupportedEncodingException e) {
      LOG.error("", e);
    }
    return null;
  }
}
