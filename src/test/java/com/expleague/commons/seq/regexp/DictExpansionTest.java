package com.expleague.commons.seq.regexp;


import com.expleague.commons.JUnitIOCapture;
import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.io.codec.seq.DictExpansion;
import com.expleague.commons.io.codec.seq.ListDictionary;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.*;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TLongIntMap;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 05.06.12
 * Time: 15:31
 */

public class DictExpansionTest extends JUnitIOCapture {

  public static final String ROOT_WIKI_FILE = System.getenv("HOME") + "/data/wiki/ru/" + "ruwiki-latest-pages-articles.xml";

  @Test
  public void testIndependent() throws Exception {
    ConversionRepository conversion = MathTools.CONVERSION;

    final List<Character> alpha = new ArrayList<>();
    for (char a = 'a'; a <= 'z'; a++)
      alpha.add(a);
    final Random rnd = new FastRandom();
    final DictExpansion<Character> de = new DictExpansion<>(alpha, alpha.size() + 100);
    for (int i = 0; i < 10000; i++) {
      final int len = rnd.nextInt(300);
      final StringBuilder builder = new StringBuilder(len);
      for (int c = 0; c < len; c++)
        builder.append((char)('a' + rnd.nextInt('z' - 'a' + 1)));
      de.accept(CharSeq.create(builder));
    }
    assertTrue('z' - 'a' + 5 > de.result().size());
  }

  @Test
  public void testRestore() throws Exception {
    final ListDictionary<Character> reference = new ListDictionary<Character>(
            CharSeq.create("a"),
            CharSeq.create("b"),
            CharSeq.create("c"),
            CharSeq.create("cc"),
            CharSeq.create("aa"),
            CharSeq.create("bb"));
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
        final List<Character> alpha = new ArrayList<>();
      for (char a = 'a'; a <= 'c'; a++) {
        alpha.add(a);
      }
      final FastRandom rnd = new FastRandom();
      final DictExpansion<Character> de = new DictExpansion<>(alpha, reference.size());
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 1000; j++) {
        final int len = rnd.nextInt(30);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.condition(rnd.nextSimple(probabs)));
        de.accept(CharSeq.create(builder));
  //      System.out.println(builder);
      }
      String resultAlpha = de.result().alphabet().toString();
//      System.out.println(resultAlpha + ": " + de.codeLengthPerChar());
      equalsAtLeastOnce = reference.alphabet().toString().equals(resultAlpha);
    }
    assertTrue(equalsAtLeastOnce);
  }

  @Test
  public void testRestoreAsym() throws Exception {
    final ListDictionary<Character> reference = new ListDictionary<Character>(
            CharSeq.create("a"),
            CharSeq.create("b"),
            CharSeq.create("c"),
            CharSeq.create("cc"),
            CharSeq.create("ab"),
            CharSeq.create("bb"));
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 30 && !equalsAtLeastOnce; i++) {
        final List<Character> alpha = new ArrayList<>();
      for (char a = 'a'; a <= 'c'; a++) {
        alpha.add(a);
      }
      final FastRandom rnd = new FastRandom();
      final DictExpansion<Character> de = new DictExpansion<>(alpha, reference.size());
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 50000; j++) {
        final int len = rnd.nextInt(30);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.condition(rnd.nextSimple(probabs)));
        de.accept(CharSeq.create(builder));
  //      System.out.println(builder);
      }
      String resultAlpha = de.result().alphabet().toString();
      System.out.println(reference.alphabet());
      System.out.println(resultAlpha);
      equalsAtLeastOnce = reference.alphabet().toString().equals(resultAlpha);
    }
    assertTrue(equalsAtLeastOnce);
  }

  @Test
  public void testRestoreLong() throws Exception {
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
      final ListDictionary<Character> reference = new ListDictionary<Character>(ArrayTools.map(
          new CharSequence[]{"daba", "carac", "abaa", "bab",
              "rabracadabra"},
          CharSeq.class, CharSeq::create));
      final ListDictionary<Character> start = new ListDictionary<Character>(ArrayTools.map(
          new CharSequence[]{"a", "b", "c", "r", "d"},
          CharSeq.class, CharSeq::create));
      final DictExpansion<Character> de = new DictExpansion<>(start, reference.size() + start.size());
      final FastRandom rng = new FastRandom();
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 1000; j++) {
        final int len = rng.nextInt(100);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.condition(rng.nextSimple(probabs)));
        de.accept(CharSeq.create(builder));
      }
      final List<? extends Seq<Character>> resultAlpha = de.result().alphabet();
      resultAlpha.removeAll(start.alphabet());
//      System.out.println(resultAlpha.toString() + ": " + de.codeLengthPerChar());
      equalsAtLeastOnce = reference.alphabet().toString().equals(resultAlpha.toString());
    }
    assertTrue(equalsAtLeastOnce);
  }

  public void notestRestoreRand() throws Exception {
    final FastRandom rng = new FastRandom();
    for (int i = 0; i < 100; i++) {
      final Set<CharSeq> known = new HashSet<>();
      //noinspection unchecked
      final Seq<Character>[] models = IntStream.range(0, 1000).mapToObj(j -> CharSeq.create(rng.nextBase64String(rng.nextPoisson(5) + 1))).filter(s -> !known.contains(s)).peek(known::add).toArray(Seq[]::new);
      //noinspection unchecked
      final Seq<Character>[] alpha = Stream.of(models).flatMap(s -> IntStream.range(0, s.length()).mapToObj(s::at)).sorted().collect(Collectors.toSet()).stream().map(CharSeqChar::new).toArray(Seq[]::new);

      final ListDictionary<Character> reference = new ListDictionary<>(models);
      final ListDictionary<Character> start = new ListDictionary<>(alpha);
      final DictExpansion<Character> de = new DictExpansion<>(start, reference.size() + start.size());
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      IntStream.range(0, 100000).parallel().forEach(j -> {
        final int len = rng.nextInt(100);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.condition(rng.nextSimple(probabs)));
        de.accept(CharSeq.create(builder));
      });
      final List<? extends Seq<Character>> resultAlpha = de.result().alphabet();
      resultAlpha.removeAll(start.alphabet());
      known.removeAll(resultAlpha);
      System.out.println("errors: " + (known.size() / (double) reference.size()));
      //      System.out.println(resultAlpha.toString() + ": " + de.codeLengthPerChar());
    }
  }

  @Test
  public void testOptimalParsing() throws Exception {
    final FastRandom rng = new FastRandom(0);
    for (int i = 0; i < 10; i++) {
      final Set<CharSeq> known = new HashSet<>();
      //noinspection unchecked
      final Seq<Character>[] models = IntStream.range(0, 1000).mapToObj(j ->
          j < 64 ? new CharSeqChar(FastRandom.BASE64_CHARS[j]) : CharSeq.create(rng.nextBase64String(rng.nextPoisson(5) + 1))
      ).filter(s -> !known.contains(s)).peek(known::add).toArray(Seq[]::new);

      ListDictionary<Character> reference = new ListDictionary<>(models);
      final Vec probabs = new ArrayVec(models.length);
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      TIntArrayList freqs = new TIntArrayList(models.length);
      for (int j = 0; j < models.length; j++) {
        freqs.add(0);
      }
      for (int j = 0; j < 100; j++) {
        final int len = rng.nextInt(10);
        if (len == 0)
          continue;

        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(models[rng.nextSimple(probabs)]);
        IntSeq parse = reference.parse(CharSeq.create(builder.toString()), freqs, freqs.sum());
        IntSeq parse1 = reference.parseEx(CharSeq.create(builder.toString()), freqs, freqs.sum());
        Assert.assertEquals(parse, parse1);
        parse.stream().forEach(idx -> freqs.setQuick(idx, freqs.getQuick(idx) + 1));
      }
    }
  }

  private <T extends Comparable<T>> boolean isSubstring(final Seq<T> s, final Seq<T> t) {
    // t is substr of s
    if (t.length() > s.length()) return false;
    for (int i = 0; i <= s.length() - t.length(); i++) {
      if (s.sub(i, i + t.length()).equals(t)) {
        //System.out.println(t + " is substr of " + s);
        return true;
      }
    }
    return false;
  }

  public void testOptimalReduce() {
    final List<Character> alphabet = new ArrayList<>();
    for (char a = 'a'; a <= 'c'; a++)
      alphabet.add(a);
    final Random rnd = new FastRandom(0);
    final DictExpansion<Character> de = new DictExpansion<>(alphabet, 10);
    for (int i = 0; i < 200; i++) {
      final int len = rnd.nextInt(150);
      final StringBuilder builder = new StringBuilder(len);
      for (int c = 0; c < len; c++)
        builder.append((char)('a' + rnd.nextInt('c' - 'a' + 1)));
      //System.out.println(CharSeq.create(builder));
      de.accept(CharSeq.create(builder));
    }
    System.out.println(de.result());
    /*System.out.println(isSubstring(CharSeq.create("aaa"), CharSeq.create("aa")));
    System.out.println(isSubstring(CharSeq.create("aaa"), CharSeq.create("aab")));
    System.out.println(isSubstring(CharSeq.create("aaab"), CharSeq.create("aab")));
    System.out.println(isSubstring(CharSeq.create("aaba"), CharSeq.create("aab")));*/
  }

  private <T extends Comparable<T>> T indexOfTwoStr(final Seq<T> first, final Seq<T> second, T betw, int ind) {
    if (ind >= 0 && ind < first.length()) {
      return first.at(ind);
    } else if (ind == first.length()) {
      return betw;
    } else if (ind > first.length() && ind < first.length() + 1 + second.length()) {
      return second.at(ind - first.length() - 1);
    } else {
      return null;
    }
  }

  private <T extends Comparable<T>> boolean isSubstring2(final Seq<T> s, final Seq<T> t) {
    // t is substr of s
    if (t.length() > s.length()) {
      return false;
    }
    T symb = null;
    int n = t.length() + 1 + s.length();
    int[] pi = new int[n];
    for (int i = 1; i < n; i++) {
      int j = pi[i-1];
      while (j > 0 && indexOfTwoStr(t, s, symb, i) != indexOfTwoStr(t, s, symb, j))
        j = pi[j-1];
      if (indexOfTwoStr(t, s, symb, i) == indexOfTwoStr(t, s, symb, j)) {
        j++;
      }
      if (j == t.length()) {
        return true;
      }
      pi[i] = j;
    }
    return false;
  }

  @Test
  public void testIsSubstring() {
    final Random rnd = new FastRandom(0);
    for (int i = 0; i < 10000; i++) {
      final int len1 = rnd.nextInt(150);
      final StringBuilder builder1 = new StringBuilder(len1);
      for (int c = 0; c < 1 + len1; c++)
        builder1.append((char)('a' + rnd.nextInt('z' - 'a' + 1)));
      final int len2 = rnd.nextInt(150);
      final StringBuilder builder2 = new StringBuilder(len1);
      for (int c = 0; c < 1 + len2; c++)
        builder2.append((char)('a' + rnd.nextInt('z' - 'a' + 1)));
      Assert.assertEquals(isSubstring(CharSeq.compact(builder1.toString()), CharSeq.compact(builder2.toString())),
              isSubstring2(CharSeq.compact(builder1.toString()), CharSeq.compact(builder2.toString())));
    }
  }

  @SuppressWarnings("unused")
  public void notestEnWikiConvert() throws Exception {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    final SAXParser parser = factory.newSAXParser();
    final File toParse = new File(ROOT_WIKI_FILE);
    final File output = new File(ROOT_WIKI_FILE + ".sentences");
    final PrintStream out = new PrintStream(new FileOutputStream(output));
//    final DictExpansion<Character> expansion = new DictExpansion<>(new HashSet<>(Arrays.asList('a')), 1000, System.out);
//    for (int i = 0; i < 1000; i++)
    parser.parse(new InputSource(new FileInputStream(toParse)), new DefaultHandler() {
      final StringBuilder path = new StringBuilder();
      final CharSeqBuilder builder = new CharSeqBuilder();
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        path.append("/").append(qName);
        builder.clear();
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if ("/mediawiki/page/revision/text".equals(path.toString())) {
          builder.appendCopy(ch, start, start + length);
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        String text = builder.build().toString();
        if (!text.startsWith("#REDIRECT") && !text.isEmpty()) {
          text = text.replaceAll("<!--[^>]*-->", "");
          text = text.replaceAll("<(?<tag>\\w+)[^>]*/>", "");
          String replaceAll = text;
          do {
            text = replaceAll;
            //noinspection MalformedRegex
            replaceAll = text.replaceAll("<(?<tag>\\w+)[^>]*>[^<]*</\\k<tag>>", "");
          }
          while(!text.equals(replaceAll));
          do {
            text = replaceAll;
            replaceAll = text.replaceAll("\\{\\{[^\\}^\\{]*\\}\\}", "");
          }
          while(!text.equals(replaceAll));
          do {
            text = replaceAll;
            replaceAll = text.replaceAll("\\{[^\\}\\{]*\\}", "");
          }
          while(!text.equals(replaceAll));

          do {
            text = replaceAll;
            replaceAll = text.replaceAll("\\[\\[[^\\]\\[]*\\]\\]", "");
          }
          while(!text.equals(replaceAll));
          do {
            text = replaceAll;
            replaceAll = text.replaceAll("\\[[^\\]\\[]*\\]", "");
          }
          while(!text.equals(replaceAll));
          text = text.replaceAll("\\([^)]*\\)", "");
          text = text.replaceAll("\\W\\w\\.", "");
          text = text.replace("===", "");
          text = text.replace("==", "");
          text = text.replace("=", " ");
          text = text.replace("&nbsp;", "");
          text = text.replace("'", "");
          text = text.replace("\"", "");
          text = text.replace("/", " ");
          text = text.replaceAll("\\s+,", ",");
          text = text.replaceAll(",+", "");
          text = text.replace(";", " ");
          text = text.replace("*", " ");
          text = text.replace(":", " ");
          text = text.replaceAll("\\s+", " ");
          final int references = text.lastIndexOf("References");
          if (references >= 0)
            text = text.substring(0, references);
          text = text.trim();
          final ReaderChopper chopper = new ReaderChopper(new StringReader(text));
          CharSequence next;
          while ((next = chopper.chopQuiet('.', '?', '!')) != null) {
            final String trim = next.toString().replaceAll("\\s+", " ").trim();
            if (trim.length() < 10)
              continue;
            out.println(trim);
//            expansion.accept(new CharSeqAdapter(trim));
          }
        }
        super.endElement(uri, localName, qName);
        path.delete(path.length() - qName.length() - 1, path.length());
      }
    });
  }

  @SuppressWarnings("unused")
  public void notestWiki() throws Exception {
    final File toParse = new File(ROOT_WIKI_FILE + ".sentences");
    final DictExpansion<Character> expansion = new DictExpansion<>(new HashSet<>(Collections.singletonList('a')), 1000, System.out);

    for (int i = 0; i < 1; i++) {
      CharSeqTools.processLines(new FileReader(toParse), new Consumer<CharSequence>() {
        int index = 0;
        @Override
        public void accept(CharSequence arg) {
          if (arg.length() < 150)
            return;
          expansion.accept(CharSeq.create(arg));
          if (++index % 10000 == 0)
            try {
              expansion.printPairs(new FileWriter(new File(ROOT_WIKI_FILE + ".dict")));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
        }
      });
    }
    expansion.printPairs(new FileWriter(new File(ROOT_WIKI_FILE + ".dict")));
    System.out.println();
//    final DictExpansion<Character> expansion = new DictExpansion<>(new HashSet<>(Arrays.asList('a')), 1000, System.out);
  }
}