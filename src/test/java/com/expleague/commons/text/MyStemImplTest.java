package com.expleague.commons.text;

import com.expleague.commons.text.lemmer.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

@SuppressWarnings("ConstantConditions")
public class MyStemImplTest {

  private MyStemImpl stemmer;

  @Before
  public void setUp() throws Exception {
    stemmer = new MyStemImpl(Paths.get("/home/mpikalov/Downloads/SimpleSearch/mystem"));
  }

  @Test
  public void testTokens() {
    Assert.assertEquals("[Hello, world]", stemmer.parse("Hello world").toString());
    Assert.assertEquals("[кошкин, дома]", stemmer.parse("кошкин дома").toString());
    Assert.assertEquals("[кошкин, 5, дома]", stemmer.parse("кошкин 5 дома").toString());
  }

  @Test
  public void testPOS() {
    Assert.assertEquals(PartOfSpeech.S, stemmer.parse("дом").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.A, stemmer.parse("качественный").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.ADV, stemmer.parse("быстро").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.ANUM, stemmer.parse("двенадцатый").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.CONJ, stemmer.parse("и").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.INTJ, stemmer.parse("ах").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.ADVPRO, stemmer.parse("здесь").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.NUM, stemmer.parse("двенадцать").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.PART, stemmer.parse("не").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.PR, stemmer.parse("по").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.SPRO, stemmer.parse("нечто").get(0).lemma().pos());
    Assert.assertEquals(PartOfSpeech.V, stemmer.parse("бежать").get(0).lemma().pos());
  }

  @Test
  public void testGrammaticalNumber() {
    Assert.assertEquals(true, stemmer.parse("замки").get(0).as(Noun.class).isPlural());
    Assert.assertEquals(false, stemmer.parse("замок").get(0).as(Noun.class).isPlural());
    Assert.assertEquals(false, stemmer.parse("пятнадцатый").get(0).as(NumAdjective.class).isPlural());
    Assert.assertEquals(true, stemmer.parse("пятнадцатые").get(0).as(NumAdjective.class).isPlural());
    Assert.assertEquals(false, stemmer.parse("медленный").get(0).as(Adjective.class).isPlural());
    Assert.assertEquals(true, stemmer.parse("медленные").get(0).as(Adjective.class).isPlural());
    Assert.assertEquals(false, stemmer.parse("бежал").get(0).as(Verb.class).isPlural());
    Assert.assertEquals(true, stemmer.parse("бежали").get(0).as(Verb.class).isPlural());
    Assert.assertEquals(false, stemmer.parse("оба").get(0).as(Numeral.class).isPlural());
  }

  @Test
  public void testGrammaticalGender() {
    Assert.assertEquals(CorePOS.GrammaticalGender.M, stemmer.parse("стол").get(0).as(Noun.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.F, stemmer.parse("карта").get(0).as(Noun.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.N, stemmer.parse("облако").get(0).as(Noun.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.M, stemmer.parse("качественный").get(0).as(Adjective.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.F, stemmer.parse("качественная").get(0).as(Adjective.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.N, stemmer.parse("качественное").get(0).as(Adjective.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.M, stemmer.parse("шел").get(0).as(Verb.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.F, stemmer.parse("шла").get(0).as(Verb.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.N, stemmer.parse("шло").get(0).as(Verb.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.M, stemmer.parse("оба").get(0).as(Numeral.class).grammaticalGender());
    Assert.assertEquals(CorePOS.GrammaticalGender.F, stemmer.parse("обе").get(0).as(Numeral.class).grammaticalGender());
  }

  @Test
  public void testGrammaticalCase() {
    Assert.assertEquals(GrammaticalCase.NOM, stemmer.parse("замок").get(0).as(Noun.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.GEN, stemmer.parse("строим дома").get(1).as(Noun.class).grammaticalCase());
    Assert.assertEquals(null, stemmer.parse("отпугиватель").get(0).as(Noun.class));
    Assert.assertEquals(GrammaticalCase.ACC, stemmer.parse("качественный").get(0).as(Adjective.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.DAT, stemmer.parse("полным").get(0).as(Adjective.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.ABL, stemmer.parse("машине").get(0).as(Noun.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.INS, stemmer.parse("камнем").get(0).as(Noun.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.VOC, stemmer.parse("боже").get(0).as(Noun.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.ACC, stemmer.parse("пятьдесят").get(0).as(Numeral.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.ABL, stemmer.parse("пятидесятых").get(0).as(NumAdjective.class).grammaticalCase());
  }

  @Test
  public void testGrammaticalForm() {
    Assert.assertEquals(Adjective.GrammaticalForm.BREV, stemmer.parse("свеж").get(0).as(Adjective.class).grammaticalForm());
    Assert.assertEquals(Adjective.GrammaticalForm.PLEN, stemmer.parse("свежий").get(0).as(Adjective.class).grammaticalForm());
    Assert.assertEquals(null, stemmer.parse("пятидесятый").get(0).as(NumAdjective.class).grammaticalForm());
  }

  @Test
  public void testComparisonDegree() {
    Assert.assertEquals(null, stemmer.parse("сильный").get(0).as(Adjective.class).comparisonDegree());
    Assert.assertEquals(Adjective.ComparisonDegree.COMP, stemmer.parse("сильнее").get(0).as(Adjective.class).comparisonDegree());
    Assert.assertEquals(Adjective.ComparisonDegree.SUPR, stemmer.parse("сильнейший").get(0).as(Adjective.class).comparisonDegree());
  }

  @Test
  public void testGrammaticalAnimacy() {
    Assert.assertEquals(false, stemmer.parse("сильный").get(0).as(Adjective.class).isAnimate());
    Assert.assertEquals(true, stemmer.parse("зверь").get(0).as(Noun.class).isAnimate());
    Assert.assertEquals(false, stemmer.parse("стол").get(0).as(Noun.class).isAnimate());
    Assert.assertEquals(true, stemmer.parse("комар").get(0).as(Noun.class).isAnimate());
    Assert.assertEquals(false, stemmer.parse("кирпич").get(0).as(Noun.class).isAnimate());
  }
}
