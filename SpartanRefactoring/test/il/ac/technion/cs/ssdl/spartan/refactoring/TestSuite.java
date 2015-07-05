package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * An abstract representation of our test suite, which is represented in
 * directory tree.
 *
 * @author Yossi Gil
 * @since 2014/05/24
 */
/**
 * @author yogi
 */
public abstract class TestSuite {
  /**
   * A String determines whereas we are at the IN or OUT side of the test See
   * TestCases test files for reference.
   */
  final static String testKeyword = "<Test Result>";
  /**
   * Suffix for test files.
   */
  final static String testSuffix = ".test";
  /**
   * Folder in which all test cases are found
   */
  public static final File location = new File("TestCases");

  static String readFile(final File f) {
    try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
      final StringBuilder $ = new StringBuilder();
      for (String line = r.readLine(); line != null; line = r.readLine())
        $.append(line).append(System.lineSeparator());
      return $.toString();
    } catch (final IOException e) {
      fail(e.toString());
      return null;
    }
  }

  static Spartanization makeSpartanizationObject(final File f) {
    return makeSpartanizationObject(f.getName());
  }

  static Spartanization makeSpartanizationObject(final String folderForClass) {
    final Class<?> c = asClass(folderForClass);
    assertNotNull(c);
    final Object $ = getInstance(c);
    assertNotNull($);
    return (Spartanization) $;
  }

  /**
   * Instantiates a {@link Class} object if possible, otherwise generate an
   * assertion failure
   *
   * @param c
   *          an arbitrary class object
   * @return an instance of the parameter
   */
  public static Object getInstance(final Class<?> c) {
    try {
      return c.newInstance();
    } catch (final SecurityException e) {
      error("Security exception in instantiating ", c, e);
    } catch (final ExceptionInInitializerError e) {
      error("Error in instantiating class", c, e);
    } catch (final InstantiationException e) {
      error("Nullary constructor threw an exception in class", c, e);
    } catch (final IllegalAccessException e) {
      error("Missing public constructor (probably) in class", c, e);
    }
    return null;
  }

  private static Spartanization error(final String message, final Class<?> c, final Throwable e) {
    System.err.println(message + " '" + c.getCanonicalName() + "' " + e.getMessage());
    return null;
  }

  /**
   * Convert a canonical name of a class into a {@link Class} object, if
   * possible, otherwise generate an assertion failure
   *
   * @param name
   *          the canonical name of some class
   * @return the object representing this class
   * @since 2014/05/23
   */
  private static Class<?> asClass(final String name) {
    try {
      return Class.forName(name);
    } catch (final ClassNotFoundException e) {
      fail(name + ": class not found. " + e.getMessage());
      return null;
    }
  }

  /**
   * An abstract class representing the concept of traversing the
   * {@link #location} while generating test cases.
   *
   * @see TestSuite.Traverse.Files
   * @see TestSuite.Traverse.Directories
   * @author Yossi Gil
   * @since 2014/05/24
   */
  public static abstract class Traverse extends TestSuite {
    /**
     * @return a collection of all test cases generated in the traversal
     */
    public final Collection<Object[]> go() {
      final List<Object[]> $ = new ArrayList<>();
      for (final File f : location.listFiles())
        go($, f);
      return $;
    }

    /**
     * Collect test cases from each file in {@link #location}
     *
     * @param $
     *          where to save the collected test cases
     * @param f
     *          an entry in {@link #location}
     */
    public abstract void go(List<Object[]> $, final File f);
  }

  /**
   ** An abstract class to be extended and implemented by client, while
   * overriding {@link #go(List, File)} as per customer's need.
   *
   * @see TestSuite.Traverse.Files
   * @see TestSuite.Traverse
   * @author Yossi Gil
   * @since 2014/05/24
   */
  public static abstract class Directories extends TestSuite.Traverse {
    /**
     * Adds a test case to the collection of all test cases generated in the
     * traversal
     */
    @Override public final void go(final List<Object[]> $, final File f) {
      if (f.isDirectory()) {
        final Object[] c = makeCase(f);
        if (c != null)
          $.add(c);
      }
    }

    abstract Object[] makeCase(File d);
  }

  /**
   ** An abstract class to be extended and implemented by client, while
   * overriding {@link #go(List, File)} as per customer's need.
   *
   * @see TestSuite.Traverse.Directories
   * @see TestSuite.Traverse
   * @author Yossi Gil
   * @since 2014/05/24
   */
  public static abstract class Files extends TestSuite.Traverse {
    /* (non-Javadoc)
     *
     * @see
     * il.ac.technion.cs.ssdl.spartan.refactoring.TestSuite.Traverse#go(java
     * .util.List, java.io.File) */
    @Override public void go(final List<Object[]> $, final File d) {
      final Spartanization s = makeSpartanizationObject(d);
      for (final File f : d.listFiles())
        if (f.isFile() && f.exists()) {
          final Object[] c = makeCase(s, d, f, f.getName());
          if (c != null)
            $.add(c);
        }
    }

    abstract Object[] makeCase(final Spartanization s, final File d, final File f, final String name);
  }

  /* Auxiliary function for test suite inherited classes */
  enum TestDirection {
    In, Out
  }

  /**
   * Makes an Input file out of a Test file
   */
  static File makeInFile(final File f) {
    return createTempFile(deleteTestKeyword(fileToStringBuilder(f)), TestDirection.In, f);
  }

  private static StringBuilder deleteTestKeyword(final StringBuilder $) {
    if ($.indexOf(testKeyword) > 0)
      $.delete($.indexOf(testKeyword), $.length());
    return $;
  }

  /**
   * Makes an Output file out of a Test file
   */
  static File makeOutFile(final File f) {
    final StringBuilder $ = fileToStringBuilder(f);
    if ($.indexOf(testKeyword) > 0)
      $.delete(0, $.indexOf(testKeyword) + testKeyword.length() + ($.indexOf("\r\n") > 0 ? 2 : 1));
    return createTempFile($, TestDirection.Out, f);
  }

  /**
   * Creates a temporary file - including lazy deletion.
   */
  static File createTempFile(final StringBuilder b, final TestDirection direction, final File f) {
    File $;
    try {
      $ = File.createTempFile(f.getName().replace(".", ""), direction == TestDirection.In ? ".in" : ".out");
    } catch (final IOException e) {
      $ = null; // Failed to create temporary file
    }
    try (final RandomAccessFile fh = new RandomAccessFile($, "rw")) {
      fh.writeBytes(b.toString());
      if ($ != null)
        $.deleteOnExit();
    } catch (final IOException e) {
      e.printStackTrace(); // Probably permissions problem
    }
    return $;
  }

  /**
   * Creates a StringBuilder object out of a file object.
   */
  static StringBuilder fileToStringBuilder(final File f) {
    try (final Scanner $ = new Scanner(f)) {
      return new StringBuilder($.useDelimiter("\\Z").next());
    } catch (final Exception e) {
      return new StringBuilder("");
    }
  }
}
