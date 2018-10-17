package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Interjection extends LemmaInfo {

    public Interjection(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.INTJ);
    }

    @Override
    public String toString() {
        return lemma() + "(межд.)";
    }

    static class Factory extends LemmaInfo.DefaultFactory {
        public Factory() {
            super(PartOfSpeech.INTJ);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Interjection(lemma, weight);
        }
    }
}
