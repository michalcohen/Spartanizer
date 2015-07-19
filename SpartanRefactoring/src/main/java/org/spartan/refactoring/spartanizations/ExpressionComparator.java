package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.countNodes;
import static org.spartan.refactoring.utils.Funcs.countNonWhites;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Is;

/**
 * Various methods for comparing
 *
 * @author Yossi Gil
 * @since 2015-07-19
 *
 */
public enum ExpressionComparator implements Comparator<Expression> {
  /**
   * Order on terms in addition: literals must be last. Sort literals by length.
   *
   *
   * @author Yossi Gil
   * @since 2015-07-19
   *
   */
  ADDITION {
    @Override public int compare(final Expression e1, final Expression e2) {
      int $;
      return ($ = literalCompare(e1, e2)) != 0 || //
          ($ = nodesCompare(e1, e2)) != 0 || //
          ($ = characterCompare(e1, e2)) != 0 //
              ? $ : 0;
    }
  },
  /**
   * Order on terms in multiplication: literals must be last. Sort literals by
   * length.
   *
   *
   * @author Yossi Gil
   * @since 2015-07-19
   *
   */
  MULTIPLICATION {
    @Override public int compare(final Expression e1, final Expression e2) {
      int $;
      return ($ = literalCompare(e2, e1)) != 0 || //
          ($ = nodesCompare(e1, e2)) != 0 || //
          ($ = characterCompare(e1, e2)) != 0 //
              ? $ : 0;
    }
  };
  static int literalCompare(final Expression e1, final Expression e2) {
    return asBit(Is.literal(e1)) - asBit(Is.literal(e2));
  }
  static int nodesCompare(final Expression e1, final Expression e2) {
    return round(countNodes(e1) - countNodes(e2), Wrings.TOKEN_THRESHOLD);
  }
  static int characterCompare(final Expression e1, final Expression e2) {
    return countNonWhites(e1) - countNonWhites(e2);
  }
  static int round(final int $, final int threshold) {
    return Math.abs($) > threshold ? $ : 0;
  }
  static int asBit(final boolean b) {
    return b ? 1 : 0;
  }
}