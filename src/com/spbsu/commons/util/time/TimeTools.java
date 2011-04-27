package com.spbsu.commons.util.time;

import com.spbsu.commons.util.Factories;
import com.spbsu.commons.util.frame.Frame;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: selivanov
 * Date: 26.06.2009 21:53:28
 */
public class TimeTools {
  static Map<TimeUnit, int[]> timeUnit2roundCalendarFields = Factories.hashMap();
  static Map<TimeUnit, Integer> timeUnit2CalendarFields = Factories.hashMap();
  public static final long ONE_DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

  static {
    timeUnit2roundCalendarFields.put(TimeUnit.MILLISECONDS, new int[]{});
    timeUnit2roundCalendarFields.put(TimeUnit.SECONDS, new int[]{Calendar.MILLISECOND});
    timeUnit2roundCalendarFields.put(TimeUnit.MINUTES, new int[]{Calendar.SECOND, Calendar.MILLISECOND});
    timeUnit2roundCalendarFields.put(TimeUnit.HOURS, new int[]{Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND});
    timeUnit2roundCalendarFields.put(TimeUnit.DAYS, new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND});

    timeUnit2CalendarFields.put(TimeUnit.MILLISECONDS, Calendar.MILLISECOND);
    timeUnit2CalendarFields.put(TimeUnit.SECONDS, Calendar.SECOND);
    timeUnit2CalendarFields.put(TimeUnit.MINUTES, Calendar.MINUTE);
    timeUnit2CalendarFields.put(TimeUnit.HOURS, Calendar.HOUR_OF_DAY);
  }

  public static Date round(final Date date, TimeUnit unit) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    for (int field : timeUnit2roundCalendarFields.get(unit)) {
      calendar.set(field, calendar.getMinimum(field));
    }
    return calendar.getTime();
  }

  public static Frame<Date> createVicinity(Date date, TimeUnit timeUnit) {
    final Date start = round(date, timeUnit);
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(start);
    calendar.add(timeUnit2CalendarFields.get(timeUnit), 1);
    return Frame.create(start, calendar.getTime());
  }

  public static Frame<Date> createTimeFrame(Date start, TimeUnit timeUnit, long duration) {
    return Frame.create(start, new Date(start.getTime() + timeUnit.toMillis(duration)));
  }

  public static long timeFrameDuration(Frame<Date> timeFrame) {
    return timeFrame.getEnd().getTime() - timeFrame.getStart().getTime();
  }

  public static int timeMinutesFrameDuration(Frame<Date> timeFrame) {
    return (int) ((timeFrame.getEnd().getTime() - timeFrame.getStart().getTime()) / 60000);
  }

  public static int timeHoursFrameDuration(Frame<Date> timeFrame) {
    return (int) ((timeFrame.getEnd().getTime() - timeFrame.getStart().getTime()) / 3600000);
  }

  public static Date add(final Date date, final int field, int value) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(field, value);
    return calendar.getTime();
  }

  public static Date increment(Date start, TimeUnit unit, int shift) {
    return new Date(start.getTime() + unit.toMillis(shift));
  }

  public static Iterator<Date> timeFrameIterator(final Frame<Date> timeFrame, final int field, final int value) {
    return new Iterator<Date>() {
      private Date currentDate = timeFrame.getStart();

      @Override
      public boolean hasNext() {
        return currentDate.getTime() >= timeFrame.getStart().getTime() && currentDate.getTime() <= timeFrame.getEnd().getTime();
      }

      @Override
      public Date next() {
        final Date date = currentDate;
        currentDate = add(date, field, value);
        return date;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static long timeMillisFrameDuration(Frame<Date> frame) {
    return frame.getEnd().getTime() - frame.getStart().getTime();
  }
}
