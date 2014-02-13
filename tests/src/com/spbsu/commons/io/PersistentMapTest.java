package com.spbsu.commons.io;

import com.spbsu.commons.func.converters.CharArrayCSFactory;
import com.spbsu.commons.func.converters.CharSequence2BufferConverter;
import com.spbsu.commons.func.converters.StringCSFactory;
import com.spbsu.commons.io.persist.PersistentMap;
import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: terry
 * Date: 07.11.2009
 * Time: 15:55:48
 * To change this template use File | Settings | File Templates.
 */
public class PersistentMapTest extends TestCase {

  private File file;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    file = File.createTempFile("persist", "");
  }

  public PersistentMap<String, String> createMap() throws IOException {
    return createMap(file);
  }

  public PersistentMap<String, String> createMap(File file) throws IOException {
    return new PersistentMap<String, String>(file,
        new CharSequence2BufferConverter<String>(new StringCSFactory()),
        new CharSequence2BufferConverter<String>(new StringCSFactory()), 100);
  }

  public void testSimplePutAndGet() throws IOException {
    PersistentMap<String, String> map = createMap();
    map.put("xxxxxxxx", "yyyyyyyy");
    assertEquals("yyyyyyyy", map.get("xxxxxxxx"));
    map.flush();
    map = createMap(file);
    assertEquals("yyyyyyyy", map.get("xxxxxxxx"));
    map.put("xxxxxxxxZ", "yyyyyyyyZ");
    map.flush();
    map = createMap(file);
    assertEquals("yyyyyyyy", map.get("xxxxxxxx"));
    assertEquals("yyyyyyyyZ", map.get("xxxxxxxxZ"));
    assertEquals(2, map.size());
  }

  public void testPutAndGet1000() throws IOException {
    PersistentMap<String, String> map = createMap();
    String key = "xxxxxxxx";
    String value = "yyyyyyyy";
    for (int i = 0; i < 1000; i++) {
      key = key + i;
      value = value + i;
      map.put(key, value);
    }
    key = "xxxxxxxx";
    value = "yyyyyyyy";
    for (int i = 0; i < 1000; i++) {
      key = key + i;
      value = value + i;
      assertEquals(value, map.get(key));
    }
    map.flush();
    map = createMap(file);
    assertEquals(1000, map.size());
    key = "xxxxxxxx";
    value = "yyyyyyyy";
    for (int i = 0; i < 1000; i++) {
      key = key + i;
      value = value + i;
      assertTrue(map.containsKey(key));
      assertEquals(value, map.get(key));
    }
  }

  public void testPutAndGet10000() throws IOException {
    PersistentMap<String, String> map = createMap();
    String key = "xxxxxxxx";
    String value = "yyyyyyyy";
    for (int i = 0; i < 10000; i++) {
      key = key + "1";
      value = value + "2";
      map.put(key, value);
    }
    key = "xxxxxxxx";
    value = "yyyyyyyy";
    for (int i = 0; i < 10000; i++) {
      key = key + "1";
      value = value + "2";
      assertEquals(value, map.get(key));
    }
    map.flush();
    map = createMap(file);
    assertEquals(10000, map.size());
    key = "xxxxxxxx";
    value = "yyyyyyyy";
    for (int i = 0; i < 10000; i++) {
      key = key + "1";
      value = value + "2";
      assertTrue(map.containsKey(key));
      assertEquals(value, map.get(key));
    }
  }

  public void testPutAndGet100000() throws IOException {
    PersistentMap<String, String> map = createMap();
    String key = "xxxxxxxx";
    String value = "yyyyyyyy";
    for (int i = 0; i < 100000; i++) {
      map.put(key + i, value + i);
    }
    key = "xxxxxxxx";
    value = "yyyyyyyy";
    for (int i = 0; i < 100000; i++) {
      assertEquals(value + i, map.get(key + i));
    }
    map.flush();
    map = createMap(file);
    assertEquals(100000, map.size());
    key = "xxxxxxxx";
    value = "yyyyyyyy";
    for (int i = 0; i < 100000; i++) {
      assertTrue(map.containsKey(key + i));
      assertEquals(value + i, map.get(key + i));
    }
  }

  public void testContains() throws IOException {
    PersistentMap<String, String> map = createMap();
    assertTrue(map.isEmpty());
    map.put("x", "y");
    assertTrue(map.containsKey("x"));
  }

  public void testContains2() throws Exception {
    PersistentMap<String, String> map = createMap();
    assertFalse(map.containsKey("x"));
    map.put("x", "y");
    map.flush();
    map.put("q", "w");
    map.flush();
    assertTrue(map.containsKey("q"));
    assertTrue(map.containsKey("x"));
  }

  public void testSize() throws Exception {
    PersistentMap<String, String> map = createMap();
    map.put("x", "y");
    assertEquals(1, map.size());
  }

  public void testKeys() throws Exception {
    final PersistentMap<String, String> map = createMap();
    final HashSet<CharSequence> keys = new HashSet<CharSequence>();
    for (int i = 0; i < 10000; i++) {
      final String cs = generateRandomString(10).toString();
      keys.add(cs);
      map.put(cs, cs);
    }
    map.flush();
    for (int i = 0; i < 10000; i++) {
      final String cs = generateRandomString(10).toString();
      keys.add(cs);
      map.put(cs, cs);
    }
    final Set<String> set = map.keySet();
    assertEquals(keys.size(), set.size());
    keys.removeAll(set);
    assertTrue(keys.isEmpty());
  }

  public void testKeys2() throws Exception {
    final PersistentMap<String, String> map = createMap();
    final HashSet<CharSequence> keys = new HashSet<CharSequence>();
    for (int i = 0; i < 23475; i++) {
      final String cs = generateRandomString(10).toString();
      keys.add(cs);
      map.put(cs, cs);
    }
    map.flush();
    map.close();
    PersistentMap<String, String> reopen = createMap();

    final Set<String> set = reopen.keySet();
    assertEquals(keys.size(), set.size());
    keys.removeAll(set);
    assertTrue(keys.isEmpty());
  }

  public void testKeysNoFlush() throws Exception {
    final PersistentMap<String, String> map = createMap();
    final HashSet<CharSequence> keys = new HashSet<CharSequence>();
    for (int i = 0; i < 1000; i++) {
      final String cs = generateRandomString(10).toString();
      keys.add(cs);
      map.put(cs, cs);
    }

    final Set<String> set = map.keySet();
    assertEquals(keys.size(), set.size());
    keys.removeAll(set);
    assertTrue(keys.isEmpty());
  }

  public void testGetNull() throws IOException {
    PersistentMap<String, String> map = createMap();
    assertTrue(map.isEmpty());
    assertEquals(null, map.get("xxxxxxxx"));
  }

  public void testCloseAndDeleteFile() throws IOException {
    PersistentMap<String, String> map = createMap();
    map.close();
    assertTrue(file.delete());
  }

  public void testAfterClose() throws Exception {
    PersistentMap<String, String> map = createMap();
    map.close();
    boolean fail = true;
    try {
      map.get("x");
    } catch (Exception e) {
      fail = false;
    }
    if (fail) fail();
  }

  public void testFlushes() throws Exception {
    PersistentMap<String, String> map = createMap();
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 10; j++) {
        map.put("x" + i + j, "x" + i + j);
      }
      map.flush();
    }
    map.close();
    map = createMap();
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 10; j++) {
        map.put("y" + i + j, "y" + i + j);
      }
      map.flush();
    }
  }

  public static double normRandom() { // Box-Muller normal distributed variable generation, second var is skipped
    final double a = Math.random();
    final double b = Math.random();
    return Math.sqrt(-2 * Math.log(a)) * Math.sin(2 * Math.PI * b);
  }

  public void testPizdecHashMap() throws Exception {
    final Map<CharSequence, CharSequence> map = new HashMap<CharSequence, CharSequence>();
    final List<String> words = new ArrayList<String>(100000);
    Interval.start();
    for (int i = 0; i < 1000000; i++) {
      final int wordIndex = Math.min((int)(Math.pow(normRandom(), 2) * words.size() / 2.), words.size());
      if (wordIndex == words.size()) {
        final int newWordLength = Math.abs((int)(normRandom() * 2 + 6)) + 2; // poisson distribution is close to normal for big k values
        StringBuilder newWord = generateRandomString(newWordLength);
        words.add(newWord.toString());
//        System.out.println("Append: " + newWord);
      }
      CharSequence key = words.get(wordIndex);
      CharSequence value = map.get(key);
      if (value != null && value.length() > 100000)
        continue;
      final String newValue = (value != null ? value.toString() : "") + generateRandomString(10);
      map.put(key, newValue);
//      System.out.println(key + " " + newValue);
    }
    System.out.println(words.size());
    Interval.stopAndPrint();
  }

  public void testPizdecPersistentMap() throws Exception {
    final PersistentMap<String, String> map = createMap();
    final List<String> words = new ArrayList<String>(100000);
    Interval.start();
    for (int i = 0; i < 1000000; i++) {
      final int wordIndex = Math.min((int)(Math.pow(normRandom(), 2) * words.size() / 2.), words.size());
      if (wordIndex == words.size()) {
        final int newWordLength = Math.abs((int)(normRandom() * 2 + 6)) + 2; // poisson distribution is close to normal for big k values
        StringBuilder newWord = generateRandomString(newWordLength);
        words.add(newWord.toString());
//        System.out.println("Append: " + newWord);
      }
      if (updateValue(map, words, wordIndex))
        continue;

      if (i % 100000 == 0) {
        Interval.stopAndPrint();
        Interval.start();
      }

//      System.out.println(key + " " + newValue);
    }
    System.out.println(words.size());
    Interval.stopAndPrint();
  }

  private boolean updateValue(PersistentMap<String, String> map, List<String> words, int wordIndex) {
    String key = words.get(wordIndex);
    CharSequence value = map.get(key);
    if (value != null && value.length() > 1000000)
      return true;
    if (!(wordIndex == words.size() - 1 || value != null))
      assertTrue(false);
    final String newValue = (value != null ? value.toString() : "") + generateRandomString(10);
    map.put(key, newValue);
    return false;
  }

  private StringBuilder generateRandomString(int newWordLength) {
    StringBuilder newWord = new StringBuilder();
    for (int j = 0; j < newWordLength; j++){
      newWord.append((char)(Math.random() * ('z' - 'a') + 'a'));
    }
    return newWord;
  }
}
