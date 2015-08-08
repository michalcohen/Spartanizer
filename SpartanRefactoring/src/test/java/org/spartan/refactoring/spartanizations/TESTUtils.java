package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.wring.TrimmerTest.countOpportunities;
import static org.spartan.utils.Utils.removePrefix;
import static org.spartan.utils.Utils.removeSuffix;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.Document;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.wring.Trimmer;

/**
 * @author Yossi Gil
 * @since 2015-07-17
 */
public enum TESTUtils {
  ;
  static final String WHITES = "(?m)\\s+";
  private static final String PRE_STATEMENT = //
  "package p;public class SpongeBob {\n" + //
      "public boolean squarePants(){\n" + //
      "";
  private static final String POST_STATEMENT = //
  "" + //
      "} // END OF METHO\n" + //
      "} // END OF PACKAGE\n" + //
      "";
  private static final String PRE_EXPRESSION = PRE_STATEMENT + "   return ";
  private static final String POST_EXPRESSION = ";\n" + POST_STATEMENT;
  public static void assertNoChange(final String input) {
    assertSimilar(input, peelExpression(apply(new Trimmer(), wrapExpression(input))));
  }
  /**
   * A test to check that that the actual output is similar to the actual value.
   *
   * @param expected JD
   * @param actual JD
   */
  public static void assertSimilar(final String expected, final Document actual) {
    assertSimilar(expected, actual.get());
  }
  public static void assertSimilar(final String expected, final String actual) {
    if (!expected.equals(actual))
      assertEquals(compressSpaces(expected), compressSpaces(actual));
  }
  /**
   * Convert a given {@link String} into an {@link Statement}, or fail the
   * current test, if such a conversion is not possible
   *
   * @param statement a {@link String} that represents a Java statement
   * @return an {@link Statement} data structure representing the parameter.
   */
  public static Statement asSingle(final String statement) {
    assertThat(statement, notNullValue());
    final ASTNode n = As.STATEMENTS.ast(statement);
    assertThat(n, notNullValue());
    return Extract.singleStatement(n);
  }
  public static String compressSpaces(final String s) {
    String $ = s//
        .replaceAll("(?m)\\s+", " ") // Squeeze whites
        .replaceAll("^\\s", "") // Opening whites
        .replaceAll("\\s$", "") // Closing whites
        ;
    for (final String operator : new String[] { ":", ",", "\\{", "\\}", "=", ":", "\\?", ";", "\\+", ">", ">=", "!=", "==", "<", "<=", "-", "\\*", "\\|", "\\&", "%", "\\(", "\\)",
        "[\\^]" })
      $ = $ //
          .replaceAll(WHITES + operator, operator) // Preceding whites
          .replaceAll(operator + WHITES, operator) // Trailing whites
          ;
    return $;
  }
  public static final String peelExpression(final String s) {
    return removeSuffix(removePrefix(s, PRE_EXPRESSION), POST_EXPRESSION);
  }
  public static final String peelStatement(final String s) {
    return removeSuffix(removePrefix(s, PRE_STATEMENT), POST_STATEMENT);
  }
  public static Document rewrite(final Spartanization s, final CompilationUnit u, final Document $) {
    try {
      s.createRewrite(u, null).rewriteAST($, null).apply($);
      return $;
    } catch (final Exception e) {
      fail(e.getMessage());
    }
    return null;
  }
  public static final String wrapExpression(final String s) {
    return PRE_EXPRESSION + s + POST_EXPRESSION;
  }
  public static final String wrapStatement(final String s) {
    return PRE_STATEMENT + s + POST_STATEMENT;
  }
  static String apply(final Trimmer t, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(t, u, d).get();
  }
  static void assertNoOpportunity(final Spartanization s, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertEquals(u.toString(), 0, countOpportunities(s, u));
  }
  static void assertNotEvenSimilar(final String expected, final String actual) {
    assertNotEquals(compressSpaces(expected), compressSpaces(actual));
  }
  static void assertOneOpportunity(final Spartanization s, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertEquals(u.toString(), 1, countOpportunities(s, u));
  }
}
