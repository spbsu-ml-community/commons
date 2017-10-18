package com.expleague.commons.geo;

import junit.framework.TestCase;

/**
 * User: zador
 * Date: 07.02.2014 7:38 PM
 */

public class GeoBoundsTest extends TestCase {
  public void testCenter() {
    final GeoBounds geoBounds = new GeoBounds(GeoPoint.valueOf(50, 5), GeoPoint.valueOf(10, 35));
    assertEquals(GeoPoint.valueOf(30, 20), geoBounds.getCenter());
    assertEquals(30.0, geoBounds.getWidth());
    assertEquals(40.0, geoBounds.getHeight());
  }
}
