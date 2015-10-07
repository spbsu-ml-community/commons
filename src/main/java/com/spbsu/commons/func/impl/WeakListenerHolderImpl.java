package com.spbsu.commons.func.impl;

import com.spbsu.commons.func.Action;
import com.spbsu.commons.func.WeakListenerHolder;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: solar
 * Date: 18.11.13
 * Time: 19:37
 */
public class WeakListenerHolderImpl<Event> implements WeakListenerHolder<Event> {
  protected final List<WeakReference<Action<? super Event>>> listeners = new CopyOnWriteArrayList<>();
  @Override
  public void addListener(final Action<? super Event> lst) {
    listeners.add(new WeakReference<>(lst));
  }

  @Override
  public void removeListener(Action<? super Event> action) {
    final Iterator<WeakReference<Action<? super Event>>> it = listeners.iterator();
    while (it.hasNext()) {
      final WeakReference<Action<? super Event>> next = it.next();
      if (action.equals(next.get())) {
        listeners.remove(next);
        break;
      }
    }
  }

  protected void invoke(final Event e) {
    final Iterator<WeakReference<Action<? super Event>>> it = listeners.iterator();
    while (it.hasNext()) {
      final WeakReference<Action<? super Event>> next = it.next();
      final Action<? super Event> action = next.get();
      if (action != null)
        action.invoke(e);
      else
        it.remove();
    }
  }
}
