package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.countNodes;

import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Utils;

/**
 * Various methods for comparing
 *
 * @author Yossi Gil
 * @since 2015-07-19
 *
 */
public enum Comparator implements java.util.Comparator<Expression> {
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
      if (Is.numericLiteral(e1) != Is.numericLiteral(e2))
        return Utils.compare(Is.numericLiteral(e1), Is.numericLiteral(e2));
      if (Wrings.moreArguments(e1, e2))
        return 1;
      if (Wrings.moreArguments(e2, e1))
        return -1;
      final int $ = countNodes(e1) - countNodes(e2);
      return Math.abs($) > Wrings.TOKEN_THRESHOLD ? $ : 0;
    }
  };
}