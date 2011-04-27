package com.spbsu.commons.util.frame;

import com.spbsu.commons.util.Factories;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: terry
 * Date: 30.10.2009
 * Time: 20:11:57
 */
public class FrameTools {

  public static <T extends Comparable<T>> boolean contains(Frame<T> src, Frame<T> arg) {
    return src.getStart().compareTo(arg.getStart()) <= 0 && src.getEnd().compareTo(arg.getEnd()) >= 0;
  }

  public static <T extends Comparable<T>> Frame<T>[] sort(Frame<T>... frames) {
    final List<Frame<T>> byStartSorted = Factories.arrayList(frames);
    Collections.sort(byStartSorted, new Comparator<Frame<T>>() {
      @Override
      public int compare(Frame<T> o1, Frame<T> o2) {
        return o1.getStart().compareTo(o2.getStart());
      }
    });
    //noinspection unchecked
    return byStartSorted.toArray((Frame<T>[]) new Frame[byStartSorted.size()]);
  }

  public static <T extends Comparable<T>> Frame<T>[] merge(Frame<T>... frames) {
    final Frame<T>[] byStartSorted = sort(frames);
    final List<Frame<T>> merged = Factories.arrayList();
    if (frames.length == 0) {
      return frames;
    }
    T start = byStartSorted[0].getStart();
    T end = byStartSorted[0].getEnd();
    for (Frame<T> frame : byStartSorted) {
      if (frame.getStart().compareTo(end) > 0) {
        merged.add(Frame.create(start, end));
        start = frame.getStart();
        end = frame.getEnd();
      } else if (frame.getEnd().compareTo(end) > 0) {
        end = frame.getEnd();
      }
    }
    merged.add(Frame.create(start, end));
    //noinspection unchecked
    return merged.toArray((Frame<T>[]) new Frame[0]);
  }

  public static <T extends Comparable<T>> Frame<T>[] subtract(Frame<T> frame, Frame<T>... subtrahends) {
    throw new UnsupportedOperationException();//todo
  }
}
