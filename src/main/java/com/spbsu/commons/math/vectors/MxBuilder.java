package com.spbsu.commons.math.vectors;

import java.util.List;

/**
 * User: qdeee
 * Date: 20.03.15
 */
public interface MxBuilder {
  MxBuilder add(final Vec vec);

  MxBuilder addAll(final List<Vec> vecs);

  Mx build();
}
