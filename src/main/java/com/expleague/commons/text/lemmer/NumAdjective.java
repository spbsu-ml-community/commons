package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class NumAdjective extends Adjective {

    public NumAdjective(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
                        GrammaticalCase grammaticalCase, GrammaticalForm grammaticalForm,
                        ComparisonDegree comparisonDegree, boolean grammaticalAnimacy) {
        super(lemma, weight, PartOfSpeech.ANUM, plural, grammaticalGender, grammaticalCase,
                grammaticalForm, comparisonDegree, grammaticalAnimacy);
    }

    @Override
    public String toString() {
        return lemma() + "(числ. прил., " + grammaticalGender().description() + ", " +
                grammaticalCase().description() + ", " + grammaticalForm().description() + ", " +
                comparisonDegree().description() + ", " + (isPlural() ? "мн." : "ед.")+ ")";
    }

    static class Factory extends Adjective.Factory {

        public Factory() {
            super(PartOfSpeech.ANUM);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new NumAdjective(lemma, weight, plural, grammaticalGender,
                    grammaticalCase, grammaticalForm, comparisonDegree, grammaticalAnimacy);
        }
    }
}
