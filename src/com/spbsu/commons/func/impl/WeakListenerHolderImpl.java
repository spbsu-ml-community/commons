package com.spbsu.commons.func.impl;

import com.spbsu.commons.func.Action;
import com.spbsu.commons.func.WeakListenerHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: solar
 * Date: 18.11.13
 * Time: 19:37
 */
public class WeakListenerHolderImpl<Event> implements WeakListenerHolder<Event> {
  List<WeakReference<Action<Event>>> listeners = new ArrayList<WeakReference<Action<Event>>>();
  @Override
  public void addListener(Action<Event> lst) {
    listeners.add(new WeakReference<Action<Event>>(lst));
  }

  protected void invoke(Event e) {
    final Iterator<WeakReference<Action<Event>>> it = listeners.iterator();
    while (it.hasNext()) {
      WeakReference<Action<Event>> next = it.next();
      final Action<Event> action = next.get();
      if (action != null)
        action.invoke(e);
      else
        it.remove();
    }
  }
}
