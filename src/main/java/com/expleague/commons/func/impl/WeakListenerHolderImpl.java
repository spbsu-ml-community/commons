package com.expleague.commons.func.impl;

import com.expleague.commons.func.WeakListenerHolder;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * User: solar
 * Date: 18.11.13
 * Time: 19:37
 */
public class WeakListenerHolderImpl<Event> implements WeakListenerHolder<Event> {
  protected final List<WeakReference<Consumer<? super Event>>> listeners = new CopyOnWriteArrayList<>();
  @Override
  public void addListener(final Consumer<? super Event> lst) {
    listeners.add(new WeakReference<>(lst));
  }

  @Override
  public void removeListener(Consumer<? super Event> action) {
    final Iterator<WeakReference<Consumer<? super Event>>> it = listeners.iterator();
    while (it.hasNext()) {
      final WeakReference<Consumer<? super Event>> next = it.next();
      if (action.equals(next.get())) {
        listeners.remove(next);
        break;
      }
    }
  }

  protected void invoke(final Event e) {
    final Iterator<WeakReference<Consumer<? super Event>>> it = listeners.iterator();
    while (it.hasNext()) {
      final WeakReference<Consumer<? super Event>> next = it.next();
      final Consumer<? super Event> action = next.get();
      if (action != null)
        action.accept(e);
      else {
        listeners.remove(next);
      }
    }
  }
}
