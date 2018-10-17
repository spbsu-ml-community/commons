package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class Verb extends CorePOS {
    private GrammaticalTense grammaticalTense;
    private GrammaticalMood grammaticalMood;
    private GrammaticalPerson grammaticalPerson;
    private GrammaticalVoice grammaticalVoice;
    private GrammaticalTransitivity grammaticalTransitivity;

    public Verb(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
                GrammaticalTense grammaticalTense, GrammaticalMood grammaticalMood,
                GrammaticalPerson grammaticalPerson, GrammaticalVoice grammaticalVoice,
                GrammaticalTransitivity grammaticalTransitivity) {
        super(lemma, weight, PartOfSpeech.V, plural, grammaticalGender);
        this.grammaticalTense = grammaticalTense;
        this.grammaticalMood = grammaticalMood;
        this.grammaticalPerson = grammaticalPerson;
        this.grammaticalVoice = grammaticalVoice;
        this.grammaticalTransitivity = grammaticalTransitivity;
    }

    public GrammaticalTense grammaticalTense() {
        return grammaticalTense;
    }

    public GrammaticalMood grammaticalMood() {
        return grammaticalMood;
    }

    public GrammaticalPerson grammaticalPerson() {
        return grammaticalPerson;
    }

    public GrammaticalVoice grammaticalVoice() {
        return grammaticalVoice;
    }

    public GrammaticalTransitivity grammaticalTransitivity() {
        return grammaticalTransitivity;
    }

    @Override
    public String toString() {
        return lemma() + "(гл., " + grammaticalGender().description() + ", " +
                grammaticalTense().description + ", " + grammaticalMood().description + ", " +
                grammaticalPerson().description + ", " + grammaticalVoice().description + ", " +
                grammaticalTransitivity().description + ", " + (isPlural() ? "мн." : "ед.")+ ")";
    }

    public enum GrammaticalTense {
        PRAES("наст", "настоящее"),
        INPRAES("непрош", "непрошедшее"),
        PRAET("прош", "прошедшее");

        private final String description;
        private final String shortName;

        GrammaticalTense(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static GrammaticalTense parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(GrammaticalTense.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }

    public enum GrammaticalMood {
        GER("деепр", "деепричастие"),
        INF("инф", "инфинитив"),
        PARTCP("прич", "причастие"),
        INDIC("изъяв", "изьявительное наклонение"),
        IMPER("пов", "повелительное наклонение");

        private final String description;
        private final String shortName;

        GrammaticalMood(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static GrammaticalMood parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(GrammaticalMood.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }

    public enum GrammaticalPerson {
        P1("1-л", "1-е лицо"),
        P2("2-л", "2-е лицо"),
        P3("3-л", "3-е лицо");

        private final String description;
        private final String shortName;

        GrammaticalPerson(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static GrammaticalPerson parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(GrammaticalPerson.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }


    public enum GrammaticalVoice {
        ACT("действ", "действительный залог"),
        PASS("страд", "страдательный залог");

        private final String description;
        private final String shortName;

        GrammaticalVoice(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static GrammaticalVoice parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(GrammaticalVoice.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }

    public enum GrammaticalTransitivity {
        TRAN("пе", "переходный глагол"),
        INTR("нп", "непереходный глагол");

        private final String description;
        private final String shortName;

        GrammaticalTransitivity(String shortName, String description) {
            this.shortName = shortName;
            this.description = description;
        }

        public String description() {
            return this.description;
        }

        @Nullable
        private static GrammaticalTransitivity parse(CharSeq shortName) {
            //noinspection EqualsBetweenInconvertibleTypes
            return Stream.of(GrammaticalTransitivity.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
        }
    }

    static class Factory extends CorePOS.Factory {
        private GrammaticalTense grammaticalTense;
        private GrammaticalMood grammaticalMood;
        private GrammaticalPerson grammaticalPerson;
        private GrammaticalVoice grammaticalVoice;
        private GrammaticalTransitivity grammaticalTransitivity;

        public Factory() {
            super(PartOfSpeech.V);
        }

        @Override
        public Factory accept(CharSeq property) {
            super.accept(property);
            grammaticalTense = grammaticalTense != null ? grammaticalTense : GrammaticalTense.parse(property);
            grammaticalMood = grammaticalMood != null ? grammaticalMood : GrammaticalMood.parse(property);
            grammaticalPerson = grammaticalPerson != null ? grammaticalPerson : GrammaticalPerson.parse(property);
            grammaticalVoice = grammaticalVoice != null ? grammaticalVoice : GrammaticalVoice.parse(property);
            grammaticalTransitivity = grammaticalTransitivity != null ? grammaticalTransitivity : GrammaticalTransitivity.parse(property);
            return this;
        }

        @Override
        public LemmaInfo build() {
            return new Verb(lemma, weight, plural, grammaticalGender,
                    grammaticalTense, grammaticalMood, grammaticalPerson,
                    grammaticalVoice, grammaticalTransitivity);
        }
    }
}
