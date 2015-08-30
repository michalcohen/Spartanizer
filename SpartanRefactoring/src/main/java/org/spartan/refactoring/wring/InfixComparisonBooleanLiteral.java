package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.spartan.refactoring.utils.Extract.core;
import static org.spartan.refactoring.utils.Funcs.asBooleanLiteral;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.utils.Utils.in;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Plant;

/**
 * A {@link Wring} that eliminates redundant comparison with the two boolean
 * literals: <code><b>true</b></code> and <code><b>false</b></code>.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class InfixComparisonBooleanLiteral extends Wring.Replacing<InfixExpression> {
  private static BooleanLiteral literal(final InfixExpression e) {
    return asBooleanLiteral(core(literalOnLeft(e) ? left(e) : right(e)));
  }
  private static boolean negating(final InfixExpression e, final BooleanLiteral literal) {
    return literal.booleanValue() != (e.getOperator() == EQUALS);
  }
  private static Expression nonLiteral(final InfixExpression e) {
    return literalOnLeft(e) ? right(e) : left(e);
  }
  private static boolean literalOnLeft(final InfixExpression e) {
    return Is.booleanLiteral(core(left(e)));
  }
  private static boolean literalOnRight(final InfixExpression e) {
    return Is.booleanLiteral(core(right(e)));
  }
  @Override public final boolean scopeIncludes(final InfixExpression e) {
    return !e.hasExtendedOperands() && in(e.getOperator(), EQUALS, NOT_EQUALS) && (literalOnLeft(e) || literalOnRight(e));
  }
  @Override String description(final InfixExpression n) {
    return "Eliminate redundant comparison with '" + literal(n) + "'";
  }
  @Override Expression replacement(final InfixExpression e) {
    final BooleanLiteral literal = literal(e);
    final Expression nonliteral = core(nonLiteral(e));
    final ASTNode parent = e.getParent();
    return new Plant(!negating(e, literal) ? nonliteral : not(nonliteral)).into(parent);
  }
}