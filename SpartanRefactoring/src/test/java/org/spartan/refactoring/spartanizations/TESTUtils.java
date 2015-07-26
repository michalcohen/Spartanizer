package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.utils.Utils.removePrefix;
import static org.spartan.utils.Utils.removeSuffix;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.spartan.refactoring.utils.As;

/**
 * @author Yossi Gil
 * @since 2015-07-17
 */
public enum TESTUtils {
  ;
  static Collection<Object[]> collect(final String[][] cases) {
    final Collection<Object[]> $ = new ArrayList<>(cases.length);
    for (final String[] t : cases)
      if (t != null)
        $.add(t);
    return $;
  }
  /**
   * Convert a given {@link String} into an {@link InfixExpression}, or fail the
   * current test, if such a conversion is not possible
   *
   * @param expression a {@link String} that represents a Java expression
   * @return an {@link InfixExpression} data structure representing the
   *         parameter.
   */
  public static InfixExpression i(final String expression) {
    return (InfixExpression) e(expression);
  }
  /**
   * Convert a given {@link String} into an {@link statement}, or fail the
   * current test, if such a conversion is not possible
   *
   * @param statement a {@link String} that represents a Java statement
   * @return an {@link Statement} data structure representing the parameter.
   */
  public static Statement s(final String statement) {
    return (Statement) As.STATEMENTS.ast(statement);
  }
  /**
   * Convert a given {@link String} into an {@link ConditionalExpression}, or
   * fail the current test, if such a conversion is not possible
   *
   * @param conditionalExpression a {@link String} that represents a
   *          "conditional" (also known as "ternary") expression.
   * @return an {@link Statement} data structure representing the parameter.
   */
  public static ConditionalExpression c(final String conditionalExpression) {
    final Expression $ = e(conditionalExpression);
    assertThat(conditionalExpression, $, notNullValue());
    assertThat(conditionalExpression, $, instanceOf(ConditionalExpression.class));
    return (ConditionalExpression) $;
  }
  /**
   * Convert a given {@link String} into an {@link PrefixExpression}, or fail
   * the current test, if such a conversion is not possible
   *
   * @param expression a {@link String} that represents a Java expression
   * @return an {@link PrefixExpression} data structure representing the
   *         parameter.
   */
  public static PrefixExpression p(final String expression) {
    return (PrefixExpression) e(expression);
  }
  /**
   * Convert a given {@link String} into an {@link Expression}, or fail the
   * current test, if such a conversion is not possible
   *
   * @param expression a {@link String} that represents a Java expression
   * @return an {@link Expression} data structure representing the parameter.
   */
  public static Expression e(final String expression) {
    return (Expression) As.EXPRESSION.ast(expression);
  }
  static final String WHITES = "\\s+";
  static void assertSimilar(final String expected, final String actual) {
    if (!expected.equals(actual))
      assertEquals(compressSpaces(expected), compressSpaces(actual));
  }
  static void assertSimilar(final String expected, final Document actual) {
    assertSimilar(expected, actual.get());
  }
  static void assertNotEvenSimilar(final String expected, final String actual) {
    assertNotEquals(compressSpaces(expected), compressSpaces(actual));
  }
  static String compressSpaces(final String s) {
    String $ = s//
        .replaceAll("(?m)^[ \t]*\r?\n", "") // Remove empty lines
        .replaceAll("[ \t]+", " ") // Squeeze whites
        .replaceAll("[ \t]+$", "") // Remove trailing spaces
        .replaceAll("^[ \t]+", "") // Remove preliminary spaces
        ;
    for (final String operator : new String[] { ",", ";", "\\+", ">", ">=", "!=", "==", "<", "<=", "-", "\\*", "\\|", "\\&", "%", "\\(", "\\)", "^" })
      $ = $ //
          .replaceAll(WHITES + operator, operator) // Preceding whites
          .replaceAll(operator + WHITES, operator) // Trailing whites
          ;
    return $;
  }
  static String apply(final Trimmer s, final String from) {
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
  static Document rewrite(final Spartanization s, final CompilationUnit u, final Document $) {
    try {
      s.createRewrite(u, null).rewriteAST($, null).apply($);
      return $;
    } catch (final MalformedTreeException e) {
      fail(e.getMessage());
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
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
    assertSimilar(input, peel(apply(new Trimmer(), wrap(input))));
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
    final String unpeeled = apply(new Trimmer(), wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = peel(unpeeled);
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
