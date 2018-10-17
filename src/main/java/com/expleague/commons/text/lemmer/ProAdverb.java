package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class ProAdverb extends Adverb {

    public ProAdverb(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.ADVPRO);
    }

    @Override
    public String toString() {
        return lemma() + "(мест.нар.)";
    }

    static class Factory extends Adverb.Factory {
        public Factory() {
            super(PartOfSpeech.ADVPRO);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new ProAdverb(lemma, weight);
        }
    }
}
