package com.expleague.commons.math;

import com.expleague.commons.math.vectors.Mx;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.math.vectors.impl.mx.VecBasedMx;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * User: solar
 * Date: 21.12.2010
 * Time: 22:07:07
 */
public interface Trans extends Function<Vec,Vec> {
  int xdim();
  int ydim();
  @Nullable
  Trans gradient();

  Vec trans(Vec x);
  Vec transTo(Vec x, Vec to);

  Mx transAll(Mx x);
  Mx transAll(Mx ds, boolean parallel);

  abstract class Stub implements Trans {
    @Override
    public Trans gradient() {
      return null;
    }

    @Override
    public Vec apply(final Vec argument) {
      return trans(argument);
    }

    @Override
    public Vec transTo(final Vec argument, Vec to) {
      final Vec trans = trans(argument);
      VecTools.assign(to, trans);
      return to;
    }

    public Vec trans(final Vec arg) {
      final Vec result = new ArrayVec(ydim());
      return transTo(arg, result);
    }

    @Override
    public Mx transAll(final Mx ds) {
      return transAll(ds, false);
    }

    public Mx transAll(final Mx ds, boolean parallel) {
      final Mx result = new VecBasedMx(ydim(), new ArrayVec(ds.rows() * ydim()));
      final CountDownLatch latch = new CountDownLatch(ds.rows());
      for (int i = 0; i < ds.rows(); i++) {
        final int finalI = i;
        if (parallel)
          ForkJoinPool.commonPool().execute(() -> {
            transTo(ds.row(finalI), result.row(finalI));
            latch.countDown();
          });
        else
          transTo(ds.row(finalI), result.row(finalI));
      }
      if (parallel) {
        try {
          latch.await();
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      return result;
    }
  }
}
