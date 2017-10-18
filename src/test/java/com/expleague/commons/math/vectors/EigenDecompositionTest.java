package com.expleague.commons.math.vectors;

import com.expleague.commons.FileTestCase;
import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.math.vectors.impl.mx.VecBasedMx;

import java.io.IOException;

/**
 * User: qdeee
 * Date: 20.08.13
 */
public class EigenDecompositionTest extends FileTestCase {
  @Override
  protected String getInputFileExtension() {
    return ".txt";
  }

  @Override
  protected String getResultFileExtension() {
    return ".txt";
  }

  @Override
  protected String getTestDataPath() {
    return "tests/data/math/";
  }

  public void testEigenDecomposition() throws IOException {
    final int dim = 4;
    final Mx A = new VecBasedMx(dim, new ArrayVec(10, -10, 0, 0,
                                            -10, 10, 0, 0,
                                            0,   0,  5, -5,
                                            0,   0,  -5, 5));
//        uncommment it for success :)
//        for (int i = 0; i < dim; i++) {
//            for (int j = 0; j < dim; j++)
//                A.adjust(i, j, -0.025);
//            A.adjust(i, i, 0.1);
//        }

    final Mx sigma = new VecBasedMx(dim, dim);
    final Mx Q = new VecBasedMx(dim, dim);
    MxTools.eigenDecomposition(A, sigma, Q);
    if (VecTools.distance(A, MxTools.multiply(MxTools.transpose(Q), MxTools.multiply(sigma, Q))) > A.dim() * MathTools.EPSILON) {
      System.out.println(MxTools.multiply(MxTools.transpose(Q), MxTools.multiply(sigma, Q)).toString());
      System.out.println(A);
      assertTrue(VecTools.distance(A, MxTools.multiply(MxTools.transpose(Q), MxTools.multiply(sigma, Q))) < A.dim() * MathTools.EPSILON);
    }
  }
}
