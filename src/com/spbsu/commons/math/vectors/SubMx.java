package com.spbsu.commons.math.vectors;

/**
 * User: solar
 * Date: 01.08.12
 * Time: 15:56
 */
public class SubMx implements Mx {
  private final int start;
  private final int columns;
  private final int rows;
  private final Mx base;

  public SubMx(int start, int columns, int rows, Mx base) {
    this.start = start;
    this.columns = columns;
    this.rows = rows;
    this.base = base;
  }

  @Override
  public double get(int i, int j) {
    return base.get(start + i * base.columns() + j);
  }

  @Override
  public Mx set(int i, int j, double val) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public MxIterator nonZeroes() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public MxBasis basis() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int columns() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int rows() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public double get(int i) {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Vec set(int i, double val) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Vec adjust(int i, double increment) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int nonZeroesCount() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int dim() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
