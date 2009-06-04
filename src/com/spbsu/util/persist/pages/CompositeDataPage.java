package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 08.04.2008
 * Time: 15:44:00
 * To change this template use File | Settings | File Templates.
 */
public class CompositeDataPage extends DataPage {
  public static int COMPOSITE_PAGE_LENGTH = BTreeMap.SLOT_CAPACITY - 1 - 4 - 4; // type, length, next page pointer
  int chunkSize;
  byte[] data;
  final List<Integer> pages = new LinkedList<Integer>();
  public static final int ID = 5;

  public CompositeDataPage(BTreeMap owner, int id) {
    super(owner, id);
  }

  public CompositeDataPage(BTreeMap map) {
    super(map);
    chunkSize = 0;
  }

  protected void write(ByteBuffer buffer) throws IOException {
    if(pages == null) return;
    buffer.putInt(chunkSize);
    final Iterator<Integer> pagesIter = pages.iterator();
    Integer nextPage = pagesIter.hasNext() ? pagesIter.next() : null;
    buffer.putInt(nextPage != null ? nextPage : -1);
    int index = buffer.remaining();
    buffer.put(data, 0, Math.min(index, data.length));
    while(nextPage != null && nextPage != -1){
      final ByteBuffer nextPageBuffer = owner().getPageBuffer(nextPage, true);
      nextPageBuffer.put((byte)type());
      nextPage = pagesIter.hasNext() ? pagesIter.next() : null;
      nextPageBuffer.putInt(-1);
      nextPageBuffer.putInt(nextPage != null ? nextPage : -1);
      final int nextPageLength = nextPageBuffer.remaining();
      nextPageBuffer.put(data, index, Math.min(nextPageLength, data.length - index));
      index += nextPageLength;
    }
  }

  protected void read(ByteBuffer buffer) throws IOException{
    chunkSize = buffer.getInt();
    if(chunkSize < 0)
      return;
    int nextPage = buffer.getInt();
    pages.add(nextPage);
    data = new byte[chunkSize];
    int index = buffer.remaining();
    buffer.get(data, 0, Math.min(index, data.length));
    while(nextPage > 0){
      final ByteBuffer nextPageBuffer = owner().getPageBuffer(nextPage, false);
      nextPageBuffer.get(); // type: composite data. skip
      nextPageBuffer.getInt(); // chunk size: -1 (part). skip
      nextPage = nextPageBuffer.getInt();
      if(nextPage != -1) pages.add(nextPage);
      final int nextPageLength = nextPageBuffer.remaining();
      nextPageBuffer.get(data, index, Math.min(nextPageLength, data.length - index));
      index += nextPageLength;
    }
  }

  protected int type() {
    return (ID << 5);
  }

  public boolean isFull(){
    return true;
  }

  public ByteBuffer readData(long address) {
    if(data == null) return null; // not first page of composite
    final int cellNo = (int) (0xFFFFFF & (address >> 24));
    if(cellNo > 0) return null;
    return ByteBuffer.wrap(data);
  }

  public int chunkSize() {
    return chunkSize;
  }

  public long writeData(ByteBuffer data) {
    changed(true);
    if(this.chunkSize > 0) throw new UnsupportedOperationException("Composite pages can not be reused. This functionality is not implemented yet.");
    chunkSize = data.remaining();
    this.data = new byte[chunkSize];
    int index = COMPOSITE_PAGE_LENGTH;
    while(index < chunkSize){
      final CompositeDataPage dataPage = (CompositeDataPage)owner().pageManager.nextDataPage(chunkSize);
      dataPage.chunkSize = chunkSize;
      pages.add(dataPage.id());
      index += COMPOSITE_PAGE_LENGTH;
    }
    data.get(this.data);
    return id();
  }
}