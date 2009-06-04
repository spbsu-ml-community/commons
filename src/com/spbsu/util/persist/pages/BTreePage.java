package com.spbsu.util.persist.pages;

import com.spbsu.util.cache.CacheItem;
import com.spbsu.util.persist.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 08.04.2008
 * Time: 17:28:01
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"unchecked"})
public abstract class BTreePage implements CacheItem {
  private final BTreeMap owner;
  private int id;

  public BTreePage(BTreeMap owner, int id) {
    if(id < 0)
      throw new RuntimeException("Negative page ID!!!");
    this.owner = owner;
    this.id = id;
  }

  public BTreePage(BTreeMap owner) {
    this.owner = owner;
    owner.createPage(this);
  }

  public BTreeMap owner(){
    return owner;
  }

  public int id(){
    return id;
  }

  public static BTreePage read(BTreeMap owner, int id) throws IOException{
    final ByteBuffer pageBuffer = owner.getPageBuffer(id, false);
    byte type = pageBuffer.get();
    try {
      final int pageType = (type >> 5) & 0x7;
      BTreePage result = null;
      switch (pageType){
        case 0: result = new PageManager(owner, id, type);
        case PageManager.ID: result = new PageManager(owner, id, (byte)(type & 0x1F)); break;
        case GranulatedDataPage.ID: result = new GranulatedDataPage(owner, id, (byte)(type & 0x1F)); break;
        case LeafPage.ID: result = new LeafPage(owner, id, (byte)(type & 0x1F)); break;
        case CompositeDataPage.ID: result = new CompositeDataPage(owner, id); break;
        case NodePage.ID: result = new NodePage(owner, id); break;
      }
      if(result != null) result.read(pageBuffer.slice());
      return result;
    }
    catch (Exception e) {
      throw new RuntimeException("BTree utility is inconsistent!", e);
    }
  }

  public static void write(BTreePage page) throws IOException {
    final ByteBuffer pageBuffer = page.owner().getPageBuffer(page.id(), true);
    pageBuffer.put((byte)page.type());
    page.write(pageBuffer);
    page.frozen = true;
  }

  protected abstract void write(ByteBuffer buffer) throws IOException;
  protected abstract void read(ByteBuffer buffer) throws IOException;
  protected abstract int type();

  private boolean changed = false;
  private boolean frozen = false;

  protected final boolean changed(boolean newValue){
    if(frozen)
      throw new RuntimeException("Trying to write to frozen page!");
    changed |= newValue;
    return changed;
  }

  public final void notifyRemove() {
    try{
      if(changed)
        write(this);
//      else
//        System.out.println("Skip");
    }
    catch (IOException ioe){
      throw new RuntimeException("Unable to write page!", ioe);
    }
  }

  public final void initEmpty(int id){
    if(id < 0)
      throw new RuntimeException("Trying to create page with negative ID!");
    this.id = id;
  }
}
