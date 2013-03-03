package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;

/* Created with IntelliJ IDEA.
 * ksen | 18:34 26.02.2013 | commons
 *
 *                     *                        *
 *                     *  ONLY SQUARE MATRIX !  *
 *                     *                        *
 */

public class VecBasedSquareMx implements Mx {

  // base vector
  private Vec vector;
  // width and height of our matrix
  private int matrixDimension;

  public VecBasedSquareMx() {
    vector = new SparseVec<>();
    matrixDimension = 0;
  }

  public VecBasedSquareMx(int matrixDimension) {
    vector = new SparseVec();
    this.matrixDimension = matrixDimension;
  }

  public VecBasedSquareMx(Vec vector) { // Можно, конечно, забить нулями.
    if (Math.ceil(Math.sqrt(vector.dim())) != Math.sqrt(vector.dim()))
      throw new RuntimeException(new IllegalArgumentException("Dimension of the vector must be square of something."));
    this.vector = vector;
    this.matrixDimension = (int) Math.sqrt((vector.dim()));
  }

  /* {(0,0); (1,0); (1,1); (0,1); (2,0); (2,1); (2,2); (1,2);
   *  (0,2); (3,0); (3,1); (3,2); (3,3); (2,3); (1,3); (0,3)}
   * ~
   * |0  3  8 15|
   * |1  2  7 14|
   * |4  5  6 13|
   * |9 10 11 12|
  * */
  private int map(int i, int j) {
    return (i == Math.max(i, j)) ?
            ((int) Math.pow(i, 2) + j) :
            ((int) Math.pow(j + 1, 2) - 1 - i);
  }

  @Override
  public double get(int i, int j) {
    return vector.get(map(i, j));
  }

  @Override
  public Mx set(int i, int j, double value) {
    int temp;
    if((temp = Math.max(i, j) + 1) > matrixDimension)
      matrixDimension = temp;
    vector.set(map(i, j), value);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    int temp;
    if((temp = Math.max(i, j) + 1) > matrixDimension)
      matrixDimension = temp;
    vector.adjust(map(i, j), increment);
    return this;
  }
  //TODO: sub, row, col, nonZeroes, basis.
  @Override
  public Mx sub(int i, int j, int height, int width) {
    return null;
  }

  @Override
  public Vec row(int i) {
    return null;
  }

  @Override
  public Vec col(int j) {
    return null;
  }

  @Override
  public MxIterator nonZeroes() {
    return null;
  }

  @Override
  public MxBasis basis() {
    return null;
  }

  @Override
  public int columns() {
    return matrixDimension;
  }

  @Override
  public int rows() {
    return matrixDimension;
  }

  @Override
  public double get(int i) {
    return vector.get(i);
  }

  @Override
  public Vec set(int i, double value) {
    int temp;
    if((temp = (int)Math.floor(Math.sqrt(i)) + 1) > matrixDimension)
      matrixDimension = temp;
    return vector.set(i, value);
  }

  @Override
  public Vec adjust(int i, double increment) {
    int temp;
    if((temp = (int)Math.floor(Math.sqrt(i)) + 1) > matrixDimension)
      matrixDimension = temp;
    return vector.adjust(i, increment);
  }

  @Override
  public int dim() {
    return matrixDimension;
  }

  @Override
  public double[] toArray() {
    double[] array = new double[matrixDimension * matrixDimension];
    for(int i = 0; i < matrixDimension; i++)
      for(int j = 0; j < matrixDimension; j++)
        array[map(i, j)] = get(i, j);
    return array;
  }

  @Override
  public boolean sparse() {
    return vector.sparse();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < matrixDimension; i++) {
      for (int j = 0; j < matrixDimension; j++) {
        builder.append(j > 0 ? "\t" : "");
        builder.append(get(i, j));
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  public boolean equals(Object o) {
    return o instanceof VecBasedSquareMx &&
            (((VecBasedSquareMx) o).matrixDimension == matrixDimension) &&
            ((VecBasedSquareMx) o).vector.equals(vector);
  }

  public int hashCode() {
    return (vector.hashCode() << 1) + matrixDimension;
  }

}
