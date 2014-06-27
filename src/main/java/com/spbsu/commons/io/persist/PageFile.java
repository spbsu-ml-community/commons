package com.spbsu.commons.io.persist;

import com.spbsu.commons.io.Buffer;

/**
 * User: igorkuralenok
 * Date: 14.10.2009
 * Time: 14:44:14
 */
public interface PageFile {
  Buffer read(PageFileAddress address);
  PageFileAddress write(Buffer source);

  PageFileAddress readHeader(int index);
  void writeHeader(int index, PageFileAddress headerRef);

  void enqueueForCleanup(PageFileAddress address);
  void cleanup();

  void close();

  long size();
}
