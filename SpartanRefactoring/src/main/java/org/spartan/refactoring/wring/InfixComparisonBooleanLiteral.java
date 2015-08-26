package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.spartan.refactoring.utils.Funcs.asBooleanLiteral;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.utils.Utils.in;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Plant;

/**
 * A {@link Wring} that eliminates redundant comparison with the two boolean
 * literals: <code><b>true</b></code> and <code><b>false</b></code>.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class InfixComparisonBooleanLiteral extends Wring.OfInfixExpression {
  private static boolean negating(final InfixExpression e, final BooleanLiteral literal) {
    return literal.booleanValue() != (e.getOperator() == EQUALS);
  }
  @Override public final boolean scopeIncludes(final InfixExpression e) {
    return !e.hasExtendedOperands() && in(e.getOperator(), EQUALS, NOT_EQUALS) && (Is.booleanLiteral(right(e)) || Is.booleanLiteral(left(e)));
  }
  @Override Expression _replacement(final InfixExpression e) {
    Expression nonliteral;
    BooleanLiteral literal;
    if (Is.booleanLiteral(left(e))) {
      literal = asBooleanLiteral(left(e));
      nonliteral = right(e);
    } else {
      literal = asBooleanLiteral(right(e));
      nonliteral = left(e);
    }
    nonliteral = Extract.core(nonliteral);
    final ASTNode parent = e.getParent();
    return new Plant(!negating(e, literal) ? nonliteral : not(nonliteral)).into(parent);
  }
}