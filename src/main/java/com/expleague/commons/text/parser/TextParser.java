package com.expleague.commons.text.parser;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.Text;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.expleague.commons.text.Text.Segment.Type.*;

public class TextParser implements Text.Parser {
    private final Map<Text.Segment.Type, Lexer> lexers = new HashMap<>();
    private final String[] allTokens;

    public TextParser() {
        allTokens = new String[0];
    }

    public TextParser(final String path) {
        this(true, path);
    }

    @SuppressWarnings("WeakerAccess")
    public TextParser(final boolean isResource, final String path) {
        final BitSet codePointsPresent = new BitSet(UnicodeDictionary.COMPOSITES_START);
        final Set<IntSeq> allComposites = new HashSet<>();
        final Map<Text.Segment.Type, UnicodeStatDictionary> dictionaries = new HashMap<>();
        for (final Text.Segment.Type type : values()) {
            if (!type.isIndexed()) {
                continue;
            }
            try (final InputStream stream = isResource ?
                    TextParser.class.getResourceAsStream(path + '.' + type)
                    : Files.newInputStream(Paths.get(path + '.' + type));
            ) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Objects.requireNonNull(stream),
                        StandardCharsets.UTF_8)
                );
                final List<IntSeq> composites = new ArrayList<>();
                final TObjectDoubleMap<IntSeq> freqs = new TObjectDoubleHashMap<>();
                reader.lines().forEach(line -> {
                    final CharSequence[] split = CharSeqTools.split(line, '\t');
                    final IntSeq codePoints = CharSeqTools.transformToCodePoints(split[0]);
                    if (codePoints.length() == 0) {
                        return;
                    }
                    if (codePoints.length() > 1) {
                        composites.add(codePoints);
                        allComposites.add(codePoints);
                    } else {
                        codePointsPresent.set(codePoints.intAt(0));
                    }
                    freqs.put(codePoints, CharSeqTools.parseDouble(split[1]));
                });
                final UnicodeStatDictionary dictionary = new UnicodeStatDictionary(
                    freqs::get,
                    composites.toArray(IntSeq[]::new)
                );
                dictionaries.put(type, dictionary);
            } catch (final IOException ee) {
                throw new IllegalArgumentException(ee);
            }
        }
        final String[] allTokens = new String[allComposites.size() + codePointsPresent.cardinality()];
        final AtomicInteger counter = new AtomicInteger(0);
        codePointsPresent.stream().forEach(cp ->
            allTokens[counter.getAndIncrement()] = new String(Character.toChars(cp))
        );
        allComposites.stream().sorted().forEach(seq ->
            allTokens[counter.getAndIncrement()] = CharSeqTools.codePointsToChars(seq)
        );
        this.allTokens = allTokens;
        dictionaries.forEach((type, dictionary) -> {
            final int[] tokenIdConversion = new int[dictionary.size()];
            Arrays.fill(tokenIdConversion, -1);
            for (int i = 0; i < allTokens.length; i++) {
                final String token = allTokens[i];
                final IntSeq codePoints = CharSeqTools.transformToCodePoints(token);
                final int localIdx = dictionary.search(codePoints);
                if (codePoints.equals(dictionary.get(localIdx))) {
                    tokenIdConversion[localIdx] = i;
                }
            }
            lexers.put(type, new Lexer(dictionary, tokenIdConversion));
        });
    }

    @Override
    public Text parse(final CharSequence text) {
        return new TextImpl(this, text);
    }

    @Override
    public Lexer bySegmentType(final Text.Segment.Type type) {
        return lexers.get(type);
    }

    @Override
    public int convertTokenIdToGlobal(final Text.Segment.Type type, final int tokenId) {
        return lexers.get(type).convertToGlobal(tokenId);
    }

    @Override
    public String[] allTokens() {
        return allTokens;
    }

    static Text.Segment[] segments(final Text text) {
        final CharSeq seq = text.text();
        final int len = seq.length();
        final Text.Segment[] result = new Text.Segment[len];

        final State state = new State(text, segment -> result[segment.index()] = segment);
        while (true) {
            final int off = state.offset() + 1;
            if (off < len) {
                final char nextChar = seq.charAt(off);
                final int nextCodePoint;
                if (Character.isHighSurrogate(nextChar)) {
                    if (off + 1 < len) {
                        final char c2 = seq.charAt(off + 1);
                        if (Character.isLowSurrogate(c2)) {
                            nextCodePoint = Character.toCodePoint(nextChar, c2);
                            state.offset++;
                        } else {
                            nextCodePoint = Character.UNASSIGNED;
                        }
                    } else {
                        state.nextChar((int) nextChar, CharacterType.UNKNOWN);
                        break;
                    }
                } else {
                    nextCodePoint = nextChar;
                }
                final CharacterType nextChType = charType(nextCodePoint, state.currentCharType());
                final Text.Segment.Type currentSegType = state.segmentType();
                state.nextChar(nextCodePoint, nextChType);
                final int cacheId = currentSegType.ordinal() * CHAR_TYPES_COUNT + nextChType.ordinal();
                if (OPERATIONS_CACHE[cacheId] == null) {
                    OPERATIONS_CACHE[cacheId] = buildOperation(nextChType, currentSegType);
                }
                OPERATIONS_CACHE[cacheId].accept(state);
            } else {
                state.nextChar(-1, CharacterType.UNKNOWN);
                state.push(UNKNOWN);
                if (state.offset() >= len) {
                    break;
                }
            }
        }

        return Arrays.copyOf(result, state.segmentsCount());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    private static class State {
        private final Text text;
        private final Consumer<Text.Segment> onSegment;
        private Text.Segment.Type segmentType = UNKNOWN;
        private int segmentsCount = 0;
        private int indexedSegmentsCount = 0;

        private int segmentStart = 0;
        private int offset = -1;
        private int mark = Integer.MAX_VALUE;

        private int currentChar = -1;
        private CharacterType currentCharType = CharacterType.UNKNOWN;
        private int prevChar = -1;
        private CharacterType prevCharType;
        private Text.Segment.Type markSegmentType;

        State(final Text text, final Consumer<Text.Segment> onSegment) {
            this.text = text;
            this.onSegment = onSegment;
        }

        public CharacterType prevCharType() {
            return prevCharType;
        }

        public int prevChar() {
            return prevChar;
        }

        public Text.Segment.Type segmentType() {
            return segmentType;
        }

        public void push(final Text.Segment.Type segType) {
            if (segType != segmentType) {
                final int segmentEnd = Math.min(mark, offset);
                if (mark < offset) {
                    if (segmentStart < segmentEnd) {
                        onSegment.accept(
                            markSegmentType.create(
                                text,
                                segmentsCount++,
                                segmentStart,
                                segmentEnd - segmentStart
                            )
                        );
                        indexedSegmentsCount += markSegmentType.isIndexed() ? 1 : 0;
                    }
                    segmentType = UNKNOWN;
                    segmentStart = mark;
                    offset = mark - 1;
                    mark = Integer.MAX_VALUE;
                } else {
                    if (segmentStart < segmentEnd) {
                        onSegment.accept(
                            segmentType.create(
                                text,
                                segmentsCount++,
                                segmentStart,
                                segmentEnd - segmentStart
                            )
                        );
                        indexedSegmentsCount += segmentType.isIndexed() ? 1 : 0;
                    }
                    segmentStart = offset;
                    segmentType = segType;
                }
            }
        }

        private void changeType(final Text.Segment.Type newSegType) {
            segmentType = newSegType;
        }

        private boolean exception() {
            return mark < offset;
        }

        private void mark() {
            if (mark > offset) {
                mark = offset;
                markSegmentType = segmentType;
            }
        }

        private void mark(@SuppressWarnings("SameParameterValue") final Text.Segment.Type withSegmentType) {
            if (mark > offset) {
                mark = offset;
                markSegmentType = withSegmentType;
            }
        }

        private int currentChar() {
            return currentChar;
        }

        private CharacterType currentCharType() {
            return currentCharType;
        }

        private int segmentLength() {
            return Math.min(mark, offset) - segmentStart;
        }

        private void shift(final int shift) {
            if (mark >= offset + shift) {
                mark = Integer.MAX_VALUE;
            }
            offset += shift;
        }

        private void nextChar(final Integer cp, final CharacterType chType) {
            prevCharType = this.currentCharType;
            prevChar = this.currentChar;
            currentChar = cp;
            currentCharType = chType;
            offset++;
        }

        private void unmark() {
            mark = Integer.MAX_VALUE;
        }

        private int segmentsCount() {
            return segmentsCount;
        }

        private int offset() {
            return offset;
        }
    }

    enum CharacterType {
        UNKNOWN,
        ALPHABETIC,
        IDEOGRAPHIC,
        NUMBER,
        FLOAT_SEPARATOR,
        DASH,
        NUMBER_PREFIX,
        OTHER_PUNCTUATION,
        WHITESPACE,
    }

    private static final int CHAR_TYPES_COUNT = CharacterType.values().length;
    private static final int SEG_TYPES_COUNT = values().length;
    @SuppressWarnings("unchecked")
    private static final Consumer<State>[] OPERATIONS_CACHE =
        new Consumer[CHAR_TYPES_COUNT * SEG_TYPES_COUNT];

    private static Consumer<State> buildOperation(final CharacterType chType, final Text.Segment.Type segType) {
        final Consumer<State> accept = State::unmark;
        final Consumer<State> defaultBehavior = switch (chType) {
            case ALPHABETIC:
                yield state -> state.push(ALPHABETIC);
            case IDEOGRAPHIC:
                yield state -> state.push(IDEOGRAPHIC);
            case NUMBER:
                yield state -> state.push(NUMERIC);
            case WHITESPACE:
                yield state -> state.push(WHITESPACE);
            case NUMBER_PREFIX, FLOAT_SEPARATOR, DASH, OTHER_PUNCTUATION:
                yield state -> state.push(Text.Segment.Type.PUNCTUATION);
            case UNKNOWN:
                yield state -> state.push(UNKNOWN);
        };
        return switch (segType) {
            case UNKNOWN, IDEOGRAPHIC:
                yield defaultBehavior;
            case ALPHABETIC:
                yield switch (chType) {
                    case NUMBER -> state -> state.changeType(ENTITY);
                    case OTHER_PUNCTUATION, FLOAT_SEPARATOR, UNKNOWN -> state -> {
                        state.mark(ALPHABETIC);
                        state.changeType(ENTITY);
                    };
                    case NUMBER_PREFIX -> state -> {
                        if (!state.exception() && state.currentChar() == '-') {
                            state.mark();
                        } else {
                            state.push(PUNCTUATION);
                        }
                    };
                    case DASH -> state -> {
                        if (!state.exception()) {
                            state.mark();
                        } else {
                            state.push(PUNCTUATION);
                        }
                    };
                    case ALPHABETIC -> State::unmark;
                    case IDEOGRAPHIC, WHITESPACE -> defaultBehavior;
                };
            case ENTITY:
                yield switch (chType) {
                    case OTHER_PUNCTUATION, NUMBER_PREFIX, FLOAT_SEPARATOR, DASH -> State::mark;
                    case NUMBER, ALPHABETIC, UNKNOWN -> accept;
                    case IDEOGRAPHIC, WHITESPACE -> defaultBehavior;
                };
            case PUNCTUATION:
                yield switch (chType) {
                    case NUMBER -> state -> {
                        if (state.prevCharType() == CharacterType.NUMBER_PREFIX) {
                            if (state.segmentLength() > 1) {
                                state.shift(-1);
                                state.push(NUMERIC);
                                state.shift(1);
                            } else {
                                state.changeType(NUMERIC);
                            }
                        }
                    };
                    case FLOAT_SEPARATOR, NUMBER_PREFIX, DASH, OTHER_PUNCTUATION -> accept;
                    case UNKNOWN, IDEOGRAPHIC, ALPHABETIC, WHITESPACE -> defaultBehavior;
                };
            case NUMERIC:
                yield switch (chType) {
                    case OTHER_PUNCTUATION, NUMBER_PREFIX, DASH, ALPHABETIC, UNKNOWN ->
                        state -> state.changeType(ENTITY);
                    case FLOAT_SEPARATOR -> State::mark;
                    case NUMBER -> accept;
                    case IDEOGRAPHIC, WHITESPACE -> defaultBehavior;
                };
            case WHITESPACE:
                yield switch (chType) {
                    case WHITESPACE -> accept;
                    case UNKNOWN, IDEOGRAPHIC, ALPHABETIC, NUMBER, FLOAT_SEPARATOR, NUMBER_PREFIX, DASH, OTHER_PUNCTUATION ->
                        defaultBehavior;
                };
        };
    }

    private static final byte[][] CHAR_TYPES_CACHE = new byte[CharacterType.values().length][UnicodeDictionary.COMPOSITES_START];

    private static CharacterType charType(final int codePoint, final CharacterType prevCharType) {
        if (codePoint >= UnicodeDictionary.COMPOSITES_START) {
            return CharacterType.UNKNOWN;
        }

        final int prevCharTypeOrdinal = prevCharType.ordinal();
        if (CHAR_TYPES_CACHE[prevCharTypeOrdinal][codePoint] == 0) {
            final CharacterType resultType = switch (Character.getType(codePoint)) {
                case Character.UNASSIGNED -> {
                    yield CharacterType.UNKNOWN;
                }
                case Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER,
                    Character.MODIFIER_LETTER, Character.OTHER_LETTER -> {
                    yield Character.isIdeographic(codePoint) ? CharacterType.IDEOGRAPHIC : CharacterType.ALPHABETIC;
                }
                case Character.NON_SPACING_MARK -> {
                    yield prevCharType;
                }
                case Character.ENCLOSING_MARK, Character.COMBINING_SPACING_MARK -> {
                    yield prevCharType;
                }
                case Character.DECIMAL_DIGIT_NUMBER, Character.LETTER_NUMBER, Character.OTHER_NUMBER -> {
                    yield CharacterType.NUMBER;
                }
                case Character.SPACE_SEPARATOR, Character.LINE_SEPARATOR, Character.PARAGRAPH_SEPARATOR -> {
                    yield CharacterType.WHITESPACE;
                }
                case Character.CONTROL -> {
                    yield CharacterType.WHITESPACE;
                }
                case Character.FORMAT -> {
                    yield prevCharType;
                }
                case Character.PRIVATE_USE, Character.SURROGATE -> {
                    yield CharacterType.UNKNOWN;
                }
                case Character.DASH_PUNCTUATION -> {
                    yield CharacterType.DASH;
                }
                case Character.START_PUNCTUATION, Character.END_PUNCTUATION, Character.OTHER_PUNCTUATION,
                    Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION -> {
                    yield codePoint == '.' || codePoint == '．' || codePoint == '·' || codePoint == ','
                        ? CharacterType.FLOAT_SEPARATOR
                        : CharacterType.OTHER_PUNCTUATION;
                }
                case Character.CONNECTOR_PUNCTUATION -> {
                    yield prevCharType;
                }
                case Character.MATH_SYMBOL -> {
                    yield codePoint == '+' || codePoint == '-'
                        ? CharacterType.NUMBER_PREFIX
                        : CharacterType.OTHER_PUNCTUATION;
                }
                case Character.CURRENCY_SYMBOL -> {
                    yield CharacterType.OTHER_PUNCTUATION;
                }
                case Character.MODIFIER_SYMBOL -> {
                    yield prevCharType;
                }
                case Character.OTHER_SYMBOL -> {
                    yield CharacterType.UNKNOWN;
                }
                default -> {
                    yield CharacterType.UNKNOWN;
                }
            };
            CHAR_TYPES_CACHE[prevCharTypeOrdinal][codePoint] = (byte) (resultType.ordinal() + 1);
        }
        return CharacterType.values()[CHAR_TYPES_CACHE[prevCharTypeOrdinal][codePoint] - 1];
    }

    private static final char[][] NORMALIZATION_CACHE = new char[UnicodeDictionary.COMPOSITES_START][];

    public static int normalizeCodePoint(final int codePoint, final StringBuilder builder) {
        if (codePoint >= NORMALIZATION_CACHE.length) {
            return 0;
        }
        if (NORMALIZATION_CACHE[codePoint] == null) {
            final StringBuilder cacheBuilder = new StringBuilder();
            normalizeCodePointImpl(codePoint, cacheBuilder);
            NORMALIZATION_CACHE[codePoint] = cacheBuilder.toString().toCharArray();
        }
        final char[] cached = NORMALIZATION_CACHE[codePoint];
        builder.append(cached);
        return cached.length;
    }

    private static void normalizeCodePointImpl(final int codePoint, final StringBuilder builder) {
        switch (Character.getType(codePoint)) {
            case Character.UNASSIGNED -> {}
            case Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER -> {
                builder.append(Character.toLowerCase((char) codePoint));
            }
            case Character.TITLECASE_LETTER, Character.MODIFIER_LETTER, Character.OTHER_LETTER -> {
                final String normalized = Normalizer.normalize(Character.toString(codePoint), Normalizer.Form.NFKD);
                builder.append(normalized.toLowerCase());
            }
            case Character.NON_SPACING_MARK -> {}
            case Character.ENCLOSING_MARK, Character.COMBINING_SPACING_MARK -> {}
            case Character.DECIMAL_DIGIT_NUMBER -> {
                builder.append(Character.digit(codePoint, 10));
            }
            case Character.LETTER_NUMBER, Character.OTHER_NUMBER -> {
                builder.append(Character.digit(codePoint, 10));
            }
            case Character.SPACE_SEPARATOR, Character.LINE_SEPARATOR, Character.PARAGRAPH_SEPARATOR, Character.CONTROL -> {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ' ') {
                    break;
                }
                builder.append(' ');
            }
            case Character.FORMAT -> {}
            case Character.PRIVATE_USE, Character.SURROGATE -> {}
            case Character.DASH_PUNCTUATION -> {
                builder.append('-');
            }
            case Character.START_PUNCTUATION, Character.END_PUNCTUATION, Character.OTHER_PUNCTUATION,
                Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION -> {
                final int conversion = PUNCTUATION_CONVERSION.get(codePoint);
                builder.appendCodePoint(conversion != 0 ? conversion : codePoint);
            }
            case Character.CONNECTOR_PUNCTUATION -> {}
            case Character.MATH_SYMBOL, Character.CURRENCY_SYMBOL, Character.OTHER_SYMBOL -> {
                final String normalized = Normalizer.normalize(Character.toString(codePoint), Normalizer.Form.NFKD);
                builder.append(normalized.toLowerCase());
            }
            case Character.MODIFIER_SYMBOL -> {}
        }
    }

    private static final TIntIntHashMap PUNCTUATION_CONVERSION = new TIntIntHashMap();

    static {
        PUNCTUATION_CONVERSION.put(0x00AB, '"');
        PUNCTUATION_CONVERSION.put(0x00AD, '-');
        PUNCTUATION_CONVERSION.put(0x00B4, '\'');
        PUNCTUATION_CONVERSION.put(0x00BB, '"');
        PUNCTUATION_CONVERSION.put(0x00F7, '/');
        PUNCTUATION_CONVERSION.put(0x01C0, '|');
        PUNCTUATION_CONVERSION.put(0x01C3, '!');
        PUNCTUATION_CONVERSION.put(0x02B9, '\'');
        PUNCTUATION_CONVERSION.put(0x02BA, '"');
        PUNCTUATION_CONVERSION.put(0x02BC, '\'');
        PUNCTUATION_CONVERSION.put(0x02C4, '^');
        PUNCTUATION_CONVERSION.put(0x02C6, '^');
        PUNCTUATION_CONVERSION.put(0x02C8, '\'');
        PUNCTUATION_CONVERSION.put(0x02CB, '`');
        PUNCTUATION_CONVERSION.put(0x02CD, '_');
        PUNCTUATION_CONVERSION.put(0x02DC, '~');
        PUNCTUATION_CONVERSION.put(0x0300, '`');
        PUNCTUATION_CONVERSION.put(0x0301, '\'');
        PUNCTUATION_CONVERSION.put(0x0302, '^');
        PUNCTUATION_CONVERSION.put(0x0303, '~');
        PUNCTUATION_CONVERSION.put(0x030B, '"');
        PUNCTUATION_CONVERSION.put(0x030E, '"');
        PUNCTUATION_CONVERSION.put(0x0331, '_');
        PUNCTUATION_CONVERSION.put(0x0332, '_');
        PUNCTUATION_CONVERSION.put(0x0338, '/');
        PUNCTUATION_CONVERSION.put(0x0589, ':');
        PUNCTUATION_CONVERSION.put(0x05C0, '|');
        PUNCTUATION_CONVERSION.put(0x05C3, ':');
        PUNCTUATION_CONVERSION.put(0x066A, '%');
        PUNCTUATION_CONVERSION.put(0x066D, '*');
        PUNCTUATION_CONVERSION.put(0x200B, ' ');
        PUNCTUATION_CONVERSION.put(0x2010, '-');
        PUNCTUATION_CONVERSION.put(0x2011, '-');
        PUNCTUATION_CONVERSION.put(0x2012, '-');
        PUNCTUATION_CONVERSION.put(0x2013, '-');
        PUNCTUATION_CONVERSION.put(0x2014, '-');
        PUNCTUATION_CONVERSION.put(0x2015, '-');
        PUNCTUATION_CONVERSION.put(0x2016, '|');
        PUNCTUATION_CONVERSION.put(0x2017, '_');
        PUNCTUATION_CONVERSION.put(0x2018, '\'');
        PUNCTUATION_CONVERSION.put(0x2019, '\'');
        PUNCTUATION_CONVERSION.put(0x201A, ',');
        PUNCTUATION_CONVERSION.put(0x201B, '\'');
        PUNCTUATION_CONVERSION.put(0x201C, '"');
        PUNCTUATION_CONVERSION.put(0x201D, '"');
        PUNCTUATION_CONVERSION.put(0x201E, '"');
        PUNCTUATION_CONVERSION.put(0x201F, '"');
        PUNCTUATION_CONVERSION.put(0x2032, '\'');
        PUNCTUATION_CONVERSION.put(0x2033, '"');
        PUNCTUATION_CONVERSION.put(0x2034, '\'');
        PUNCTUATION_CONVERSION.put(0x2035, '`');
        PUNCTUATION_CONVERSION.put(0x2036, '"');
        PUNCTUATION_CONVERSION.put(0x2037, '\'');
        PUNCTUATION_CONVERSION.put(0x2038, '^');
        PUNCTUATION_CONVERSION.put(0x2039, '<');
        PUNCTUATION_CONVERSION.put(0x203A, '>');
        PUNCTUATION_CONVERSION.put(0x203D, '?');
        PUNCTUATION_CONVERSION.put(0x2044, '/');
        PUNCTUATION_CONVERSION.put(0x204E, '*');
        PUNCTUATION_CONVERSION.put(0x2052, '%');
        PUNCTUATION_CONVERSION.put(0x2053, '~');
        PUNCTUATION_CONVERSION.put(0x2060, ' ');
        PUNCTUATION_CONVERSION.put(0x20E5, '\\');
        PUNCTUATION_CONVERSION.put(0x2212, '-');
        PUNCTUATION_CONVERSION.put(0x2215, '/');
        PUNCTUATION_CONVERSION.put(0x2216, '\\');
        PUNCTUATION_CONVERSION.put(0x2217, '*');
        PUNCTUATION_CONVERSION.put(0x2223, '|');
        PUNCTUATION_CONVERSION.put(0x2236, ':');
        PUNCTUATION_CONVERSION.put(0x223C, '~');
        PUNCTUATION_CONVERSION.put(0x2264, '<');
        PUNCTUATION_CONVERSION.put(0x2265, '>');
        PUNCTUATION_CONVERSION.put(0x2266, '<');
        PUNCTUATION_CONVERSION.put(0x2267, '>');
        PUNCTUATION_CONVERSION.put(0x2303, '^');
        PUNCTUATION_CONVERSION.put(0x2329, '<');
        PUNCTUATION_CONVERSION.put(0x232A, '>');
        PUNCTUATION_CONVERSION.put(0x266F, '#');
        PUNCTUATION_CONVERSION.put(0x2731, '*');
        PUNCTUATION_CONVERSION.put(0x2758, '|');
        PUNCTUATION_CONVERSION.put(0x2762, '!');
        PUNCTUATION_CONVERSION.put(0x27E6, '[');
        PUNCTUATION_CONVERSION.put(0x27E8, '<');
        PUNCTUATION_CONVERSION.put(0x27E9, '>');
        PUNCTUATION_CONVERSION.put(0x2983, '{');
        PUNCTUATION_CONVERSION.put(0x2984, '}');
        PUNCTUATION_CONVERSION.put(0x3003, '"');
        PUNCTUATION_CONVERSION.put(0x3008, '<');
        PUNCTUATION_CONVERSION.put(0x3009, '>');
        PUNCTUATION_CONVERSION.put(0x301B, ']');
        PUNCTUATION_CONVERSION.put(0x301C, '~');
        PUNCTUATION_CONVERSION.put(0x301D, '"');
        PUNCTUATION_CONVERSION.put(0x301E, '"');
        PUNCTUATION_CONVERSION.put(0xFEFF, ' ');
    }
}
