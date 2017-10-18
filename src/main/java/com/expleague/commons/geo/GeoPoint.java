package com.expleague.commons.geo;

import org.jetbrains.annotations.NotNull;

/**
 * User: lyadzhin
 * Date: 4/23/13 11:40 AM
 */
public class GeoPoint {
  public static enum Format { LAT_LON, LON_LAT }

  public static final double MIN_LATITUDE = -90.0;
  public static final double MAX_LATITUDE = 90.0;
  public static final double MIN_LONGITUDE = -180.0;
  public static final double MAX_LONGITUDE = 180.0;

  @NotNull
  public static GeoPoint valueOf(final double latitude, final double longitude) {
    if (Double.isNaN(latitude) || Double.isInfinite(latitude) || Double.isNaN(longitude) || Double.isInfinite(latitude))
      throw new IllegalArgumentException();
    return new GeoPoint(latitude, longitude);
  }

  /**
   * Constructs object from string in format 'latitude','longitude'
   */
  public static GeoPoint valueOf(@NotNull final String latlon) {
    return valueOf(latlon, Format.LAT_LON);
  }

  /**
   * Constructs object from string in format 'longitude','latitude' if not <code>inverted</code> and 'latitude','longitude' otherwise.
   */
  public static GeoPoint valueOf(@NotNull final String stringValue, final Format format) {
    final int i = stringValue.indexOf(",");
    if (i < 0)
      throw new IllegalArgumentException();
    final double value1;
    final double value2;
    try {
      value1 = Double.parseDouble(stringValue.substring(0, i));
      value2 = Double.parseDouble(stringValue.substring(i + 1));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException();
    }
    return format == Format.LAT_LON ? valueOf(value1, value2) : valueOf(value2, value1);
  }

  private final double latitude;
  private final double longitude;

  private GeoPoint(final double latitude, final double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public boolean isBounded() {
    return Double.compare(latitude, MIN_LATITUDE) >= 0 && Double.compare(latitude, MAX_LATITUDE) <= 0 &&
           Double.compare(longitude, MIN_LONGITUDE) >= 0 && Double.compare(longitude, MAX_LONGITUDE) <= 0;
  }

  // Source: http://www.geodatasource.com/developers/java
  public double distanceTo(final GeoPoint to) {
    final double theta = Math.toRadians(getLongitude() - to.getLongitude());
    final double lat1 = Math.toRadians(getLatitude());
    final double lat2 = Math.toRadians(to.getLatitude());

    double dist = Math.sin(lat1) * Math.sin(lat2) +
                  Math.cos(lat1) * Math.cos(lat2) * Math.cos(theta);
    dist = Math.acos(dist);
    dist = Math.toDegrees(dist);
    dist *= 60 * 1.1515 * 1609.344;

    return dist;
  }

  @NotNull
  public String toString(@NotNull final Format format) {
    return format == Format.LAT_LON ? latitude + "," + longitude : longitude + "," + latitude;
  }

  @NotNull
  public GeoPoint getBounded() {
    double newLatitude = latitude;
    double newLongitude = longitude;
    if (latitude > MAX_LATITUDE) {
      newLatitude = MAX_LATITUDE;
    } else if (latitude < MIN_LATITUDE) {
      newLatitude = MIN_LATITUDE;
    }
    if (longitude > MAX_LONGITUDE) {
      newLongitude = MAX_LONGITUDE;
    } else if (longitude < MIN_LONGITUDE) {
      newLongitude = MIN_LONGITUDE;
    }

    return (newLatitude != latitude || newLongitude != longitude) ? GeoPoint.valueOf(newLatitude, newLongitude) : this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    final GeoPoint geoPoint = (GeoPoint) o;
    return Double.compare(geoPoint.latitude, latitude) == 0 && Double.compare(geoPoint.longitude, longitude) == 0;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(latitude);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return toString(Format.LAT_LON);
  }
}
