package com.spbsu.commons.util.cache.impl;

import com.spbsu.commons.util.cache.CacheStrategy;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class LRUStrategy implements CacheStrategy {
  private int misses;
  private int access;
  private ListNode[] usages;
  private ListNode head;
  private ListNode tail;

  public LRUStrategy(final int size) {
    init(size);
  }

  private void init(final int size) {
    usages = new ListNode[size];
    ListNode prev = null;
    for (int i = 0; i < usages.length; i++) {
      final ListNode usage = new ListNode(prev, i);
      if(prev != null) prev.prev = usage;
      usages[i] = usage;
      prev = usage;
    }
    head = usages[size - 1];
    tail = usages[0];
  }

  public int getStorePosition() {
    return tail.value;
  }

  public void registerAccess(final int position) {
    final LRUStrategy.ListNode node = usages[position];

    if(node != head){
      final LRUStrategy.ListNode next = node.next;
      final LRUStrategy.ListNode prev = node.prev;

      head.prev = node;
      node.next = head;
      node.prev = null;
      head = node;

      prev.next = next;
      if(next != null) next.prev = prev;
      else tail = prev;
    }
    access++;
  }

  public void removePosition(final int position) {
    final LRUStrategy.ListNode node = usages[position];

    if(node != tail){
      final LRUStrategy.ListNode next = node.next;
      final LRUStrategy.ListNode prev = node.prev;

      tail.next = node;
      node.prev = tail;
      node.next = null;
      tail = node;

      next.prev = prev;
      if(prev != null) prev.next = next;
      else head = next;
    }
  }

  public void registerCacheMiss() {
    misses++;
  }

  public int getAccessCount() {
    return access;
  }

  public int getCacheMisses() {
    return misses;
  }

  public void clear() {
    init(usages.length);
    misses = 0;
    access = 0;
  }

  private static class ListNode {
    ListNode next;
    ListNode prev;
    int value;

    public ListNode(final ListNode next, final int value) {
      this.next = next;
      this.value = value;
    }
  }
}
