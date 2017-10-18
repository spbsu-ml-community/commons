package com.expleague.commons.seq.regexp;

import java.util.Arrays;

/**
 * User: Manokk@yandex.ru
 * Date: 29.09.11
 * Time: 23:03
 */
public class Pattern<T> {
  private final Alphabet<T> alphabet;

  public enum Modifier {
    NONE,
    QUESTION,
    STAR,
    EMPTY
  }
  private Matcher.Condition<T>[] conditions;
  private Modifier[] modifiers;

  private int manyItems, bagSize;
  private static final int INITIAL_CAPACITY = 10;

  private static final String[] SIGNS = new String[]{"", "?", "*", ""};

  private String toStringValue;

  private void update() {
    toStringValue = null;
  }

  public Pattern(final Alphabet<T> alphabet) {
    this.alphabet = alphabet;
    manyItems = 0;
    bagSize = INITIAL_CAPACITY;
    conditions = new SimpleRegExp.Condition[bagSize];
    modifiers = new Modifier[bagSize];
  }

  public Alphabet<T> alphabet() {
    return alphabet;
  }
  public void add(final SimpleRegExp.Condition<T> c,
                  final Modifier m) {
    update();
    if (manyItems == bagSize) {
      ensureCapacity(manyItems * 2 + 1);
    }
    conditions[manyItems] = c;
    modifiers[manyItems] = m;
    manyItems++;
  }

  public Matcher.Condition<T> condition(final int i) {
    //if (i < manyItems && i >= 0)
    return conditions[i];
    //throw new IndexOutOfBoundsException("Condition index " + i + " out of range [0 - " + manyItems + "]");
  }

  public Modifier modifier(final int i) {
    //if (i < manyItems && i >= 0)
    return modifiers[i];
    //throw new IndexOutOfBoundsException("Modifier index " + i + " out of range [0 - " + manyItems + "]");
  }

  public void removeLast(final int elements) {
    if (manyItems < elements) {
      update();
      //throw new IllegalStateException("Cannot remove " + elements + " elements from " + manyItems);
      manyItems -= elements;
    }
  }

  public void ensureCapacity(final int minimumCapacity) {
    final SimpleRegExp.Condition<T>[] biggerConArray;
    final Modifier[] biggerModArray;

    if (bagSize < minimumCapacity) {
      biggerConArray = new Matcher.Condition[minimumCapacity];
      biggerModArray = new Modifier[minimumCapacity];
      System.arraycopy(conditions, 0, biggerConArray, 0, manyItems);
      System.arraycopy(modifiers, 0, biggerModArray, 0, manyItems);
      conditions = biggerConArray;
      modifiers = biggerModArray;
      bagSize = minimumCapacity;
    }
  }

  public Matcher.Condition[] conditions() {
    return conditions;
  }

  public void clear() {
    update();
    manyItems = 0;
  }

  public int size() {
    return manyItems;
  }

  public String toString() {
    if (toStringValue == null) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < manyItems; i++) {
        sb.append(conditions[i].toString()).append(SIGNS[modifiers[i].ordinal()]);
      }
      toStringValue = sb.toString();
    }
    return toStringValue;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Pattern pattern = (Pattern) o;

    if (bagSize != pattern.bagSize) return false;
    if (manyItems != pattern.manyItems) return false;
    if (!Arrays.equals(conditions, pattern.conditions)) return false;
    if (!Arrays.equals(modifiers, pattern.modifiers)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(conditions);
    result = 31 * result + Arrays.hashCode(modifiers);
    result = 31 * result + manyItems;
    result = 31 * result + bagSize;
    return result;
  }
}