package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;

public class LemmaInfo {
  private final CharSeq lemma;
  private final PartOfSpeech pos;
  private double weight;
  private boolean plural;

  public LemmaInfo(CharSeq lemma, double weight, PartOfSpeech pos, boolean plural) {
    this.lemma = lemma;
    this.pos = pos;
    this.weight = weight;
    this.plural = plural;
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

  public boolean isPlural() {
    return plural;
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
    protected boolean plural;

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
      //noinspection EqualsBetweenInconvertibleTypes
      if (property.equals("ед"))
        plural = false;
      //noinspection EqualsBetweenInconvertibleTypes
      if (property.equals("мн"))
        plural = true;
      return this;
    }

    @Override
    public LemmaInfo build() {
      return new LemmaInfo(lemma, weight, pos, plural);
    }
  }
}
