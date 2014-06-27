//package util;
//
//import junit.framework.TestCase;
//
//import java.util.Date;
//
///**
// * @author vp
// */
//public class DateParserTest extends TestCase {
//  public void testParseDates() throws Exception {
//    performTest(2009, 8, 3, 19, 15, 42, DateParser.parseDate("2009-08-03T19:15:42+04:00"));
//  }
//
//  private static void performTest(
//    final int y,
//    final int mo,
//    final int d,
//    final int h,
//    final int m,
//    final int s,
//    final Date actual
//  ) {
//    assertEquals(y, actual.getYear() + 1900);
//    assertEquals(mo, actual.getMonth() + 1);
//    assertEquals(d, actual.getDate());
//    assertEquals(h, actual.getHours());
//    assertEquals(m, actual.getMinutes());
//    assertEquals(s, actual.getSeconds());
//  }
//}
