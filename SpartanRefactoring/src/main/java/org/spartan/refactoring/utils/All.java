package org.spartan.refactoring.utils;

import java.util.ArrayList;
import java.util.List;

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
public enum All {
  ;
  /**
   * Obtains a list of operands.
   *
   * @param e
   *          JD
   * @return a list of all operands of the parameter, ordered from left to right
   */
  public static List<Expression> operands(final InfixExpression e) {
    final List<Expression> $ = new ArrayList<>();
    $.add(e.getLeftOperand());
    $.add(e.getRightOperand());
    if (e.hasExtendedOperands())
      $.addAll(e.extendedOperands());
    return $;
  }
}
