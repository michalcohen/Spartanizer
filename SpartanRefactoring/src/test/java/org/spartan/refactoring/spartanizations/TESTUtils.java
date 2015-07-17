package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.utils.Utils.removePrefix;
import static org.spartan.utils.Utils.removeSuffix;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.spartan.refactoring.utils.As;

/**
 * @author Yossi Gil
 * @since 2015-07-17
 *
 */
public enum TESTUtils {
  ;
  public static InfixExpression i(final String expression) {
    return (InfixExpression) e(expression);
  }
  public static PrefixExpression p(final String expression) {
    return (PrefixExpression) e(expression);
  }
  public static Expression e(final String expression) {
    return (Expression) As.EXPRESSION.ast(expression);
  }

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
  public static String compressSpaces(final String s) {
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
  static String apply(final Wringer s, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(s, u, d).get();
  }

  private static final String PRE = //
  "package p; \n" + //
      "public class SpongeBob {\n" + //
      " public boolean squarePants() {\n" + //
      "   return ";
  private static final String POST = //
  "" + //
      ";\n" + //
      " }" + //
      "}" + //
      "";

  static final String peel(final String s) {
    return removeSuffix(removePrefix(s, PRE), POST);
  }
  static final String wrap(final String s) {
    return PRE + s + POST;
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
  static Expression asExpression(final String expression) {
    return (InfixExpression) As.EXPRESSION.ast(expression);
  }
  static void assertLegible(final Wring s, final String expression) {
    assertTrue(s.eligible((InfixExpression) asExpression(expression)));
  }
  static void assertNoChange(final String input) {
    assertSimilar(input, peel(apply(new Wringer(), wrap(input))));
  }
  static void assertNotLegible(final Wring s, final InfixExpression e) {
    assertFalse(s.eligible(e));
  }
  static void assertNotLegible(final Wring s, final String expression) {
    final InfixExpression e = (InfixExpression) asExpression(expression);
    assertNotLegible(s, e);
  }
  static void assertNotWithinScope(final Wring s, final InfixExpression e) {
    assertFalse(s.scopeIncludes(e));
  }
  static void assertNotWithinScope(final Wring s, final String expression) {
    final InfixExpression e = (InfixExpression) asExpression(expression);
    assertNotWithinScope(s, e);
  }
  static void assertSimplifiesTo(final String from, final String expected) {
    final String wrap = wrap(from);
    assertEquals(from, peel(wrap));
    final String unpeeled = apply(new Wringer(), wrap);
    final String peeled = peel(unpeeled);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + " is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }
  static void assertWithinScope(final Wring s, final InfixExpression e) {
    assertTrue(s.scopeIncludes(e));
  }
  static void assertWithinScope(final Wring s, final String expression) {
    final InfixExpression e = (InfixExpression) asExpression(expression);
    assertWithinScope(s, e);
  }
  static void asserWithinScope(final Wring s, final String expression) {
    final InfixExpression e = (InfixExpression) asExpression(expression);
    assertNotWithinScope(s, e);
  }
}
