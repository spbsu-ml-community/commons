package com.spbsu.commons.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spbsu.commons.seq.CharSeqReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * User: solar
 * Date: 19.10.14
 * Time: 11:51
 */
public final class JSONTools {
  private static final ObjectMapper OBJECT_MAPPER;

  public static String escape(String symbol) {
    try {
      return OBJECT_MAPPER.writeValueAsString(symbol);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }

  private JSONTools() {
  }

  @NotNull
  public static JsonParser parseJSON(@NotNull final CharSequence part) throws IOException {
    return OBJECT_MAPPER.getFactory().createParser(new CharSeqReader(part));
  }
}
