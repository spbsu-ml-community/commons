package com.spbsu.commons.util;

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
public class JSONTools {
  public static JsonParser parseJSON(final CharSequence part) throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.getFactory().enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    return objectMapper.getFactory().createParser(new CharSeqReader(part));
  }

  public static JsonParser parseJSON(final CharBufferSeq part) throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.getFactory().enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    return objectMapper.getFactory().createParser(part.getReader());
  }
}
