package com.spbsu.commons.io;

import com.spbsu.commons.io.codec.ArithmeticCoding;
import com.spbsu.commons.io.codec.CompositeStatTextCoding;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.ListDictionary;
import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

/**
 * User: solar
 * Date: 03.06.14
 * Time: 14:40
 */
public class CompositeTextCodingTest extends TestCase {
  public static CharSequence[] queries;
  public static CharSequence[] urls;

  static private synchronized void loadDataSet() {
    try {
      if (queries == null) {
        List<CharSequence> queries = new ArrayList<CharSequence>();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./commons/tests/data/text/queries_all.tsv.gz"))));
        String line;
        while((line = lnr.readLine()) != null) {
          StringTokenizer tok = new StringTokenizer(line, "\t");
          tok.nextToken(); // skip index
          queries.add(tok.nextToken());
        }
        CompositeTextCodingTest.queries = queries.toArray(new CharSequence[queries.size()]);
      }

//      if (urls == null) {
//        List<CharSequence> urls = new ArrayList<CharSequence>();
//        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./commons/tests/data/text/urls.txt.gz"))));
//        String line;
//        while((line = lnr.readLine()) != null) {
//          StringTokenizer tok = new StringTokenizer(line, "\t");
////          tok.nextToken(); // skip query
////          tok.nextToken(); // skip relev
//          urls.add(tok.nextToken() + "\n");
//        }
//        CompositeTextCodingTest.urls = urls.toArray(new CharSequence[urls.size()]);
//      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void setUp() throws Exception {
    loadDataSet();
  }

  public void testSimpleCodingQuery() {
    FastRandom rng = new FastRandom(0);
    int bytes = 0;
    final HashSet<Character> alpha = new HashSet<Character>();
    for (int i = 0; i < queries.length; i++) {
      CharSequence query = queries[i];
      bytes += 2 * query.length();
      for (int t = 0; t < query.length(); t++)
        alpha.add(query.charAt(t));
    }

    CompositeStatTextCoding coding = new CompositeStatTextCoding(alpha, 50000);

    for (int i = 0; i < 200000; i++) {
      CharSequence query = queries[rng.nextInt(queries.length)];
      coding.accept(query);
    }

    final ByteBuffer buffer = ByteBuffer.allocate(bytes);
    final ListDictionary result = coding.expansion().result();
    final CompositeStatTextCoding.Encode encode = coding.new Encode(buffer);
    for (CharSequence sequence : result.alphabet()) {
      System.out.println(sequence);
    }
    for (int i = 0; i < queries.length; i++) {
      CharSequence query = queries[i];
      encode.write(query);
    }
    System.out.println(result.alphabet().size() + " " + buffer.position());
  }

//  public void testSimpleCodingURL() {
//    FastRandom rng = new FastRandom(0);
//    int bytes = 0;
//    final HashSet<Character> alpha = new HashSet<Character>();
//    for (int i = 0; i < urls.length; i++) {
//      CharSequence query = urls[i];
//      bytes += 2 * query.length();
//      for (int t = 0; t < query.length(); t++)
//        alpha.add(query.charAt(t));
//    }
//
//    CompositeStatTextCoding coding = new CompositeStatTextCoding(alpha, 100000);
//
//    for (int i = 0; i < 50000000; i++) {
//      final CharSequence query = urls[rng.nextInt(urls.length)];
//      coding.accept(query);
//    }
//
//    final ByteBuffer buffer = ByteBuffer.allocate(bytes);
//    final CompositeStatTextCoding.Encode encode = coding.new Encode(buffer);
////    for (int i = 0; i < 100 && i < coding.expansion().expansion().length; i++) {
////      System.out.println(coding.expansion().expansion()[i]);
////    }
//    double sum = 0;
//    final ListDictionary dict = coding.expansion().result();
//
//    int[] symbolFreqs = new int[dict.size()];
//    for (int i = 0; i < urls.length; i++) {
//      CharSequence suffix = urls[i];
//      while(suffix.length() > 0) {
//        final int symbol = dict.search(suffix);
//        suffix = suffix.subSequence(dict.get(symbol).length(), suffix.length());
//        symbolFreqs[symbol]++;
//      }
//    }
//
//    int total = 0;
//    int textLength = 0;
//    for (int i = 0; i < dict.size(); i++) {
//      final int freq = symbolFreqs[i];
//      textLength += freq * dict.get(i).length();
//      total += freq;
//      if (freq > 0) {
//        sum -= freq * Math.log(symbolFreqs[i])/Math.log(2);
//      }
//    }
//    final double codeLength = (sum + total * Math.log(total) / Math.log(2)) / 8;
//    System.out.println("Expected code length: " + codeLength / 1024. + "kb. Expected rate: " + codeLength / textLength);
//
//    for (int i = 0; i < urls.length; i++) {
//      CharSequence query = urls[i];
//      encode.write(query);
//    }
//    System.out.println(dict.size() + " " + buffer.position());
//  }
}
