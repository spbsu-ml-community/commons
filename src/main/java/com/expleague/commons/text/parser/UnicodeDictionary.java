package com.expleague.commons.text.parser;

import com.expleague.commons.io.codec.seq.DictionaryBase;
import com.expleague.commons.seq.*;
import com.expleague.commons.util.Pair;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "WeakerAccess"})
public class UnicodeDictionary extends DictionaryBase<Integer> {
    public static final int COMPOSITES_START = 1_114_112 + 1; // unicode maximum codepoint + 1
    private final IntSeq[] composites;
    private final int[] parents;
    private final CharSeq[] seqs;
    private final Comparator<Seq<Integer>> cmp;

    public UnicodeDictionary() {
        this(new IntSeq[0]);
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public UnicodeDictionary(final CharSequence... composites) {
        this(Arrays.stream(composites).map(CharSeqTools::transformToCodePoints).toArray(IntSeq[]::new));
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public UnicodeDictionary(final IntSeq... composites) {
        this.composites = composites;
        this.parents = new int[composites.length];
        this.seqs = new CharSeq[composites.length];
        final Stack<Pair<IntSeq, Integer>> parents = new Stack<>();
        this.cmp = CharSeqTools.lexicographicalComparator(Integer.class);
        Arrays.sort(this.composites, cmp);

        for (int i = 0; i < composites.length; i++) {
            final IntSeq current = this.composites[i];
            final StringBuilder seqBuilder = new StringBuilder();
            current.stream().forEach(seqBuilder::appendCodePoint);
            this.seqs[i] = CharSeq.create(seqBuilder.toString());
            this.parents[i] = current.intAt(0);
            while (!parents.empty()) {
                if (CharSeqTools.startsWith(current, parents.peek().getFirst())) {
                    this.parents[i] = parents.peek().getSecond();
                    break;
                }
                parents.pop();
            }
            parents.push(Pair.create(current, i + COMPOSITES_START));
        }
    }

    public IntSeq parse(final CharSeq seq) {
        return parse(CharSeqTools.transformToCodePoints(seq));
    }

    public IntSeq parse(final CharSeq seq, final TIntList freqs, final double total) {
        return parse(CharSeqTools.transformToCodePoints(seq), freqs, total);
    }


    public int search(final CharSequence text) {
        return search(CharSeqTools.transformToCodePoints(text));
    }

    public int search(final IntSeq seq) {
        return search(seq, null);
    }

    @Override
    public int search(final Seq<Integer> seq, final TIntSet excludes) {
        if (seq instanceof IntSeq) {
            return this.search((IntSeq) seq, excludes);
        }
        return this.search((IntSeq) (CharSeqTools.<Integer>create(seq.toArray())), excludes);
    }

    public int search(final IntSeq seq, final TIntSet excludes) {
        if (seq.length() == 0) {
            throw new IllegalArgumentException("Empty sequence");
        }
        //noinspection DuplicatedCode
        int index = Arrays.binarySearch(composites, seq, cmp);
        if (index >= 0) {
            if (excludes == null || !excludes.contains(index + COMPOSITES_START))
                return index + COMPOSITES_START;
            index = -(parents[index] + 2 - COMPOSITES_START);
        }
        index = -(index + 2) + COMPOSITES_START;
        final int firstCP = seq.intAt(0);
        if (index < COMPOSITES_START || firstCP != composites[index - COMPOSITES_START].intAt(0)) { // first character does not match, no need to traverse parents
            return firstCP;
        }
        while (index >= COMPOSITES_START) {
            if (CharSeqTools.startsWith(seq, composites[index - COMPOSITES_START])
                && (excludes == null || !excludes.contains(index)))
                return index;
            index = parents[index - COMPOSITES_START];
        }
        return firstCP;
    }

    @Override
    public IntSeq get(final int index) {
        if (index < COMPOSITES_START) {
            return new IntSeqInt(index);
        }
        return composites[index - COMPOSITES_START];
    }

    @Override
    public int size() {
        return COMPOSITES_START + composites.length;
    }

    /**
     * Use this carefully, because of number of tokens (at least COMPOSITES_START of them).
     */
    @Override
    public List<IntSeq> alphabet() {
        return Stream.concat(
            IntStream.range(0, COMPOSITES_START).mapToObj(IntSeqInt::new),
            Arrays.stream(composites)
        ).toList();
    }

    public List<IntSeq> composites() {
        return List.of(composites);
    }

    @Override
    public int parent(final int tokenId) {
        return tokenId < COMPOSITES_START ? -1 : parents[tokenId - COMPOSITES_START];
    }

    public static boolean isComposite(final int token) {
        return token >= COMPOSITES_START;
    }

    public boolean contains(final IntSeq token) {
        if (token.length() == 1) {
            return true;
        }
        return token.equals(get(search(token)));
    }

    public CharSeq chars(final int tokenId) {
        if (tokenId < COMPOSITES_START) {
            return CharSeq.create(Character.toChars(tokenId));
        }
        return seqs[tokenId - COMPOSITES_START];
    }

    public List<CharSeq> charsAlphabet() {
        final CharSeq[] allTokens = new CharSeq[size()];
        for (int i = 0; i < allTokens.length; i++) {
            allTokens[i] = chars(i);
        }
        return List.of(allTokens);
    }
}
