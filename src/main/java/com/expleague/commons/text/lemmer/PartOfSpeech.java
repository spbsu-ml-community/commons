package com.expleague.commons.text.lemmer;

import java.util.function.Supplier;

public enum PartOfSpeech {
  A("прилагательное"),
  ADV("наречие"),
  ADVPRO("местоименное наречие"),
  ANUM("числительное-прилагательное"),
  APRO("местоимение-прилагательное"),
  COM("часть композита - сложного слова"),
  CONJ("союз"),
  INTJ("междометие"),
  NUM("числительное"),
  PART("частица"),
  PR("предлог"),
  S("существительное", Noun.Factory::new),
  SPRO("местоимение-существительное"),
  V("глагол");

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
