package com.expleague.commons.filters;

import com.expleague.commons.system.RuntimeUtils;

/**
 * User: solar
 * Date: 25.06.13
 * Time: 10:41
 */
public class ClassFilter<T> implements Filter<T> {
  public final Class<?> clazz;
  public final Class<?>[] typeParameters;

  public ClassFilter(final Class<?> clazz, final Class<?>... typeParameters) {
    this.clazz = clazz;
    this.typeParameters = typeParameters;
    if (typeParameters.length != 0 && typeParameters.length != clazz.getTypeParameters().length)
      throw new IllegalArgumentException("Type parameters count does not equal parameters of class");
  }

  @Override
  public boolean accept(final T t) {
    if (!clazz.isAssignableFrom(t.getClass()))
      return false;
    if (typeParameters.length == 0)
      return true;
    final Class[] parameters = RuntimeUtils.findTypeParameters(t.getClass(), clazz);
    for(int i = 0; i < parameters.length; i++) {
      if (!typeParameters[i].isAssignableFrom(parameters[i]))
        return false;
    }
    return true;
  }
}
