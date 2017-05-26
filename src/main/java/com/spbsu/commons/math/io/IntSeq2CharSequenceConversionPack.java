package com.spbsu.commons.math.io;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.IntSeq;
import com.spbsu.commons.seq.IntSeqBuilder;

import java.text.NumberFormat;
import java.util.stream.Collectors;

/**
 * Created by solar on 18.05.17.
 */
public class IntSeq2CharSequenceConversionPack implements Converter<IntSeq, CharSequence> {
    @Override
    public IntSeq convertFrom(final CharSequence source) {
        final IntSeqBuilder builder = new IntSeqBuilder();
        CharSeqTools.split(source, " ", false).mapToInt(CharSeqTools::parseInt).forEach(builder::append);
        return builder.build();
    }

    @Override
    public CharSequence convertTo(final IntSeq v) {
        final NumberFormat prettyPrint = MathTools.numberFormatter();
        return v.stream().mapToObj(Integer::toString).collect(Collectors.joining(" "));
    }
}
