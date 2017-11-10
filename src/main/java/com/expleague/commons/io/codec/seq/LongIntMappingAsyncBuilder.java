package com.expleague.commons.io.codec.seq;

import com.expleague.commons.util.ArrayTools;
import com.expleague.commons.util.sync.StateLatch;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * User: solar
 * Date: 15.10.15
 * Time: 14:02
 */
public class LongIntMappingAsyncBuilder {
  private int accumulatorSize = 0;
  private long[] accumulatorKeys;
  private int[] accumulatorValues;
  private final int tlBufferSize;
  private long accumulatorValuesTotal;
  private final List<BufferWithState> allBuffers = new CopyOnWriteArrayList<>();
  private ThreadLocal<BufferWithState> tlBuffer = new ThreadLocal<BufferWithState>(){
    @Override
    protected BufferWithState initialValue() {
      final BufferWithState result = new BufferWithState();
      allBuffers.add(result);
      return result;
    }
  };

  public LongIntMappingAsyncBuilder(int tlBufferSize) {
    this.tlBufferSize = tlBufferSize;
  }

  public void populateImpl(Consumer<TLongIntMap> map) throws InterruptedException {
    final BufferWithState bufferWithState = tlBuffer.get();
    final TLongIntMap buffer = bufferWithState.map;
    bufferWithState.state(1, 2);
    map.accept(buffer);
    if (buffer.size() >= tlBufferSize) {
      flushBuffer(bufferWithState);
    }
    bufferWithState.state(1);
  }

  private void flushBuffer(BufferWithState bufferWithState) {
    bufferWithState.state(4);
    final TLongIntMap buffer = bufferWithState.map;
    final long[] bufferKeys = buffer.keys();
    final int[] bufferValues = buffer.values();
    ArrayTools.parallelSort(bufferKeys, bufferValues);
    synchronized (this) {
      if (buffer.isEmpty())
        return;
      final long[] currentKeys = accumulatorKeys;
      final int[] currentValues = accumulatorValues;
      final int acculength = accumulatorSize;
      accumulatorKeys = new long[acculength + buffer.size()];
      accumulatorValues = new int[acculength + buffer.size()];
      int indexA = 0;
      int indexB = 0;
      int index = 0;
      long total = 0l;
      final int bufferLength = bufferKeys.length;
      final long[] accumulatorKeysLocal = this.accumulatorKeys;
      final int[] accumulatorValuesLocal = this.accumulatorValues;
      while (indexA < acculength || indexB < bufferLength) {
        if (indexB >= bufferLength) {
          accumulatorKeysLocal[index] = currentKeys[indexA];
          total += accumulatorValuesLocal[index] = currentValues[indexA];
          index++;
          indexA++;
        }
        else if (indexA >= acculength) {
          accumulatorKeysLocal[index] = bufferKeys[indexB];
          total += accumulatorValuesLocal[index] = bufferValues[indexB];
          index++;
          indexB++;
        }
        else if (currentKeys[indexA] < bufferKeys[indexB]) {
          accumulatorKeysLocal[index] = currentKeys[indexA];
          total += accumulatorValuesLocal[index] = currentValues[indexA];
          index++;
          indexA++;
        }
        else if (currentKeys[indexA] > bufferKeys[indexB]) {
          accumulatorKeysLocal[index] = bufferKeys[indexB];
          total += accumulatorValuesLocal[index] = bufferValues[indexB];
          index++;
          indexB++;
        }
        else { // equals
          accumulatorKeysLocal[index] = bufferKeys[indexB];
          total += accumulatorValuesLocal[index] = bufferValues[indexB] + currentValues[indexA];
          index++;
          indexA++;
          indexB++;
        }
      }
      accumulatorSize = index;
      accumulatorValuesTotal = total;
      buffer.clear();
    }
    bufferWithState.state(1);
  }

  public void visitRange(long from, long to, TLongIntProcedure todo) {
    flush();
    synchronized (this) {
      int index = Arrays.binarySearch(accumulatorKeys, from);
      index = index < 0 ? -index - 1 : index;
      while (accumulatorKeys.length > index && accumulatorKeys[index] < from)
        index++;
      while (accumulatorKeys.length > index && accumulatorKeys[index] < to) {
        todo.execute(accumulatorKeys[index], accumulatorValues[index]);
        index++;
      }
    }
  }

  public void visit(TLongIntProcedure todo) {
    flush();
    synchronized (this) {
      int index = 0;
      while (accumulatorKeys.length > index) {
        todo.execute(accumulatorKeys[index], accumulatorValues[index]);
        index++;
      }
    }
  }

  private void flush() {
    for (int i = 0; i < allBuffers.size(); i++) {
      final BufferWithState bufferWithState = allBuffers.get(i);
      bufferWithState.await(1);
      flushBuffer(bufferWithState);
    }
  }

  public void populate(Consumer<TLongIntMap> map) {
    try {
      populateImpl(map);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public long accumulatedValuesTotal() {
    return accumulatorValuesTotal;
  }

  class BufferWithState {
    private final TLongIntMap map = new TLongIntHashMap((int)(tlBufferSize / 0.8), 0.8f);
    private final StateLatch latch = new StateLatch(1);

    public void state(int newState) {
      latch.state(newState);
    }

    public void await(int mask) {
      try {
        latch.await(mask);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    public void state(int from, int to) {
      await(from);
      state(to);
    }
  }
}
