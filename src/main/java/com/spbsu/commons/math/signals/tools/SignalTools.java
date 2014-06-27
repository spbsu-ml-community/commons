package com.spbsu.commons.math.signals.tools;

import com.spbsu.commons.math.signals.Signal;
import com.spbsu.commons.math.signals.SignalProcessor;
import com.spbsu.commons.math.signals.generic.GenericSignal;
import com.spbsu.commons.math.signals.generic.HashSetAdditiveSignal;
import com.spbsu.commons.math.signals.generic.SetAdditiveSignal;
import com.spbsu.commons.math.stat.Distribution;
import com.spbsu.commons.math.stat.impl.SampleDistribution;

import java.util.Collection;
import java.util.Set;

/**
 * @author vp
 */
public class SignalTools {
  public static <T> Distribution<T> getValueDistribution(final Signal<T> signal) {
    final SampleDistribution<T> distribution = new SampleDistribution<T>();
    signal.process(new SignalProcessor<T>() {
      @Override
      public void process(final long timestamp, final T value) {
        distribution.update(value);
      }
    });
    return distribution;
  }

  public static <T, C extends Collection<T>> Distribution<T> getValueDistributionWithCollection(final Signal<C> signal) {
    final SampleDistribution<T> distribution = new SampleDistribution<T>();
    signal.process(new SignalProcessor<C>() {
      @Override
      public void process(final long timestamp, final C collection) {
        for (final T value : collection) {
          distribution.update(value);
        }
      }
    });
    return distribution;
  }

  public static <T> GenericSignal<Set<T>> sumGenericSignals(final GenericSignal<T>... signals) {
    final SetAdditiveSignal<T> result = new HashSetAdditiveSignal<T>();
    for (GenericSignal<T> signal : signals) {
      for (int i = 0; i < signal.getTimestampCount(); i++) {
        result.occur(signal.getTimestamp(i), signal.getValue(i));
      }
    }
    return result.getSignal();
  }
}
