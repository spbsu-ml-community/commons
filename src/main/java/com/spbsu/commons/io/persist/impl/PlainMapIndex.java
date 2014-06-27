package com.spbsu.commons.io.persist.impl;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.func.converters.NioConverterTools;
import com.spbsu.commons.io.persist.MapIndex;
import com.spbsu.commons.io.persist.PageFile;
import com.spbsu.commons.io.persist.PageFileAddress;

import java.util.*;

/**
 * User: igorkuralenok
 * Date: 14.10.2009
 * Time: 15:10:44
 */
public class PlainMapIndex<K> implements MapIndex<K> {
  private final Map<K, PageFileAddress> addresses = new HashMap<K, PageFileAddress>();
  private final PageFile file;
  private final Converter<K, Buffer> keyConverter;

  public PlainMapIndex(PageFile file, Converter<K, Buffer> keyConverter) {
    this.file = file;
    this.keyConverter = keyConverter;
    if (file.size() > 1) {
      final PageFileAddress indexAddress = file.readHeader(0);
      final Buffer buffer = file.read(indexAddress);
      while (buffer.remaining() != 0) {
        int count = NioConverterTools.restoreSize(buffer);
        for (int i = 0; i < count; i++) {
          final K key = keyConverter.convertFrom(buffer);
          PageFileAddress valueAddress = PageFileAddress.CONVERTER.convertFrom(buffer);
          addresses.put(key, valueAddress);
        }
      }
    }
  }

  public PageFileAddress get(K key) {
    final PageFileAddress address = addresses.get(key);
    return address != null ? address : PageFileAddress.UNKNOWN;
  }

  public void set(K key, PageFileAddress address) {
    final PageFileAddress old = addresses.get(key);
    if (old != null)
      file.enqueueForCleanup(old);
    addresses.put(key, address);

  }

  public void flush() {
    final List<Buffer> buffers = new ArrayList<Buffer>(addresses.size() * 2);
    Buffer indexSizeBuffer = NioConverterTools.storeSize(addresses.size());
    buffers.add(indexSizeBuffer);
    for (Map.Entry<K, PageFileAddress> entry : addresses.entrySet()) {
      buffers.add(keyConverter.convertTo(entry.getKey()));
      buffers.add(PageFileAddress.CONVERTER.convertTo(entry.getValue()));
    }
    file.enqueueForCleanup(file.readHeader(0));
    file.writeHeader(0, file.write(BufferFactory.join(buffers.toArray(new Buffer[buffers.size()]))));
  }

  public int size() {
    return addresses.size();
  }

  @Override
  public Set<K> keySet() {
    return addresses.keySet();
  }
}
