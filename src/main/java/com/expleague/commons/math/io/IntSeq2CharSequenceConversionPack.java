package com.expleague.commons.math.io;

import com.expleague.commons.func.Converter;
import com.expleague.commons.math.MathTools;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.IntSeqBuilder;

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
