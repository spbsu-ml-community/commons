package com.expleague.commons.text.lemmer;

import java.util.function.Supplier;

public enum PartOfSpeech {
  A("прилагательное", Adjective.Factory::new),
  ADV("наречие", Adverb.Factory::new),
  ADVPRO("местоименное наречие", ProAdverb.Factory::new),
  ANUM("числительное-прилагательное", NumAdjective.Factory::new),
  APRO("местоимение-прилагательное", ProAdjective.Factory::new),
  COM("часть композита - сложного слова", Composite.Factory::new),
  CONJ("союз", Conjunction.Factory::new),
  INTJ("междометие", Interjection.Factory::new),
  NUM("числительное", Numeral.Factory::new),
  PART("частица", Particle.Factory::new),
  PR("предлог", Pretext.Factory::new),
  S("существительное", Noun.Factory::new),
  SPRO("местоимение-существительное", ProNoun.Factory::new),
  V("глагол", Verb.Factory::new);

  final String name;
  final Supplier<LemmaInfo.Factory> factory;

  PartOfSpeech(String description) {
    this.name = description;
    this.factory = () -> LemmaInfo.defaultFactory(this);
  }

  PartOfSpeech(String description, Supplier<LemmaInfo.Factory> factorySupplier) {
    this.name = description;
    this.factory = factorySupplier;
  }

  String description() {
    return name;
  }
}
