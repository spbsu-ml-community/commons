package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;
import com.spbsu.util.persist.PageManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * User: igorkuralenok
 * Date: 08.04.2008
 */
public class NodePage extends IndexPage {
  private static final int B = (BTreeMap.SLOT_CAPACITY - 1) / 8;
  final TreeMap<Integer, Integer> values = new TreeMap<Integer, Integer>();
  int upperBoundAddress;
  public static final int ID = 6;

  public NodePage(BTreeMap owner, int id) {
    super(owner, id);
  }

  public NodePage(BTreeMap owner) {
    super(owner);
  }

  protected void write(ByteBuffer buffer) throws IOException {
    int next = -1;
    for (Map.Entry<Integer, Integer> entry : values.entrySet()) {
      next = entry.getKey();
      buffer.putInt(next);
      buffer.putInt(entry.getValue());
    }
    buffer.putInt(next);
    buffer.putInt(upperBoundAddress);
  }

  protected void read(ByteBuffer buffer) {
    int current;
    int next = buffer.getInt();
    do{
      current = next;
      values.put(current, buffer.getInt());
      next = buffer.getInt();
      if(buffer.remaining() <= 4) break;
    }
    while(current < next);
    upperBoundAddress = buffer.getInt();
  }

  protected int type(){
    return (ID << 5);
  }

  public Integer find(int hashCode) {
    if(values.isEmpty()) return null;
    final Map.Entry<Integer, Integer> ceilValue = values.ceilingEntry(hashCode);
    if(ceilValue == null) return upperBoundAddress;
    else return ceilValue.getValue();
  }

  void insert(Stack<NodePage> parents, BTreePage left, BTreePage right, int split){
    changed(true);
    if(values.isEmpty()){
      upperBoundAddress = right.id();
      values.put(split, left.id());
      return;
    }

    values.put(split, left.id());
    if(values.size() >= B){
      final NodePage brother = split(parents);
      brother.upperBoundAddress = brother.values.lastEntry().getValue();
      brother.values.remove(brother.values.lastKey());
    }
  }

  private NodePage split(Stack<NodePage> parents) {
    final PageManager pageManager = owner().pageManager;
    final NodePage brother = pageManager.nextNodePage(false);
    final Iterator<Map.Entry<Integer, Integer>> iter = values.entrySet().iterator();
    int index = 0;
    int lastMovedHash = -1;
    while (iter.hasNext() && index <= B / 2) {
      final Map.Entry<Integer, Integer> entry = iter.next();
      iter.remove();
      lastMovedHash = entry.getKey();
      brother.values.put(entry.getKey(), entry.getValue());
      index++;
    }
    brother.changed(true);
    if(parents.isEmpty()){
      final NodePage topMost = pageManager.nextNodePage(true);
      topMost.insert(parents, brother, this, lastMovedHash);
    }
    else parents.pop().insert(parents, brother, this, lastMovedHash);
    return brother;
  }
}