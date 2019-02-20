package com.expleague.commons.math.vectors.impl.nn.impl;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntryImpl implements NearestNeighbourIndex.Entry {
  private final int index;
  private final long id;
  private Vec vec;
  private double distance;
  private int hashDistance;

  public EntryImpl(int index, long id, Vec vec, double distance) {
    this.id = id;
    this.vec = vec;
    this.distance = distance;
    this.index = index;
  }

  public EntryImpl(int index, long id, int hashDistance) {
    this.id = id;
    this.hashDistance = hashDistance;
    this.distance = -1;
    this.index = index;
  }

  public int index() {
    return index;
  }

  public long id() {
    return id;
  }

  public Vec vec() {
    return vec;
  }

  @Override
  public String toString() {
    return "Entry{" +
        "id=" + id +
//        ", vec=" + vec +
        ", distance=" + distance +
        ", hdistance=" + hashDistance +
        '}';
  }

  @Override
  public int compareTo(@NotNull NearestNeighbourIndex.Entry o) {
    return Double.compare(distance(), o.distance());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EntryImpl)) return false;
    EntryImpl entry = (EntryImpl) o;
    return id == entry.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public double distance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public int hashDistance() {
    return hashDistance;
  }

  public void setHashDistance(int hashDistance) {
    this.hashDistance = hashDistance;
  }

  public void setVec(Vec vec) {
    this.vec = vec;
  }
}
