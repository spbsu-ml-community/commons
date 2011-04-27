package com.spbsu.commons.math.stat;

import com.spbsu.commons.math.stat.impl.NumericSampleDistribution;
import com.spbsu.commons.math.stat.impl.SampleDistribution;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author vp
 */
public class DistributionTest extends TestCase {
  public void testStringDistribution() throws Exception {
    final SampleDistribution<String> dist = new SampleDistribution<String>();
    final String s1 = "s1";
    final String s2 = "s2";
    final String s3 = "s3";
    final String s4 = "s4";

    dist.update(s1);
    dist.update(s1);
    dist.update(s2);
    dist.update(s3);
    dist.update(s3);
    dist.update(s3);

    final Object[] universum = dist.getUniversum();
    assertEquals(3, universum.length);

    final HashSet<Object> set = new HashSet<Object>(Arrays.asList(universum));
    assertTrue(set.remove(s1));
    assertTrue(set.remove(s2));
    assertTrue(set.remove(s3));
    assertFalse(set.remove(s4));
    assertTrue(set.isEmpty());

    assertEquals(2d / 6, dist.getProbability(s1));
    assertEquals(1d / 6, dist.getProbability(s2));
    assertEquals(3d / 6, dist.getProbability(s3));
  }

  public void testNumberDistribution() throws Exception {
    final NumericSampleDistribution<Integer> dist = new NumericSampleDistribution<Integer>();
    final int s1 = 10;
    final int s2 = 20;
    final int s3 = 30;
    final int s4 = 40;

    dist.update(s1);
    dist.update(s1);
    dist.update(s2);
    dist.update(s3);
    dist.update(s3);
    dist.update(s3);

    final Object[] universum = dist.getUniversum();
    assertEquals(3, universum.length);

    final HashSet<Object> set = new HashSet<Object>(Arrays.asList(universum));
    assertTrue(set.remove(s1));
    assertTrue(set.remove(s2));
    assertTrue(set.remove(s3));
    assertFalse(set.remove(s4));
    assertTrue(set.isEmpty());

    assertEquals(2d / 6, dist.getProbability(s1));
    assertEquals(1d / 6, dist.getProbability(s2));
    assertEquals(3d / 6, dist.getProbability(s3));

    assertEquals(10d, dist.getMin());
    assertEquals(30d, dist.getMax());

    final double mean = 130d / 6;
    assertEquals(mean, dist.getMean());

    final double variance = (2 * (s1 - mean) * (s1 - mean) + (s2 - mean) * (s2 - mean) + 3 * (s3 - mean) * (s3 - mean)) / (6 - 1);
    assertTrue(Math.abs(variance - dist.getVariance()) < 1E-7);
    assertTrue(Math.abs(Math.sqrt(variance) - dist.getStandardDeviation()) < 1E-7);
  }
}
