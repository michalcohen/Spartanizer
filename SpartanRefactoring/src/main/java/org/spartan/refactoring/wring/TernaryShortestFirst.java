package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;

import org.eclipse.jdt.core.dom.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>a ? (f,g,h) : c(d,e)</code> into
 * <code>a ? c(d,e) : f(g,h)</code>
 *
 * @author Yossi Gil
 * @since 2015-08-14
 */
public final class TernaryShortestFirst extends Wring.Replacing<ConditionalExpression> {
  @Override ConditionalExpression replacement(final ConditionalExpression e) {
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
  @Override String description(@SuppressWarnings("unused") final ConditionalExpression _) {
    return "Invert logical condition and exhange order of '?' and ':' operands to conditional expression";
  }
}