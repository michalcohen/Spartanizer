package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a sentence phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 */
public enum Into {
  ;
  static final String WHITES = "(?m)\\s+";
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
   * Remove all non-essential spaces from a string that represents Java code.
   *
   * @param javaCode JD
   * @return the parameter, with all redundant spaces removes from it
   */
  public static String compressSpaces(final String javaCode) {
    String $ = javaCode//
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
   * Convert a given {@link String} into an {@link PrefixExpression}, or fail
   * the current test, if such a conversion is not possible
   *
   * @param expression a {@link String} that represents a Java expression
   * @return a {@link PrefixExpression} data structure representing the
   *         parameter.
   */
  public static PrefixExpression p(final String expression) {
    return (PrefixExpression) e(expression);
  }
  /**
   * Convert a given {@link String} into an {@link Statement}, or fail the
   * current test, if such a conversion is not possible
   *
   * @param statement a {@link String} that represents a Java statement
   * @return an {@link Statement} data structure representing the parameter.
   */
  public static Statement s(final String statement) {
    assertThat(statement, notNullValue());
    final ASTNode n = As.STATEMENTS.ast(statement);
    assertThat(statement, n, notNullValue());
    assertThat(statement, n, instanceOf(Statement.class));
    return (Statement) n;
  }
  /**
   * Convert a given {@link String} into an {@link Assignment}, or fail the
   * current test, if such a conversion is not possible
   *
   * @param expression a {@link String} that represents a Java statement
   * @return an {@link Statement} data structure representing the parameter.
   */
  public static Assignment a(final String expression) {
    return (Assignment) e(expression);
  }
}
