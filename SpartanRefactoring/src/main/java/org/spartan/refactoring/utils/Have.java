package org.spartan.refactoring.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

/**
 * A an empty <code><b>enum</b></code> for fluent programming. The name says it
 * all: The name, followed by a dot, followed by a method name, should read like
 * a word phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 *
 */
public enum Have {
  ;
  /**
   * Determine whether a literal is present
   *
   * @param es
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> one or more of the elements
   *         that is a literal.
   */
  public static boolean literal(final List<Expression> es) {
    for (final Expression e : es)
      if (Is.literal(e))
        return true;
    return false;
  }
  /**
   * Determine whether a literal is present
   *
   * @param es
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> one or more of the elements
   *         that is a literal.
   */
  public static boolean literal(final Expression... es) {
    for (final Expression e : es)
      if (Is.literal(e))
        return true;
    return false;
  }
  /**
   * Determine whether a numerical literal is present
   *
   * @param es
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> one or more of the elements
   *         that is a numeric literal.
   */
  public static boolean numericLiteral(final List<Expression> es) {
    for (final Expression e : es)
      if (Is.numericLiteral(e))
        return true;
    return false;
  }
  /**
   * Determine whether a numerical literal is present
   *
   * @param es
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> one or more of the elements
   *         that is a numeric literal.
   */
  public static boolean numericLiteral(final Expression... es) {
    for (final Expression e : es)
      if (Is.numericLiteral(e))
        return true;
    return false;
  }
  /**
   * Determine whether a boolean literal is present
   *
   * @param es
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> one or more of the elements
   *         that is a boolean literal.
   */
  public static boolean booleanLiteral(final List<Expression> es) {
    for (final Expression e : es)
      if (Is.booleanLiteral(e))
        return true;
    return false;
  }
}
