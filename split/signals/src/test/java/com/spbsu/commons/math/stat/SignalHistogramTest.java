package com.spbsu.commons.math.stat;

import java.util.Random;


import com.spbsu.commons.math.stat.impl.ArrayHistogram;
import com.spbsu.commons.math.stat.impl.FrameHistogram;
import com.spbsu.commons.math.stat.impl.SparseHistogram;
import com.spbsu.commons.math.stat.tools.HistogramTools;
import com.spbsu.commons.util.time.TimeFrame;
import junit.framework.TestCase;

/**
 * @author vp
 */
public class SignalHistogramTest extends TestCase {
  public void testSparseDumb() throws Exception {
    final Histogram x = new SparseHistogram(2);
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

  public void testFrameHistogram() throws Exception {
    final TimeFrame frame = new TimeFrame(0L, 1000L);
    final FrameHistogram x = FrameHistogram.createByResolution(frame, 10);
    assertEquals(frame, x.getFrame());
    assertEquals(10L, x.getResolution());
    assertEquals(100, x.getBinCount());

    final FrameHistogram y = FrameHistogram.createByBinCount(frame, 100);
    assertEquals(frame, y.getFrame());
    assertEquals(10L, y.getResolution());
    assertEquals(100, y.getBinCount());

    final FrameHistogram z = FrameHistogram.createByResolution(frame, 100);
    z.update(10, 100);
    z.update(250, 3);
    z.update(809, 2);
    z.update(70, 1);
    z.update(700, 12);
    z.update(1000, 90);
    assertEquals(101, z.getValue(0));
    assertEquals(0, z.getValue(1));
    assertEquals(3, z.getValue(2));
    assertEquals(0, z.getValue(3));
    assertEquals(0, z.getValue(4));
    assertEquals(0, z.getValue(5));
    assertEquals(0, z.getValue(6));
    assertEquals(12, z.getValue(7));
    assertEquals(2, z.getValue(8));
    assertEquals(90, z.getValue(9));
  }

  public void testSparseFrameHistogram() throws Exception {
    final TimeFrame frame = new TimeFrame(0L, 1000L);
    final FrameHistogram x = FrameHistogram.createSparseByResolution(frame, 10);
    assertEquals(frame, x.getFrame());
    assertEquals(10L, x.getResolution());
    assertEquals(100, x.getBinCount());

    final FrameHistogram y = FrameHistogram.createSparseByBinCount(frame, 100);
    assertEquals(frame, y.getFrame());
    assertEquals(10L, y.getResolution());
    assertEquals(100, y.getBinCount());

    final FrameHistogram z = FrameHistogram.createSparseByResolution(frame, 100);
    z.update(10, 100);
    z.update(250, 3);
    z.update(809, 2);
    z.update(70, 1);
    z.update(700, 12);
    z.update(1000, 90);
    assertEquals(101, z.getValue(0));
    assertEquals(0, z.getValue(1));
    assertEquals(3, z.getValue(2));
    assertEquals(0, z.getValue(3));
    assertEquals(0, z.getValue(4));
    assertEquals(0, z.getValue(5));
    assertEquals(0, z.getValue(6));
    assertEquals(12, z.getValue(7));
    assertEquals(2, z.getValue(8));
    assertEquals(90, z.getValue(9));
  }
}
