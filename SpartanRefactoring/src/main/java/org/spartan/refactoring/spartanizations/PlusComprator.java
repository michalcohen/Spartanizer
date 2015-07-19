package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.countNodes;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Utils;

/**
 * @author Yossi Gil
 * @since 2015-07-19
 *
 */
public final class PlusComprator implements Comparator<Expression> {
  /** Instantiates the enclosing class ({@link PlusComprator}) */
  private PlusComprator() {
    // Empty constructor
  }
  @Override public int compare(final Expression e1, final Expression e2) {
    if (Is.numericLiteral(e1) || Is.numericLiteral(e2))
      return Utils.compare(Is.numericLiteral(e1), Is.numericLiteral(e2));
    if (Wrings.moreArguments(e1, e2))
      return 1;
    if (Wrings.moreArguments(e2, e1))
      return -1;
    final int $ = countNodes(e1) - countNodes(e2);
    return Math.abs($) > Wrings.TOKEN_THRESHOLD ? $ : 0;
  }

  /** The single instance of this class */
  public static final PlusComprator INSTANCE = new PlusComprator();
}