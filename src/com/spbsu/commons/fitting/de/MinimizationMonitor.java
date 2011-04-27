package com.spbsu.commons.fitting.de;

/**
 * @author vp
 */
public interface MinimizationMonitor {
	public void newMinimum(double value, double[] parameterValues);
}