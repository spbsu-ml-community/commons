package com.expleague.commons.text;

import com.expleague.commons.io.codec.seq.ListDictionary;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.Seq;
import com.expleague.commons.text.parser.UnicodeDictionary;
import com.expleague.commons.text.parser.UnicodeStatDictionary;
import com.expleague.commons.text.parser.optimization.UnicodeDictionaryOptimization;
import com.expleague.commons.text.parser.optimization.UnicodeStatDictionarySampler;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class UnicodeDictionaryOptimizationTest {
    @Test
    public void testVariantsVisitor() {
        final ListDictionary<Character> reference = new ListDictionary<Character>(
                CharSeq.create("a"),
                CharSeq.create("aa"),
                CharSeq.create("aaa")
        );

        final UnicodeStatDictionarySampler sampler = new UnicodeStatDictionarySampler(new UnicodeStatDictionary(
                token -> 1.,
                "aa",
                "aaa"
        ));
        final String sample = IntStream.range(0, 100).mapToObj(idx -> "a").collect(Collectors.joining());
        sampler.modelIndependence(CharSeqTools.transformToCodePoints(sample), 0, null);
    }

    @Test
    public void testIndependent() {
        final Random rnd = new FastRandom(100500);
        final UnicodeDictionaryOptimization de = new UnicodeDictionaryOptimization(100, 1000);
        final Stream<List<CharSequence>> rngSequences = Stream.generate(() -> {
            final int len = rnd.nextInt(300);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                builder.append((char) ('a' + rnd.nextInt('z' - 'a')));
            }
            return List.of(CharSeq.create(builder));
        });
        final UnicodeDictionary result = de.train(rngSequences.limit(20_000).iterator());
        Assert.assertEquals(0, result.composites().size());
    }

    @Test
    public void testSampling() {
        final FastRandom rnd = new FastRandom(100500);
        final TObjectFloatMap<IntSeq> freqs = new TObjectFloatHashMap<>();
        final IntSeq[] reference = Stream.of("a", "b", "aa", "aaa", "aaaa")
                .map(CharSeqTools::transformToCodePoints)
                .peek(token -> freqs.put(token, 1.f))
                .toArray(IntSeq[]::new);
        final UnicodeStatDictionary dictionary = new UnicodeStatDictionary(
                freqs::get,
                Arrays.stream(reference).filter(seq -> seq.length() > 1).toArray(IntSeq[]::new)
        );
        final UnicodeStatDictionarySampler sampler = new UnicodeStatDictionarySampler(dictionary);
        final Vec probabs = new ArrayVec(freqs.size());
        VecTools.fill(probabs, 1.);
        VecTools.normalizeL1(probabs);
        final Stream<CharSeq> seqStream = Stream.generate(() -> {
            final int len = rnd.nextInt(30);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                final int next = rnd.nextSimple(probabs, 1.);
                final IntSeq seq = reference[next];
                seq.stream().forEach(ch -> builder.append((char) ch));
            }
            return CharSeq.create(builder);
        });

        final TObjectFloatMap<IntSeq> result = new TObjectFloatHashMap<>();
        seqStream.limit(1000).forEach(seq -> {
//            final IntSeq codePoints = CharSeqTools.transformToCodePoints(seq);
//            System.out.println(seq);
//            dictionary.modelIndependence(codePoints, (variant, prob) -> {
//                variant.stream().forEach(tok -> result.adjustOrPutValue(dictionary.get(tok), prob, prob));
//            }, false);
            final IntSeq sample = sampler.sample(rnd, seq);
            sample.stream().forEach(tok -> result.adjustOrPutValue(dictionary.get(tok), 1, 1));
        });
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testRestoreRepetitions() {
        final ListDictionary<Character> reference = new ListDictionary<Character>(
                CharSeq.create("a"),
                CharSeq.create("aa"),
                CharSeq.create("aaa"),
                CharSeq.create("b")
        );

        final FastRandom rnd = new FastRandom();
        final UnicodeDictionaryOptimization de = new UnicodeDictionaryOptimization(2, 10000);
        final Vec probabs = new ArrayVec(reference.size());
        VecTools.fill(probabs, 1.);
        VecTools.normalizeL1(probabs);
        final Stream<List<CharSequence>> seqStream = Stream.generate(() -> {
            final int len = rnd.nextInt(30);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                builder.append(reference.condition(rnd.nextSimple(probabs)));
            }
            return List.of(CharSeq.create(builder));
        });
        final UnicodeDictionary result = de.train(seqStream.limit(100_000).iterator());
        result.composites().stream().map(de::statistics).forEach(System.out::println);
        Assert.assertEquals(1, result.composites().size());
    }


    @Test
    public void testRestore() {
        final ListDictionary<Character> reference = new ListDictionary<Character>(
                CharSeq.create("a"),
                CharSeq.create("b"),
                CharSeq.create("c"),
                CharSeq.create("cc"),
                CharSeq.create("aa"),
                CharSeq.create("bb")
        );

        final FastRandom rnd = new FastRandom(100500);
        final UnicodeDictionaryOptimization de = new UnicodeDictionaryOptimization(3, 10_000);
        final Vec probabs = new ArrayVec(reference.size());
        VecTools.fill(probabs, 1.);
        VecTools.normalizeL1(probabs);
        final Stream<List<CharSequence>> seqStream = Stream.generate(() -> {
            final int len = rnd.nextInt(30);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                builder.append(reference.condition(rnd.nextSimple(probabs)));
            }
            return List.of(CharSeq.create(builder));
        });
        final UnicodeDictionary result = de.train(seqStream.limit(100_000).iterator());
        Assert.assertEquals(3, result.composites().size());
    }

    @Test
    public void testRestoreAsym() {
        final ListDictionary<Character> reference = new ListDictionary<Character>(
                CharSeq.create("a"),
                CharSeq.create("b"),
                CharSeq.create("c"),
                CharSeq.create("cc"),
                CharSeq.create("ab"),
                CharSeq.create("bb")
        );

        final FastRandom rnd = new FastRandom(100500);
        final UnicodeDictionaryOptimization de = new UnicodeDictionaryOptimization(3, 10000);
        final Vec probabs = new ArrayVec(reference.size());
        VecTools.fill(probabs, 1.);
        VecTools.normalizeL1(probabs);
        final Stream<List<CharSequence>> seqStream = Stream.generate(() -> {
            final int len = rnd.nextInt(30);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                builder.append(reference.condition(rnd.nextSimple(probabs)));
            }
            return List.of(CharSeq.create(builder));
        });
        final UnicodeDictionary result = de.train(seqStream.limit(100_000).iterator());
        result.composites().stream().map(de::statistics).forEach(System.out::println);
        Assert.assertEquals(3, result.composites().size());
    }

    @Test
    public void testRestoreLong() {
        final ListDictionary<Character> reference = new ListDictionary<Character>(ArrayTools.map(
                new CharSequence[]{
                        "daba", "carac", "abaa", "bab",
                        "rabracadabra"
                },
                CharSeq.class, CharSeq::create
        ));

        final FastRandom rnd = new FastRandom();
        final UnicodeDictionaryOptimization de = new UnicodeDictionaryOptimization(5, 10000);
        final Vec probabs = new ArrayVec(reference.size());
        VecTools.fill(probabs, 1.);
        VecTools.normalizeL1(probabs);
        final Stream<List<CharSequence>> seqStream = Stream.generate(() -> {
            final int len = rnd.nextInt(30);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                builder.append(reference.condition(rnd.nextSimple(probabs)));
            }
            return List.of(CharSeq.create(builder));
        });
        final UnicodeDictionary result = de.train(seqStream.limit(100_000).iterator());
        result.composites().stream().map(de::statistics).forEach(System.out::println);
        Assert.assertEquals(5, result.composites().size());
    }

    @Test
//    @Ignore
    public void testRestoreRand() {
        final FastRandom rng = new FastRandom(100500);
        final Set<CharSeq> known = new HashSet<>();
        //noinspection unchecked
        final Seq<Character>[] models = IntStream.range(0, 1000)
                .mapToObj(ii -> CharSeq.create(rng.nextBase64String(rng.nextPoisson(5) + 1)))
                .filter(ss -> !known.contains(ss))
                .peek(known::add)
                .toArray(Seq[]::new);
        final ListDictionary<Character> reference = new ListDictionary<>(models);

        final FastRandom rnd = new FastRandom();
        final UnicodeDictionaryOptimization de = new UnicodeDictionaryOptimization(models.length * 2, 1_000);
        final Vec probabs = new ArrayVec(reference.size());
        VecTools.fill(probabs, 1.);
        VecTools.normalizeL1(probabs);
        final Stream<List<CharSequence>> seqStream = Stream.generate(() -> {
            final int len = rnd.nextInt(100);
            final StringBuilder builder = new StringBuilder(len);
            for (int c = 0; c < len; c++) {
                builder.append(reference.condition(rnd.nextSimple(probabs)));
            }
            return List.of(CharSeq.create(builder));
        });
        final UnicodeDictionary result = de.train(seqStream.limit(15_000).iterator());
        System.out.println(result.composites().size());
        final Set<IntSeq> resultSet = new HashSet<>(result.composites());
        int foundCounter = 0;
        for (final Seq<Character> model : models) {
            final IntSeq modelCodePoints = CharSeqTools.transformToCodePoints((CharSequence) model);
            foundCounter += resultSet.contains(modelCodePoints) ? 1 : 0;
        }
        Assert.assertTrue(foundCounter / ((double) models.length) > 0.8);
        System.out.println(foundCounter);
        System.out.println(models.length);
    }

    public static final int BATCH_SIZE = 1000;

    @Test
    public void testQueries() throws IOException {
        final UnicodeDictionaryOptimization optimization = new UnicodeDictionaryOptimization(50_000, 1_000_000);
        final String dir = "/Users/ikuralenok/Downloads/";
        final String[] files = new String[]{
                "part-00000-d12261b6-41ca-4c18-891d-2bec0cfb2501-c000.txt",
                "part-00001-d12261b6-41ca-4c18-891d-2bec0cfb2501-c000.txt"
        };

        final Stream<List<CharSequence>> data = StreamSupport.stream(new Spliterators.AbstractSpliterator<List<CharSequence>>(Long.MAX_VALUE, 0) {
            BufferedReader reader;
            int iteration = 0;
            @Override
            public boolean tryAdvance(Consumer<? super List<CharSequence>> action) {
                try {
                    if (reader == null)
                        reader = Files.newBufferedReader(Paths.get(dir, files[iteration++ % files.length]));
                    final List<CharSequence> batch = new ArrayList<>(BATCH_SIZE);
                    while (batch.size() < BATCH_SIZE) {
                        final String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        batch.add(line);
                    }
                    if (!batch.isEmpty()) {
                        action.accept(batch);
                        return true;
                    }
                } catch (IOException ignore) {
                }
                reader = null;
                return tryAdvance(action);
            }
        }, false);

        int[] iteration = new int[]{0};
        final Consumer<UnicodeStatDictionary> dictWriter = dict -> {
            try {
                if (++iteration[0] % 100 == 0) {
                    dict.dump(Files.newBufferedWriter(Paths.get("./", "output-" + iteration[0] + ".dict")));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        optimization.addListener(dictWriter);
        final UnicodeStatDictionary trainedDict = optimization.train(data.limit(10_000_000).iterator());

        trainedDict.dump(Files.newBufferedWriter(Paths.get("./", "output.dict")));
    }
}
