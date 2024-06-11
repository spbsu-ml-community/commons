/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2022. All rights reserved.
 */

package com.expleague.commons.text.parser.optimization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.Text;
import com.expleague.commons.text.parser.TextParser;
import com.expleague.commons.text.parser.UnicodeDictionary;
import com.expleague.commons.text.parser.UnicodeStatDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("MissingJavadoc")
public final class TrainDictionary {
    private static final Logger LOG = LoggerFactory.getLogger(TrainDictionary.class);
    private static final int BATCH_SIZE = 100;

    private static class Parameters {
        @Parameter(names = "--texts", required = true, description = "Path to textual file where each line is considered as one text.")
        String textsPath;
        @Parameter(names = "--output-path", required = true, description = "Save dictionary to this path.")
        String saveTo;
        @Parameter(names = "--iteration-size", required = true, description = "The size of iteration while training dictionary.")
        int iterationSize;
        @Parameter(names = "--sample-size", required = true, description = "The size of iteration while training dictionary.")
        int sampleSize;
        @Parameter(names = "--entity-dict-size", description = "The size of entities' dictionary.")
        int entityDictSize = 50_000;
        @Parameter(names = "--alphabetic-dict-size", description = "The size of alphabetic dictionary.")
        int alphabeticDictSize = 200_000;
        @Parameter(names = "--ideographic-dict-size", description = "The size of ideographic dictionary.")
        int ideographicDictSize = 50_000;
        @Parameter(names = "--numeric-dict-size", description = "The size of numerics' dictionary.")
        int numericDictSize = 1_000;
    }

    public static void main(final String[] args) {
        final Parameters params = new Parameters();

        final Map<Text.Segment.Type, Integer> dictSizes = new HashMap<>();
        dictSizes.put(Text.Segment.Type.ENTITY, params.entityDictSize);
        dictSizes.put(Text.Segment.Type.ALPHABETIC, params.alphabeticDictSize);
        dictSizes.put(Text.Segment.Type.IDEOGRAPHIC, params.ideographicDictSize);
        dictSizes.put(Text.Segment.Type.NUMERIC, params.numericDictSize);

        final Map<Text.Segment.Type, Integer> iterationSizes = new HashMap<>();
        iterationSizes.put(Text.Segment.Type.ENTITY, 5);
        iterationSizes.put(Text.Segment.Type.ALPHABETIC, 10);
        iterationSizes.put(Text.Segment.Type.IDEOGRAPHIC, 1);
        iterationSizes.put(Text.Segment.Type.NUMERIC, 2);
        JCommander.newBuilder().args(args).addObject(params).build().parse();

        System.out.println("Dictionary training.");
        final long startTs = System.currentTimeMillis();
        final Map<Text.Segment.Type, TrainingBranch> segmentOptimizations = new HashMap<>();
        for (final Text.Segment.Type type : Text.Segment.Type.values()) {
            if (type.isIndexed()) {
                final UnicodeDictionaryOptimization optimization = new UnicodeDictionaryOptimization(
                    type.toString(),
                    dictSizes.get(type),
                    params.iterationSize * iterationSizes.get(type)
                );
                segmentOptimizations.put(type, new TrainingBranch(type, optimization, result -> {
                    try {
                        saveDict(params, result::prob, result, type);
                    } catch (final IOException exception) {
                        LOG.error("Exception during dictionary save", exception);
                    }
                }));
            }
        }

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10000);
        final int processors = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                Math.max(1, processors),
                Math.max(1, processors),
                1,
                TimeUnit.SECONDS,
                queue
        );
        executor.prestartAllCoreThreads();
        final FastRandom rnd = new FastRandom();
        final TextParser parser = new TextParser();
        sample(Paths.get(params.textsPath), params.iterationSize, params.sampleSize, rnd)
                .forEach(line -> {
                    try {
                        queue.put(() -> {
                            final Text parsedText = parser.parse(line);
                            final Text.Segment[] segments = parsedText.segments();
                            final List<ArrayList<CharSequence>> batches = Arrays.stream(Text.Segment.Type.values())
                                    .map(type -> new ArrayList<CharSequence>(BATCH_SIZE))
                                    .collect(Collectors.toList());
                            for (final Text.Segment segment : segments) {
                                if (!segment.type().isIndexed()) {
                                    continue;
                                }
                                final List<CharSequence> batchOfType = batches.get(segment.type().ordinal());
                                batchOfType.add(segment.normalizedText().text());
                                if (batchOfType.size() >= BATCH_SIZE) {
                                    batches.set(segment.type().ordinal(), new ArrayList<>(BATCH_SIZE));
                                    pushBatch(batchOfType, segmentOptimizations.get(segment.type()));
                                }
                            }
                            for (int i = 0; i < Text.Segment.Type.values().length; i++) {
                                final Text.Segment.Type type = Text.Segment.Type.values()[i];
                                final ArrayList<CharSequence> batch = batches.get(i);
                                if (!batch.isEmpty()) {
                                    pushBatch(batch, segmentOptimizations.get(type));
                                }
                            }
                        });
                    } catch (final InterruptedException ee) {
                        throw new RuntimeException(ee);
                    }
                });
        System.out.printf("Learning time [%d ms]\n", System.currentTimeMillis() - startTs);

        //---------------------------------------------------------------------------------
        System.out.print("Recalculate frequencies ");
        System.out.printf(" [%d ms]\n", System.currentTimeMillis() - startTs);
        System.out.println("Done.");
    }

    private static void pushBatch(final List<CharSequence> batchOfType, final TrainingBranch branch) {
        try {
            if (!branch.isComplete()) {
                branch.stream().offer(batchOfType, 10, TimeUnit.SECONDS);
            }
        } catch (final InterruptedException ee) {
            throw new RuntimeException(ee);
        }
    }

    record TrainingBranch(
        Text.Segment.Type type,
        UnicodeDictionaryOptimization optimization,
        ArrayBlockingQueue<List<CharSequence>> stream,
        CompletableFuture<UnicodeStatDictionary> result,
        Consumer<UnicodeStatDictionary> resultWriter
    ) {
        TrainingBranch(
            final Text.Segment.Type type,
            final UnicodeDictionaryOptimization optimization,
            final Consumer<UnicodeStatDictionary> resultWriter
        ) {
            this(type, optimization, new ArrayBlockingQueue<>(BATCH_SIZE), new CompletableFuture<>(), resultWriter);
            final Thread trainingThread = new Thread(this::train);
            trainingThread.setDaemon(true);
            trainingThread.setName("Training-lexer-" + type());
            trainingThread.start();
            optimization.addListener(resultWriter);
        }

        void train() {
            try {
                result.complete(optimization.train(new TrainIterator()));
                LOG.error("Training of {} successfully finished", type);
            } catch (final Throwable th) {
                LOG.error("Training of {} concluded with exception", type, th);
            }
        }

        private boolean isComplete() {
            return result.isDone();
        }

        private class TrainIterator implements Iterator<List<CharSequence>> {
            private boolean finished = false;
            private List<CharSequence> nextSegment;

            @Override
            public boolean hasNext() {
                if (finished) {
                    return false;
                }
                if (nextSegment != null) {
                    return true;
                }
                try {
                    nextSegment = stream().poll(1, TimeUnit.HOURS);
                    if (nextSegment == null) { // poison pill
                        finished = true;
                        return false;
                    }
                    return true;
                } catch (final InterruptedException ee) {
                    throw new RuntimeException(ee);
                }
            }

            @Override
            public List<CharSequence> next() {
                if (hasNext()) {
                    final List<CharSequence> result = nextSegment;
                    nextSegment = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
        }
    }

    private static void saveDict(
        final Parameters params,
        final ToDoubleFunction<IntSeq> freqs,
        final UnicodeDictionary dictionary,
        final Text.Segment.Type type
    ) throws IOException {
        try (final BufferedWriter out = Files.newBufferedWriter(Paths.get(params.saveTo, "." + type))) {
            for (int i = 0; i < dictionary.size(); i++) {
                final double freq = freqs.applyAsDouble(dictionary.get(i));
                final StringBuilder builder = new StringBuilder();
                dictionary.get(i).stream().forEach(builder::appendCodePoint);
                if (freq > 1e-10) {
                    out.write(builder + "\t" + freq + '\n');
                }
            }
        }
    }

    private static Stream<CharSeq> sample(final Path path, final int size, final int sampleSize, final FastRandom rng) {
        try {
            final List<FileSampler> samplers;
            if (Files.isDirectory(path)) {
                try (final Stream<Path> directoryStream = Files.list(path)) {
                    final List<Path> files = directoryStream.filter(Predicate.not(Files::isDirectory)).toList();
                    samplers = files.stream()
                        .map(file -> new FileSampler(file, size / files.size(), sampleSize / files.size(), rng))
                        .toList();
                }
            } else {
                samplers = List.of(new FileSampler(path, size, sampleSize, rng));
            }
            //noinspection unchecked
            final Iterator<CharSeq>[] samples = samplers.stream()
                .map(FileSampler::sample)
                .map(Stream::iterator)
                .toArray(Iterator[]::new);
            return IntStream.generate(() -> rng.nextInt(samples.length)).mapToObj(sno -> {
                if (samples[sno].hasNext()) {
                    return samples[sno].next();
                }
                return null;
            }).filter(Objects::nonNull);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class FileSampler {
        private static final ThreadGroup SAMPLERS_GROUP = new ThreadGroup("File samples");
        private final int samplesCount;
        private final Path path;
        private final int sampleSize;
        private final FastRandom rng;
        private volatile CharSeq[] samples;
        private volatile CharSeq[] nextChunk;
        private final AtomicInteger chunkOffset = new AtomicInteger();

        private FileSampler(final Path path, final int samplesCount, final int sampleSize, final FastRandom rng) {
            this.samplesCount = samplesCount;
            this.path = path;
            this.sampleSize = sampleSize;
            this.rng = rng;
            final Thread samplingThread = new Thread(SAMPLERS_GROUP, this::sampleWorker);
            samplingThread.setDaemon(true);
            samplingThread.setName("Sampling from " + path);
            samplingThread.start();
        }

        public Stream<CharSeq> sample() {
            nextChunk();
            return Stream.generate(() -> {
                while (true) {
                    final CharSeq[] chunk = nextChunk;
                    final int nextOff = chunkOffset.getAndIncrement();
                    if (nextOff < chunk.length) {
                        return chunk[nextOff];
                    }
                    nextChunk();
                }
            });
        }

        public synchronized void nextChunk() {
            try {
                while (samples == null) {
                    this.wait();
                }
                nextChunk = samples;
                chunkOffset.set(0);
                samples = null;
                this.notifyAll();
                LOG.info("Next sample {}", path);
            } catch (final InterruptedException ie) {
                LOG.warn("Interrupted while waiting for sample generation");
                throw new RuntimeException(ie);
            }
        }

        private void sampleWorker() {
            final CharSeq[] samples = new CharSeq[samplesCount];
            Instant progressTime = Instant.now();
            long read = 0;
            while (true) {
                long samplesObserved = 0;
                int counter = samplesCount;
                try (final BufferedReader reader = Files.newBufferedReader(path)) {
                    final Iterator<String> linesIt = reader.lines().iterator();
                    while (linesIt.hasNext()) {
                        final CharSeq next = CharSeq.create(linesIt.next());
                        read += next.length();
                        if (samplesObserved < samplesCount) {
                            samples[(int) samplesObserved++] = next;
                        } else {
                            samplesObserved++;
                            final int val = rng.nextInt((int) Math.min(sampleSize, samplesObserved));
                            if (val < samplesCount) {
                                samples[val] = next;
                            }
                        }
                        if (--counter <= 0) {
                            LOG.info("Sample from {} generated. {} characters read", path, read);
                            synchronized (this) {
                                this.samples = Arrays.copyOf(samples, samples.length);
                                this.notifyAll();
                                while (this.samples != null) {
                                    this.wait();
                                }
                            }
                            counter = samplesCount;
                            read = 0;
                        }
                        if (progressTime.until(Instant.now(), ChronoUnit.MINUTES) > 1) {
                            LOG.info(
                                "Sampling progress from: {} to go: {} read so far: {}",
                                path,
                                counter,
                                read
                            );
                            progressTime = Instant.now();
                        }
                    }
                } catch (final IOException ioe) {
                    LOG.error("Error during file sampling", ioe);
                    break;
                } catch (final InterruptedException ie) {
                    LOG.warn("Interrupted while waiting for sample consumption", ie);
                    break;
                }
            }
        }
    }
}
