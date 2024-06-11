package com.expleague.commons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This class is used to harvest data in parallel and them consume this data from stream. Parallel usage of generation
 * and consumption is prohibited.
 */
public class Basket {
    private static final int MAX_BASKET_SIZE = 10_000;
    private final List<Block> blocks = new ArrayList<>();
    private volatile Block currentBlock;

    public Basket() {
        this(1);
    }

    public Basket(final int initialSize) {
        blocks.add(new Block(initialSize));
        currentBlock = blocks.get(blocks.size() - 1);
    }

    public <T> void append(final T value) {
        while (!currentBlock.tryAppend(value)) {
            appendBlock();
        }
    }

    /**
     * Use this if you don't care about performance due to isAssignable call.
     */
    @SuppressWarnings("unused")
    public <T> Stream<T> contentsOfType(final Class<T> type) {
        //noinspection unchecked
        return blocks.stream().flatMap(blk -> Arrays.stream(blk.contents))
            .map(obj -> (T) (type.isAssignableFrom(obj.getClass()) ? obj : null))
            .filter(Objects::nonNull);
    }

    private synchronized void appendBlock() {
        blocks.add(new Block(Math.min(MAX_BASKET_SIZE, currentBlock.capacity() * 2)));
        currentBlock = blocks.get(blocks.size() - 1);
    }

    public <T> Stream<T> stream() {
        //noinspection unchecked
        return blocks.stream().flatMap(Block::stream).map(obj -> (T) obj);
    }

    public boolean isEmpty() {
        return blocks.stream().noneMatch(blk -> blk.offset.get() > 0);
    }

    public long size() {
        return blocks.stream().mapToLong(blk -> blk.offset.get()).sum();
    }

    private static class Block {
        private final Object[] contents;
        private final AtomicInteger offset = new AtomicInteger();

        private Block(final int capacity) {
            this.contents = new Object[capacity];
        }

        boolean tryAppend(final Object value) {
            final int currentOffset = offset.getAndUpdate(idx -> idx < contents.length ? idx + 1 : idx);
            if (currentOffset < contents.length) {
                contents[currentOffset] = value;
                return true;
            }
            return false;
        }

        private int capacity() {
            return contents.length;
        }

        private <R> Stream<? extends R> stream() {
            //noinspection unchecked
            return (Stream<? extends R>) Arrays.stream(contents, 0, offset.get());
        }
    }
}
