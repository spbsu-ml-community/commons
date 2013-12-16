package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.iterators.MxIteratorImpl;
import gnu.trove.TIntIntHashMap;

/* Created with IntelliJ IDEA.
* ksen | 18:34 26.02.2013 | commons
*
*                     *                        *
*                     *  ONLY SQUARE MATRIX !  *
*                     *                        *
*/
//TODO(ksen): hashCode
public class VecBasedSquareMx<T> implements Mx {

  // base vector
  private SparseVec<GenericBasis<T>> vector;
  // width and height of our matrix
  private int matrixDimension;
  // vector length
  private int length;
  // key = (16 bit = i, 16 bit = j), value = vector index
  private TIntIntHashMap index;
  // vector basis
  private GenericBasis<T> basis;

  public VecBasedSquareMx(GenericBasis<T> basis) {
    this.basis = basis;
    vector = new SparseVec<GenericBasis<T>>(basis);
    matrixDimension = basis.size();
    index = new TIntIntHashMap();
    length = 0;
  }

  /* {(0,0); (1,0); (1,1); (0,1); (2,0); (2,1); (2,2); (1,2);
   *  (0,2); (3,0); (3,1); (3,2); (3,3); (2,3); (1,3); (0,3)}
   * ~
   * |0  3  8 15|
   * |1  2  7 14|
   * |4  5  6 13|
   * |9 10 11 12|
  * */
//  private int map(int i, int j) {
//    return (i == Math.max(i, j)) ?                          useless idea
//            ((int) Math.pow(i, 2) + j) :
//            ((int) Math.pow(j + 1, 2) - 1 - i);
//  }

  private int map(int i, int j) {
    return i << 16 | j;
  }

  private int map(T i, T j) {
    return basis.toIndex(i) << 16 | basis.toIndex(j);
  }

  @Override
  public double get(int i, int j) {
    return index.containsKey(map(i, j)) ? vector.get(index.get(map(i, j))) : 0;
  }

  public double get(T i, T j) {
    return index.containsKey(map(i, j)) ? vector.get(index.get(map(i, j))) : 0;
  }

  @Override
  public Mx set(int i, int j, double value) {
    int temp;
    if((temp = Math.max(i, j) + 1) > matrixDimension)
      matrixDimension = temp;
    if(index.containsKey(map(i, j)))
      vector.set(index.get(map(i, j)), value);
    else {
      index.put(map(i, j), length);
      vector.set(length++, value);
    }
    return this;
  }

  public Mx set(T i, T j, double value) {
    int temp;
    if((temp = Math.max(basis.toIndex(i), basis.toIndex(j)) + 1) > matrixDimension)
      matrixDimension = temp;
    if(index.containsKey(map(i, j)))
      vector.set(index.get(map(i, j)), value);
    else {
      index.put(map(i, j), length);
      vector.set(length++, value);
    }
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    int temp;
    if((temp = Math.max(i, j) + 1) > matrixDimension)
      matrixDimension = temp;
    if(index.containsKey(map(i, j)))
      vector.adjust(index.get(map(i, j)), increment);
    else {
      index.put(map(i, j), length);
      vector.adjust(length++, increment);
    }
    return this;
  }

  public Mx adjust(T i, T j, double increment) {
    int temp;
    if((temp = Math.max(basis.toIndex(i), basis.toIndex(j)) + 1) > matrixDimension)
      matrixDimension = temp;
    if(index.containsKey(map(i, j)))
      vector.adjust(index.get(map(i, j)), increment);
    else {
      index.put(map(i, j), length);
      vector.adjust(length++, increment);
    }
    return this;
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    return new VecBasedMx(width,
                          new IndexTransVec(vector, new SubMxTransformation(matrixDimension, i, j, width, height)));
  }

  @Override
  public Vec row(int i) {
    return new IndexTransVec(vector,
                             new SubMxTransformation(matrixDimension, i, 0, matrixDimension, 1));
  }

  public Vec row(T i) {
    return new IndexTransVec(vector,
                             new SubMxTransformation(matrixDimension, basis.toIndex(i), 0, matrixDimension, 1));
  }

  @Override
  public Vec col(int j) {
    return new IndexTransVec(vector,
                             new SubMxTransformation(matrixDimension, 0, j, 1, matrixDimension));
  }

  public Vec col(T j) {
    return new IndexTransVec(vector,
                             new SubMxTransformation(matrixDimension, 0, basis.toIndex(j), 1, matrixDimension));
  }

  @Override
  public MxIterator nonZeroes() {
    return new MxIteratorImpl(vector.nonZeroes(), matrixDimension);
  }

  @Override
  public MxBasis basis() {
    return new MxBasisImpl(matrixDimension,  matrixDimension);
  }

  public GenericBasis<T> genericBasis() {
    return new MapBasis<T>(basis);
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
    return vector.set(i, value);
  }

  @Override
  public Vec adjust(int i, double increment) {
    return vector.adjust(i, increment);
  }

  @Override
  public int dim() {
    return matrixDimension;
  }

  @Override
  public double[] toArray() {
    int size = matrixDimension;
    double[] result = new double[size * size];
    for(int i = 0; i < size; i++)
      for(int j = 0; j < size; j++)
        result[size * i + j] = get(i, j);
    return result;
  }

  @Override
  public Vec sub(int start, int len) {
    return vector.sub(start, len);
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < matrixDimension; i++) {
      for (int j = 0; j < matrixDimension; j++) {
        builder.append(j > 0 ? ",\t" : "");
        builder.append("(").append(basis.fromIndex(i)).append(",").append(basis.fromIndex(j)).append(")").append(get(i, j));
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  @Override public boolean equals(Object o) {
    return o instanceof VecBasedSquareMx &&
                        (((VecBasedSquareMx) o).matrixDimension == matrixDimension) &&
                        ((VecBasedSquareMx) o).vector.equals(vector) &&
                        ((VecBasedSquareMx) o).basis.equals(basis);
  }

  @Override public int hashCode() {
    return (vector.hashCode() << 1) + matrixDimension;
  }

}
