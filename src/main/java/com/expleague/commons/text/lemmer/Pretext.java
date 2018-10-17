package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Pretext extends LemmaInfo {

    public Pretext(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.PR);
    }

    @Override
    public String toString() {
        return lemma() + "(пред.)";
    }

    static class Factory extends LemmaInfo.DefaultFactory {
        public Factory() {
            super(PartOfSpeech.PR);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Pretext(lemma, weight);
        }
    }
}
