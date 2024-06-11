package com.expleague.commons.text.parser;

import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.Seq;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import static java.lang.Math.exp;
import static java.lang.Math.log;

@SuppressWarnings({"WeakerAccess", "unused"})
public class UnicodeStatDictionary extends UnicodeDictionary {
    public static final double UNKNOWN_SYMBOL_PROBABILITY = 1e-12;
    private final double[] logProb;

    @FunctionalInterface
    public interface StatProvider {
        double weight(IntSeq token);
    }

    public UnicodeStatDictionary() {
        this(tokenId -> 1. / COMPOSITES_START, new IntSeq[0]);
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public UnicodeStatDictionary(final StatProvider stat, final CharSequence... composites) {
        this(stat, Arrays.stream(composites).map(CharSeqTools::transformToCodePoints).toArray(IntSeq[]::new));
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public UnicodeStatDictionary(final StatProvider stat, final IntSeq... composites) {
        super(composites);
        final int size = size();
        this.logProb = new double[size];
        final double totalLogWeight = log(IntStream.range(0, size).parallel().mapToDouble(idx -> {
            final IntSeq token = get(idx);
            final double tokenWeight = Math.max(UNKNOWN_SYMBOL_PROBABILITY, stat.weight(token));
            this.logProb[idx] = log(tokenWeight);
            return tokenWeight;
        }).sum());
        IntStream.range(0, size).parallel().forEach(idx -> this.logProb[idx] -= totalLogWeight);
    }

    public double prob(final CharSequence seq) {
        return prob(CharSeqTools.transformToCodePoints(seq));
    }

    public double prob(final IntSeq seq) {
        final int found = search(seq);
        if (!seq.equals(get(found))) {
            return 0.;
        }
        return exp(logProb[found]);
    }

    public double prob(final int tokenId) {
        return exp(logProb[tokenId]);
    }

    @Override
    public IntSeq parse(final Seq<Integer> seq) {
        return parse((IntSeq)seq, (IntPredicate) null);
    }

    public void dump(final Writer to) throws IOException {
        for (int i = 0; i < size(); i++) {
            final IntSeq seq = get(i);
            final double freq = this.prob(i);
            if (freq < 1e-6) {
                continue;
            }
            try {
                final StringBuilder builder = new StringBuilder();
                seq.stream().forEach(builder::appendCodePoint);
                if (freq > 0) {
                    to.append(builder).append('\t').append(Double.toString(freq)).append('\n');
                }
            }
            catch (MalformedInputException mie) {
                mie.printStackTrace();
            }
        }
    }

    public IntSeq parse(final IntSeq seq, final @Nullable IntPredicate filter) {
        final int len = seq.length();
        final double[] score = new double[len + 1];
        Arrays.fill(score, Double.NEGATIVE_INFINITY);
        score[0] = 0;
        final int[] symbols = new int[len + 1];
        for (int pos = 0; pos < len; pos++) {
            final IntSeq suffix = seq.sub(pos, len);
            int sym = search(suffix);
            do {
                final int symLen = get(sym).length();
                final double symLogProb = this.logProb[sym];

                if (score[symLen + pos] < score[pos] + symLogProb && (filter == null || filter.test(sym))) {
                    score[symLen + pos] = score[pos] + symLogProb;
                    symbols[symLen + pos] = sym;
                }
            }
            while ((sym = parent(sym)) >= 0);
        }
        if (Double.isInfinite(score[len])) {
            return new IntSeq();
        }
        int pos = len;
        int index = 0;
        while (pos > 0) {
            final int sym = symbols[pos];
            symbols[len - (index++)] = sym;
            pos -= get(sym).length();
        }
        return new IntSeq(symbols, len - index + 1, len + 1);
    }

    public double logProb(final int tokenId) {
        return logProb[tokenId];
    }
}
