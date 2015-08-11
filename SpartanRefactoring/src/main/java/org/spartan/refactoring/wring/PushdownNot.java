package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.spartan.refactoring.utils.Extract.core;
import static org.spartan.refactoring.utils.Funcs.asAndOrOr;
import static org.spartan.refactoring.utils.Funcs.asBooleanLiteral;
import static org.spartan.refactoring.utils.Funcs.asComparison;
import static org.spartan.refactoring.utils.Funcs.asNot;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Restructure.conjugate;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;

public final class PushdownNot extends Wring.OfPrefixExpression {
  static Expression notOfLiteral(final BooleanLiteral l) {
    final BooleanLiteral $ = duplicate(l);
    $.setBooleanValue(!l.booleanValue());
    return $;
  }
  static Expression perhapsComparison(final Expression inner) {
    return perhapsComparison(asComparison(inner));
  }
  static Expression perhapsComparison(final InfixExpression inner) {
    return inner == null ? null : comparison(inner);
  }
  static Expression perhapsDeMorgan(final Expression e) {
    return perhapsDeMorgan(asAndOrOr(e));
  }
  static Expression perhapsDeMorgan(final InfixExpression e) {
    return e == null ? null : applyDeMorgan(e);
  }
  static Expression applyDeMorgan(final InfixExpression inner) {
    final List<Expression> operands = new ArrayList<>();
    for (final Expression e : All.operands(flatten(inner)))
      operands.add(not(e));
    return Subject.operands(operands).to(conjugate(inner.getOperator()));
  }
  static Expression perhapsDoubleNegation(final Expression inner) {
    return perhapsDoubleNegation(asNot(inner));
  }
  static Expression perhapsDoubleNegation(final PrefixExpression inner) {
    return inner == null ? null : inner.getOperand();
  }
  static Expression perhapsNotOfLiteral(final Expression inner) {
    return !Is.booleanLiteral(inner) ? null : notOfLiteral(asBooleanLiteral(inner));
  }
  static Expression pushdownNot(final Expression inner) {
    Expression $;
    return ($ = perhapsNotOfLiteral(inner)) != null//
        || ($ = perhapsDoubleNegation(inner)) != null//
        || ($ = perhapsDeMorgan(inner)) != null//
        || ($ = perhapsComparison(inner)) != null //
            ? $ : null;
  }
  static Expression comparison(final InfixExpression inner) {
    return Subject.pair(inner.getLeftOperand(), inner.getRightOperand()).to(negate(inner.getOperator()));
  }
  static Expression pushdownNot(final PrefixExpression e) {
    return e == null ? null : pushdownNot(core(e.getOperand()));
  }
  static Expression getCoreLeft(final InfixExpression e) {
    return core(e.getLeftOperand());
  }
  /**
   * @param o JD
   * @return the operator that produces the logical negation of the parameter
   */
  public static Operator negate(final Operator o) {
    if (o == null)
      return null;
    if (o.equals(EQUALS))
      return NOT_EQUALS;
    if (o.equals(NOT_EQUALS))
      return EQUALS;
    if (o.equals(LESS_EQUALS))
      return GREATER;
    if (o.equals(GREATER))
      return LESS_EQUALS;
    if (o.equals(GREATER_EQUALS))
      return LESS;
    if (o.equals(LESS))
      return GREATER_EQUALS;
    return null;
  }
  static Expression getCoreRight(final InfixExpression e) {
    return core(e.getRightOperand());
  }
  @Override public boolean scopeIncludes(final PrefixExpression e) {
    return e != null && asNot(e) != null && Wrings.hasOpportunity(asNot(e));
  }
  @Override public String toString() {
    return "Pushdown not";
  }
  @Override boolean _eligible(@SuppressWarnings("unused") final PrefixExpression _) {
    return true;
  }
  @Override Expression _replacement(final PrefixExpression e) {
    return simplifyNot(e);
  }
  public static Expression simplifyNot(final PrefixExpression e) {
    return pushdownNot(asNot(e));
  }
}
