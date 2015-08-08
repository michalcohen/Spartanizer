package org.spartan.refactoring.wring;
import static org.spartan.refactoring.utils.Funcs.asConditionalExpression;
import static org.spartan.refactoring.utils.Funcs.makeAND;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Restructure.getCore;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Subject;

final class CollapseTernary extends Wring.OfConditionalExpression {
  private static Expression collapse(final ConditionalExpression e) {
    if (e == null)
      return null;
    Expression $;
    return ($ = collapseOnElse(e)) != null || ($ = collaspeOnThen(e)) != null ? $ : null;
  }
  private static Expression collaspeOnThen(final ConditionalExpression e) {
    final ConditionalExpression then = asConditionalExpression(getCore(e.getThenExpression()));
    if (then == null)
      return null;
    final Expression elze = getCore(e.getElseExpression());
    final Expression thenThen = getCore(then.getThenExpression());
    final Expression thenElse = getCore(then.getElseExpression());
    if (same(thenElse, elze))
      return new Subject.Pair(thenThen, elze).toCondition(makeAND(e.getExpression(), then.getExpression()));
    if (same(thenThen, elze))
      return new Subject.Pair(thenElse, elze).toCondition(makeAND(e.getExpression(), not(then.getExpression())));
    return null;
  }
  private static Expression collapseOnElse(final ConditionalExpression e) {
    final ConditionalExpression elze = asConditionalExpression(getCore(e.getElseExpression()));
    if (elze == null)
      return null;
    final Expression then = getCore(e.getThenExpression());
    final Expression elseThen = getCore(elze.getThenExpression());
    final Expression elseElse = getCore(elze.getElseExpression());
    if (same(then, elseElse))
      return new Subject.Pair(elseThen, then).toCondition(makeAND(not(e.getExpression()), elze.getExpression()));
    if (same(then, elseThen))
      return new Subject.Pair(elseElse, then).toCondition(makeAND(not(e.getExpression()), not(elze.getExpression())));
    return null;
  }
  @Override boolean _eligible(@SuppressWarnings("unused") final ConditionalExpression _) {
    return true;
  }
  @Override Expression _replacement(final ConditionalExpression e) {
    return collapse(e);
  }
  @Override boolean scopeIncludes(final ConditionalExpression e) {
    return collapse(e) != null;
  }
}