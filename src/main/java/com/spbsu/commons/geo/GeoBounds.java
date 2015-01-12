package com.spbsu.commons.geo;

import org.jetbrains.annotations.NotNull;

/**
 * Rectangular area on map.
 *
 * User: lyadzhin
 * Date: 5/21/13 4:08 PM
 */
public class GeoBounds {
  @NotNull
  private final GeoPoint northWestPoint;
  @NotNull
  private final GeoPoint southEastPoint;

  public GeoBounds(@NotNull final GeoPoint northWestPoint, @NotNull final GeoPoint southEastPoint) {
    this.northWestPoint = northWestPoint;
    this.southEastPoint = southEastPoint;
  }

  @NotNull
  public GeoPoint getNorthWestPoint() {
    return northWestPoint;
  }

  @NotNull
  public GeoPoint getNorthEastPoint() {
    return GeoPoint.valueOf(northWestPoint.getLatitude(), southEastPoint.getLongitude());
  }


  @NotNull
  public GeoPoint getSouthEastPoint() {
    return southEastPoint;
  }

  @NotNull
  public GeoPoint getSouthWestPoint() {
    return GeoPoint.valueOf(southEastPoint.getLatitude(), northWestPoint.getLongitude());
  }

  /**
   * Linear width in degrees
   */
  public double getWidth() {
    return southEastPoint.getLongitude() - northWestPoint.getLongitude();
  }

  /**
   * Linear height in degrees
   */
  public double getHeight() {
    return northWestPoint.getLatitude() - southEastPoint.getLatitude();
  }

  @NotNull
  public GeoPoint getCenter() {
    final double centerX = northWestPoint.getLongitude() + getWidth() / 2.0;
    final double centerY = southEastPoint.getLatitude() + getHeight() / 2.0;
    return GeoPoint.valueOf(centerY, centerX);
  }

  public boolean intersects(@NotNull final GeoBounds other) {
    // implement
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "[" + northWestPoint + "," + southEastPoint + "]";
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final GeoBounds that = (GeoBounds) o;
    return southEastPoint.equals(that.southEastPoint) && northWestPoint.equals(that.northWestPoint);

  }

  @Override
  public int hashCode() {
    int result = northWestPoint.hashCode();
    result = 31 * result + southEastPoint.hashCode();
    return result;
  }
}
