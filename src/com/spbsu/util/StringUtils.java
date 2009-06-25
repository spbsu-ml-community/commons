package com.spbsu.util;

/**
 * User: vasiliy
 * Date: Jun 25, 2009
 */
public class StringUtils {
  /**
   * Copy-pasted from apache StringUtils
   */
  @SuppressWarnings({"JavaDoc"})
  public static boolean isBlank(CharSequence str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Copy-pasted from apache StringUtils
   */
  @SuppressWarnings({"JavaDoc"})
  public static boolean isNotBlank(CharSequence str) {
      return !StringUtils.isBlank(str);
  }
}
