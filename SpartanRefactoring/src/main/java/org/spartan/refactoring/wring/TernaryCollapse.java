package org.spartan.refactoring.wring;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.spartan.refactoring.utils.Extract.core;
import static org.spartan.refactoring.utils.Funcs.asConditionalExpression;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Funcs.same;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Subject;

final public class TernaryCollapse extends Wring.OfConditionalExpression {
  private static Expression collapse(final ConditionalExpression e) {
    if (e == null)
      return null;
    Expression $;
    return ($ = collapseOnElse(e)) != null || ($ = collaspeOnThen(e)) != null ? $ : null;
  }
  private static Expression collapseOnElse(final ConditionalExpression e) {
    final ConditionalExpression elze = asConditionalExpression(core(e.getElseExpression()));
    if (elze == null)
      return null;
    final Expression then = core(e.getThenExpression());
    final Expression elseThen = core(elze.getThenExpression());
    final Expression elseElse = core(elze.getElseExpression());
    if (!same(then, elseElse) && !same(then, elseThen))
      return null;
    return same(then, elseElse) ? Subject.pair(elseThen, then).toCondition(Subject.pair(not(e.getExpression()), elze.getExpression()).to(CONDITIONAL_AND))
        : Subject.pair(elseElse, then).toCondition(Subject.pair(not(e.getExpression()), not(elze.getExpression())).to(CONDITIONAL_AND));
  }
  private static Expression collaspeOnThen(final ConditionalExpression e) {
    final ConditionalExpression then = asConditionalExpression(core(e.getThenExpression()));
    if (then == null)
      return null;
    final Expression elze = core(e.getElseExpression());
    final Expression thenThen = core(then.getThenExpression());
    final Expression thenElse = core(then.getElseExpression());
    return same(thenElse, elze) ? Subject.pair(thenThen, elze).toCondition(Subject.pair(e.getExpression(), then.getExpression()).to(CONDITIONAL_AND))
        : !same(thenThen, elze) ? null : Subject.pair(thenElse, elze).toCondition(Subject.pair(e.getExpression(), not(then.getExpression())).to(CONDITIONAL_AND));
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