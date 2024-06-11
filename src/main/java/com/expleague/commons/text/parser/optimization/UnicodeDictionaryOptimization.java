package com.expleague.commons.text.parser.optimization;

import com.expleague.commons.func.impl.WeakListenerHolderImpl;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.CharSeqChar;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.parser.UnicodeDictionary;
import com.expleague.commons.text.parser.UnicodeStatDictionary;
import com.expleague.commons.util.Basket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.log;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 04.06.12
 * Time: 18:23
 */
@SuppressWarnings("unused")
public class UnicodeDictionaryOptimization extends WeakListenerHolderImpl<UnicodeStatDictionary> {
    private static final Logger LOG = LoggerFactory.getLogger(UnicodeDictionaryOptimization.class);
    private static final int CLEANUP_PERIOD = 10;
    private static final int TERM_STATS_TTL = 10;

    private final int iterationSize;
    private final int targetSize;
    private final Map<IntSeq, TermStats> accumulatedStatistics = new HashMap<>();
    private final String name;
    private UnicodeStatDictionary model;
    private UnicodeStatDictionarySampler sampler;
    private long totalObserved = 0;

    public UnicodeDictionaryOptimization(final int size) {
        this("", size, 10000);
    }

    public UnicodeDictionaryOptimization(final int size, final int iterationSize) {
        this("", size, iterationSize);
    }

    public UnicodeDictionaryOptimization(final String name, final int size, final int iterationSize) {
        this.name = name;
        this.iterationSize = iterationSize;
        this.targetSize = size;
    }

    private record ScoredToken(IntSeq token, double score, double count, boolean isPair) {}

    public UnicodeStatDictionary train(final Iterator<List<CharSequence>> data) {
        model = new UnicodeStatDictionary();
        int equalCounter = 0;

        double bestCompression = 1;
        int stepsWithoutEnhancement = 0;
        int effectiveIteration = iterationSize;
        int iteration = 0;
        while (data.hasNext()) {
            sampler = new UnicodeStatDictionarySampler(model);
            final Stats stats = observeStatistics(data, sampler, effectiveIteration, new FastRandom());
            final Basket candidates = new Basket(10000);
            {
                final double compressionRate;
                {
                    double textLength = 0;
                    for (int i = 0; i < model.size(); i++) {
                        final IntSeq seq = model.get(i);
                        final int tokenUTF8Len = seq.stream().map(cp -> 1
                            + (Integer.numberOfLeadingZeros(cp) <= 24 ? 1 : 0)
                            + (Integer.numberOfLeadingZeros(cp) <= 16 ? 1 : 0)
                            + (Integer.numberOfLeadingZeros(cp) <= 8 ? 1 : 0)
                        ).sum();
                        textLength += tokenUTF8Len * stats.freq(i);
                    }

                    final double codeLength = stats.codeLength();
                    if (codeLength < 0) {
                        LOG.warn("Negative code length!");
                        compressionRate = 1;
                    } else if (textLength < 0) {
                        LOG.warn("Negative text length!");
                        compressionRate = 1;
                    } else {
                        compressionRate = codeLength / textLength;
                    }
                }

                if (bestCompression > compressionRate) {
                    bestCompression = compressionRate;
                    stepsWithoutEnhancement = 0;
                } else if (++stepsWithoutEnhancement > 3) {
                    effectiveIteration = (int)(effectiveIteration * 1.05);
                    stepsWithoutEnhancement = 0;
                    LOG.info("{} iteration size expanded to {}", name, effectiveIteration);
                }
                LOG.info(
                    "{} iteration: {} size: {} rate: {} sample power: {}",
                    name,
                    iteration,
                    model.composites().size(),
                    compressionRate,
                    stats.power()
                );
            }
            invoke(model);

            stats.forEachToken((tokenId, count) -> {
                final IntSeq token = model.get(tokenId);
                if (UnicodeDictionary.isComposite(tokenId)) {
                    final double score = sampler.klTermE(model.get(tokenId), count, stats.power(), true);
                    if (score > 0.0) {
                        candidates.append(new ScoredToken(token, -score, count, false));
                    }
                }
            }, true);
            final int finalIteration = iteration;
            { // expand and update stats
                stats.forEachPair((token, count) -> {
                    if (model.contains(token)) {
                        return;
                    }
                    final double score = sampler.klTermE(token, count, stats.power(), false);
                    if (score > 0.) {
                        candidates.append(new ScoredToken(token, -score, count, true));
                    }
                });
            }

            { // update stats for tokens
                stats.forEachToken((tokenId, count) ->
                    accumulatedStatistics.computeIfAbsent(model.get(tokenId), TermStats::new)
                        .update(finalIteration, count, stats.power()), false);
            }
            if (iteration % CLEANUP_PERIOD == 0){ // cleanup statistics
                final List<IntSeq> values = new ArrayList<>(accumulatedStatistics.keySet());
                values.forEach(key -> {
                    final TermStats termStats = accumulatedStatistics.get(key);
                    if (termStats.lastUpdated() < finalIteration - TERM_STATS_TTL) {
                        accumulatedStatistics.remove(key);
                    }
                });
                totalObserved += (long) stats.power();
            }

            { // generating next model version
                final List<IntSeq> dictComposites = candidates.<ScoredToken>stream()
                    .sorted(Comparator.comparingDouble(ScoredToken::score))
                    .limit(targetSize)
                    .peek(st -> {
                        if (st.isPair()) {
                            accumulatedStatistics.computeIfAbsent(st.token(), TermStats::new)
                                .update(finalIteration, st.count(), stats.power());
                        }
                    })
                    .map(ScoredToken::token)
                    .sorted(CharSeqTools.lexicographicalComparator(Integer.class))
                    .toList();
                if (dictComposites.equals(model.composites())) { // no changes found, early stop?
                    if (equalCounter++ > 2) {
                        LOG.info("Early stop: {} repetitions found", equalCounter);
                        break;
                    }
                } else {
                    equalCounter = 0;
                }
                model = new UnicodeStatDictionary(
                    token -> {
                        final TermStats termStats = accumulatedStatistics.get(token);
                        return termStats != null ? termStats.p() : 0.f;
                    },
                    dictComposites.toArray(IntSeq[]::new)
                );
            }
            iteration++;
        }

        return model;
    }

    private static Stats observeStatistics(
        final Iterator<List<CharSequence>> data,
        final UnicodeStatDictionarySampler sampler,
        final int count,
        final FastRandom rng
    ) {
        final int processors = Runtime.getRuntime().availableProcessors();
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(processors * 3);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                processors,
                processors,
                1,
                TimeUnit.SECONDS,
                queue
        );
        executor.prestartAllCoreThreads();
        final Stats stats = new Stats(sampler.dictionary());
        try {
            int counter = 0;
            while (data.hasNext() && counter < count) {
                final List<? extends CharSequence> batch = data.next();
                if (executor.isTerminating())
                    break;

                queue.put(() -> {
                    for (final CharSequence item : batch) {
                        final IntSeq tokens = sampler.sample(rng, CharSeqTools.transformToCodePoints(item));
                        final int len = tokens.length();
                        int prevToken = -1;
                        for (int j = 0; j < len; j++) {
                            final int token = tokens.intAt(j);
                            stats.accept(prevToken, token, 1.f);
                            prevToken = token;
                        }
                    }
                });
                counter += batch.size();
            }
            executor.shutdown();
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(1, TimeUnit.MINUTES);
            return stats;
        } catch (final InterruptedException ee) {
            throw new RuntimeException(ee);
        }
    }

    public TermStats statistics(final CharSequence token) {
        return accumulatedStatistics.get(CharSeqTools.transformToCodePoints(token));
    }

    public TermStats statistics(final IntSeq token) {
        return accumulatedStatistics.get(token);
    }

    @SuppressWarnings("WeakerAccess")
    public class TermStats {
        private final IntSeq token;
        private double count = 0;
        private double power = 0;
        private int lastUpdated = 0;

        private TermStats(final IntSeq token) {
            this.token = token;
        }

        public double p() {
            return count / power;
        }


        public void update(final int iteration, final double count, final double power) {
            this.count += count;
            this.power += power;
            lastUpdated = iteration;
        }

        @Override
        public String toString() {
            final String str = token.stream()
                .mapToObj(ch -> new CharSeqChar((char) ch))
                .collect(Collectors.joining());
            return str + " score: " + sampler.klTermE(token, count, totalObserved, true);
        }

        private int lastUpdated() {
            return lastUpdated;
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class Stats {
        private static final double MULTIPLIER = 1e5;
        private final AtomicLongArray tokens;
        private final ConcurrentHashMap<IntSeq, AtomicLong> pairs = new ConcurrentHashMap<>();
        private final AtomicLong power = new AtomicLong();
        private final UnicodeDictionary dictionary;

        private Stats(final UnicodeDictionary dictionary) {
            this.dictionary = dictionary;
            final int size = dictionary.size();
            this.tokens = new AtomicLongArray(size);
        }

        public void accept(final int prevToken, final int token, final float weightOrig) {
            long weight = (long)(weightOrig * MULTIPLIER);
            tokens.addAndGet(token, weight);
            power.addAndGet(weight);
            if (prevToken >= 0) {
                final IntSeq pairSeq = CharSeqTools.concat(dictionary.get(prevToken), dictionary.get(token));
                pairs.computeIfAbsent(pairSeq, seq -> new AtomicLong(0)).addAndGet(weight);
            }
        }
        public void forEachToken(final TokenStatConsumer consumer, final boolean parallel) {
            final IntStream stream;
            if (parallel) {
                stream = IntStream.range(0, tokens.length()).parallel();
            } else {
                stream = IntStream.range(0, tokens.length());
            }
            stream.forEach(idx -> {
                final double freq = tokens.get(idx) / MULTIPLIER;
                if (freq > 0) {
                    consumer.accept(idx, freq);
                }
            });
        }

        public void forEachPair(final PairStatConsumer consumer) {
            pairs.entrySet().stream().parallel()
                .forEach(entry -> consumer.accept(entry.getKey(), entry.getValue().get() / MULTIPLIER));
        }

        private double power() {
            return power.get() / MULTIPLIER;
        }

        public double freq(final int tokenId) {
            return tokens.get(tokenId) / MULTIPLIER;
        }

        public double codeLength() {
            double sum = 0;
            int nzCount = 0;
            for (int i = 0; i < tokens.length(); i++) {
                final double freq = freq(i);
                if (freq > 0) {
                    sum -= freq * log(1 + freq);
                    nzCount++;
                }
            }
            return (sum + (power() + nzCount) * log(power() + nzCount)) / log(2) / 8;
        }

        @FunctionalInterface
        interface TokenStatConsumer {
            void accept(int token, double count);
        }

        @FunctionalInterface
        interface PairStatConsumer {
            void accept(IntSeq token, double count);
        }
    }
}
