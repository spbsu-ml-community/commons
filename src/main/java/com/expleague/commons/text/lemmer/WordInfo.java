package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class WordInfo {
  private final CharSeq token;
  private final List<LemmaInfo> lemmas;
  private final LemmaInfo bestLemma;

  public WordInfo(CharSeq token) {
    this.token = token;
    lemmas = Collections.emptyList();
    bestLemma = null;
  }

  public WordInfo(CharSeq token, List<LemmaInfo> lemmas) {
    this.token = token;
    this.lemmas = lemmas;
    bestLemma = lemmas.stream().max(
        Comparator.comparingDouble(lemma -> {
          double w = lemma.weight() + (token.equals(lemma.lemma()) ? 0.001 : 0);
          if (lemma instanceof Noun) {
            final Noun noun = (Noun) lemma;
            w += (noun.padezh() == Noun.Padezh.NOM ? 0.0001 : 0)
                + (noun.isPlural() ? -0.0002 : 0)
                + (noun.isName() ? -0.5 : 0);
          }
          w -= lemmas.indexOf(lemma) * 0.00001;
//          System.out.println(lemma + "\t" + w);
          return w;
        })
    ).orElse(null);
  }

  public CharSeq token() {
    return token;
  }

  public Stream<LemmaInfo> lemmas() {
    return lemmas.stream();
  }

  @Nullable
  public LemmaInfo lemma() {
    return bestLemma;
  }

  @Nullable
  public <T extends LemmaInfo> T as(PartOfSpeech pos) {
    //noinspection unchecked
    return bestLemma == null ? null : (T)(bestLemma.pos() == pos ? bestLemma : lemmas.stream().filter(l -> l.pos() == pos).findAny().orElse(null));
  }

  @Nullable
  public <T extends LemmaInfo> T as(Class<T> type) {
    //noinspection unchecked
    return bestLemma == null ? null : (T)(type.isAssignableFrom(bestLemma.getClass()) ? bestLemma : lemmas.stream().filter(l -> type.isAssignableFrom(l.getClass())).findAny().orElse(null));
  }

  @Override
  public String toString() {
    return token.toString();
  }
}
