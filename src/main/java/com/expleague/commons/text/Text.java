package com.expleague.commons.text;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.parser.SegmentImpl;
import com.expleague.commons.text.parser.TextParser;
import com.expleague.commons.text.parser.UnicodeDictionary;

/**
 * Text interface is an entry point for text parsing. To use this subsystem you need to create parser with
 * {@link #parser(String)} and build Text object by a CharSequence. The parsing process is split into two steps. At first
 * we split the text onto text segments {@link #segments()}, and then each segment is tokenized with dedicated lexer
 * {@link Parser#bySegmentType(Segment.Type)}. The result tokens, which are coming from different lexers reindexed
 * in a token space and final ids, common for all segments returned with {@link Token#id()}.
 */
@SuppressWarnings("unused")
public interface Text {
    Segment[] segments();
    CharSeq text();
    Parser parser();
    Token[] tokens();
    int wordsCount();

    static Parser parser(final String config) {
        return new TextParser(config);
    }

    interface Segment {
        Token[] tokens(final int startWordIdx);

        Text owner();
        int index();

        int offset();
        int length();

        Type type();

        CharSequence text();
        Normalized normalizedText();

        enum Type {
            UNKNOWN(false),
            WHITESPACE(false),
            PUNCTUATION(false),
            NUMERIC(true),
            ALPHABETIC(true),
            ENTITY(true),
            IDEOGRAPHIC(true),
            ;
            private final boolean indexed;

            Type(final boolean indexed) {
                this.indexed = indexed;
            }

            public Segment create(final Text owner, final int index, final int offset, final int length) {
                return new SegmentImpl(owner, index, offset, length, this);
            }

            public boolean isIndexed() {
                return indexed;
            }
        }
    }

    interface Token  {
        Segment owner();
        int index();
        String text();
        int id();

        default float score() {
            return 1.f;
        }

        default int wordPosition() {
            return 0;
        }

        default int tokenPosition() {
            return index();
        }

        default boolean isLastTokenInWord() {
            return true;
        }

        /**
         * @return {@code true} if this token position corresponds to a one token word.
         */
        default boolean isFullWord() {
            return tokenPosition() == 0 && isLastTokenInWord();
        }
    }

    interface Normalized {
        CharSeq text();
        IntSeq codePoints();
        int offsetAt(int normalizedOffset);
        int widthAt(int normalizedOffset);
    }

    interface Parser {
        Text parse(CharSequence text);
        UnicodeDictionary bySegmentType(Segment.Type type); // TODO: return UnicodeStatDictionary-children

        int convertTokenIdToGlobal(Segment.Type type, int tokenId);
        String[] allTokens();
    }
}