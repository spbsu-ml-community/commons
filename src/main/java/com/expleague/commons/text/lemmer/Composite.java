package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Composite extends LemmaInfo {

    public Composite(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.COM);
    }

    @Override
    public String toString() {
        return lemma() + "(час.ком.)";
    }

    static class Factory extends LemmaInfo.DefaultFactory {
        public Factory() {
            super(PartOfSpeech.COM);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Composite(lemma, weight);
        }
    }
}
