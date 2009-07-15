package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;

import java.nio.ByteBuffer;

/**
 * User: igorkuralenok
 * Date: 18.04.2008
 */
public abstract class DataPage extends BTreePage{
  public static int COMPOSITE_PAGE_LENGTH = BTreeMap.SLOT_CAPACITY - 1 - 4 - 4; // type, length, next page pointer

  public DataPage(BTreeMap owner, int id) {
    super(owner, id);
  }

  public DataPage(BTreeMap owner) {
    super(owner);
  }

  public abstract ByteBuffer readData(long address);
  public abstract long writeData(ByteBuffer data);

  public abstract int chunkSize();

  public abstract boolean isFull();
}
