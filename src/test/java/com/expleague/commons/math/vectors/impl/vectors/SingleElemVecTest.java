package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: qdeee
 * Date: 21.05.17
 */
public class SingleElemVecTest {
  @Test
  public void basicOperations() throws Exception {
    final int pos = 1;
    final double value = 5.;
    final int dim = 4;

    final Vec vec = new SingleElemVec(pos, value, dim);
    assertEquals(dim, vec.dim());
    assertTrue(vec.isImmutable());

    for (int i = 0; i < vec.dim(); i++) {
      final double expected = i == pos ? value : 0;
      final double actual = vec.get(i);
      Assert.assertEquals(expected, actual, MathTools.EPSILON);
    }

    assertArrayEquals(new double[]{0, 5, 0, 0}, vec.toArray(), MathTools.EPSILON);

    final VecIterator iter = vec.nonZeroes();
    assertNotNull(iter);
    assertFalse(iter.isValid());
    assertTrue(iter.advance());
    assertTrue(iter.isValid());
    assertEquals(value, iter.value(), MathTools.EPSILON);
    assertEquals(pos, iter.index());
    assertFalse(iter.advance());
    assertFalse(iter.isValid());
  }
}