package org.spartan.refactoring.utils;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

/**
 * *An empty <code><b>enum</b></code> for fluent programming. The name should
 * say it all: The name, followed by a dot, followed by a method name, should
 * read like a sentence phrase.
 *
 * Specifically, this class determines precedence and associativity of Java
 * operators; data is drawn from
 * {@link "http://introcs.cs.princeton.edu/java/11precedence/"}
 *
 * @author Yossi Gil
 * @since 2015-07-14
 *
 */
public enum Precedence {
  ;
  /**
   * *An empty <code><b>enum</b></code> for fluent programming. The name should
   * say it all: The name, followed by a dot, followed by a method name, should
   * read like a sentence phrase.
   *
   * @author Yossi Gil
   * @since 2015-07-14
   *
   */
  public enum Is {
    ;
    /**
     * determine whether an integer falls within the legal range of precedences.
     *
     * @param precedence
     *          JD
     *
     * @return <code><b>true</b></code> <i>iff</i> the parameter a legal
     *         precedence of Java.
     */
    public static boolean legal(final int precedence) {
      return precedence >= 1 && precedence <= 15;
    }
  }

  private final static int UNDEFINED = -1;
  private static final ChainStringToIntMap of = new ChainStringToIntMap()//
      .putOn(1, "[]", ".", "() invoke", "++ post", "-- post") //
      .putOn(2, "++ pre", "-- pre", "+ unary", "- unary", "!", "~") //
      .putOn(3, "() cast", "new") //
      .putOn(4, "*", "/", "%") // multiplicative
      .putOn(5, "+", "-") // additive
      .putOn(6, ">>", "<<", ">>>") // shift
      .putOn(7, "<", "<=", ">", ">=", "instanceof") // relational
      .putOn(8, "==", "!=") // equality
      .putOn(9, "&") // bitwise AND
      .putOn(10, "^") // bitwise XOR
      .putOn(11, "|") // bitwise OR
      .putOn(12, "&&") // conditional AND
      .putOn(13, "||") // conditional OR
      .putOn(14, "?", ":") // conditional
      .putOn(15, "=", // assignment
          "+=", "-=", // assignment, additive
          "*= ", "/=", "%=", // assignment, multiplicative
          "&=", "^=", "|=", // assignment, bitwise
          "<<=", ">>=", ">>>="// assignment, shift
  );

  private static int of(final Assignment a) {
    return of(a.getOperator());
  }
  /**
   * Determine the precedence of an
   * {@link org.eclipse.jdt.core.dom.Assignment.Operator}
   *
   * @param o
   *          JD
   * @return the precedence of the parameter
   */
  public static int of(final Assignment.Operator o) {
    return of(o.toString());
  }
  /**
   * Determine the precedence of the operator present on an {@link Expression}
   *
   * @param e
   *          JD
   * @return the precedence of the parameter
   */
  public static int of(final Expression e) {
    if (e instanceof InfixExpression)
      return of((InfixExpression) e);
    if (e instanceof Assignment)
      return of((Assignment) e);
    return UNDEFINED;
  }
  private static int of(final InfixExpression e) {
    return of(e.getOperator());
  }
  /**
   * Determine the precedence of an
   * {@link org.eclipse.jdt.core.dom.InfixExpression.Operator}
   *
   * @param o
   *          JD
   * @return the precedence of the parameter
   */
  public static int of(final InfixExpression.Operator o) {
    return of(o.toString());
  }
  private static int of(final String key) {
    return of.containsKey(key) ? of.get(key) : UNDEFINED;
  }
  /**
   * Determine the precedence of two expressions is the same.
   *
   * @param e1
   *          JD
   * @param e2
   *          JD
   * @return the precedence of the parameter
   */
  public static boolean same(final Expression e1, final Expression e2) {
    assert Precedence.of(e1) != UNDEFINED;
    assert Precedence.of(e2) != UNDEFINED;
    return Precedence.of(e1) == Precedence.of(e2);
  }
  /**
   * Determine whether an expression has the same precedence as that of a given
   * operator.
   *
   * @param o
   *          JD
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the precedence of the two
   *         parameters is the same.
   */
  public static boolean same(final Operator o, final Expression e) {
    assert Precedence.of(o) != UNDEFINED;
    assert Precedence.of(e) != UNDEFINED;
    return Precedence.of(o) == Precedence.of(e);
  }
}
