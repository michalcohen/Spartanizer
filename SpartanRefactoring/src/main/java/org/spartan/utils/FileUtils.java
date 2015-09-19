package org.spartan.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for utility methods when working with files and directories
 *
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 */
public class FileUtils {
  /**
   * Returns a list of all the .java files found recursively within the provided
   * paths
   *
   * @param paths Directories to scan
   * @return a list of absolute paths to .java files found within the provided
   *         directories. If no files were found, an empty list is returned
   */
  public static List<String> findAllJavaFiles(final String... paths) {
    final List<String> $ = new ArrayList<>();
    if (paths.length == 0)
      return $;
    for (final String s : paths) {
      if (s == null)
        continue;
      final File f = new File(s);
      if (f.exists() && f.isDirectory())
        iterateFiles(new File(s), $);
    }
    return $;
  }
  private static void iterateFiles(final File dir, final List<String> files) {
    if (dir == null)
      return;
    for (final File f : dir.listFiles()) {
      if (f.isDirectory())
        iterateFiles(f, files);
      if (f.isFile() && f.toPath().endsWith(".java"))
        files.add(f.getAbsolutePath());
    }
  }
}
