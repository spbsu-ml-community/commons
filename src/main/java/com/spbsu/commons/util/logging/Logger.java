package com.spbsu.commons.util.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * User: terry
 * Date: 10.10.2009
 */
public class Logger implements Log {
  public static Logger create(Class clazz) {
    return create(clazz.getName());
  }

  public static Logger create(String name) {
    return new Logger(name);
  }

  private final Log innerLog;

  private Logger(String name) {
    this.innerLog = LogFactory.getLog(name);
  }

  public void message(String msg) {
    innerLog.info(msg);
  }

  public void error(Throwable e) throws RuntimeException {
    innerLog.error("Exception caught: ", e);
    throw new RuntimeException(e);
  }

  public void error(String message, Throwable e) throws RuntimeException {
    innerLog.error(message, e);
    throw new RuntimeException(message, e);
  }

  public void assertTrue(boolean condition, String message) {
    if (!condition) {
      innerLog.error("Assertion failed: " + message);
      throw new RuntimeException(message);
    }
  }

  public void warning(String message, Throwable th) {
    innerLog.warn(message, th);
  }

  public void warning(String message) {
    innerLog.warn(message);
  }

  public void warning(Throwable th) {
    innerLog.warn("Exception occured!", th);
  }

  public void error(String message) {
    innerLog.error(message);
    throw new RuntimeException(message);
  }

  public void info(CharSequence message) {
    innerLog.info(message.toString());
  }

  // common logging deleging methods

  public boolean isDebugEnabled() {
    return innerLog.isDebugEnabled();
  }

  public boolean isErrorEnabled() {
    return innerLog.isErrorEnabled();
  }

  public boolean isFatalEnabled() {
    return innerLog.isFatalEnabled();
  }

  public boolean isInfoEnabled() {
    return innerLog.isInfoEnabled();
  }

  public boolean isTraceEnabled() {
    return innerLog.isTraceEnabled();
  }

  public boolean isWarnEnabled() {
    return innerLog.isWarnEnabled();
  }

  public void trace(final Object o) {
    innerLog.trace(o);
  }

  public void trace(final Object o, final Throwable throwable) {
    innerLog.trace(o, throwable);
  }

  public void debug(final Object o) {
    innerLog.debug(o);
  }

  public void debug(final Object o, final Throwable throwable) {
    innerLog.debug(o, throwable);
  }

  public void info(final Object o) {
    innerLog.info(o);
  }

  public void info(final Object o, final Throwable throwable) {
    innerLog.info(o, throwable);
  }

  public void warn(final Object o) {
    innerLog.warn(o);
  }

  public void warn(final Object o, final Throwable throwable) {
    innerLog.warn(o, throwable);
  }

  public void error(final Object o) {
    innerLog.error(o);
  }

  public void error(final Object o, final Throwable throwable) {
    innerLog.error(o, throwable);
  }

  public void fatal(final Object o) {
    innerLog.fatal(o);
  }

  public void fatal(final Object o, final Throwable throwable) {
    innerLog.fatal(o, throwable);
  }
}
