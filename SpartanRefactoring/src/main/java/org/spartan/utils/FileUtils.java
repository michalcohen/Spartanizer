package org.spartan.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for utility methods when working with files and directories
 *
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015/09/19
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
  /**
   * Returns the contents of a source file
   *
   * @param path The source file's path
   * @return the source file's contents, or an empty string in case of an error
   */
  public static String readSourceFromFile(final String path) {
    try {
      return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      return "";
    }
  }
  @SuppressWarnings("javadoc") public static void writeSourceToFile(final String path, final String source) {
    try {
      final PrintWriter p = new PrintWriter(path);
      p.write(source);
      p.flush();
      p.close();
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  private static void iterateFiles(final File dir, final List<String> files) {
    if (dir == null)
      return;
    for (final File f : dir.listFiles()) {
      if (f.isDirectory())
        iterateFiles(f, files);
      if (f.isFile() && f.getName().endsWith(".java"))
        files.add(f.getAbsolutePath());
    }
  }
}
