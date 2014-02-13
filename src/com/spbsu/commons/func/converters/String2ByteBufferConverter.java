package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.StreamTools;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 15:27:51
 */
public class String2ByteBufferConverter extends CharSequence2BufferConverter<String> {
  public String2ByteBufferConverter() {
    super(new StringCSFactory());
  }
}
