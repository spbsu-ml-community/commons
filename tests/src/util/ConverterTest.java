package util;

import com.spbsu.commons.func.converters.*;
import com.spbsu.commons.io.Buffer;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: igorkuralenok
 * Date: 22.06.2009
 */
public class ConverterTest extends TestCase {
  public void testSizeConvertion() {
    int[] sizes = new int[]{0, 1, 100, 10000, 299999, 17000000};
    for (int size : sizes) {
      ByteBuffer buffer = ByteBuffer.allocate(5);
      NioConverterTools.storeSize(size, buffer);
      buffer.rewind();
      assertEquals(size, NioConverterTools.restoreSize(buffer));
    }
  }

//  public void testPairStringIntegerConverter() {
//    final Pair<String,Integer> pair = Pair.create("string", 100);
//    final Pair2ByteBufferConverter<String,Integer> conv = new Pair2ByteBufferConverter<String,Integer>(
//      new String2ByteBufferConverter(),
//      new Integer2ByteBufferConverter()
//    );
//    final ByteBuffer buffer = conv.convertFrom(pair);
//    final Pair<String, Integer> result = conv.convertTo(buffer);
//    assertEquals(pair, result);
//    assertEquals(buffer.limit(), buffer.position());
//  }

  public void testStringConverter() {
    final String s = "string";
    final String2ByteBufferConverter conv = new String2ByteBufferConverter();
    final Buffer buffer = conv.convertTo(s);
    final String s1 = conv.convertFrom(buffer);
    assertEquals(s, s1);
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testListIntegerConverter() {
    final ArrayList<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);

    final List2BufferConverter<Integer> conv = new ArrayList2BufferConverter<Integer>(new Integer2BufferConverter());
    final Buffer buffer = conv.convertTo(list);

    final List<Integer> result = conv.convertFrom(buffer);
    assertEquals(list.size(), result.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i), result.get(i));
    }
    assertEquals(buffer.limit(), buffer.position());
  }

  public void testListStringConverter() {
    final ArrayList<String> list = new ArrayList<String>();
    list.add("1");
    list.add("2");

    final List2BufferConverter<String> conv = new ArrayList2BufferConverter<String>(new String2ByteBufferConverter());
    final Buffer buffer = conv.convertTo(list);

    final List<String> result = conv.convertFrom(buffer);
    assertEquals(list.size(), result.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i), result.get(i));
    }
    assertEquals(buffer.limit(), buffer.position());
  }

//  public void testListPairStringIntegerConverter() {
//    final ArrayList<Pair<String, Integer>> list = new ArrayList<Pair<String, Integer>>();
//    list.add(Pair.create("string1", 1));
//    list.add(Pair.create("string2", 2));
//
//    final Pair2ByteBufferConverter<String,Integer> conv = new Pair2ByteBufferConverter<String,Integer>(
//      new String2ByteBufferConverter(),
//      new Integer2ByteBufferConverter()
//    );
//    final List2ByteBufferConverter<Pair<String, Integer>> listConv = new List2ByteBufferConverter<Pair<String, Integer>>(conv);
//    final ByteBuffer buffer = listConv.convertFrom(list);
//    final List<Pair<String, Integer>> result = listConv.convertTo(buffer);
//
//    assertEquals(list.size(), result.size());
//    for (int i = 0; i < list.size(); i++) {
//      assertEquals(list.get(i), result.get(i));
//    }
//    assertEquals(buffer.limit(), buffer.position());
//  }
//
//  public void testListPairIntegerIntegerConverter() {
//    final ArrayList<Pair<Integer, Integer>> list = new ArrayList<Pair<Integer, Integer>>();
//    list.add(Pair.create(0, 1));
//    list.add(Pair.create(2, 3));
//
//    final Pair2ByteBufferConverter<Integer,Integer> conv = new Pair2ByteBufferConverter<Integer,Integer>(
//      new Integer2ByteBufferConverter(),
//      new Integer2ByteBufferConverter()
//    );
//    final List2ByteBufferConverter<Pair<Integer, Integer>> listConv = new List2ByteBufferConverter<Pair<Integer, Integer>>(conv);
//    final ByteBuffer buffer = listConv.convertFrom(list);
//    final List<Pair<Integer, Integer>> result = listConv.convertTo(buffer);
//
//    assertEquals(list.size(), result.size());
//    for (int i = 0; i < list.size(); i++) {
//      assertEquals(list.get(i), result.get(i));
//    }
//    assertEquals(buffer.limit(), buffer.position());
//  }
}
