package com.spbsu.commons.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;


import com.spbsu.commons.io.codec.ArithmeticCoding;
import com.spbsu.commons.io.codec.CompositeStatTextCoding;
import com.spbsu.commons.io.codec.seq.DictExpansion;
import com.spbsu.commons.io.codec.seq.ListDictionary;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.ByteSeq;
import com.spbsu.commons.seq.CharSeqAdapter;
import com.spbsu.commons.seq.Seq;
import junit.framework.TestCase;

/**
 * User: solar
 * Date: 03.06.14
 * Time: 14:40
 */
public class CompositeTextCodingTest extends TestCase {
  public static CharSequence[] queries;
  public static CharSequence[] urls;
  public static CharSequence[] packages;
  public static CharSequence[] user_sessions;

  private static synchronized void loadDataSet() {
    try {
      if (queries == null) {
        List<CharSequence> queries = new ArrayList<CharSequence>();
        try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./commons/src/test/data/text/queries.txt.gz"))))) {
          String line;
          while ((line = lnr.readLine()) != null) {
            queries.add(line + "\n");
          }
          CompositeTextCodingTest.queries = queries.toArray(new CharSequence[queries.size()]);
        }
      }

      if (urls == null) {
        List<CharSequence> urls = new ArrayList<CharSequence>();
        try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./commons/src/test/data/text/urls.txt.gz"))))) {
          String line;
          while ((line = lnr.readLine()) != null) {
            urls.add(line + "\n");
          }
          CompositeTextCodingTest.urls = urls.toArray(new CharSequence[urls.size()]);
        }
      }

      if (packages == null && false) {
        List<CharSequence> packs = new ArrayList<>();
        File dir = new File("/Users/solar/Downloads/results");
        for (String packName : dir.list()) {
          final File packFile = new File(dir, packName);
          if (!packFile.isHidden() && !packFile.isDirectory())
            packs.add(StreamTools.readFile(packFile));
        }
        CompositeTextCodingTest.packages = packs.toArray(new CharSequence[packs.size()]);
      }
      if (user_sessions == null) {
        List<CharSequence> user_sessions = new ArrayList<>();
        try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("/Users/solar/Downloads/session-sample.txt.gz"))))){
          String line;
          while ((line = lnr.readLine()) != null) {
            user_sessions.add(line + "\n");
            if (user_sessions.size() > 10000)
              break;
          }
          CompositeTextCodingTest.user_sessions = user_sessions.toArray(new CharSequence[user_sessions.size()]);
        }
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
        final int symbol = dict.search(new CharSeqAdapter(suffix));
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

  public void testSimpleByteCodingURL() {
    FastRandom rng = new FastRandom(0);
    int bytes = 0;
    final HashSet<Byte> alpha = new HashSet<>();
    for (int i = 0; i < urls.length; i++) {
      CharSequence query = urls[i];
      bytes += 2 * query.length();
      for (int t = 0; t < query.length(); t++)
        alpha.add((byte)query.charAt(t));
    }

    DictExpansion<Byte> expansion = new DictExpansion<>(alpha, 1000);

    final Charset charset = Charset.forName("UTF-8");
    for (int i = 0; i < 100000; i++) {
      final ByteSeq query = new ByteSeq(urls[rng.nextInt(urls.length)].toString().getBytes(charset));
      expansion.accept(query);
    }

    final ListDictionary dict = expansion.result();

    int[] symbolFreqs = new int[dict.size()];
    for (int i = 0; i < urls.length; i++) {
      CharSequence suffix = urls[i];
      while(suffix.length() > 0) {
        final int symbol = dict.search(new ByteSeq(suffix.toString().getBytes(charset)));
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
      final int freq1 = expansion.resultFreqs()[i];
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
//    encode.output = new ArithmeticCoding.Encoder(buffer, symbolFreqs);
//    for (int i = 0; i < urls.length; i++) {
//      CharSequence query = urls[i];
//      encode.write(query);
//    }
//    System.out.println(dict.size() + " " + buffer.position());
//    assertTrue(rate < 0.44);
  }

  public void testPackageCoding() throws IOException {
    if (packages == null)
      return;
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
    final ListDictionary<Character> result = coding.expansion().result();
    FileWriter output = new FileWriter("./out.dict");
    for (Seq<Character> sequence : result.alphabet()) {
      output.append(sequence.toString()).append("\n");
    }
    output.close();
    for (int i = 0; i < packages.length; i++) {
      CharSequence query = packages[i];
      encode.write(query);
    }
    System.out.println(result.alphabet().size() + " " + buffer.position());
    assertTrue(buffer.position() < 140000);
  }

  public void testUserSessionsCoding() throws IOException {
    if (user_sessions == null)
      return;
    FastRandom rng = new FastRandom(0);
    long bytes = 0;
    final HashSet<Character> alpha = new HashSet<>();
    for (int i = 0; i < user_sessions.length; i++) {
      CharSequence query = user_sessions[i];
      bytes += 2 * query.length();
      for (int t = 0; t < query.length(); t++)
        alpha.add(query.charAt(t));
    }

    CompositeStatTextCoding coding = new CompositeStatTextCoding(alpha, 100000);

    for (int i = 0; i < 1000000; i++) {
      CharSequence query = user_sessions[rng.nextInt(user_sessions.length)];
      coding.accept(query);
    }

    final ByteBuffer buffer = ByteBuffer.allocate((int)Math.min(1000000000l, bytes/10));
    final CompositeStatTextCoding.Encode encode = coding.new Encode(buffer);
    final ListDictionary<Character> result = coding.expansion().result();
    FileWriter output = new FileWriter("./out.dict");
    for (Seq<Character> sequence : result.alphabet()) {
      output.append(sequence.toString()).append("\n");
    }
    output.close();
    for (int i = 0; i < user_sessions.length; i++) {
      CharSequence query = user_sessions[i];
      encode.write(query);
    }
    System.out.println(result.alphabet().size() + " " + buffer.position());
  }

}
