package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Particle extends LemmaInfo {

    public Particle(CharSeq lemma, double weight) {
        super(lemma, weight, PartOfSpeech.PART);
    }

    @Override
    public String toString() {
        return lemma() + "(част.)";
    }

    static class Factory extends LemmaInfo.DefaultFactory {
        public Factory() {
            super(PartOfSpeech.PART);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Particle(lemma, weight);
        }
    }
}
