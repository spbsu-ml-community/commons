package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;
import com.spbsu.util.persist.PageManager;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 08.04.2008
 * Time: 15:44:00
 * To change this template use File | Settings | File Templates.
 */
public class GranulatedDataPage extends DataPage {
  int chunkSize;
  byte[] data;
  short firstUnused = 0;
  public static final int ID = 2;


  public GranulatedDataPage(BTreeMap owner, int id, byte type) {
    super(owner, id);
    chunkSize = 1 << type;
  }

  public GranulatedDataPage(BTreeMap map, int degree) {
    super(map);
    changed(true);
    if(chunkSize != 0)
      throw new RuntimeException();
    chunkSize = 1 << degree;
    firstUnused = 0;
    data = new byte[BTreeMap.SLOT_CAPACITY - 3];
  }

  protected void write(ByteBuffer buffer) throws IOException {
    buffer.putShort(firstUnused);
    buffer.put(data);
  }

  protected void read(ByteBuffer buffer) throws IOException{
    firstUnused = buffer.getShort();
    data = new byte[buffer.remaining()];
    buffer.get(data);
  }

  protected int type() {
    return (ID << 5) | PageManager.degree(chunkSize);
  }

  public boolean isFull(){
    return (firstUnused + 1) * chunkSize > data.length;
  }

  public ByteBuffer readData(long address) {
    final int cellNo = (int)(0xFFFF & (address >> 24));
    if(cellNo >= firstUnused)
      return null;
    return ByteBuffer.wrap(data, cellNo * chunkSize, chunkSize);
  }

  public int chunkSize() {
    return chunkSize;
  }

  public long writeData(ByteBuffer data) {
    changed(true);
    if(isFull())
      throw new UnsupportedOperationException("This page is full");
    if(data.remaining() > chunkSize)
      throw new IllegalArgumentException("This page can not store data more then " + chunkSize + " bytes!");
    final long address = id() | ((0xFFFFl & firstUnused) << 24);
    data.get(this.data, firstUnused * chunkSize, data.remaining());
    if((address & 0xFFFFFF) != id() || ((address >> 24) & 0xFFFF) != firstUnused)
      throw new RuntimeException("Invalid address allocated!");
    firstUnused++;
    return address;
  }
}
