package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Adverb extends LemmaInfo {

    public Adverb(CharSeq lemma, double weight, PartOfSpeech pos) {
        super(lemma, weight, pos);
    }

    public Adverb(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.ADV);
    }

    @Override
    public String toString() {
        return lemma() + "(нар.)";
    }

    static class Factory extends LemmaInfo.DefaultFactory {
        public Factory(PartOfSpeech pos) {
            super(pos);
        }

        public Factory() {
            super(PartOfSpeech.ADV);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Adverb(lemma, weight);
        }
    }
}
