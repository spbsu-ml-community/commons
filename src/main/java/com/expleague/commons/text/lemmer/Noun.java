package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class Noun extends CorePOS {
  private final GrammaticalCase grammaticalCase;
  private final boolean isPartOfName;
  private final boolean grammaticalAnimacy;

    public Noun(CharSeq lemma, double weight, PartOfSpeech pos, boolean plural, GrammaticalGender grammaticalGender,
                GrammaticalCase grammaticalCase, boolean isPartOfName, boolean grammaticalAnimacy) {
        super(lemma, weight, pos, plural, grammaticalGender);
        this.grammaticalCase = grammaticalCase;
        this.isPartOfName = isPartOfName;
        this.grammaticalAnimacy = grammaticalAnimacy;
    }

  public Noun(CharSeq lemma, double weight, boolean plural, GrammaticalGender grammaticalGender,
              GrammaticalCase grammaticalCase, boolean isPartOfName, boolean grammaticalAnimacy) {
    super(lemma, weight, PartOfSpeech.S, plural, grammaticalGender);
    this.grammaticalCase = grammaticalCase;
    this.isPartOfName = isPartOfName;
    this.grammaticalAnimacy = grammaticalAnimacy;
  }

  public GrammaticalCase grammaticalCase() {
    return grammaticalCase;
  }

  public boolean isName() {
        return isPartOfName;
    }

  public boolean isAnimate() {
    return grammaticalAnimacy;
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
    protected boolean grammaticalAnimacy;

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
        grammaticalAnimacy = true;
      if (property.equals("неод"))
        grammaticalAnimacy = false;
      grammaticalCase = grammaticalCase != null ? grammaticalCase : GrammaticalCase.parse(property);
      return this;
    }

    @Override
    public LemmaInfo build() {
      return new Noun(lemma, weight, plural, grammaticalGender, grammaticalCase, name, grammaticalAnimacy);
    }
  }
}
