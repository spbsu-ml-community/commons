package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.vectors.Basis;
import com.expleague.commons.math.vectors.BasisVecIterator;
import com.expleague.commons.math.vectors.GenericBasis;
import com.expleague.commons.math.vectors.impl.basis.MapBasis;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;


import java.util.HashMap;
import java.util.Map;

/**
 * User: terry
 * Date: 16.12.2009
 */
public class DVector<A> extends CommonBasisVec<A> {
  private static final Map<Class, GenericBasis> basises = new HashMap<Class, GenericBasis>();
  private static Basis active;
  private static Class activeClass;

  private static synchronized <T> GenericBasis<T> basis(final Class<T> clazz) {
    if (activeClass != clazz && !clazz.equals(activeClass)) {
      GenericBasis basis = basises.get(clazz);
      if (basis == null)
        basises.put(clazz, basis = new MapBasis<T>());
      activeClass = clazz;
      active = basis;
    }
    //noinspection unchecked
    return (GenericBasis<T>)active;
  }

  public DVector(final Class<A> aClass) {
    super(basis(aClass));
  }

  public DVector(final A[] axes, final double[] projections) {
    //noinspection unchecked
    super(basis((Class<A>)axes.getClass().getComponentType()), axes, projections);
  }

  public DVector(final TObjectDoubleHashMap<A> axis2Projection, final Class<A> clazz) {
    super(basis(clazz));
    final int[] indeces = new int[axis2Projection.size()];
    final double[] values = new double[axis2Projection.size()];

    axis2Projection.forEachEntry(new TObjectDoubleProcedure<A>() {
      int index;
      @Override
      public boolean execute(final A a, final double v) {
        if(v != 0) {
          indeces[index] = basis().toIndex(a);
          values[index] = v;
          index++;
        }
        return true;
      }
    });
    init(indeces, values);
  }

  public double[] values() {
    return values.toArray();
  }

  public void forEach(final TObjectDoubleProcedure<A> procedure) {
    final BasisVecIterator<A> iter = iterator();
    while (iter.advance()) {
      if(!procedure.execute(iter.key(), iter.value()))
        return;
    }
  }
}