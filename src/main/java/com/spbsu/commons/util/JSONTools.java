package com.spbsu.commons.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spbsu.commons.seq.CharBufferSeq;
import com.spbsu.commons.seq.CharSeqReader;

/**
 * User: solar
 * Date: 19.10.14
 * Time: 11:51
 */
public final class JSONTools {
  private static final ObjectMapper OBJECT_MAPPER;

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

  @NotNull
  public static JsonParser parseJSON(@NotNull final CharBufferSeq part) throws IOException {
    return OBJECT_MAPPER.getFactory().createParser(part.getReader());
  }
}
