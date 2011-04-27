package com.spbsu.commons.fitting.de;

/**
 * @author vp
 */
public abstract class MinimizationAlgorithm {
  public static double EPSILON = 2.220446049250313E-16;

  /**
   * total number of function evaluations necessary
   */
  public int numFun;

  /**
   * maxFun is the maximum number of calls to fun allowed.
   * the default value of 0 indicates no limit on the number
   * of calls.
   */
  public int maxFun = 0;

  /**
   * numFuncStops is the number of consecutive positive
   * evaluations of the stop criterion based on function evaluation
   * necessary to cause the abortion of the optimization
   * (default is 4)
   */
  public int numFuncStops = 4;

  // number of fStops
  private int countFuncStops;

  // old function and parameter values
  private double fxold;
  private double[] xold;

  public double findMinimum(
    final MultivariateFunction function,
    final double[] initialGuess
  ) {
    optimize(function, initialGuess, EPSILON, EPSILON);

    return function.evaluate(initialGuess);
  }

  /**
   * Find minimum close to vector x
   * (desired fractional digits for each parameter is specified)
   *
   * @param function            multivariate function
   * @param initialGuess         initial guesses for the minimum
   *                     (contains the location of the minimum on return)
   * @param resultPrecision desired fractional digits in the function value
   * @param argumentPrecision  desired fractional digits in parameters x
   * @return minimal function value
   */
  public double findMinimum(
    final MultivariateFunction function,
    final double[] initialGuess,
    final int resultPrecision,
    final int argumentPrecision
  ) {
    return findMinimum(function, initialGuess, resultPrecision, argumentPrecision, null);
  }

  /**
   * Find minimum close to vector x
   * (desired fractional digits for each parameter is specified)
   *
   * @param function            multivariate function
   * @param initialGuess         initial guesses for the minimum
   *                     (contains the location of the minimum on return)
   * @param resultPrecision desired fractional digits in the function value
   * @param argumentPrecision  desired fractional digits in parameters x
   * @return minimal function value
   */
  public double findMinimum(
    final MultivariateFunction function,
    final double[] initialGuess,
    final int resultPrecision,
    final int argumentPrecision,
    final MinimizationMonitor monitor
  ) {
    final double tolfx = Math.pow(10, -1 - resultPrecision);
    final double tolx = Math.pow(10, -1 - argumentPrecision);

    optimize(function, initialGuess, tolfx, tolx, monitor);

    // trim x
    final double m = Math.pow(10, argumentPrecision);
    for (int i = 0; i < initialGuess.length; i++) {
      initialGuess[i] = Math.round(initialGuess[i] * m) / m;
    }
    // trim fx
    return Math.round(function.evaluate(initialGuess) * m) / m;
  }

  /**
   * The actual optimization routine
   * (needs to be implemented in a subclass of MultivariateMinimum).
   * It finds a minimum close to vector x when the
   * absolute tolerance for each parameter is specified.
   *
   * @param f     multivariate function
   * @param xvec  initial guesses for the minimum
   *              (contains the location of the minimum on return)
   * @param tolfx absolute tolerance of function value
   * @param tolx  absolute tolerance of each parameter
   */
  public abstract void optimize(MultivariateFunction f, double[] xvec, double tolfx, double tolx);

  /**
   * The actual optimization routine
   * <p/>
   * It finds a minimum close to vector x when the
   * absolute tolerance for each parameter is specified.
   *
   * @param f       multivariate function
   * @param xvec    initial guesses for the minimum
   *                (contains the location of the minimum on return)
   * @param tolfx   absolute tolerance of function value
   * @param tolx    absolute tolerance of each parameter
   * @param monitor A monitor object that receives information about the minimising process (for display purposes)
   * @note The default implementation just calls the optimize function with out the Monitor!
   */

  public void optimize(MultivariateFunction f, double[] xvec, double tolfx, double tolx, MinimizationMonitor monitor) {
    optimize(f, xvec, tolfx, tolx);
  }

  /**
   * Checks whether optimization should stop
   *
   * @param fx        current function value
   * @param x         current values of function parameters
   * @param tolfx     absolute tolerance of function value
   * @param tolx      absolute tolerance of each parameter
   * @param firstCall needs to be set to true when this routine is first called
   *                  otherwise it should be set to false
   * @return true if either x and its previous value are sufficiently similar
   *         or if fx and its previous values are sufficiently similar
   *         (test on function value has to be succesful numFuncStops consecutive
   *         times)
   */
  public boolean stopCondition(
    double fx,
    double[] x,
    double tolfx,
    double tolx,
    boolean firstCall
  ) {
    boolean stop = false;

    if (firstCall) {
      countFuncStops = 0;
      fxold = fx;
      xold = new double[x.length];
      copy(xold, x);
    } else {
      if (xStop(x, xold, tolx)) {
        stop = true;
      } else {
        if (fxStop(fx, fxold, tolfx)) {
          countFuncStops++;
        } else {
          countFuncStops = 0;
        }

        if (countFuncStops >= numFuncStops) {
          stop = true;
        }
      }
    }

    if (!stop) {
      fxold = fx;
      copy(xold, x);
    }

    return stop;
  }


  /**
   * Copy source vector into target vector
   *
   * @param target parameter array
   * @param source parameter array
   */
  public static void copy(double[] target, double[] source) {
    System.arraycopy(source, 0, target, 0, source.length);
  }

  private boolean xStop(final double[] x, final double[] xold, final double tolx) {
    for (int i = 0; i < x.length; i++) {
      if (Math.abs(x[i] - xold[i]) > tolx) {
        return false;
      }
    }

    return true;
  }

  private boolean fxStop(final double fx, final double fxold, final double tolfx) {
    return Math.abs(fx - fxold) <= tolfx;
  }
}
