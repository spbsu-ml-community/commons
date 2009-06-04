package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 6:52:03
 * To change this template use File | Settings | File Templates.
 */
public class NotImplementedException extends RuntimeException{
  public NotImplementedException() {
    super("Not implemented yet!");
  }
}
