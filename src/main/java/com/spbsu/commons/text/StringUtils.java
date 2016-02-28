package com.spbsu.commons.text;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: alms
 * Date: 25.01.2009
 * Time: 18:26:28
 */
public class StringUtils {
    public static final String EMPTY = "";

    public static String concatWithDelimeter(final CharSequence delimeter, final CharSequence... strings) {
        return concatWithDelimeter(delimeter, Arrays.asList(strings));
    }

    public static String concatWithDelimeter(final CharSequence delimeter, final List<? extends CharSequence> strings) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.size() - 1; i++) {
            result.append(strings.get(i));
            result.append(delimeter);
        }

        if (strings.size() > 0) {
            result.append(strings.get(strings.size() - 1));
        }

        return result.toString();
    }

    public static String repeatWithDelimeter(final CharSequence delimeter, final CharSequence string, final int count) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < count - 1; i++) {
            result.append(string);
            result.append(delimeter);
        }

        if (count > 0) {
            result.append(string);
        }

        return result.toString();
    }

    public static String concat(final List<? extends CharSequence> seq) {
        int size = 0;
        for (final CharSequence cs : seq) {
            size += cs.length();
        }

        final StringBuilder result = new StringBuilder(size + 1);
        for (final CharSequence cs : seq) {
            result.append(cs);
        }

        return result.toString();
    }

    public static String concat(final CharSequence... seq) {
        return concat(Arrays.asList(seq));
    }

    public static boolean isBlank(@Nullable final CharSequence cs) {
        final int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String[] split(final String str, final String delimetet, final int initialCapacity) {
        final char[] text = str.toCharArray();
        final List<String> strFacotorValue = new ArrayList<String>(initialCapacity);
        int begin = 0;
        int end;
        while ((end = str.indexOf(delimetet, begin)) >= 0) {
            strFacotorValue.add(str.substring(begin, end));
            begin = end + 1;
        }
        strFacotorValue.add(str.substring(begin, text.length));
        return strFacotorValue.toArray(new String[strFacotorValue.size()]);
    }

    public static List<String> split2List(final String str, final String delimetet) {
        final char[] text = str.toCharArray();
        final List<String> strFacotorValue = new ArrayList<String>();
        int begin = 0;
        int end;
        while ((end = str.indexOf(delimetet, begin)) >= 0) {
            strFacotorValue.add(str.substring(begin, end));
            begin = end + 1;
        }
        strFacotorValue.add(str.substring(begin, text.length));
        return strFacotorValue;
    }

    private StringUtils() {
    }
}
