package com.spbsu.commons.text.charset.bigram;

import junit.framework.TestCase;

import java.nio.charset.Charset;

import com.spbsu.commons.text.charset.TextDecoderTools;

/**
 * @author lyadzhin
 */
//todo: test 'ё' (include in stat.)
//todo: collision - mac & win1251
public class BigramsTextDecoderTest extends TestCase {
  public void testEnglishText() {
    performTest("Republican Mike Huckabee wins in Iowa's caucuses, with Barack Obama" +
                    " victorious in the Democratic race");
  }

  public void testEnglishWithVerySmallAmountOfRussian() {
    performTest("Бам! Republican Mike Huckabee wins in Iowa's caucuses, with Barack Obama" +
                    " victorious in the Democratic race");
  }

  public void testEnglishWithSmallAmountOfRussian() {
    performTest("Новусть: Republican Mike Huckabee wins in Iowa's caucuses, with Barack Obama" +
                    " victorious in the Democratic race");
    performTest("Новасть: Republican Mike Huckabee wins in Iowa's caucuses, with Barack Obama" +
                    " victorious in the Democratic race");
    performTest("Новость: Republican Mike Huckabee wins in Iowa's caucuses, with Barack Obama" +
                    " victorious in the Democratic race");
  }

  public void testAbracadabra() {
    performTest("афыафываьлвлдаифтадилтолауитлоктилоуткоуп");
    performTest("ешщошщроекшщодывтиащойцуаощшй  цуоашйцашцаш");
    performTest("мтьдавмтлоатвмло твамтло атмлтамлвамлыавтоватмвамтывмтловмлтвм");
    performTest("оапуопщшуцокпшщоуцкпшощшкуцпощш оуцпкокупощшукопшцукпшуцкпощшупцуопцкп");
  }

  public void testEmptyBytes() {
    performTest("");
  }

  public void testOneCyrillicWord() {
    performTest("чебурашка");
    performTest("банан");
    performTest("апельсин");
    performTest("мандарин");
    performTest("водка");
  }

  public void testTwoCyrillicWords() {
    performTest("зеленый чебурашка");
    performTest("красный банан");
    performTest("сиреневый апельсин");
    performTest("пурпурный мандарин");
    //performTest("синяя водка");
  }

  public void testCyrillicText() {
    performTest("Актерской профессией увлекался еще в детстве. Учась во второй школе города " +
            "Ейска посещал театральный кружок.");
  }

  public void testCaseSensitivity() {
    performTest("АкТеРсКОй пРоФесСиЕй УВлекАЛся еще в дЕтстве. УчАСь во ВТОРОЙ ШКОЛЕ ГОРОДА " +
            "ЕЙСКА ПОСЕЩАЛ ТЕАТРАЛЬНЫЙ КРУЖОК.");
    /*performTest("актерской профессией увлекался еще в детстве. учась во второй школе города " +
            "ейска посещал театральный кружок.");*/
    /*performTest("АКТЕРСКОЙ ПРОФЕССИЕЙ УВЛЕКАЛСЯ ЕЩЁ В ДЕТСТВЕ. УЧАСЬ ВО ВТОРОЙ ШКОЛЕ ГОРОДА " +
            "ЕЙСКА ПОСЕЩАЛ ТЕАТРАЛЬНЫЙ КРУЖОК.");*/
  }

  public void testOldText() {
    performTest("Не лепо ли ны бяшетъ, братие,\n" +
            "начяти старыми словесы\n" +
            "трудныхъ повестий о пълку Игореве,\n" +
            "Игоря Святъславлича?\n" +
            "Начати же ся тъй песни\n" +
            "по былинамь сего времени,\n" +
            "а не по замышлению Бояню!\n" +
            "Боянъ бо вещий,\n" +
            "аще кому хотяше песнь творити,\n" +
            "то растекашется мыслию по древу,\n" +
            "серымъ вълкомъ по земли,\n" +
            "шизымъ орломъ подъ облакы.\n" +
            "Помняшеть бо рече,\n" +
            "първыхъ временъ усобице.\n" +
            "Тогда пущашеть 10 соколовь на стадо лебедей;\n" +
            "который дотечаше,\n" +
            "та преди песнь пояше -\n" +
            "старому Ярославу,\n" +
            "храброму Мстиславу,\n" +
            "иже зареза Редедю предъ пълкы касожьскыми,\n" +
            "красному Романови Святъславличю.\n" +
            "Боянъ же, братие, не 10 соколовь\n" +
            "на стадо лебедей пущаше,\n" +
            "нъ своя вещиа пръсты\n" +
            "на живая струны въскладаше;\n" +
            "они же сами княземъ славу рокотаху.\n" +
            "\n" +
            "Почнемъ же, братие, повесть сию\n" +
            "отъ стараго Владимера до ныняшнего Игоря,\n" +
            "иже истягну умь крепостию своею\n" +
            "и поостри сердца своего мужествомъ,\n" +
            "наполънився ратнаго духа,\n" +
            "наведе своя храбрыя плъкы\n" +
            "на землю Половецькую\n" +
            "за землю Руськую.");
  }

  private void performTest(String s) {
    for (String charset : TextDecoderTools.CYRILLIC_CHARSETS) {
      final byte[] bytes = s.getBytes(Charset.forName(charset));
      assertEquals(charset, s, TextDecoderTools.decodeCyrillicText(bytes));
    }
  }
}