package com.spbsu.commons.util;

/**
 * User: solar
 * Date: 26.07.12
 * Time: 20:09
 */
public class TextUtil {
  public static int split(String s, String[] parts, char delim) {
    int start = 0;
    int index = 0;
    do {
      int next = s.indexOf(delim, start);
      parts[index++] = s.substring(start, start = (next >= 0 ? next : s.length()));
      start++;
    }
    while(start < s.length());
    return index;
  }

}
