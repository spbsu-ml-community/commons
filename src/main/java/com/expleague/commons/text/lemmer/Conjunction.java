package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Conjunction extends LemmaInfo {

    public Conjunction(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.CONJ);
    }

    @Override
    public String toString() {
        return lemma() + "(союз)";
    }

    static class Factory extends LemmaInfo.DefaultFactory {
        public Factory() {
            super(PartOfSpeech.CONJ);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Conjunction(lemma, weight);
        }
    }
}
