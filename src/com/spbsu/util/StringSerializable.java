package com.spbsu.util;

/**
 * User: Igor Kuralenok
 * Date: 25.07.2006
 */
public interface StringSerializable {
  void readString(String str);
  String writeString();
}
