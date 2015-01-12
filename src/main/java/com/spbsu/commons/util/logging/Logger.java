package com.spbsu.commons.util.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * User: terry
 * Date: 10.10.2009
 */
public class Logger implements Log {
  public static Logger create(final Class clazz) {
    return create(clazz.getName());
  }

  public static Logger create(final String name) {
    return new Logger(name);
  }

  private final Log innerLog;

  private Logger(final String name) {
    this.innerLog = LogFactory.getLog(name);
  }

  public void message(final String msg) {
    innerLog.info(msg);
  }

  public void error(final Throwable e) throws RuntimeException {
    innerLog.error("Exception caught: ", e);
    throw new RuntimeException(e);
  }

  public void error(final String message, final Throwable e) throws RuntimeException {
    innerLog.error(message, e);
    throw new RuntimeException(message, e);
  }

  public void assertTrue(final boolean condition, final String message) {
    if (!condition) {
      innerLog.error("Assertion failed: " + message);
      throw new RuntimeException(message);
    }
  }

  public void warning(final String message, final Throwable th) {
    innerLog.warn(message, th);
  }

  public void warning(final String message) {
    innerLog.warn(message);
  }

  public void warning(final Throwable th) {
    innerLog.warn("Exception occured!", th);
  }

  public void error(final String message) {
    innerLog.error(message);
    throw new RuntimeException(message);
  }

  public void info(final CharSequence message) {
    innerLog.info(message.toString());
  }

  // common logging deleging methods

  @Override
  public boolean isDebugEnabled() {
    return innerLog.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return innerLog.isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return innerLog.isFatalEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return innerLog.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return innerLog.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return innerLog.isWarnEnabled();
  }

  @Override
  public void trace(final Object o) {
    innerLog.trace(o);
  }

  @Override
  public void trace(final Object o, final Throwable throwable) {
    innerLog.trace(o, throwable);
  }

  @Override
  public void debug(final Object o) {
    innerLog.debug(o);
  }

  @Override
  public void debug(final Object o, final Throwable throwable) {
    innerLog.debug(o, throwable);
  }

  @Override
  public void info(final Object o) {
    innerLog.info(o);
  }

  @Override
  public void info(final Object o, final Throwable throwable) {
    innerLog.info(o, throwable);
  }

  @Override
  public void warn(final Object o) {
    innerLog.warn(o);
  }

  @Override
  public void warn(final Object o, final Throwable throwable) {
    innerLog.warn(o, throwable);
  }

  @Override
  public void error(final Object o) {
    innerLog.error(o);
  }

  @Override
  public void error(final Object o, final Throwable throwable) {
    innerLog.error(o, throwable);
  }

  @Override
  public void fatal(final Object o) {
    innerLog.fatal(o);
  }

  @Override
  public void fatal(final Object o, final Throwable throwable) {
    innerLog.fatal(o, throwable);
  }
}
