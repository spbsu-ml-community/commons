package com.expleague.commons.io.codec.seq;

import com.expleague.commons.util.ArrayTools;
import com.expleague.commons.util.sync.StateLatch;
import gnu.trove.impl.hash.TLongIntHash;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
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
  private final List<WeakReference<BufferWithState>> allBuffers = new CopyOnWriteArrayList<>();
  private ThreadLocal<BufferWithState> tlBuffer = ThreadLocal.withInitial(() -> {
    final BufferWithState result = new BufferWithState();
    allBuffers.add(new WeakReference<>(result));
    return result;
  });

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
    final TLongIntMap buffer = bufferWithState.map;
    if (buffer.isEmpty())
      return;
    bufferWithState.state(4);
    final long[] bufferKeys = buffer.keys();
    final int[] bufferValues = buffer.values();
    ArrayTools.parallelSort(bufferKeys, bufferValues);
    synchronized (this) {
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
      final BufferWithState value = new BufferWithState();
      tlBuffer.set(value);
      allBuffers.add(new WeakReference<>(value));
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
    Iterator<WeakReference<BufferWithState>> it = allBuffers.iterator();
    while (it.hasNext()) {
      BufferWithState bufferWithState = it.next().get();
      if (bufferWithState != null) {
        bufferWithState.await(1);
        flushBuffer(bufferWithState);
      }
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

  public TLongIntMap asMap() {
    flush();
    synchronized (this) {
      final TLongIntMap map = new TLongIntHashMap(accumulatorKeys.length, (float) 0.8);
      int index = 0;
      while (accumulatorKeys.length > index) {
        map.put(accumulatorKeys[index], accumulatorValues[index]);
        index++;
      }
      return map;
    }
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
