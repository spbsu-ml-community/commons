package com.expleague.commons.filters;

/**
 * Created by inikifor on 18.12.14.
 */
public interface ExplainableFilter<T> extends Filter<T> {

  String explain();

}
