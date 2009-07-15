package com.spbsu.util;

import java.io.File;

/**
 * User: lawless
 * Date: 07.07.2007
 */
public abstract class DirectoryTraverser {
  private DirectoryTraverser() {}

  public static void traverse(final File root, final DirectoryVisitor visitor) {
    if (!root.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + root);
    }
    for (final File f : root.listFiles()) {
      if (f.isFile()) {
        visitor.visitFile(f);
      }
      else if (f.isDirectory()) {
        if (".svn".equals(f.getName())) continue;
        if (!visitor.visitDirectory(f)) continue;
        traverse(f, visitor);
      }
    }
  }

  public static void visitSubDirs(final File root, final DirectoryVisitor visitor) {
    if (!root.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + root);
    }
    for (final File f : root.listFiles()) {
      if (f.isDirectory() && !".svn".equals(f.getName())) {
        visitor.visitDirectory(f);
      }
    }
  }
}
