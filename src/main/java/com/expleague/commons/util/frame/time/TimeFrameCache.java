package com.expleague.commons.util.frame.time;

import com.expleague.commons.func.Computable;
import com.expleague.commons.util.Factories;
import com.expleague.commons.util.cache.Cache;
import com.expleague.commons.util.frame.Frame;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * User: terry
 * Date: 30.10.2009
 * Time: 19:59:58
 */
public class TimeFrameCache<V> implements Cache<Frame<Date>, Set<V>> {
  private final Computable<V, Date> keyComputer;
  private final TimeUnit granule;

  private final Cache<Frame<Date>, Set<V>> cache;

  public static <V> TimeFrameCache<V> create(final Cache<Frame<Date>, Set<V>> cache, final TimeUnit granule,
                                             final Computable<V, Date> keyDataForValue) {
    return new TimeFrameCache<V>(cache, granule, keyDataForValue);
  }

  public TimeFrameCache(final Cache<Frame<Date>, Set<V>> cache, final TimeUnit granule, final Computable<V, Date> keyComputer) {
    this.cache = cache;
    this.granule = granule;
    this.keyComputer = keyComputer;
  }

  public synchronized void populate(final V value) {
    final Frame<Date> dateFrame = TimeTools.createVicinity(keyComputer.compute(value), granule);
    final Set<V> cachedData = cache.get(dateFrame);
    if (cachedData != null) {
      cachedData.add(value);
    }
  }

  @Deprecated
  @Override
  public Set<V> put(final Frame<Date> key, final Set<V> value) {
    final List<Frame<Date>> granulated = granulateTimeFrame(key);
    for (final Frame<Date> frame : granulated) {
      final Set<V> acc = Factories.hashSet(value.size() / granulated.size(), 1.0f);
      accumulate(frame, value, acc);
      cache.put(frame, acc);
    }
    return value;
  }

  @Override
  public synchronized Set<V> get(final Frame<Date> key) {
    return get(key, null);
  }

  @Override
  public synchronized void flush() {
    cache.flush();
  }

  @Override
  public synchronized void clear() {
    cache.clear();
  }

  @Override
  public synchronized Set<V> get(final Frame<Date> key, final Computable<Frame<Date>, Set<V>> wayToGet) {
    final List<Frame<Date>> granulatedFrames = granulateTimeFrame(key);
    final Collection<Set<V>> cachedData = Factories.arrayList(granulatedFrames.size());
    int dataSize = 0;
    for (final Frame<Date> granulatedFrame : granulatedFrames) {
      Set<V> dataInFrame = cache.get(granulatedFrame);
      if (dataInFrame == null) {
        if (wayToGet != null) {
          cache.put(granulatedFrame, dataInFrame = wayToGet.compute(granulatedFrame));
        } else {
          return null;
        }
      }
      cachedData.add(dataInFrame);
      dataSize += dataInFrame.size();
    }
    final Set<V> acc = Factories.hashSet(dataSize, 1.0f);
    for (final Set<V> cached : cachedData) {
      accumulate(key, cached, acc);
    }
    return acc;
  }

  @Override
  public synchronized void clear(final Frame<Date> key) {
    for (final Frame<Date> dateFrame : granulateTimeFrame(key)) {
      cache.clear(dateFrame);
    }
  }

  private void accumulate(final Frame<Date> frame, final Set<V> src, final Set<V> acc) {
    for (final V data : src) {
      if (frame.contains(keyComputer.compute(data))) {
        acc.add(data);
      }
    }
  }

  private List<Frame<Date>> granulateTimeFrame(final Frame<Date> frame) {
    final Frame<Date> endFrame = TimeTools.createVicinity(frame.getEnd(), granule);
    Frame<Date> startFrame = TimeTools.createVicinity(frame.getStart(), granule);
    final List<Frame<Date>> granulatedFrames = Factories.arrayList(startFrame);
    while (!startFrame.equals(endFrame)) {
      final Frame<Date> shift = TimeTools.createTimeFrame(startFrame.getEnd(), granule, 1);
      granulatedFrames.add(shift);
      startFrame = shift;
    }
    return granulatedFrames;
  }
}
