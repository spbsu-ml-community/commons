package com.spbsu.commons.math.signals.numeric;

import com.spbsu.commons.math.signals.tools.NumericSignalTools;
import gnu.trove.list.array.TIntArrayList;


/**
 * User: terry
 * Date: 13.12.2009
 */
public class IntSignal extends BaseNumericSignal<Integer> {
  protected final TIntArrayList values;
  protected int cachedSum = -1;

  public IntSignal() {
    super();
    this.values = new TIntArrayList();
  }

  public IntSignal(long[] timestamps, int[] values) {
    super(timestamps);
    this.values = new TIntArrayList(values);
  }

  public IntSignal(final int timestampCount) {
    super(timestampCount);
    this.values = new TIntArrayList(timestampCount);
  }

  protected IntSignal(TIntArrayList values) {
    super();
    this.values = values;
  }

  protected IntSignal(long[] timestamps, TIntArrayList values) {
    super(timestamps);
    this.values = values;
  }

  public int[] getNativeValues() {
    return values.toArray();
  }

  @Override
  public Integer[] getValues() {
    final Integer[] result = new Integer[values.size()];
    for (int i = 0; i < values.size(); i++) {
      result[i] = values.getQuick(i);
    }
    return result;
  }

  @Override
  public Integer getValue(int index) {
    return values.getQuick(index);
  }

  public int getNativeValue(int index) {
    return values.getQuick(index);
  }

  @Override
  protected void insertValue(int index, Integer value) {
    values.insert(index, value);
    updateCachedSum(value);
  }

  @Override
  protected void adjustValue(int index, Integer value) {
    values.setQuick(index, values.getQuick(index) + value);
    updateCachedSum(value);
  }

  private void updateCachedSum(final int v) {
    initCachedSum();
    cachedSum += v;
  }

  public int sum() {
    initCachedSum();
    return cachedSum;
  }

  private void initCachedSum() {
    if (cachedSum == -1) cachedSum = NumericSignalTools.sumValues(this);
  }
}
