package com.expleague.commons.io.codec.seq;

import com.expleague.commons.util.ArrayTools;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * User: solar
 * Date: 15.10.15
 * Time: 14:02
 */
public class LongIntMappingAsyncBuilder {
  private volatile int accumulatorSize = 0;
  private volatile long[] accumulatorKeys;
  private volatile int[] accumulatorValues;
  private volatile long accumulatorValuesTotal;
  private final int tlBufferSize;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Set<BufferWithLock> allBuffers = new HashSet<>();
  private ThreadLocal<Supplier<BufferWithLock>> tlBuffer = ThreadLocal.withInitial(() -> this.new BufferHandler());

  public LongIntMappingAsyncBuilder(int tlBufferSize) {
    this.tlBufferSize = tlBufferSize;
  }

  public void populate(Consumer<TLongIntMap> map) {
    BufferWithLock bufferWithLock;
    int counter = 0;
    do {
      bufferWithLock = tlBuffer.get().get();
      if (counter++ > 10)
        LockSupport.parkNanos(1_000_000);
    }
    while (!bufferWithLock.lock.tryLock());
    int size;
    try {
      map.accept(bufferWithLock.map);
      size = bufferWithLock.map.size();
    }
    finally {
      bufferWithLock.lock.unlock();
    }
    if (size >= tlBufferSize) {
      flushBuffer(bufferWithLock);
    }
  }

  public void visitRange(long from, long to, TLongIntProcedure todo) {
    flush();
    lock.readLock().lock();
    try {
      int index = Arrays.binarySearch(accumulatorKeys, from);
      index = index < 0 ? -index - 1 : index;
      while (accumulatorKeys.length > index && accumulatorKeys[index] < from)
        index++;
      while (accumulatorKeys.length > index && accumulatorKeys[index] < to) {
        todo.execute(accumulatorKeys[index], accumulatorValues[index]);
        index++;
      }
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public void visit(TLongIntProcedure todo) {
    flush();
    lock.readLock().lock();
    try {
      int index = 0;
      while (accumulatorSize > index) {
        todo.execute(accumulatorKeys[index], accumulatorValues[index]);
        index++;
      }
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public long accumulatedValuesTotal() {
    return accumulatorValuesTotal;
  }

  public TLongIntMap asMap() {
    flush();
    lock.readLock().lock();
    try {
      final TLongIntMap map = new TLongIntHashMap(accumulatorKeys.length, (float) 0.8);
      int index = 0;
      while (accumulatorKeys.length > index) {
        map.put(accumulatorKeys[index], accumulatorValues[index]);
        index++;
      }
      return map;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  private BufferWithLock allocate(BufferHandler bufferHandler) {
    lock.writeLock().lock();
    try {
      final BufferWithLock result = new BufferWithLock(bufferHandler);
      allBuffers.add(result);
      return result;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  private void flush() {
    new ArrayList<>(allBuffers).forEach(this::flushBuffer);
  }

  private void flushBuffer(BufferWithLock bufferWithState) {
    bufferWithState.lock.lock(); // will never unlock, the object will be destroyed after merge
    bufferWithState.handler.reset();

    final TLongIntMap buffer = bufferWithState.map;
    final long[] bufferKeys = buffer.keys();
    final int[] bufferValues = buffer.values();
    ArrayTools.parallelSort(bufferKeys, bufferValues);

    lock.writeLock().lock();
    try {
      allBuffers.remove(bufferWithState);

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
      long total = 0L;
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
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public double size() {
    return accumulatorSize;
  }

  private class BufferWithLock {
    private final BufferHandler handler;
    private final TLongIntMap map = new TLongIntHashMap((int)(tlBufferSize / 0.8), 0.8f);
    private final Lock lock = new ReentrantLock();

    BufferWithLock(BufferHandler handler) {
      this.handler = handler;
    }
  }

  private class BufferHandler implements Supplier<BufferWithLock> {
    private volatile BufferWithLock allocated;
    @Override
    public BufferWithLock get() {
      return allocated == null ? allocated = allocate(this) : allocated;
    }

    public void reset() {
      allocated = null;
    }
  }
}
