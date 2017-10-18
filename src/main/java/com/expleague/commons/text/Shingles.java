package com.expleague.commons.text;

import com.expleague.commons.util.Holder;
import com.expleague.commons.util.Pair;

import java.util.*;

/**
 * User: Igor Kuralenok
 * Date: 08.05.2006
 * Time: 8:59:37
 */
public class Shingles {

  public static double textDistanceByShingles(final CharSequence one, final CharSequence two) {
    final long[] shingles1 = getShingles(one, 10);
    final long[] shingles2 = getShingles(two, 10);
    for (int i = 0; i < shingles1.length; i++) {
      if (shingles1[i] != shingles2[i]) {
        return (1 - (i / 10.0));
      }
    }
    return 0;
  }

  public static long[] getShingles(CharSequence text, final int count) {
    final Holder<CharSequence> wordHolder = new Holder<CharSequence>();
    final Stack<Holder<Long>> shingles = new Stack<Holder<Long>>();
    final List<Pair<Long, Integer>> maxShingles = new LinkedList<Pair<Long, Integer>>();
    int position = 0;
    while (text.length() > 0) {
      text = nextWord(text, wordHolder);
      final long textHash = hash(wordHolder.getValue());
      for (final Holder<Long> shingle : shingles) {
        shingle.setValue(shingle.getValue() * 32 + textHash);
      }
      shingles.push(new Holder<Long>(textHash));
      if (shingles.size() >= 5) {
        final Holder<Long> fullShingle = shingles.remove(0);
        final Long newShingle = fullShingle.getValue();

        final ListIterator<Pair<Long, Integer>> iterator = maxShingles.listIterator();
        boolean inserted = false;
        while (iterator.hasNext()) {
          final Long shingle = iterator.next().getFirst();
          if (shingle > newShingle) {
            maxShingles.add(iterator.previousIndex(), Pair.create(newShingle, position));
            inserted = true;
            break;
          }
        }
        if (!inserted) {
          maxShingles.add(maxShingles.size(), Pair.create(newShingle, position));
        }
        if (maxShingles.size() > count) {
          maxShingles.remove(0);
        }
      }
      position++;
    }
    final long[] result = new long[count];
    int index = maxShingles.size();
    for (final Pair<Long, Integer> shingle : maxShingles) {
      result[--index] = shingle.getFirst();
    }
    return result;
  }

  private static long hash(final CharSequence seq) {
    long hash = 0;

    for (int i = 0; i < seq.length(); i++) {
      hash = 31 * hash + seq.charAt(i);
    }
    return hash;
  }

  private static CharSequence nextWord(final CharSequence sequence, final Holder<CharSequence> wordHolder) {
    int offset = 0;
    while (offset < sequence.length() && !Character.isLetterOrDigit(sequence.charAt(offset))) {
      offset++;
    }
    final int wordStart = offset;
    while (offset < sequence.length()
        && (sequence.charAt(offset) == '-'
        || sequence.charAt(offset) == '_'
        || Character.isLetterOrDigit(sequence.charAt(offset)))) {
      offset++;
    }
    wordHolder.setValue(sequence.subSequence(wordStart, offset));
    return sequence.subSequence(offset, sequence.length());
  }
}
