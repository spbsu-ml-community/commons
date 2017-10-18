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
 * Created by vkokarev on 11.03.15.
 */
@SuppressWarnings("UnusedDeclaration")
public class HashMap2CharSequenceConverter implements Converter<HashMap, CharSequence>, ConversionDependant {
    private ConversionRepository repository;

    @SuppressWarnings("unchecked")
    @Override
    public HashMap convertFrom(final CharSequence source) {
        final HashMap result = new HashMap();
        final List<Pair> convert = repository.convert(source, List.class);
        for (int i = 0; i < convert.size(); i++) {
            final Pair pair = convert.get(i);
            result.put(pair.first, pair.second);
        }
        return result;
    }

    @Override
    public CharSequence convertTo(final HashMap data) {
        final List<Pair> temp = new ArrayList<>(data.size());

        for (Object o : data.entrySet()) {
            final Map.Entry entry = (Map.Entry) o;
            temp.add(Pair.create(entry.getKey(), entry.getValue()));
        }
        return repository.convert(temp, CharSequence.class);
    }

    @Override
    public void setConversionRepository(ConversionRepository repository) {
        this.repository = repository;
    }
}

