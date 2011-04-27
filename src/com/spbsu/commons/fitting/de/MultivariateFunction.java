package com.spbsu.commons.fitting.de;

/**
 * @author vp
 */
public interface MultivariateFunction {
  /**
   * compute function value
   *
   * @param argument function argument (vector)
   * @return function value
   */
  double evaluate(double[] argument);


  /**
   * get number of arguments
   *
   * @return number of arguments
   */
  int getNumArguments();

  /**
   * get lower bound of argument n
   *
   * @param n argument number
   * @return lower bound
   */
  double getLowerBound(int n);

  /**
   * get upper bound of argument n
   *
   * @param n argument number
   * @return upper bound
   */
  double getUpperBound(int n);

}
