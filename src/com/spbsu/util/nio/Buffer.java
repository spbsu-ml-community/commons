package com.spbsu.util.nio;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:10:42
 * To change this template use File | Settings | File Templates.
 */
public interface Buffer {
  int capacity();
  int remaining();

  int limit();
  void limit(int pos);

  void position(int pos);
  int position();
}
