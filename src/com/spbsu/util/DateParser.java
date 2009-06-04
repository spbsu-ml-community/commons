package com.spbsu.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Lawless
 */
public class DateParser {
  private static HashMap<String, Integer> monthToIntMap = new HashMap<String, Integer>(12);
  private static HashSet<String> weekDays = new HashSet<String>(12);

  static {
    monthToIntMap.put("jan", 0);
    monthToIntMap.put("feb", 1);
    monthToIntMap.put("mar", 2);
    monthToIntMap.put("apr", 3);
    monthToIntMap.put("may", 4);
    monthToIntMap.put("jun", 5);
    monthToIntMap.put("jul", 6);
    monthToIntMap.put("aug", 7);
    monthToIntMap.put("sep", 8);
    monthToIntMap.put("oct", 9);
    monthToIntMap.put("nov", 10);
    monthToIntMap.put("dec", 11);

    weekDays.add("sun");
    weekDays.add("mon");
    weekDays.add("two");
    weekDays.add("wen");
    weekDays.add("thu");
    weekDays.add("fri");
    weekDays.add("sat");
  }

  /**
   * Parses source as date in form 'dd MMM yyyy HH:mm:ss'
   *
   * @param source
   */

  public static Date parseDate(final String source) {
    try {
      final SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US);
      return format.parse(source);
    }
    catch (ParseException e) {
      // Next step
    }
    try {
      final SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US);
      return format.parse(source);
    }
    catch (ParseException e) {
      // Next step
    }
    try {
      final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
      return format.parse(source);
    }
    catch (ParseException e) {
      // Next step
    }
    try {
      final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.US);
      return format.parse(source);
    }
    catch (ParseException e) {
      // Next step
    }
    // "EEE, d MMM yyyy HH:mm:ss Z"
    try {
      final SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
      return format.parse(source);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses source as date in form 'dd MMM yyyy HH:mm:ss'
   * @param source
   *
   * 2 times faster than
   */
  public static Date parseDate2(final String source) {
    final int day, month, year, hours, minuts, seconds;
    final Scanner scanner = new Scanner(source.replace(':', ' '));
    final String ss = source.substring(0, 3);
//    System.out.println("source = " + source);
//    System.out.println("ss = " + ss);
    if (weekDays.contains(ss.toLowerCase())) {
      scanner.next();
    }
    day = scanner.nextInt();
    month = monthToIntMap.get(scanner.next().toLowerCase());
    year = scanner.nextInt();
    hours = scanner.nextInt();
    minuts = scanner.nextInt();
    seconds = scanner.nextInt();
    return new Date(year, month, day, hours, minuts, seconds);
  }
}
