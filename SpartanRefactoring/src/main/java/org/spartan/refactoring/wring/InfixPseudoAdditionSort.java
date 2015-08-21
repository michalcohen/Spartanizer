package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.OR;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.spartan.refactoring.utils.ExpressionComparator;
import org.spartan.refactoring.utils.Subject;
/**
 * A {@link Wring} that sorts the arguments of an expression using the same
 * sorting order as {@link Operator#PLUS} expression, except that we do not
 * worry about commutativity. Unlike {@link InfixAdditionSort}, we know that
 * the reordering is always possible.
 *
 * @see InfixAdditionSort
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class InfixPseudoAdditionSort extends Wring.OfInfixExpression {
  private static boolean sort(final InfixExpression e) {
    return sort(Wrings.allOperands(e));
  }
  private static boolean sort(final List<Expression> es) {
    return Wrings.sort(es, ExpressionComparator.ADDITION);
  }
  @Override boolean _eligible(final InfixExpression e) {
    return sort(e);
  }
  @Override Expression _replacement(final InfixExpression e) {
    final List<Expression> operands = Wrings.allOperands(e);
    return !sort(operands) ? null : Subject.operands(operands).to(e.getOperator());
  }
  @Override boolean scopeIncludes(final InfixExpression e) {
    return in(e.getOperator(), OR);
  }
}