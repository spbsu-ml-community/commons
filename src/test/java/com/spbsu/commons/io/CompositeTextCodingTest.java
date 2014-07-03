package com.spbsu.commons.io;

import com.spbsu.commons.io.codec.ArithmeticCoding;
import com.spbsu.commons.io.codec.CompositeStatTextCoding;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.io.codec.seq.ListDictionary;
import com.spbsu.commons.seq.CharSeqTools;
import junit.framework.TestCase;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * User: solar
 * Date: 03.06.14
 * Time: 14:40
 */
public class CompositeTextCodingTest extends TestCase {
  public static CharSequence[] queries;
  public static CharSequence[] urls;
  public static CharSequence[] packages;

  static private synchronized void loadDataSet() {
    try {
      if (queries == null) {
        List<CharSequence> queries = new ArrayList<CharSequence>();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./commons/src/test/data/text/queries.txt.gz"))));
        String line;
        while((line = lnr.readLine()) != null) {
          queries.add(line + "\n");
        }
        CompositeTextCodingTest.queries = queries.toArray(new CharSequence[queries.size()]);
      }

      if (urls == null) {
        List<CharSequence> urls = new ArrayList<CharSequence>();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./commons/src/test/data/text/urls.txt.gz"))));
        String line;
        while((line = lnr.readLine()) != null) {
          urls.add(line + "\n");
        }
        CompositeTextCodingTest.urls = urls.toArray(new CharSequence[urls.size()]);
      }

      if (packages == null && false) {
        List<CharSequence> packs = new ArrayList<CharSequence>();
        File dir = new File("/Users/solar/Downloads/results");
        for (String packName : dir.list()) {
          final File packFile = new File(dir, packName);
          if (!packFile.isHidden() && !packFile.isDirectory())
            packs.add(StreamTools.readFile(packFile));
        }
        CompositeTextCodingTest.packages = packs.toArray(new CharSequence[packs.size()]);
      }
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

    CompositeStatTextCoding coding = new CompositeStatTextCoding(alpha, 1000);

    for (int i = 0; i < 100000; i++) {
      CharSequence query = queries[rng.nextInt(queries.length)];
      coding.accept(query);
    }

    final ByteBuffer buffer = ByteBuffer.allocate(bytes);
    final CompositeStatTextCoding.Encode encode = coding.new Encode(buffer);
    final ListDictionary result = coding.expansion().result();
//    for (CharSequence sequence : result.alphabet()) {
//      System.out.println(sequence);
//    }
    for (int i = 0; i < queries.length; i++) {
      CharSequence query = queries[i];
      encode.write(query);
    }
    System.out.println(result.alphabet().size() + " " + buffer.position());
    assertTrue(buffer.position() < 140000);
  }

  public void testSimpleCodingURL() {
    FastRandom rng = new FastRandom(0);
    int bytes = 0;
    final HashSet<Character> alpha = new HashSet<Character>();
    for (int i = 0; i < urls.length; i++) {
      CharSequence query = urls[i];
      bytes += 2 * query.length();
      for (int t = 0; t < query.length(); t++)
        alpha.add(query.charAt(t));
    }

    CompositeStatTextCoding coding = new CompositeStatTextCoding(alpha, 1000);

    for (int i = 0; i < 100000; i++) {
      final CharSequence query = urls[rng.nextInt(urls.length)];
      coding.accept(query);
    }

    final ByteBuffer buffer = ByteBuffer.allocate(bytes);
    final CompositeStatTextCoding.Encode encode = coding.new Encode(buffer);
    final ListDictionary dict = coding.expansion().result();

    int[] symbolFreqs = new int[dict.size()];
    for (int i = 0; i < urls.length; i++) {
      CharSequence suffix = urls[i];
      while(suffix.length() > 0) {
        final int symbol = dict.search(suffix);
        suffix = suffix.subSequence(dict.get(symbol).length(), suffix.length());
        symbolFreqs[symbol]++;
      }
    }

    int total = 0;
    int total1 = 0;
    int textLength = 0;
    double sum = 0;
    double sum1 = 0;
    for (int i = 0; i < dict.size(); i++) {
      final int freq = symbolFreqs[i];
      textLength += freq * dict.get(i).length();
      total += freq;
      final int freq1 = coding.expansion().resultFreqs()[i];
      total1 += freq1;
      if (freq > 0) {
        sum -= freq * Math.log(freq)/Math.log(2);
      }
      if (freq1 > 0) {
        sum1 -= freq * Math.log(freq1)/Math.log(2);
      }
    }
    final double codeLength = (sum + total * Math.log(total) / Math.log(2)) / 8;
    final double codeLength1 = (sum1 + total * Math.log(total1) / Math.log(2)) / 8;
    System.out.println("Expected code length: " + codeLength / 1024. + "kb. Expected rate: " + codeLength / textLength);
    final double rate = codeLength1 / textLength;
    System.out.println("Expected code length true: " + codeLength1 / 1024. + "kb. Expected rate true: " + rate);

//    final ListDictionary result = coding.expansion().result();
//    for (CharSequence sequence : result.alphabet()) {
//      System.out.println(sequence);
//    }
    encode.output = new ArithmeticCoding.Encoder(buffer, symbolFreqs);
    for (int i = 0; i < urls.length; i++) {
      CharSequence query = urls[i];
      encode.write(query);
    }
    System.out.println(dict.size() + " " + buffer.position());
    assertTrue(rate < 0.44);
  }

  public void untestPackageCoding() throws IOException {
    FastRandom rng = new FastRandom(0);
    long bytes = 0;
    final HashSet<Character> alpha = new HashSet<Character>();
    for (int i = 0; i < packages.length; i++) {
      CharSequence query = packages[i];
      bytes += 2 * query.length();
      for (int t = 0; t < query.length(); t++)
        alpha.add(query.charAt(t));
    }

    CompositeStatTextCoding coding = new CompositeStatTextCoding(alpha, 10000);

    for (int i = 0; i < 100000; i++) {
      CharSequence query = packages[rng.nextInt(packages.length)];
      coding.accept(query);
    }

    final ByteBuffer buffer = ByteBuffer.allocate((int)Math.min(1000000000l, bytes/10));
    final CompositeStatTextCoding.Encode encode = coding.new Encode(buffer);
    final ListDictionary result = coding.expansion().result();
    FileWriter output = new FileWriter("./out.dict");
    for (CharSequence sequence : result.alphabet()) {
      output.append(sequence).append("\n");
    }
    output.close();
    for (int i = 0; i < packages.length; i++) {
      CharSequence query = packages[i];
      encode.write(query);
    }
    System.out.println(result.alphabet().size() + " " + buffer.position());
    assertTrue(buffer.position() < 140000);
  }

}