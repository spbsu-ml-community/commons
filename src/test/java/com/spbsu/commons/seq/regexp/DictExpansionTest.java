package com.spbsu.commons.seq.regexp;


import com.spbsu.commons.func.Processor;
import com.spbsu.commons.io.codec.seq.DictExpansion;
import com.spbsu.commons.io.codec.seq.ListDictionary;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.CharSeqBuilder;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.ReaderChopper;
import com.spbsu.commons.util.ArrayTools;
import junit.framework.TestCase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 05.06.12
 * Time: 15:31
 */

public class DictExpansionTest extends TestCase {

  public static final String ROOT_WIKI_FILE = System.getenv("HOME") + "/data/wiki/ru/" + "ruwiki-latest-pages-articles.xml";

  public void testIndependent() throws Exception {
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
    assertEquals('z' - 'a' + 1, de.result().size());
  }

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
      for (int j = 0; j < 10000; j++) {
        final int len = rnd.nextInt(30);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.get(rnd.nextSimple(probabs)));
        de.accept(CharSeq.create(builder));
  //      System.out.println(builder);
      }
      String resultAlpha = de.result().alphabet().toString();
      System.out.println(resultAlpha);
      equalsAtLeastOnce = reference.alphabet().toString().equals(resultAlpha);
    }
    assertTrue(equalsAtLeastOnce);
  }

  public void testRestoreLong() throws Exception {
    boolean equalsAtLeastOnce = false;
    for (int i = 0; i < 10 && !equalsAtLeastOnce; i++) {
      final ListDictionary<Character> reference = new ListDictionary<Character>(ArrayTools.map(
          new CharSequence[]{"a", "b", "c", "r", "d", "cc", "aa", "bb", "rabracadabra"},
          CharSeq.class, CharSeq::create));
      final ListDictionary<Character> start = new ListDictionary<Character>(ArrayTools.map(
          new CharSequence[]{"a", "b", "c", "r", "d"},
          CharSeq.class, CharSeq::create));
      final DictExpansion<Character> de = new DictExpansion<>(start, reference.size());
      final FastRandom rng = new FastRandom();
      final Vec probabs = new ArrayVec(reference.size());
      VecTools.fill(probabs, 1.);
      VecTools.normalizeL1(probabs);
      for (int j = 0; j < 10000; j++) {
        final int len = rng.nextInt(100);
        final StringBuilder builder = new StringBuilder(len);
        for (int c = 0; c < len; c++)
          builder.append(reference.get(rng.nextSimple(probabs)));
        de.accept(CharSeq.create(builder));
      }
      equalsAtLeastOnce = reference.alphabet().toString().equals(de.result().alphabet().toString());
    }
    assertTrue(equalsAtLeastOnce);
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
          builder.append(ch, start, start + length);
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
      CharSeqTools.processLines(new FileReader(toParse), new Processor<CharSequence>() {
        int index = 0;
        @Override
        public void process(CharSequence arg) {
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