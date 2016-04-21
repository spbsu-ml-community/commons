package com.spbsu.commons.random;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import org.apache.commons.math3.util.FastMath;

import java.util.Random;

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
    double p = FastMath.exp(-meanFreq);
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

  public double nextGamma(double shape, double scale) {
    if (shape < 1)
      throw new IllegalArgumentException("Theta parameter must be positive");
    final double delta = shape - (int)shape;
    double ksi = 0;
    if (delta > MathTools.EPSILON) {
      double eta;
      do {
        final double a = nextDouble();
        final double b = nextDouble();
        final double c = nextDouble();

        if (a <= E / (E + delta)) {
          ksi = pow(b, 1 / delta);
          eta = c * FastMath.pow(ksi, delta - 1);
        } else {
          ksi = 1 - log(b);
          eta = c * FastMath.exp(-ksi);
        }
      }
      while (eta > FastMath.pow(ksi, delta - 1) * exp(-ksi));
    }

    double result = 0;
    for (int i = 0; i < (int)shape; i++) {
      result += Math.log(nextDouble());
    }
    return scale * (ksi - result);
  }

  static char[] BASE64_CHARS;
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