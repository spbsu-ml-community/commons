package com.expleague.commons.math.stat;

import java.util.Random;


import com.expleague.commons.math.stat.impl.ArrayHistogram;
import com.expleague.commons.math.stat.tools.HistogramTools;
import junit.framework.TestCase;

/**
 * @author vp
 */
public class HistogramTest extends TestCase {
  public void testCorrelation() throws Exception {
    final int n = 30;
    final Histogram x = new ArrayHistogram(n);
    final Histogram y = new ArrayHistogram(n);
    final Histogram z = new ArrayHistogram(n);
    final Random random = new Random(1000);
    for (int i = 0; i < n; i++) {
      x.addToBin(i, (long) (100 * random.nextDouble()));
      y.addToBin(i, (long) (100 * random.nextDouble()));
      z.addToBin(i, 10 * x.getValue(i) + 100);
    }
    assertEquals(1., HistogramTools.correlation(x, x));
    assertEquals(1., HistogramTools.correlation(x, z));
    assertTrue(Math.abs(HistogramTools.correlation(x, y)) < 1);
  }

  public void testDumb() throws Exception {
    final Histogram x = new ArrayHistogram(2);
    assertEquals(2, x.getBinCount());
    x.addToBin(0, 100);
    x.addToBin(1, 200);
    assertEquals(100, x.getValue(0));
    assertEquals(200, x.getValue(1));
    assertEquals(150., x.getMean());
    x.addToBin(1, 300);
    assertEquals(100, x.getValue(0));
    assertEquals(500, x.getValue(1));
    assertEquals(300., x.getMean());
    assertEquals(500., x.getMean(1, 1));
    assertEquals(300., x.getMean(0, 1));
  }
}
