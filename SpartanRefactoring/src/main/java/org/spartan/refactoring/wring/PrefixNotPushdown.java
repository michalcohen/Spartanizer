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
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} that pushes down "<code>!</code>", the negation operator as
 * much as possible, using the de-Morgan and other simplification rules.
 *
 * @author Yossi Gil
 * @since 2015-7-17
 */
public final class PrefixNotPushdown extends Wring.OfPrefixExpression {
  /**
   * @param o JD
   * @return the operator that produces the logical negation of the parameter
   */
  public static Operator negate(final Operator o) {
    return o == null ? null
        : o.equals(EQUALS) ? NOT_EQUALS
            : o.equals(NOT_EQUALS) ? EQUALS
                : o.equals(LESS_EQUALS) ? GREATER : o.equals(GREATER) //
                    ? LESS_EQUALS : o.equals(GREATER_EQUALS) ? LESS : //
                      !o.equals(LESS) ? null : GREATER_EQUALS;
  }
  public static Expression simplifyNot(final PrefixExpression e) {
    return pushdownNot(asNot(Extract.core(e)));
  }
  private static Expression tryToSimplify(final Expression e) {
    final Expression $ = pushdownNot(asNot(e));
    return $ != null ? $ : e;
  }
  static Expression applyDeMorgan(final InfixExpression inner) {
    final List<Expression> operands = new ArrayList<>();
    for (final Expression e : All.operands(flatten(inner)))
      operands.add(not(e));
    return Subject.operands(operands).to(conjugate(inner.getOperator()));
  }
  static Expression comparison(final InfixExpression e) {
    return Subject.pair(e.getLeftOperand(), e.getRightOperand()).to(negate(e.getOperator()));
  }
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
  static Expression perhapsDoubleNegation(final Expression e) {
    return perhapsDoubleNegation(asNot(e));
  }
  static Expression perhapsDoubleNegation(final PrefixExpression e) {
    return e == null ? null : tryToSimplify(core(e.getOperand()));
  }
  static Expression perhapsNotOfLiteral(final Expression e) {
    return !Is.booleanLiteral(e) ? null : notOfLiteral(asBooleanLiteral(e));
  }
  static Expression pushdownNot(final Expression e) {
    Expression $;
    return ($ = perhapsNotOfLiteral(e)) != null//
        || ($ = perhapsDoubleNegation(e)) != null//
        || ($ = perhapsDeMorgan(e)) != null//
        || ($ = perhapsComparison(e)) != null //
        ? $ : null;
  }
  static Expression pushdownNot(final PrefixExpression e) {
    return e == null ? null : pushdownNot(core(e.getOperand()));
  }
  @Override public boolean scopeIncludes(final PrefixExpression e) {
    return e != null && asNot(e) != null && Wrings.hasOpportunity(asNot(e));
  }
  @Override Expression _replacement(final PrefixExpression e) {
    return simplifyNot(e);
  }
}
