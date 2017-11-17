package com.expleague.commons.func.converters;

import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.func.types.TypeConverter;
import com.expleague.commons.func.types.impl.TypeConvertersCollection;
import junit.framework.TestCase;

/**
 * User: solar
 * Date: 24.06.13
 * Time: 15:12
 */
public class TypeConversionTest extends TestCase {
  public void testConvertersPackageInit() throws Exception {
    final String convert = new TypeConvertersCollection("com.expleague.commons.func.converters" +
        ".test.a").convert(10, String.class);
    assertEquals("10", convert);
  }

  public void testConvertersPackageInitInheritancePlusPrivateClass() throws Exception {
    final String convert = new TypeConvertersCollection("com.expleague.commons.func.converters" +
        ".test.b").convert(10, String.class);
    assertEquals("10", convert);
  }

  public static class A<T> implements TypeConverter<T, String> {
    @Override
    public String convert(final T from) {
      return from.toString();
    }
  }

  public static class B extends A<Integer> {}

  public void testConvertersPackageInitDeepInheritance() throws Exception {
    final String convert = new TypeConvertersCollection(B.class).convert(10, String.class);
    assertEquals("10", convert);
  }

  public static class C implements TypeConverter<Integer, String> {
    @Override
    public String convert(final Integer from) {
      return from.toString();
    }
  }

  public void testConvertersPackageInheritanceInDestination() throws Exception {
    final CharSequence convert = new TypeConvertersCollection(C.class).convert(10, CharSequence.class);
    assertEquals("10", convert.toString());
  }

  public static class D implements TypeConverter<A, String> {
    @Override
    public String convert(final A from) {
      return "Hello, A";
    }
  }

  public void testConvertersPackageInheritanceInSource() throws Exception {
    final String convert = new TypeConvertersCollection(D.class).convert(new B(), String.class);
    assertEquals("Hello, A", convert);
  }

  public static class E implements TypeConverter<B, String> {
    @Override
    public String convert(final B from) {
      return "Hello, B";
    }
  }

  public void testConvertersPackageInheritanceInSourcePreference() throws Exception {
    final String convert = new TypeConvertersCollection(D.class, E.class).convert(new B(), String.class);
    assertEquals("Hello, B", convert);
  }

  public void testGeneric1() throws Exception {
    assertEquals("10", ConversionRepository.ROOT.convert(10, CharSequence.class));
  }

}
