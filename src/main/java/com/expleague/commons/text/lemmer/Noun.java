package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Noun extends CorePOS {
  private final GrammaticalCase grammaticalCase;
  private final boolean name;
  private final boolean animate;

    public Noun(CharSeq lemma, double weight, PartOfSpeech pos, boolean plural, GrammaticalGender grammaticalGender,
                GrammaticalCase grammaticalCase, boolean name, boolean animate) {
        super(lemma, weight, pos, plural, grammaticalGender);
        this.grammaticalCase = grammaticalCase;
        this.name = name;
        this.animate = animate;
    }

  public Noun(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
              GrammaticalCase grammaticalCase, boolean name, boolean animate) {
    super(lemma, weight, PartOfSpeech.S, plural, grammaticalGender);
    this.grammaticalCase = grammaticalCase;
    this.name = name;
    this.animate = animate;
  }

  public GrammaticalCase grammaticalCase() {
    return grammaticalCase;
  }

  public boolean isName() {
        return name;
    }

  public boolean isAnimate() {
    return animate;
  }

  @Override
  public String toString() {
    return lemma() + "(сущ., " + ", " + grammaticalGender().description() + ", " +
            grammaticalCase().description() + ", " + (isAnimate() ? "одуш." : "неодущ") + ", " +
            (isName() ? "имя" : "") + ", " + (isPlural() ? "мн." : "ед.") + ")";
  }

  static class Factory extends CorePOS.Factory {
    protected GrammaticalCase grammaticalCase;
    protected boolean name;
    protected boolean animate;

    public Factory(PartOfSpeech pos) {
          super(pos);
      }

    public Factory() {
      super(PartOfSpeech.S);
    }

    @Override
    public Factory accept(CharSeq property) {
      super.accept(property);
      if (property.equals("фам") || property.equals("отч") || property.equals("имя")) {
        name = true;
      }
      if (property.equals("од"))
          animate = true;
      if (property.equals("неод"))
          animate = false;
      grammaticalCase = grammaticalCase != null ? grammaticalCase : GrammaticalCase.parse(property);
      return this;
    }

    @Override
    public LemmaInfo build() {
      return new Noun(lemma, weight, plural, grammaticalGender, grammaticalCase, name, animate);
    }
  }
}
