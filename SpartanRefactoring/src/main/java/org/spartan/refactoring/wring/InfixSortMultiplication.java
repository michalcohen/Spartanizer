package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.TIMES;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.spartan.refactoring.utils.ExpressionComparator;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} that sorts the arguments of a {@link Operator#PLUS}
 * expression. Extra care is taken to leave intact the use of
 * {@link Operator#PLUS} for the concatenation of {@link String}s.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class InfixSortMultiplication extends Wring.Replacing<InfixExpression> {
  private static boolean sort(final InfixExpression e) {
    return sort(Extract.allOperands(e));
  }
  private static boolean sort(final List<Expression> es) {
    return Wrings.sort(es, ExpressionComparator.MULTIPLICATION);
  }
  @Override boolean eligible(final InfixExpression e) {
    return sort(e);
  }
  @Override Expression replacement(final InfixExpression e) {
    final List<Expression> operands = Extract.allOperands(e);
    return !sort(operands) ? null : Subject.operands(operands).to(e.getOperator());
  }
  @Override boolean scopeIncludes(final InfixExpression e) {
    return in(e.getOperator(), TIMES);
  }
  @Override String description(final InfixExpression e) {
    return "Reorder operands of " + e.getOperator();
  }
}