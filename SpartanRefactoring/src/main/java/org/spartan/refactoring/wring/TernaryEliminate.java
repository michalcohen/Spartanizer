package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.same;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Plant;

/**
 * A {@link Wring} to eliminate a ternary in which both branches are identical
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class TernaryEliminate extends Wring.OfConditionalExpression {
  @Override Expression _replacement(final ConditionalExpression e) {
    return new Plant(Extract.core(e.getThenExpression())).into(e.getParent());
  }
  @Override boolean scopeIncludes(final ConditionalExpression e) {
    return e != null && same(e.getThenExpression(), e.getElseExpression());
  }
}