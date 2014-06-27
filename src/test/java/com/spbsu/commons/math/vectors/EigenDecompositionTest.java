package com.spbsu.commons.math.vectors;

import com.spbsu.commons.FileTestCase;
import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;

import java.io.IOException;

import static com.spbsu.commons.math.vectors.VecTools.*;

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
    int dim = 4;
    Mx A = new VecBasedMx(dim, new ArrayVec(10, -10, 0, 0,
                                            -10, 10, 0, 0,
                                            0,   0,  5, -5,
                                            0,   0,  -5, 5));
//        uncommment it for success :)
//        for (int i = 0; i < dim; i++) {
//            for (int j = 0; j < dim; j++)
//                A.adjust(i, j, -0.025);
//            A.adjust(i, i, 0.1);
//        }

    Mx sigma = new VecBasedMx(dim, dim);
    Mx Q = new VecBasedMx(dim, dim);
    MxTools.eigenDecomposition(A, Q, sigma);
    if (distance(A, MxTools.multiply(MxTools.transpose(Q), MxTools.multiply(sigma, Q))) > A.dim() * MathTools.EPSILON) {
      System.out.println(MxTools.multiply(MxTools.transpose(Q), MxTools.multiply(sigma, Q)).toString());
      System.out.println(A);
      assertTrue(distance(A, MxTools.multiply(MxTools.transpose(Q), MxTools.multiply(sigma, Q))) < A.dim() * MathTools.EPSILON);
    }
  }
}
