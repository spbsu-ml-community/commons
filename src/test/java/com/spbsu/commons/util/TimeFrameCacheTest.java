package com.spbsu.commons.util;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.util.cache.CacheStrategy;
import com.spbsu.commons.util.cache.impl.FixedSizeCache;
import com.spbsu.commons.util.frame.Frame;
import com.spbsu.commons.util.frame.time.TimeFrameCache;
import com.spbsu.commons.util.frame.time.TimeTools;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * User: terry
 * Date: 01.11.2009
 * Time: 13:24:16
 */
public class TimeFrameCacheTest extends TestCase {

  @SuppressWarnings({"ConstantConditions"})
  public void testPut() {
    final TimeFrameCache<Holder<Date>> cache = createCache();
    final Date now = new Date();
    final Holder<Date> holder = new Holder<Date>(now);
    final Holder<Date> holder2 = new Holder<Date>(TimeTools.increment(holder.getValue(), TimeUnit.MINUTES, 3));
    final Holder<Date> holder3 = new Holder<Date>(TimeTools.increment(holder.getValue(), TimeUnit.MINUTES, 5));
    Frame<Date> frame10 = TimeTools.createTimeFrame(now, TimeUnit.MINUTES, 10);
    cache.put(frame10, Factories.hashSet(holder, holder2, holder3));
    assertEquals(3, cache.get(frame10).size());
  }

  @SuppressWarnings({"ConstantConditions"})
  public void testGetModifyAndGetAgain() {
    final TimeFrameCache<Holder<Date>> cache = createCache();
    final Date now = new Date();
    final Holder<Date> holder = new Holder<Date>(now);
    Frame<Date> frame10 = TimeTools.createTimeFrame(now, TimeUnit.MINUTES, 10);
    Frame<Date> frame5 = TimeTools.createTimeFrame(now, TimeUnit.MINUTES, 5);
    assertNull(cache.get(frame10));

    Set<Holder<Date>> data = Collections.singleton(holder);

    cache.put(frame10, data);

    assertNotNull(cache.get(frame10));
    assertNotNull(cache.get(frame5));
    assertEquals(1, cache.get(frame10).size());
    assertEquals(1, cache.get(frame5).size());

    holder.setValue(TimeTools.increment(holder.getValue(), TimeUnit.MINUTES, 6));
    assertEquals(1, cache.get(frame10).size());
    assertEquals(0, cache.get(frame5).size());

    cache.populate(holder);
    assertEquals(1, cache.get(frame10).size());
    assertEquals(0, cache.get(frame5).size());

    cache.populate(new Holder<Date>(TimeTools.increment(now, TimeUnit.MINUTES, 2)));
    assertEquals(2, cache.get(frame10).size());
    assertEquals(1, cache.get(frame5).size());
  }

  private TimeFrameCache<Holder<Date>> createCache() {
    return TimeFrameCache.create(new FixedSizeCache<Frame<Date>, Set<Holder<Date>>>(100, CacheStrategy.Type.LRU),
        TimeUnit.MINUTES, new Computable<Holder<Date>, Date>() {
          @Override
          public Date compute(Holder<Date> argument) {
            return argument.getValue();
          }
        });
  }
}
