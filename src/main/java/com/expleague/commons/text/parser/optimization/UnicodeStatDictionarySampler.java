package com.expleague.commons.text.parser.optimization;

import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.parser.UnicodeDictionary;
import com.expleague.commons.text.parser.UnicodeStatDictionary;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.IntStream;

import static java.lang.Math.exp;
import static java.lang.Math.log;

@SuppressWarnings({"WeakerAccess", "unused"})
public class UnicodeStatDictionarySampler {
    private static final double MULTIPLIER = 1e5;
    public static final double INDEPENDENCE_ALPHA = log(0.0001);
    private final double[][] parentsProb;
    private final UnicodeStatDictionary dict;

    @FunctionalInterface
    public interface VariantConsumer {
        void accept(IntSeq variant, double prob);
    }

    public UnicodeStatDictionarySampler(final UnicodeStatDictionary dict) {
        this.dict = dict;
        final int size = dict.size();
        this.parentsProb = new double[size][];
        final AtomicLongArray childrenSumAtomic = new AtomicLongArray(size);
        IntStream.range(UnicodeDictionary.COMPOSITES_START, size).parallel().forEach(idx -> {
            final IntSeq token = dict.get(idx);
            final double tokenProb = dict.prob(idx);
            int parent = dict.parent(idx);
            while (parent >= 0) {
                final IntSeq parentToken = dict.get(parent);
                childrenSumAtomic.addAndGet(parent, (long)(tokenProb * MULTIPLIER));
                parent = dict.parent(parent);
            }
        });
        final double[] childrenSum = new double[size];
        IntStream.range(0, size).parallel().forEach(idx -> childrenSum[idx] = childrenSumAtomic.get(idx) / MULTIPLIER);
        IntStream.range(UnicodeDictionary.COMPOSITES_START, size).parallel().forEach(idx -> {
            final IntSeq token = dict.get(idx);
            final TDoubleList parentsProb = new TDoubleArrayList();
            final double tokenProb = dict.prob(idx);
            parentsProb.add(tokenProb);
            double parentsSum = tokenProb;
            int parent = dict.parent(idx);
            while (parent >= 0) {
                final IntSeq parentToken = dict.get(parent);
                final IntSeq suffix = token.sub(parentToken.length(), token.length());
                final double suffixProb = modelIndependence(suffix, tokenProb, childrenSum);
                final double parentProb = dict.prob(parent) * suffixProb;
                parentsProb.add(parentProb);
                parentsSum += parentProb;
                parent = dict.parent(parent);
            }
            final double finalParentsSum = parentsSum;
            parentsProb.transformValues(val -> val / finalParentsSum);
            this.parentsProb[idx] = parentsProb.toArray();
        });
    }

    public UnicodeDictionary dictionary() {
        return dict;
    }

    public IntSeq sample(final FastRandom rnd, final CharSeq characters) {
        return sample(rnd, CharSeqTools.transformToCodePoints(characters));
    }

    public IntSeq sample(final FastRandom rng, final IntSeq seq) {
        int pos = 0;
        final int[] result = new int[seq.length()];
        int count = 0;
        while (pos < seq.length()) {
            int nextToken = dict.search(seq.sub(pos, seq.length()));
            int parent = dict.parent(nextToken);
            double rand = rng.nextDouble();
            final double[] parentsProb = this.parentsProb[nextToken];
            if (parentsProb != null) {
                int depth = 0;
                while (rand - parentsProb[depth] > 0 && parent >= 0) {
                    rand -= parentsProb[depth];
                    nextToken = parent;
                    parent = dict.parent(nextToken);
                    depth++;
                }
            }
            result[count++] = nextToken;
            pos += dict.get(nextToken).length();
        }
        return new IntSeq(result, 0, count);
    }

    public double klTermE(final IntSeq token, final double count, final double power, final boolean surplus) {
        if (token.length() <= 1) {
            return 0.;
        }
        final double tokenProb = count / power;
        final double independentProb = modelIndependence(token, surplus ? tokenProb : 0, null);
        final double lambda = independentProb * power;
        if (lambda > count || count == 1) { // count is less than expected from independence assumption
            return 0.;
        }
        final double logPoissonProb = count * log(lambda) - lambda - logGamma(count);
        if (logPoissonProb > INDEPENDENCE_ALPHA) { // can not reject independence assumption
            return 0.;
        }
        return tokenProb * log(tokenProb / independentProb);
    }

    public double modelIndependence(final IntSeq token, final double probSurplus, final double[] childrenSum) {
        if (token.length() == 0) {
            return 1.;
        }
        final int tokenId = dict.contains(token) ? dict.search(token) : -1;
        final boolean allowFullMatch = childrenSum != null;
        final double tokenProb = tokenId >= 0 ? dict.prob(tokenId) + probSurplus : probSurplus;
        double totalProb = allowFullMatch && tokenId >= 0 ? tokenProb + childrenSum[tokenId] : 0;
        if (token.length() > 1) {
            final VariantsIterator variantsIterator = new VariantsIterator(token);
            while (variantsIterator.next()) {
                double lastLogProBab = variantsIterator.logProbability(probSurplus);
                if (childrenSum != null) {
                    final int lastTokenInVariant = variantsIterator.variant().last();
                    if (lastTokenInVariant >= 0) {
                        lastLogProBab += log(childrenSum[lastTokenInVariant]);
                    }
                }
                totalProb += exp(lastLogProBab);
            }
        }
        return totalProb;
    }

    public static double logGamma(final double xx) {
        final double tmp = (xx - 0.5) * Math.log(xx + 4.5) - (xx + 4.5);
        final double ser = 1.0 + 76.18009173 / (xx + 0) - 86.50532033 / (xx + 1)
            + 24.01409822 / (xx + 2) - 1.231739516 / (xx + 3)
            + 0.00120858003 / (xx + 4) - 0.00000536382 / (xx + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    private class VariantsIterator {
        public static final double VARIANTS_LOG_THRESHOLD = -20.;
        private final IntSeq input;
        private double minProbab;
        private final int[] variant;
        private final double[] bestSuffixSplitLogProb;
        private final int len;
        private double logVarProbab;
        private int depth;
        private int pos;
        private long variantsCount = 0;

        private VariantsIterator(final IntSeq input) {
            this.input = input;
            this.len = input.length();
            this.variant = new int[len];
            this.pos = -1;
            this.depth = 0;
            bestSuffixSplitLogProb = new double[len];
            for (int i = 0; i < len; i++) {
                final IntSeq suffix = input.sub(i, input.length());
                bestSuffixSplitLogProb[i] = dict.parse(suffix, tokenId -> dict.get(tokenId).length() < input.length())
                    .stream().mapToDouble(dict::logProb).sum();
            }
            this.minProbab = bestSuffixSplitLogProb[0] + VARIANTS_LOG_THRESHOLD;
        }

        public boolean next() {
            if (pos < 0) {
                pos = 0;
                while (!parseToEnd()) {
                    if (!next()) { // no variants
                        return false;
                    }
                }
                variantsCount++;
                return true;
            }
            boolean hasVariant;
            do {
                do {
                    depth--;
                    if (depth < 0) {
                        return false;
                    }
                    pos -= dict.get(variant[depth]).length();
                    logVarProbab -= dict.logProb(variant[depth]);
                    variant[depth] = dict.parent(variant[depth]);
                } while(variant[depth] < 0);
                pos += dict.get(variant[depth]).length();
                logVarProbab += dict.logProb(variant[depth]);
                depth++;
            } while (earlyStop() || !parseToEnd());
            if (++variantsCount % 100_000 == 0) {
                this.minProbab += log(2);
            }
            return true;
        }

        private boolean parseToEnd() {
            while (pos < input.length()) {
                int token = dict.search(input.sub(pos, len));
                int tokLen = dict.get(token).length();
                if (pos == 0 && tokLen == len && len > 1) {
                    token = dict.parent(token);
                    tokLen = dict.get(token).length();
                }
                variant[depth++] = token;
                pos += tokLen;
                logVarProbab += dict.logProb(token);
                if (earlyStop()) {
                    return false;
                }
            }
            return true;
        }

        private boolean earlyStop() {
            return logVarProbab + (pos < len ? bestSuffixSplitLogProb[pos] : 0) < minProbab;
        }

        double logProbability(final double probSurplus) {
            double result = 0;
            for (int i = 0; i < depth; i++) {
                result += dict.logProb(variant[i]);
            }
            return result / (1 - probSurplus);
        }

        private IntSeq variant() {
            return new IntSeq(variant, 0, depth);
        }
    }
}
