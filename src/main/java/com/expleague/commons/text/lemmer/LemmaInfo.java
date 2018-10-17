package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class LemmaInfo {
    private final CharSeq lemma;
    private final PartOfSpeech pos;
    private double weight;

    public LemmaInfo(CharSeq lemma, double weight, PartOfSpeech pos) {
        this.lemma = lemma;
        this.pos = pos;
        this.weight = weight;
    }

    public CharSeq lemma() {
        return this.lemma;
    }

    public PartOfSpeech pos() {
        return pos;
    }

    public double weight() {
        return weight;
    }

    @Override
    public String toString() {
        return lemma + "(" + pos.description() + ")";
    }

    interface Factory {
        Factory lemma(CharSeq lemma, double weight);
        Factory accept(CharSeq property);

        LemmaInfo build();
    }

    public static Factory defaultFactory(PartOfSpeech pos) {
        return new DefaultFactory(pos);
    }

    protected static class DefaultFactory implements Factory {
        protected final PartOfSpeech pos;
        protected CharSeq lemma;
        protected double weight;

        public DefaultFactory(PartOfSpeech pos) {
            this.pos = pos;
        }

        @Override
        public Factory lemma(CharSeq lemma, double weight) {
            this.lemma = lemma;
            this.weight = weight;
            return this;
        }

        @Override
        public Factory accept(CharSeq property) {
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new LemmaInfo(lemma, weight, pos);
        }
    }
}
