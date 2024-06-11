package com.expleague.commons.text.parser;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.Text;
import gnu.trove.list.array.TIntArrayList;

public class SegmentImpl implements Text.Segment {
    private final Text owner;
    private final int index;
    private final int offset;
    private final int length;
    private final Type type;
    private final String[] allTokens;

    private Text.Normalized normalized;

    public SegmentImpl(
        final Text owner,
        final int index,
        final int offset,
        final int length,
        final Type type
    ) {
        this.owner = owner;
        this.index = index;
        this.offset = offset;
        this.length = length;
        this.type = type;
        this.allTokens = owner().parser().allTokens();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(switch (type()) {
            case UNKNOWN -> "unknown";
            case WHITESPACE -> "w";
            case PUNCTUATION -> "p";
            case NUMERIC -> "n";
            case ALPHABETIC -> "a";
            case ENTITY -> "e";
            case IDEOGRAPHIC -> "h";
        });
        result.append('[');
        final Text.Token[] tokens = tokens(0);
        for (final Text.Token token : tokens) {
            if (token.tokenPosition() == 0) {
                result.append('{');
            } else {
                result.append('|');
            }
            result.append(token.text());
            if (token.isLastTokenInWord()) {
                result.append('}');
            }
        }
        result.append(']');
        return result.toString();
    }

    @Override
    public Text.Token[] tokens(final int startWordIdx) {
        boolean afterPunctuation = false;
        if (index > 0) {
            final Text.Segment[] segments = owner().segments();
            int prev = index - 1;
            while (prev >= 0 && !segments[prev].type().isIndexed()) {
                if (segments[prev].type() == Type.PUNCTUATION) {
                    afterPunctuation = true;
                    break;
                }
                prev--;
            }
        }
        final UnicodeDictionary lexer = owner.parser().bySegmentType(type);
        if (lexer == null) { // fallback for simple segments without trained lexer
            final SimpleToken token = new SimpleToken(text().toString(), 0, startWordIdx, -1, true, afterPunctuation);
            return new Text.Token[]{token};
        }
        final IntSeq tokenIds = lexer.parse(normalizedText().text());
        final Text.Token[] tokens = new Text.Token[tokenIds.length()];
        final boolean wordByToken = type == Type.IDEOGRAPHIC;
        if (tokenIds.length() > 1) {
            final int firstTokenId = tokenIds.intAt(0);
            int currentWordIndex = startWordIdx;
            tokens[0] = createToken(0, firstTokenId, wordByToken, afterPunctuation, currentWordIndex);
            currentWordIndex += wordByToken ? 1 : 0;
            final int lastTokenIdx = tokens.length - 1;
            for (int i = 1; i < lastTokenIdx; i++) {
                tokens[i] = createToken(wordByToken ? 0 : i, tokenIds.intAt(i), wordByToken, false, currentWordIndex);
                currentWordIndex += wordByToken ? 1 : 0;
            }
            tokens[lastTokenIdx] = createToken(wordByToken ? 0 : lastTokenIdx, tokenIds.intAt(lastTokenIdx), true, false, currentWordIndex);
        } else if (tokenIds.length() > 0) {
            tokens[0] = createToken(0, tokenIds.intAt(0), true, afterPunctuation, startWordIdx);
        }
        return tokens;
    }

    private SimpleToken createToken(
        final int index,
        final int tokenId,
        final boolean lastInWord,
        final boolean ap,
        final int wordIndex
    ) {
        if (tokenId >= 0) {
            return new SimpleToken(allTokens[tokenId], index, wordIndex, tokenId, lastInWord, ap);
        }
        return new SimpleToken("?", index, wordIndex, tokenId, lastInWord, ap);
    }

    @Override
    public Text owner() {
        return owner;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public CharSequence text() {
        return owner.text().sub(offset, offset + length);
    }

    @Override
    public Text.Normalized normalizedText() {
        if (normalized != null) {
            return normalized;
        }
        final IntSeq codePoints = CharSeqTools.transformToCodePoints(text());
        final int len = codePoints.length();
        final StringBuilder result = new StringBuilder();
        final int textLen = text().length();
        final TIntArrayList widths = new TIntArrayList(textLen);
        final TIntArrayList offsets = new TIntArrayList(textLen); // offsets are shifted to left by one position
        widths.add(0);
        for (int i = 0; i < len; i++) {
            final int width = TextParser.normalizeCodePoint(codePoints.intAt(i), result);
            final int lastOffset = widths.size() - 1;
            widths.setQuick(lastOffset, widths.getQuick(lastOffset) + Character.charCount(codePoints.intAt(i)));
            if (width > 0) {
                offsets.add(i);
                widths.add(0);
            }
        }
        final CharSeq normalizedText = CharSeq.create(result.toString());
        return normalized = new Text.Normalized() {
            @Override
            public IntSeq codePoints() {
                return codePoints;
            }

            @Override
            public CharSeq text() {
                return normalizedText;
            }

            @Override
            public int offsetAt(final int normalizedOffset) {
                return normalizedOffset > 0 ? offsets.getQuick(normalizedOffset - 1) : 0;
            }

            @Override
            public int widthAt(final int normalizedOffset) {
                return widths.getQuick(normalizedOffset);
            }
        };
    }

    private class SimpleToken implements Text.Token {
        private final String text;
        private final int index;
        private final int wordIdx;
        private final int tokenId;
        private final boolean lastInWord;
        private final boolean afterPunctuation;

        private SimpleToken(
            final String text,
            final int idx,
            final int wordIdx,
            final int tokenId,
            final boolean lastInWord,
            final boolean afterPunctuation
        ) {
            this.text = text;
            this.index = idx;
            this.wordIdx = wordIdx;
            this.tokenId = tokenId;
            this.lastInWord = lastInWord;
            this.afterPunctuation = afterPunctuation;
        }

        @Override
        public Text.Segment owner() {
            return SegmentImpl.this;
        }

        @Override
        public int wordPosition() {
            return wordIdx;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public String text() {
            return text;
        }

        @Override
        public int id() {
            return tokenId;
        }

        public boolean isAfterPunctuation() {
            return afterPunctuation;
        }

        @Override
        public boolean isLastTokenInWord() {
            return lastInWord;
        }
    }
}
