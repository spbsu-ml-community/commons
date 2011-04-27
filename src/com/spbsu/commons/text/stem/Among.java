package com.spbsu.commons.text.stem;

import java.lang.reflect.Method;

public final class Among {
  public Among(
    final String s,
    final int substring_i,
    final int result,
    final String methodname,
    final AbstractStemmer methodobject
  ) {
    this.s_size = s.length();
    this.s = s;
    this.substring_i = substring_i;
    this.result = result;
    this.methodobject = methodobject;
    if (methodname.length() == 0) {
      this.method = null;
    }
    else {
      try {
        this.method = methodobject.getClass().
            getDeclaredMethod(methodname, new Class[0]);
      }
      catch (NoSuchMethodException e) {
        // FIXME - debug message
        this.method = null;
      }
    }
  }

  public final int s_size; /* search string */
  public final String s; /* search string */
  public final int substring_i; /* index to longest matching substring */
  public final int result;      /* result of the lookup */
  public Method method; /* method to use if substring matches */
  public final AbstractStemmer methodobject; /* object to invoke method on */
}
