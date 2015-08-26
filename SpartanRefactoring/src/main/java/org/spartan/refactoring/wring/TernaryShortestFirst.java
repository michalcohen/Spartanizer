package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.not;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>a ? (f,g,h) : c(d,e) </code> into
 * <code> a ? c(d,e) : f(g,h) </code>
 *
 * @author Yossi Gil
 * @since 2015-08-14
 */
public final class TernaryShortestFirst extends Wring.OfConditionalExpression {
  @Override ConditionalExpression _replacement(final ConditionalExpression e) {
    final ConditionalExpression $ = Subject.pair(e.getElseExpression(), e.getThenExpression()).toCondition(not(e.getExpression()));
    final Expression then = $.getElseExpression();
    final Expression elze = $.getThenExpression();
    if (!Is.conditional(then) && Is.conditional(elze))
      return null;
    if (Is.conditional(then) && !Is.conditional(elze))
      return $;
    final Expression condition = not($.getExpression());
    return Wrings.length(condition, then) > Wrings.length(not(condition), elze) ? $ : null;
  }
}