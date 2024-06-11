package com.expleague.commons.text.parser;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.text.parser.UnicodeDictionary;
import com.expleague.commons.text.parser.UnicodeStatDictionary;
import org.jetbrains.annotations.NotNull;

/**
 * UnicodeStatDictionary with token id conversion, needed to unify tokens space between dictionaries for different
 * segments
 */
@SuppressWarnings("WeakerAccess")
public class Lexer extends UnicodeStatDictionary {
    private final UnicodeDictionary dictionary;
    private final int[] tokenIdConversion;

    public Lexer(UnicodeStatDictionary dictionary, int [] tokenIdConversion) {
        super(dictionary::prob, dictionary.composites().toArray(IntSeq[]::new));
        this.dictionary = dictionary;
        this.tokenIdConversion = tokenIdConversion;
    }

    @Override
    public IntSeq parse(CharSeq word) {
        try {
            final int[] tokenIds = dictionary.parse(word).toArray();
            for (int i = 0; i < tokenIds.length; i++) {
                tokenIds[i] = tokenIdConversion[tokenIds[i]];
            }
            return new IntSeq(tokenIds);
        } catch (final Exception ex) {
            throw new RuntimeException("Unable to parse: '%s'.".formatted(word), ex);
        }
    }

    public CharSeq token(int tokenId) {
        return dictionary.chars(tokenId);
    }

    public int convertToGlobal(int tokenId) {
        return tokenIdConversion != null ? tokenIdConversion[tokenId] : tokenId;
    }
}
