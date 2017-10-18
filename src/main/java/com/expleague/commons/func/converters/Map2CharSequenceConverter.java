package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;
import com.expleague.commons.func.types.ConversionDependant;
import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shutovich
 * Date: 07.12.12
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("UnusedDeclaration")
public class Map2CharSequenceConverter implements Converter<Map, CharSequence>, ConversionDependant {
  private ConversionRepository repository;

  @SuppressWarnings("unchecked")
  @Override
  public Map convertFrom(final CharSequence source) {
    final Map result = new HashMap();
    final List<Pair> convert = repository.convert(source, List.class);
    for (int i = 0; i < convert.size(); i++) {
      final Pair pair = convert.get(i);
      result.put(pair.first, pair.second);
    }
    return result;
  }

  @Override
  public CharSequence convertTo(final Map data) {
    final List<Pair> temp = new ArrayList<>(data.size());

    for (Object o : data.entrySet()) {
      final Map.Entry entry = (Map.Entry)o;
      temp.add(Pair.create(entry.getKey(), entry.getValue()));
    }
    return repository.convert(temp, CharSequence.class);
  }

  @Override
  public void setConversionRepository(ConversionRepository repository) {
    this.repository = repository;
  }
}
