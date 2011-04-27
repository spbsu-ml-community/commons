package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.converters.NioConverterTools;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vp
 */
public class Map2BufferConverter<K,V> implements Converter<Map<K,V>, Buffer> {
  final Converter<K, Buffer> keyConverter;
  final Converter<V, Buffer> valuesConverter;

  public Map2BufferConverter(Converter<K, Buffer> keyConverter, Converter<V, Buffer> valuesConverter) {
    this.valuesConverter = valuesConverter;
    this.keyConverter = keyConverter;
  }

  public Map<K,V> convertFrom(Buffer source) {
    Map<K,V> result = new HashMap<K,V>();
    final int size = NioConverterTools.restoreSize(source);
    for (int i = 0; i < size; i++) {
      result.put(keyConverter.convertFrom(source), valuesConverter.convertFrom(source));
    }
    return result;
  }

  public Buffer convertTo(final Map<K, V> map) {
    final Buffer[] buffersArray = new Buffer[map.size() * 2 + 1];
    int i = 0;
    for (final Map.Entry<K, V> entry : map.entrySet()) {
      final Buffer key = keyConverter.convertTo(entry.getKey());
      final Buffer value = valuesConverter.convertTo(entry.getValue());
      buffersArray[2 * i + 1] = key;
      buffersArray[2 * i + 2] = value;
      i++;
    }
    buffersArray[0] =NioConverterTools.storeSize(map.size());
    return BufferFactory.join(buffersArray);
  }
}
