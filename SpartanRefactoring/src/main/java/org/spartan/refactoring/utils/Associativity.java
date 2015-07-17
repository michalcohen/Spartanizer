package org.spartan.refactoring.utils;

import static org.spartan.utils.Utils.intIsIn;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

/**
 * A an empty <code><b>enum</b></code> for fluent programming. The name says it
 * all: The name, followed by a dot, followed by a method name, should read like
 * a word phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 *
 */
public enum Associativity {
  ;
  static boolean isR2L(final Expression e) {
    return isR2L(Precedence.of(e));
  }
  static boolean isL2R(final Expression e) {
    return !isR2L(Precedence.of(e));
  }
  private static boolean isR2L(final int precedence) {
    assert Precedence.Is.legal(precedence);
    return intIsIn(precedence, 2, 3, 14, 15);
  }
  public static boolean isL2R(final InfixExpression.Operator o) {
    return isR2L(Precedence.of(o));
  }
}
