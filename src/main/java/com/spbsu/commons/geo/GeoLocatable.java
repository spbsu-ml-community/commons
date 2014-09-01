package com.spbsu.commons.geo;

import org.jetbrains.annotations.NotNull;

/**
 * User: zador
 * Date: 2014-09-01 14:14
 */
public interface GeoLocatable {
  @NotNull
  GeoPoint getGeoLocation();
}
