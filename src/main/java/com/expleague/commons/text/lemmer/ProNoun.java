package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class ProNoun extends Noun {

    public ProNoun(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
                        GrammaticalCase grammaticalCase, boolean name, boolean grammaticalAnimacy) {
        super(lemma, weight, PartOfSpeech.SPRO, plural, grammaticalGender, grammaticalCase, name, grammaticalAnimacy);
    }

    @Override
    public String toString() {
        return lemma() + "(мест.сущ., " + ", " + grammaticalGender().description() + ", " +
                grammaticalCase().description() + ", " + (isAnimate() ? "одуш." : "неодущ") + ", " +
                (isName() ? "имя" : "") + ", " + (isPlural() ? "мн." : "ед.") + ")";
    }

    static class Factory extends Noun.Factory {

        public Factory() {
            super(PartOfSpeech.SPRO);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new ProNoun(lemma, weight, plural, grammaticalGender,
                    grammaticalCase, name, grammaticalAnimacy);
        }
    }
}
