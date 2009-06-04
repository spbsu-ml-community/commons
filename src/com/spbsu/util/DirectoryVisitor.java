package com.spbsu.util;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: lawless
 * Date: 07.07.2007
 * Time: 22:50:11
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryVisitor {
  protected void visitFile(final File file) {}
  protected boolean visitDirectory(final File directory) {return true;}
}
