package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;

import org.eclipse.jdt.core.dom.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to eliminate a ternary in which both branches are identical
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class TernaryEliminate extends Wring.Replacing<ConditionalExpression> {
  @Override Expression replacement(final ConditionalExpression e) {
    return new Plant(Extract.core(e.getThenExpression())).into(e.getParent());
  }
  @Override boolean scopeIncludes(final ConditionalExpression e) {
    return e != null && same(e.getThenExpression(), e.getElseExpression());
  }
  @Override String description(@SuppressWarnings("unused") final ConditionalExpression _) {
    return "Eliminate conditional exprssion with identical branches";
  }
}