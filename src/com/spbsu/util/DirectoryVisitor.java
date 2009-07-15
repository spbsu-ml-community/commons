package com.spbsu.util;

import java.io.File;

/**
 * User: lawless
 * Date: 07.07.2007
 */
public class DirectoryVisitor {
  protected void visitFile(final File file) {}
  protected boolean visitDirectory(final File directory) {return true;}
}
