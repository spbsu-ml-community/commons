package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Numeral extends CorePOS {
    GrammaticalCase grammaticalCase;

    public Numeral(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
                     GrammaticalCase grammaticalCase) {
        super(lemma, weight, PartOfSpeech.NUM, plural, grammaticalGender);
        this.grammaticalCase = grammaticalCase;
    }

    public GrammaticalCase grammaticalCase() {
        return this.grammaticalCase;
    }

    @Override
    public String toString() {
        return lemma() + "(числ., " + grammaticalGender().description() + ", " +
                grammaticalCase().description()  + ", " + (isPlural() ? "мн." : "ед.")+ ")";
    }

    static class Factory extends CorePOS.Factory {
        protected GrammaticalCase grammaticalCase;

        public Factory(PartOfSpeech pos) {
            super(pos);
        }

        public Factory() {
            super(PartOfSpeech.NUM);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            grammaticalCase = grammaticalCase != null ? grammaticalCase : GrammaticalCase.parse(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Numeral(lemma, weight, plural, grammaticalGender, grammaticalCase);
        }
    }
}
