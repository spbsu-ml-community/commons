package com.spbsu.commons.math.mining;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.math.vectors.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * User: terry
 * Date: 20.12.2009
 */
public interface GenericClusterizationAlgorithm<X, V> {
  @NotNull
  Collection<? extends Collection<X>> cluster(Collection<X> dataSet, Computable<X, V> data2DVector);
}
