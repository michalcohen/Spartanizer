package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.flip;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.spartan.refactoring.utils.ExpressionComparator;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;
/**
 * A {@link Wring} that reorder comparisons so that the specific value is
 * placed on the right. Specific value means a literal, or any of the two
 * keywords <code><b>this</b></code> or <code><b>null</b></code>.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class InfixComparisonShortestFirst extends Wring.OfInfixExpression {

  @Override public boolean scopeIncludes(final InfixExpression e) {
    return !e.hasExtendedOperands() && Is.comparison(e) && ExpressionComparator.PRUDENT.compare(e.getLeftOperand(), e.getRightOperand()) > 0;
  }
  @Override Expression _replacement(final InfixExpression e) {
    return Subject.pair(e.getRightOperand(),e.getLeftOperand()).to(flip(e.getOperator()));
  }
}