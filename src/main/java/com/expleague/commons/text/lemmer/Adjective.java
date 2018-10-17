package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class Adjective extends CorePOS {
    private final GrammaticalCase grammaticalCase;
    private final GrammaticalForm grammaticalForm;
    private final ComparisonDegree comparisonDegree;

    public Adjective(CharSeq lemma, double weight, PartOfSpeech pos, boolean plural, GrammaticalGender grammaticalGender,
                     GrammaticalCase grammaticalCase, GrammaticalForm grammaticalForm, ComparisonDegree comparisonDegree) {
        super(lemma, weight, pos, plural, grammaticalGender);
        this.grammaticalCase = grammaticalCase;
        this.grammaticalForm = grammaticalForm;
        this.comparisonDegree = comparisonDegree;
    }

    public Adjective(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
                     GrammaticalCase grammaticalCase, GrammaticalForm grammaticalForm, ComparisonDegree comparisonDegree) {
        super(lemma, weight, PartOfSpeech.A, plural, grammaticalGender);
        this.grammaticalCase = grammaticalCase;
        this.grammaticalForm = grammaticalForm;
        this.comparisonDegree = comparisonDegree;
    }

    public GrammaticalCase grammaticalCase() {
        return this.grammaticalCase;
    }

    public GrammaticalForm grammaticalForm() {
        return this.grammaticalForm;
    }

    public ComparisonDegree comparisonDegree() {
        return this.comparisonDegree;
    }

    @Override
    public String toString() {
        return lemma() + "(прил., " + grammaticalGender().description() + ", " +
                grammaticalCase().description() + ", " + grammaticalForm().description + ", " +
                comparisonDegree().description + ", " + (isPlural() ? "мн." : "ед.")+ ")";
    }

    public enum GrammaticalForm {
        BREV("кр", "краткая форма"),
        PLEN("полн", "полная форма"),
        POSS("притяж", "притяжательная форма");

        private final String description;
        private final String shortName;

        GrammaticalForm(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static GrammaticalForm parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(GrammaticalForm.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }

    public enum ComparisonDegree {
        SUPR("прев", "превосходная"),
        COMP("срав", "сравнительная");

        private final String description;
        private final String shortName;

        ComparisonDegree(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static ComparisonDegree parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(ComparisonDegree.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }

    static class Factory extends CorePOS.Factory {
        protected GrammaticalCase grammaticalCase;
        protected GrammaticalForm grammaticalForm;
        protected ComparisonDegree comparisonDegree;

        public Factory(PartOfSpeech pos) {
            super(pos);
        }

        public Factory() {
            super(PartOfSpeech.A);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            grammaticalCase = grammaticalCase != null ? grammaticalCase : GrammaticalCase.parse(property);
            grammaticalForm = grammaticalForm != null ? grammaticalForm : GrammaticalForm.parse(property);
            comparisonDegree = comparisonDegree != null ? comparisonDegree : ComparisonDegree.parse(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Adjective(lemma, weight, plural, grammaticalGender,
                    grammaticalCase, grammaticalForm, comparisonDegree);
        }
    }
}
