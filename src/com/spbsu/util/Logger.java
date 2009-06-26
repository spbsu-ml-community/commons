package com.spbsu.util;

import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author solar
 * @author lyadzhin
 */
public class Logger extends java.util.logging.Logger {
  public static Logger create(Class clazz) {
    return create(clazz.getName());
  }
  
  public static Logger create(String name) {
    LogManager manager = LogManager.getLogManager();
    Logger result = (Logger)manager.getLogger(name);
    if (result == null) {
        result = new Logger(name);
        manager.addLogger(result);
        result = (Logger)manager.getLogger(name);
    }
    return result;
  }

  private Logger(String name) {
    super(name, null);
  }

  public void message(String msg) {
    log(Level.INFO, msg);
  }

  public void error(Throwable e) {
    this.log(Level.SEVERE, "Exception caught: ", e);
    throw new RuntimeException(e);
  }

  public void error(String message, Throwable e) {
    this.log(Level.SEVERE, message, e);
    throw new RuntimeException(message, e);
  }

  public void assertTrue(boolean condition, String message) {
    if(!condition){
      this.log(Level.WARNING, "Assertion failed: " + message);
      throw new RuntimeException(message);
    }
  }

  public void warning(String message, Throwable th) {
    this.log(Level.WARNING, message, th);
  }

  public void warning(Throwable th) {
    this.log(Level.WARNING, "Exception occured!", th);
  }

  public void error(String message) {
    this.log(Level.SEVERE, message);
    throw new RuntimeException(message);
  }

  public void info(CharSequence message) {
    this.log(Level.INFO, message.toString());
  }
}
