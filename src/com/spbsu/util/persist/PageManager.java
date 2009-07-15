package com.spbsu.util.persist;

import com.spbsu.util.Logger;
import com.spbsu.util.persist.pages.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: igorkuralenok
 * Date: 08.04.2008
 */
@SuppressWarnings({"unchecked"})
public class PageManager extends BTreePage {
  private static final Logger LOG = Logger.create(PageManager.class);
  private static final int MAX_GRADE = degree(BTreeMap.SLOT_CAPACITY - 1);
  private static final int MAGIC_BYTES = 0x12345678;
  private int size;
  private int treeHeadPage;
  private int version;
  private int dataPages[];
  public static final int ID = 1;

  @SuppressWarnings({"UnusedDeclaration"})
  public PageManager(BTreeMap owner, int id, byte type) {
    super(owner, id);
  }

  public PageManager(BTreeMap owner) {
    super(owner);
    version = 1;
    size = 0;
    treeHeadPage = -1;
    dataPages = new int[MAX_GRADE + 1];
    for (int i = 0; i < dataPages.length; i++) {
      dataPages[i] = -1;
    }
  }

  public int size(){
    return size;
  }

  public IndexPage treeHead(){
    if(treeHeadPage < 0){
      treeHeadPage = nextLeafPage().id();
    }
    return (IndexPage)owner().getPage(treeHeadPage);
  }

  public LeafPage nextLeafPage(){
    return new LeafPage(owner());
  }

  public NodePage nextNodePage(boolean head) {
    final NodePage result = new NodePage(owner());
    if(head) treeHeadPage = result.id();
    return result;
  }

  public DataPage nextDataPage(int dataSize){
    int degree = degree(dataSize);

    DataPage result = null;
    if(degree > MAX_GRADE) degree = MAX_GRADE;
    else if(dataPages[degree] > 0)
      result = (DataPage)owner().getPage(dataPages[degree]);

    if(result == null || result.isFull()){
      if (degree != MAX_GRADE) result = new GranulatedDataPage(owner(), degree);
      else result = new CompositeDataPage(owner());
      dataPages[degree] = result.id();
    }
    return result;
  }

  public void notifyElementAppend(){
    size++;
  }

  protected void write(ByteBuffer buffer) throws IOException {
    buffer.putInt(MAGIC_BYTES);
    buffer.putInt(version);
    buffer.putInt(size);
    buffer.putInt(treeHeadPage);
    for (int dataPage : dataPages) {
      buffer.putInt(dataPage);
    }
  }

  protected void read(ByteBuffer buffer) {
    final int magicBytes = buffer.getInt();
    if(magicBytes == MAGIC_BYTES) {
      dataPages = new int[MAX_GRADE + 1];
      version = buffer.getInt();
      size = buffer.getInt();
      treeHeadPage = buffer.getInt();
      for (int i = 0; i < dataPages.length; i++) {
        dataPages[i] = buffer.getInt();
      }
    }
    else initEmpty(0);
  }

  protected int type() {
    return ID << 5;
  }

  public static int degree(int value){
    int index = 0;
    while((value - 1) >> ++index > 0);
    return index;
  }

  public void write() {
    try {
      BTreePage.write(this);
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }
}
