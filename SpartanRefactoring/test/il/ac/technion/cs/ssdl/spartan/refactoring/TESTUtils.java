package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;

import il.ac.technion.cs.ssdl.spartan.utils.As;

enum TESTUtils {
  ;
  static final String WHITES = "\\s+";

  public static void assertSimilar(final String expected, final String actual) {
    if (!expected.equals(actual))
      assertEquals(compressSpaces(expected), compressSpaces(actual));
  }

  public static void assertSimilar(final String expected, final Document actual) {
    assertSimilar(expected, actual.get());
  }

  public static void assertNotEvenSimilar(final String expected, final String actual) {
    assertNotEquals(compressSpaces(expected), compressSpaces(actual));
  }

  private static String compressSpaces(final String s) {
    String $ = s//
        .replaceAll("(?m)^[ \t]*\r?\n", "") // Remove empty lines
        .replaceAll("[ \t]+", " ") // Squeeze whites
        .replaceAll("[ \t]+$", "") // Remove trailing spaces
        .replaceAll("^[ \t]+$", "") // No space at line beginnings
        ;
    for (final String operator : new String[] { ",", "\\+", "-", "\\*", "\\|", "\\&", "%", "\\(", "\\)", "^" })
      $ = $ //
          .replaceAll(WHITES + operator, operator) // Preceding whites
          .replaceAll(operator + WHITES, operator) // Trailing whites
          ;
    return $;
  }

  static int countOppportunities(final Spartanization s, final File f) {
    return countOppportunities(s, As.string(f));
  }

  protected static int countOppportunities(final Spartanization s, final String input) {
    return s.findOpportunities((CompilationUnit) As.COMPILIATION_UNIT.ast(input)).size();
  }

  static int countOpportunities(final Spartanization s, final CompilationUnit u) {
    return s.findOpportunities(u).size();
  }

  static void assertOneOpportunity(final Spartanization s, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertEquals(u.toString(), 1, countOpportunities(s, u));
  }

  static void assertNoOpportunity(final Spartanization s, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertEquals(u.toString(), 0, countOpportunities(s, u));
  }

  static Document rewrite(final Spartanization s, final CompilationUnit u, final Document d) {
    try {
      s.createRewrite(u, null).rewriteAST(d, null).apply(d);
      return d;
    } catch (final MalformedTreeException e) {
      fail(e.getMessage());
    } catch (final IllegalArgumentException e) {
      fail(e.getMessage());
    } catch (final BadLocationException e) {
      fail(e.getMessage());
    }
    return null;
  }
}
