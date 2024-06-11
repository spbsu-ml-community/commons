package com.expleague.commons.text.parser;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextImpl implements Text {
    private final TextParser parser;
    private final CharSeq text;
    private final Segment[] segments;
    private final Token[] tokens;
    private final int wordsCount;

    public TextImpl(final TextParser parser, final CharSequence text) {
        this.parser = parser;
        this.text = CharSeq.create(text);
        this.segments = TextParser.segments(this);
        final List<Token> tokens = new ArrayList<>(text.length());
        int wordsCount = 0;
        for (final Segment segment : segments) {
            if (!segment.type().isIndexed()) {
                continue;
            }
            for (final Token tok : segment.tokens(wordsCount)) {
                tokens.add(tok);
                wordsCount += tok.isLastTokenInWord() ? 1 : 0;
            }
        }
        this.wordsCount = wordsCount;
        this.tokens = tokens.toArray(Token[]::new);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final Segment segment : segments()) {
            builder.append(segment.toString());
        }
        return builder.toString();
    }

    @Override
    public Segment[] segments() {
        return segments;
    }

    @Override
    public CharSeq text() {
        return text;
    }

    @Override
    public Parser parser() {
        return parser;
    }

    @Override
    public Token[] tokens() {
        return tokens;
    }

    @Override
    public int wordsCount() {
        return wordsCount;
    }
}
