package com.expleague.commons.random;

import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.lang.Math.*;

/**
 * Created by IntelliJ IDEA.
 * User: solar
 * Date: 19.01.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class FastRandom extends Random {
  private final long seed;
  private final ThreadLocal<RandState> state = new ThreadLocal<RandState>() {
    @Override
    protected RandState initialValue() {
      return new RandState(seed);
    }
  };

  public FastRandom() {
    this(System.nanoTime());
  }

  public FastRandom(final long seed) {
    this.seed = seed;
  }

  @Override
  public long nextLong() {
    return state.get().advance();
  }

  @Override
  public double nextGaussian() {
    return sqrt(-2. * Math.log(nextDouble())) * cos(2 * Math.PI * nextDouble());
  }

  @Override
  protected int next(final int bits) {
    return (int) (nextLong() >>> (64-bits));
  }

  public int nextPoisson(final double meanFreq) {
    if (meanFreq > 25) {
      final double val = nextNormal(meanFreq, Math.sqrt(meanFreq));
      if (val < 0)
        return nextPoisson(meanFreq);
      return (int) val + (val - (int)val >= 0.5 ? 1 : 0);
    }
    int x = 0;
    double p = Math.exp(-meanFreq);
    double s = p;
    final double u = nextDouble();
    while (u > s) {
      x++;
      p *= meanFreq / x;
      s += p;
    }
    return x;
  }

  private double nextNormal(final double meanFreq, final double stddev) {
    return nextGaussian() * stddev + meanFreq;
  }

  public int nextSimple(final Vec row) {
    double sum = 0;
    {
      final VecIterator it = row.nonZeroes();
      while(it.advance()) {
        sum += it.value();
      }
    }
    return nextSimple(row, sum);
  }

  public int nextSimple(final Vec row, final double len) {
    double rnd = nextDouble() * len;
    final VecIterator it = row.nonZeroes();
    while(rnd > 0 && it.advance()) {
      rnd -= it.value();
    }
    return it.isValid() ? it.index() : -1;
  }

  private final byte[] randomBytes = new byte[8];
  private byte pos = 8;
  //don't use it in parallel
  public final byte nextByte() {
    if (pos == 8) {
      final long value = nextLong();
      randomBytes[0] = (byte) (value >> 56);
      randomBytes[1] = (byte) (value >> 48);
      randomBytes[2] = (byte) (value >> 40);
      randomBytes[3] = (byte) (value >> 32);
      randomBytes[4] = (byte) (value >> 24);
      randomBytes[5] = (byte) (value >> 18);
      randomBytes[6] = (byte) (value >> 8);
      randomBytes[7] = (byte) (value);
      pos = 0;
    }
    return randomBytes[pos++];
  }
  private final byte mask = 127;
  public final int nextByte(int k) {
    return (mask & nextByte()) % k;
  }

  public double nextStandardExponential() {
    return -Math.log(1.0 - nextDouble());
  }

  public double nextStandardGamma(final double shape) {
    double b, c;
    double U, V, X, Y;

    if (shape == 1.0) {
      return nextStandardExponential();
    } else if (shape < 1.0) {
      for (; ; ) {
        U = nextDouble();
        V = nextStandardExponential();

        if (U <= 1.0 - shape) {
          X = Math.pow(U, 1. / shape);

          if (X <= V) {
            return X;
          }
        } else {
          Y = -Math.log((1 - U) / shape);
          X = Math.pow(1.0 - shape + shape * Y, 1. / shape);

          if (X <= (V + Y)) {
            return X;
          }
        }
      }
    } else {
      b = shape - 1. / 3.;
      c = 1. / Math.sqrt(9 * b);
      for (; ; ) {
        do {
          X = nextGaussian();
          V = 1.0 + c * X;
        } while (V <= 0.0);

        V = V * V * V;
        U = nextDouble();
        if (U < 1.0 - 0.0331 * (X * X) * (X * X)) {
          return (b * V);
        }

        if (Math.log(U) < 0.5 * X * X + b * (1. - V + Math.log(V))) {
          return (b * V);
        }
      }
    }
  }

  public Vec nextDirichlet(Vec params, Vec out) {
    double total = 0;
    double gamma;

    for (int i = 0; i < params.dim(); ++i) {
      gamma = nextStandardGamma(params.get(i));
      out.set(i, gamma);
      total += gamma;
    }

    double invTotal = 1.0 / total;

    for (int i = 0; i < params.dim(); ++i) {
      out.set(i, out.get(i) * invTotal);
    }

    return out;
  }

  public int nextBinomial(int trials, double q) {
    return IntStream.range(0, trials).map(i -> nextDouble() < q ? 1 : 0).sum();
  }

  public double nextGamma(final double shape, final double scale) {
    return scale * nextStandardGamma(shape);
  }

  public double nextBeta(double a, double b) {
    double Ga, Gb;

    if ((a <= 1.0) && (b <= 1.0)) {
      double U, V, X, Y;

      /* Use Johnk's algorithm */
      while (true) {
        U = nextDouble();
        V = nextDouble();
        X = Math.pow(U, 1.0 / a);
        Y = Math.pow(V, 1.0 / b);

        if ((X + Y) <= 1.0) {
          if (X + Y > 0) {
            return X / (X + Y);
          } else {
            double logX = Math.log(U) / a;
            double logY = Math.log(V) / b;
            double logM = logX > logY ? logX : logY;
            logX -= logM;
            logY -= logM;

            return Math.exp(logX - Math.log(Math.exp(logX) + Math.exp(logY)));
          }
        }
      }
    } else {
      Ga = nextStandardGamma(a);
      Gb = nextStandardGamma(b);
      return Ga / (Ga + Gb);
    }
  }

  public static char[] BASE64_CHARS;
  static {
    BASE64_CHARS = new char[64];
    int index = 0;
    for (char ch = 'a'; ch <= 'z'; ch++, index++) {
      BASE64_CHARS[index] = ch;
    }
    for (char ch = 'A'; ch <= 'Z'; ch++, index++) {
      BASE64_CHARS[index] = ch;
    }
    for (char ch = '0'; ch <= '9'; ch++, index++) {
      BASE64_CHARS[index] = ch;
    }
    BASE64_CHARS[index++] = '+';
    BASE64_CHARS[index] = '/';
  }

  public String nextBase64String(int count) {
    char[] chars = new char[count];
    for (int i = 0; i < count; i++) {
      chars[i] = BASE64_CHARS[nextInt(BASE64_CHARS.length)];
    }
    return new String(chars);
  }

  public String nextLowerCaseString(int count) {
    char[] chars = new char[count];
    for (int i = 0; i < count; i++) {
      chars[i] = (char)('a' + nextInt(26));
    }
    return new String(chars);
  }

  public IntStream intStream(int limit) {
    return StreamSupport.intStream(new Spliterator.OfInt() {
      @Override
      public OfInt trySplit() {
        return this;
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
        action.accept(nextInt(limit));
        return true;
      }

      @Override
      public long estimateSize() {
        return Long.MAX_VALUE;
      }

      @Override
      public int characteristics() {
        return 0;
      }
    }, false);
  }

  public IntStream base64Stream() {
    return StreamSupport.intStream(new Spliterator.OfInt() {
      @Override
      public OfInt trySplit() {
        return this;
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
        action.accept(BASE64_CHARS[nextInt(BASE64_CHARS.length)]);
        return true;
      }

      @Override
      public long estimateSize() {
        return Long.MAX_VALUE;
      }

      @Override
      public int characteristics() {
        return 0;
      }
    }, false);
  }

  public Reader base64Stream(long count) {
    return new Reader() {
      long totalLen = count;

      @Override
      public int read(@NotNull char[] cbuf, int off, int len) throws IOException {
        int read;
        for (read = 0; read < len && totalLen >= 0; read++, totalLen--)
          cbuf[off + read] = FastRandom.BASE64_CHARS[nextInt(64)];
        return read == 0 ? -1 : read;
      }

      @Override
      public void close() throws IOException {
      }
    };
  }

  private static class RandState {
    private long u;
    private long v = 4101842887655102017L;
    private long w = 1;

    public RandState(long seed) {
      u = seed ^ v;
      advance();
      v = u;
      advance();
      w = v;
      advance();
    }

    private long advance() {
      long localU = u;
      long localV = v;
      long localW = w;

      localU = localU * 2862933555777941757L + 7046029254386353087L;
      localV ^= localV >>> 17;
      localV ^= localV << 31;
      localV ^= localV >>> 8;
      localW = 4294957665L * (localW & 0xffffffffl) + (localW >>> 32);
      long x = localU ^ (localU << 21);
      x ^= x >>> 35;
      x ^= x << 4;
      final long ret = (x + localV) ^ localW;
      u = localU;
      v = localV;
      w = localW;
      return ret;
    }
  }
}