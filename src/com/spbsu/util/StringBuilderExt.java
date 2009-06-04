package com.spbsu.util;

/**
 * @author lyadzhin
 */
public class StringBuilderExt {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final StringBuilder sb;

    public StringBuilderExt() {
        this.sb = new StringBuilder();
    }

    public StringBuilderExt(int capacity) {
        this.sb = new StringBuilder(capacity);
    }

    public StringBuilderExt(CharSequence charSequence) {
        this.sb = new StringBuilder(charSequence);
    }

    public StringBuilderExt(StringBuilder sb) {
        this.sb = sb;
    }

    // New methods

    public StringBuilderExt chop() {
        sb.deleteCharAt(sb.length() - 1);
        return this;
    }

    public StringBuilderExt appendNewLine() {
        sb.append(LINE_SEPARATOR);
        return this;
    }

    // StringBuilder delegates

    public StringBuilderExt append(Object obj) {
        sb.append(obj);
        return this;
    }

    public StringBuilderExt append(String str) {
        sb.append(str);
        return this;
    }

    public StringBuilderExt append(StringBuffer sb) {
        sb.append(sb);
        return this;
    }

    public StringBuilderExt append(CharSequence s) {
        sb.append(s);
        return this;
    }

    public StringBuilderExt append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    public StringBuilderExt append(char[] str) {
        sb.append(str);
        return this;
    }

    public StringBuilderExt append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    public StringBuilderExt append(boolean b) {
        sb.append(b);
        return this;
    }

    public StringBuilderExt append(char c) {
        sb.append(c);
        return this;
    }

    public StringBuilderExt append(int i) {
        sb.append(i);
        return this;
    }

    public StringBuilderExt append(long lng) {
        sb.append(lng);
        return this;
    }

    public StringBuilderExt append(float f) {
        sb.append(f);
        return this;
    }

    public StringBuilderExt append(double d) {
        sb.append(d);
        return this;
    }

    public StringBuilderExt appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    public StringBuilderExt delete(int start, int end) {
        sb.delete(start, end);
        return this;
    }

    public StringBuilderExt deleteCharAt(int index) {
        sb.deleteCharAt(index);
        return this;
    }

    public StringBuilderExt replace(int start, int end, String str) {
        sb.replace(start, end, str);
        return this;
    }

    public StringBuilderExt insert(int index, char[] str, int offset, int len) {
        sb.insert(index, str, offset, len);
        return this;
    }

    public StringBuilderExt insert(int offset, Object obj) {
        sb.insert(offset, obj);
        return this;
    }

    public StringBuilderExt insert(int offset, String str) {
        sb.insert(offset, str);
        return this;
    }

    public StringBuilderExt insert(int offset, char[] str) {
        sb.insert(offset, str);
        return this;
    }

    public StringBuilderExt insert(int dstOffset, CharSequence s) {
        sb.insert(dstOffset, s);
        return this;
    }

    public StringBuilderExt insert(int dstOffset, CharSequence s, int start, int end) {
        sb.insert(dstOffset, s, start, end);
        return this;
    }

    public StringBuilderExt insert(int offset, boolean b) {
        sb.insert(offset, b);
        return this;
    }

    public StringBuilderExt insert(int offset, char c) {
        sb.insert(offset, c);
        return this;
    }

    public StringBuilderExt insert(int offset, int i) {
        sb.insert(offset, i);
        return this;
    }

    public StringBuilderExt insert(int offset, long l) {
        sb.insert(offset, l);
        return this;
    }

    public StringBuilderExt insert(int offset, float f) {
        sb.insert(offset, f);
        return this;
    }

    public StringBuilderExt insert(int offset, double d) {
        sb.insert(offset, d);
        return this;
    }

    public int indexOf(String str) {
        return sb.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return sb.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    public StringBuilderExt reverse() {
        sb.reverse();
        return this;
    }

    public String toString() {
        return sb.toString();
    }

    public int length() {
        return sb.length();
    }

    public int capacity() {
        return sb.capacity();
    }

    public void ensureCapacity(int minimumCapacity) {
        sb.ensureCapacity(minimumCapacity);
    }

    public void trimToSize() {
        sb.trimToSize();
    }

    public void setLength(int newLength) {
        sb.setLength(newLength);
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return sb.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return sb.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        sb.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void setCharAt(int index, char ch) {
        sb.setCharAt(index, ch);
    }

    public String substring(int start) {
        return sb.substring(start);
    }

    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    public String substring(int start, int end) {
        return sb.substring(start, end);
    }
}
