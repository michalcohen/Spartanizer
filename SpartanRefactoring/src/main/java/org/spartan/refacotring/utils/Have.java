package org.spartan.refacotring.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

/**
 * A class for fluent programming.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 *
 */
public enum Have {
  ;
  public static boolean literal(final List<Expression> es) {
    for (final Expression e : es)
      if (Is.literal(e))
        return true;
    return false;
  }
  public static boolean numericalLiteral(final List<Expression> es) {
    for (final Expression e : es)
      if (Is.numericalLiteral(e))
        return true;
    return false;
  }
}
