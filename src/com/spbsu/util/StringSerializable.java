package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 25.07.2006
 * Time: 17:03:09
 * To change this template use File | Settings | File Templates.
 */
public interface StringSerializable {
  void readString(String str);
  String writeString();
}
