package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class CorePOS extends LemmaInfo{
  private boolean plural;
  private GrammaticalGender grammaticalGender;

  public CorePOS(CharSeq lemma, double weight, PartOfSpeech pos, boolean plural, GrammaticalGender grammaticalGender) {
    super(lemma, weight, pos);
    this.plural = plural;
    this.grammaticalGender = grammaticalGender;
  }

  public boolean isPlural() {
    return plural;
  }

  public GrammaticalGender grammaticalGender() {
    return this.grammaticalGender;
  }

  @Override
  public String toString() {
    return lemma() + "(" + pos().description() + ", " + grammaticalGender.description + ", " + (isPlural() ? "мн." : "ед.") +")";
  }

  public enum GrammaticalGender {
    M("муж", "мужской род"),
    F("жен", "женский род"),
    N("сред", "средний род");

    private final String description;
    private final String shortName;

    GrammaticalGender(String shortName, String description) {
      this.shortName = shortName;
      this.description = description;
    }

    public String description() {
      return this.description;
    }

    @Nullable
    private static GrammaticalGender parse(CharSeq shortName) {
      //noinspection EqualsBetweenInconvertibleTypes
      return Stream.of(GrammaticalGender.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
    }
  }

  protected static class Factory extends LemmaInfo.DefaultFactory {
    protected boolean plural;
    protected GrammaticalGender grammaticalGender;

    public Factory(PartOfSpeech pos) {
      super(pos);
    }

    @Override
    public Factory accept(CharSeq property) {
      super.accept(property);
      //noinspection EqualsBetweenInconvertibleTypes
      if (property.equals("ед"))
        plural = false;
      //noinspection EqualsBetweenInconvertibleTypes
      if (property.equals("мн"))
        plural = true;
      grammaticalGender = grammaticalGender != null ? grammaticalGender : GrammaticalGender.parse(property);
      return this;
    }

    @Override
    public LemmaInfo build() {
      return new CorePOS(lemma, weight, pos, plural, grammaticalGender);
    }
  }
}
