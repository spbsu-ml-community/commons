package com.expleague.commons.geo;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * User: lyadzhin
 * Date: 4/23/13 12:19 PM
 */
public class GeoPointTest extends TestCase {
  public static final GeoPoint GEO_SPB_BENUA = GeoPoint.valueOf("59.958695,30.403104");
  public static final GeoPoint GEO_MSK_MOROZOV = GeoPoint.valueOf("55.733675,37.587808");

  public void testInvalidConstruction() {
    assertValueOfThrowsIAE("");
    assertValueOfThrowsIAE("asdfasdf");
    assertValueOfThrowsIAE("5345");
    assertValueOfThrowsIAE("5345.54");
    assertValueOfThrowsIAE("5345.54,");
    assertValueOfThrowsIAE(",5345.54");
    assertValueOfThrowsIAE("NaN,0");
    assertValueOfThrowsIAE("NaN,NaN");
    assertValueOfThrowsIAE("0,Nan");
    assertValueOfThrowsIAE("0,Inf");
    assertValueOfThrowsIAE("Inf,0");
  }

  private static void assertValueOfThrowsIAE(final String s) {
    boolean thrown = false;
    try {
      GeoPoint.valueOf(s);
    } catch (IllegalArgumentException e) {
      thrown = true;
    }

    if (!thrown)
      fail("IAE not thrown: " + s);
  }
  
  public void testConstruction() {

    assertEquals(-90.5, GeoPoint.valueOf("-90.5,0").getLatitude());
    assertEquals(0.0, GeoPoint.valueOf("-90.5,0").getLongitude());
    assertEquals(90.5, GeoPoint.valueOf("90.5,0").getLatitude());
    assertEquals(0.0, GeoPoint.valueOf("90.5,0").getLongitude());
    assertEquals(55.0, GeoPoint.valueOf("55,180.01").getLatitude());
    assertEquals(180.01, GeoPoint.valueOf("55,180.01").getLongitude());

    assertEquals(53.0, GeoPoint.valueOf("53,54.43").getLatitude());
    assertEquals(54.43, GeoPoint.valueOf("53,54.43").getLongitude());

    assertEquals(53.55, GeoPoint.valueOf("53.55,54.43").getLatitude());
    assertEquals(54.43, GeoPoint.valueOf("53.55,54.43").getLongitude());

    assertEquals(0.0, GeoPoint.valueOf("0,0").getLatitude());
    assertEquals(0.0, GeoPoint.valueOf("0,0").getLongitude());

    assertEquals(-90.0, GeoPoint.valueOf("-90,180").getLatitude());
    assertEquals(180.0, GeoPoint.valueOf("-90,180").getLongitude());
  }

  public void testBound() {
    final GeoPoint p1 = GeoPoint.valueOf("-90.5,0").getBounded();
    assertEquals(-90.0, p1.getLatitude());
    assertEquals(0.0, p1.getLongitude());

    final GeoPoint p2 = GeoPoint.valueOf("55,180.01").getBounded();
    assertEquals(55.0, p2.getLatitude());
    assertEquals(180.0, p2.getLongitude());

    final GeoPoint p3 = GeoPoint.valueOf("102.73807525634766,57.94528579711914").getBounded();
    assertEquals(90.0, p3.getLatitude());
    assertEquals(57.94528579711914, p3.getLongitude());
  }

  public void testEquals() {
    assertTrue(GeoPoint.valueOf("-90,180").equals(GeoPoint.valueOf("-90,180")));
    assertFalse(GeoPoint.valueOf("-90,180").equals(GeoPoint.valueOf("-90,179.5")));
  }

  public void testDistance() {
    final double distance1 = GEO_MSK_MOROZOV.distanceTo(GEO_SPB_BENUA);
    Assert.assertFalse(Double.isNaN(distance1));
    final double distance2 = GEO_SPB_BENUA.distanceTo(GEO_MSK_MOROZOV);
    Assert.assertFalse(Double.isNaN(distance2));
    Assert.assertEquals(distance1, distance2);
  }
}
