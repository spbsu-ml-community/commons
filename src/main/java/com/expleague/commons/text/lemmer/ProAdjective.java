package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class ProAdjective extends Adjective {

    public ProAdjective(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
                        GrammaticalCase grammaticalCase, GrammaticalForm grammaticalForm,
                        ComparisonDegree comparisonDegree, boolean grammaticalAnimacy) {
        super(lemma, weight, PartOfSpeech.APRO, plural, grammaticalGender, grammaticalCase,
                grammaticalForm, comparisonDegree, grammaticalAnimacy);
    }

    @Override
    public String toString() {
        return lemma() + "(мест.прил., " + grammaticalGender().description() + ", " +
                grammaticalCase().description() + ", " + grammaticalForm().description() + ", " +
                comparisonDegree().description() + ", " + (isPlural() ? "мн." : "ед.")+ ")";
    }

    static class Factory extends Adjective.Factory {

        public Factory() {
            super(PartOfSpeech.APRO);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new ProAdjective(lemma, weight, plural, grammaticalGender,
                    grammaticalCase, grammaticalForm, comparisonDegree, grammaticalAnimacy);
        }
    }
}
