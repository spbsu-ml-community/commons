package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class Noun extends LemmaInfo {
  private final Padezh padezh;
  private final boolean name;

  public Noun(CharSeq lemma, double weight, Padezh padezh, boolean plural, boolean name) {
    super(lemma, weight, PartOfSpeech.S, plural);
    this.padezh = padezh;
    this.name = name;
  }

  public Padezh padezh() {
    return padezh;
  }

  @Override
  public String toString() {
    return lemma() + "(сущ., " + padezh().description + ", " + (isPlural() ? "мн." : "ед.")+ ")";
  }

  public boolean isName() {
    return name;
  }

  public enum Padezh {
    NOM("им", "именительный"),
    GEN("род", "родительный"),
    DAT("дат", "дательный"),
    ACC("вин", "винительный"),
    INS("твор", "творительный"),
    ABL("пр", "предложный"),
    PART("парт", "партитив (второй родительный)"),
    LOC("местн", "местный (второй предложный)"),
    VOC("зват", "звательный");

    private final String description;
    private final String shortName;

    Padezh(String shortName, String description) {
      this.shortName = shortName;
      this.description = description;
    }

    public String description() {
      return this.description;
    }

    @Nullable
    private static Padezh parse(CharSeq shortName) {
      //noinspection EqualsBetweenInconvertibleTypes
      return Stream.of(Padezh.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
    }
  }

  static class Factory extends LemmaInfo.DefaultFactory {
    private Padezh padezh;
    private boolean name;
    public Factory() {
      super(PartOfSpeech.S);
    }

    @Override
    public Factory accept(CharSeq property) {
      super.accept(property);
      if (property.equals("фам") || property.equals("отч") || property.equals("имя")) {
        name = true;
      }
      padezh = padezh != null ? padezh : Padezh.parse(property);
      return this;
    }

    @Override
    public LemmaInfo build() {
      return new Noun(lemma, weight, padezh, plural, name);
    }
  }
}
