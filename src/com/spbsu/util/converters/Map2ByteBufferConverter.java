package com.spbsu.util.converters;

import com.spbsu.util.Converter;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * User: igorkuralenok
 * Date: 21.06.2009
 */
public class Map2ByteBufferConverter<K,V> implements Converter<Map<K,V>, ByteBuffer> {
  final Converter<K, ByteBuffer> keyConverter;
  final Converter<V, ByteBuffer> valuesConverter;

  public Map2ByteBufferConverter(Converter<K, ByteBuffer> keyConverter, Converter<V, ByteBuffer> valuesConverter) {
    this.valuesConverter = valuesConverter;
    this.keyConverter = keyConverter;
  }

  public Map<K,V> convertTo(ByteBuffer source) {
    Map<K,V> result = new HashMap<K,V>();
    final int size = ConverterUtil.restoreSize(source);
    for (int i = 0; i < size; i++) {
      result.put(keyConverter.convertTo(source), valuesConverter.convertTo(source));
    }
    return result;
  }

  public ByteBuffer convertFrom(Map<K, V> map) {
    final List<ByteBuffer> buffersArray = new LinkedList<ByteBuffer>();
    int totalSize = 0;
    for (final Map.Entry<K, V> entry : map.entrySet()) {
      final ByteBuffer key = keyConverter.convertFrom(entry.getKey());
      final ByteBuffer value = valuesConverter.convertFrom(entry.getValue());
      buffersArray.add(key);
      buffersArray.add(value);
      totalSize += key.remaining() + value.remaining();
    }
    final ByteBuffer buffer = ByteBuffer.allocate(totalSize + 4);
    ConverterUtil.storeSize(map.size(), buffer);

    for (ByteBuffer buff : buffersArray) {
      buffer.put(buff);
    }
    final int avaliable = buffer.position();
    buffer.rewind();
    buffer.limit(avaliable);
    return buffer;
  }
}
