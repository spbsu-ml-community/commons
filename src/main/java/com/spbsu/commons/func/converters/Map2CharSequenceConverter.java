package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.func.types.ConversionDependant;
import com.spbsu.commons.func.types.ConversionRepository;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.util.Pair;

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
    Map result = new HashMap();
    final List<Pair> convert = repository.convert(source, List.class);
    for (int i = 0; i < convert.size(); i++) {
      Pair pair = convert.get(i);
      result.put(pair.first, pair.second);
    }
    return result;
  }

  @Override
  public CharSequence convertTo(final Map data) {
    List<Pair> temp = new ArrayList<>(data.size());

    for (Object o : data.entrySet()) {
      Map.Entry entry = (Map.Entry)o;
      temp.add(Pair.create(entry.getKey(), entry.getValue()));
    }
    return repository.convert(temp, CharSequence.class);
  }

  @Override
  public void setConversionRepository(ConversionRepository repository) {
    this.repository = repository;
  }
}
