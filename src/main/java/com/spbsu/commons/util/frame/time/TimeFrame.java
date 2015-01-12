package com.spbsu.commons.util.frame.time;

import com.spbsu.commons.util.frame.Frame;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * User: terry
 * Date: 16.12.2009
 */
public class TimeFrame extends Frame<Date> {
  public static final TimeFrame EMPTY = new TimeFrame(0, 0);
  public static final TimeFrame INFINITY = new TimeFrame(0, Long.MAX_VALUE);

  private final long startTime;
  private final long endTime;

  public static TimeFrame create(@NotNull final Date start, @NotNull final Date end) {
    return new TimeFrame(start, end);
  }

  public static TimeFrame create(final long start, final long end) {
    return new TimeFrame(start, end);
  }

  public TimeFrame(final long start, final long end) {
    this(new Date(start), new Date(end));
  }

  public TimeFrame(@NotNull final Date start, @NotNull final Date end) {
    super(start, end);
    startTime = start.getTime();
    endTime = end.getTime();
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public long durationMs() {
    return endTime - startTime;
  }

  public boolean contains(final long point) {
    return startTime <= point && point <= endTime;
  }

  public boolean isEmpty() {
    return this == EMPTY;
  }
}
