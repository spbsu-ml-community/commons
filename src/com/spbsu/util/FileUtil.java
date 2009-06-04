package com.spbsu.util;

import java.io.File;
import java.util.ArrayList;

/**
 * @author lawless
 */
public class FileUtil {
  public static boolean hasSubDirs(final File root) {
    if (!root.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + root);
    }
    for (final File f : root.listFiles()) {
      if (f.isDirectory() && !".svn".equals(f.getName())) {
        return true;
      }
    }
    return false;
  }

  public static int countFiles(final File dir) {
    final Holder<Integer> holder = new Holder<Integer>(0);
    DirectoryTraverser.traverse(
      dir,
      new DirectoryVisitor() {
        protected void visitFile(final File file) {
          final Integer value = holder.getValue();
          holder.setValue(value == null ? 0 : value + 1);
        }
      }
    );
    return holder.getValue();
  }

  public static File[] getSubDirs(final File root) {
    final ArrayList<File> result = new ArrayList<File>();
    DirectoryTraverser.traverse(
      root,
      new DirectoryVisitor() {
        protected boolean visitDirectory(final File directory) {
          result.add(directory);
          return false;
        }
      }
    );

    return result.toArray(new File[result.size()]);
  }

  public static String getRelativePath(final File parent, final File child) {
    if (parent.equals(child)) return "";
    return child.getAbsolutePath().substring(parent.getAbsolutePath().length() + 1).replace('\\', '/');
  }

  public static File nextNewFile(final File directory, final String name) {
    if (!directory.isDirectory()) {
      directory.mkdirs();
    }
    int index = 0;
    while (true) {
      final File file = new File(directory, name + Integer.toString(index));
      if (!file.isFile() && !file.isDirectory()) {
        return file;
      }
      index++;
    }
  }

  public static File nextNewDir(final File parentDirectory, final String name) {
    if (!parentDirectory.isDirectory()) {
      parentDirectory.mkdirs();
    }
    int index = 0;
    while (true) {
      final File dir = new File(parentDirectory, name + Integer.toString(index));
      if (!dir.isDirectory() && !dir.isFile()) {
        dir.mkdir();
        return dir;
      }
      index++;
    }
  }

  private FileUtil() {
  }

  public static void deleteDirectoryWithContents(final File dir) {
    final File[] files = dir.listFiles();
    if(files == null) return;
    for (final File file : files) {
      long time = System.currentTimeMillis();
      if (file.isDirectory()) deleteDirectoryWithContents(file);
      else while (file.exists() && System.currentTimeMillis() - time < 1000) file.delete();
    }
    dir.delete();
  }
}
