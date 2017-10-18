package com.expleague.commons.util.frame;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: terry
 * Date: 14.10.2009
 * Time: 18:05:28
 * To change this template use File | Settings | File Templates.
 */
public class Frame<T extends Comparable<T>> {
  @NotNull private final T start;
  @NotNull private final T end;

  public static <T extends Comparable<T>> Frame<T> create(@NotNull final T start, @NotNull final T end) {
    return new Frame<T>(start, end);
  }
  
  public Frame(@NotNull final T start, @NotNull final T end) {
    if (start.compareTo(end) > 0) {
      throw new IllegalArgumentException("start '" + start + "' more then end '" + end + "'");
    }
    this.start = start;
    this.end = end;
  }

  @NotNull
  public T getStart() {
    return start;
  }

  @NotNull
  public T getEnd() {
    return end;
  }  

  public boolean contains(final T point) {
    return start.compareTo(point) <= 0 && end.compareTo(point) >= 0;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Frame)) return false;
    final Frame frame = (Frame) o;
    return end.equals(frame.end) && start.equals(frame.start);
  }

  @Override
  public int hashCode() {
    int result = start.hashCode();
    result = 31 * result + end.hashCode();
    return result;
  }
}
