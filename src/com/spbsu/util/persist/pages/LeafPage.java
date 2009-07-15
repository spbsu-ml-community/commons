package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;
import com.spbsu.util.persist.PageManager;
import gnu.trove.TIntLongHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Stack;

/**
 * User: igorkuralenok
 * Date: 08.04.2008
 */
public class LeafPage extends IndexPage {
  private static final int B = (BTreeMap.SLOT_CAPACITY - 3) / (8+4);
  private int[] knownKeys = null;
  private long[] knownValues = null;
  TIntLongHashMap newValues = null;
  int upperBoundAddress;
  int type;
  public static final int ID = 3;

  public LeafPage(BTreeMap owner, int id, byte type) {
    super(owner, id);
    this.type = type;
  }

  public LeafPage(BTreeMap owner) {
    super(owner);
    knownKeys = new int[0];
    knownValues = new long[0];
  }

  protected void write(final ByteBuffer buffer) throws IOException {
    normalize();
    buffer.putShort((short)knownKeys.length);
    buffer.asIntBuffer().put(knownKeys);
    buffer.position(buffer.position() + knownKeys.length * 4);
    buffer.asLongBuffer().put(knownValues);
  }

  protected void read(ByteBuffer buffer) {
    final short count = buffer.getShort();
    knownKeys = new int[count];
    knownValues = new long[count];
    buffer.asIntBuffer().get(knownKeys);
    buffer.position(buffer.position() + knownKeys.length * 4);
    buffer.asLongBuffer().get(knownValues);
  }

  protected int type(){
    return (ID << 5) | type;
  }

  public long find(int hashCode) {
    if(newValues != null){
      final long value = newValues.get(hashCode);
      if(value != 0) return value;
    }
    final int keyIndex = Arrays.binarySearch(knownKeys, hashCode);
    if(keyIndex >= 0) return knownValues[keyIndex];
    return 0;
  }

  public void insert(int hashCode, Stack<NodePage> parents, byte[] dataChunk) {
    changed(true);
    final PageManager pageManager = owner().pageManager;
    final DataPage page = pageManager.nextDataPage(dataChunk.length);
    final long address = page.writeData(ByteBuffer.wrap(dataChunk));
    if(newValues == null) newValues = new TIntLongHashMap();
    newValues.put(hashCode, address);
    if(newValues.size() + knownKeys.length > B){
      normalize();
      split(parents);
    }
  }

  private void normalize() {
    if(newValues == null || newValues.isEmpty()) return;
    int[] keys = new int[knownKeys.length + newValues.size()];
    long[] values = new long[knownKeys.length + newValues.size()];
    mergeArrayWithHash(keys, values, knownKeys, knownValues, newValues);
    knownKeys = keys;
    knownValues = values;
    newValues.clear();
  }

  public static void mergeArrayWithHash(int[] keys, long[] values, int[] knownKeys, long[] knownValues, TIntLongHashMap newValues) {
    final int[] newKeys = newValues.keys();
    Arrays.sort(newKeys);

    int oldIndex = 0;
    int prevOldIndex = 0;
    int newIndex = 0;
    int index = 0;
    while(oldIndex < knownKeys.length && newIndex < newKeys.length){
      if(newKeys[newIndex] <= knownKeys[oldIndex]){
        if(oldIndex > prevOldIndex){
          final int sequentLength = oldIndex - prevOldIndex;
          System.arraycopy(knownKeys, prevOldIndex, keys, index, sequentLength);
          System.arraycopy(knownValues, prevOldIndex, values, index, sequentLength);
          index += sequentLength;
          prevOldIndex = oldIndex;
        }
        if(newKeys[newIndex] == knownKeys[oldIndex]){
          prevOldIndex++; oldIndex++;
        }
        keys[index] = newKeys[newIndex++];
        values[index] = newValues.get(keys[index]);
        index ++;
      }
      else oldIndex++;
    }

    final int knownKeysLeft = knownKeys.length - prevOldIndex;
    if(knownKeysLeft > 0){
      System.arraycopy(knownKeys, prevOldIndex, keys, index, knownKeysLeft);
      System.arraycopy(knownValues, prevOldIndex, values, index, knownKeysLeft);
      index += knownKeysLeft;
    }

    for(; newIndex < newKeys.length; newIndex++){
      keys[index] = newKeys[newIndex];
      values[index++] = newValues.get(newKeys[newIndex]);
    }
  }

  private LeafPage split(Stack<NodePage> parents) {
    final PageManager pageManager = owner().pageManager;
    final LeafPage brother = pageManager.nextLeafPage();
    int[] leftPartKeys = new int[knownKeys.length / 2], rightPartKeys = new int[knownKeys.length - knownKeys.length / 2];
    long[] leftPartValues = new long[knownKeys.length / 2], rightPartValues = new long[knownKeys.length - knownKeys.length / 2];
    System.arraycopy(knownKeys, 0, leftPartKeys, 0, leftPartKeys.length);
    System.arraycopy(knownKeys, leftPartKeys.length, rightPartKeys, 0, rightPartKeys.length);
    System.arraycopy(knownValues, 0, leftPartValues, 0, leftPartKeys.length);
    System.arraycopy(knownValues, leftPartKeys.length, rightPartValues, 0, rightPartKeys.length);
    brother.knownKeys = leftPartKeys;
    brother.knownValues = leftPartValues;
    knownKeys = rightPartKeys;
    knownValues = rightPartValues;

    brother.changed(true);
    if(parents.isEmpty()){
      final NodePage topMost = pageManager.nextNodePage(true);
      topMost.insert(parents, brother, this, leftPartKeys[leftPartKeys.length - 1]);
    }
    else parents.peek().insert(parents, brother, this, leftPartKeys[leftPartKeys.length - 1]);
    return brother;
  }

  public void update(int hashCode, long newAddress) {
    if (newValues == null || !newValues.containsKey(hashCode)) {
      final int index = Arrays.binarySearch(knownKeys, hashCode);
      if (index < 0) throw new RuntimeException("Can not update address: no such key found!");
      knownValues[index] = newAddress;
    }
    else newValues.put(hashCode, newAddress);
    changed(true);
  }
}
