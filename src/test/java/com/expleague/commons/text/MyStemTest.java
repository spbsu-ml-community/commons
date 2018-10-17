package com.expleague.commons.text;

import com.expleague.commons.text.lemmer.GrammaticalCase;
import com.expleague.commons.text.lemmer.MyStem;
import com.expleague.commons.text.lemmer.Noun;
import com.expleague.commons.text.lemmer.PartOfSpeech;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

@SuppressWarnings("ConstantConditions")
public class MyStemTest {
  @Test
  public void testTokens() {
    MyStem stemmer = new MyStem(Paths.get("/Users/solar/bin/mystem"));
    Assert.assertEquals("[Hello, world]", stemmer.parse("Hello world").toString());
    Assert.assertEquals("[кошкин, дома]", stemmer.parse("кошкин дома").toString());
    Assert.assertEquals("[кошкин, 5, дома]", stemmer.parse("кошкин 5 дома").toString());
  }

  @Test
  public void testPOS() {
    MyStem stemmer = new MyStem(Paths.get("/Users/solar/bin/mystem"));
    Assert.assertEquals(PartOfSpeech.S, stemmer.parse("дом").get(0).lemma().pos());
  }

  @Test
  public void testPadezh() {
    MyStem stemmer = new MyStem(Paths.get("/Users/solar/bin/mystem"));
    Assert.assertEquals(GrammaticalCase.NOM, stemmer.parse("замок").get(0).as(Noun.class).grammaticalCase());
    Assert.assertEquals(GrammaticalCase.GEN, stemmer.parse("строим дома").get(1).as(Noun.class).grammaticalCase());
    Assert.assertEquals(null, stemmer.parse("отпугиватель").get(0).as(Noun.class));
  }
}
