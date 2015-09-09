package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asConditionalExpression;
import static org.spartan.refactoring.utils.Extract.*;
import static org.spartan.refactoring.utils.Funcs.logicalNot;

import org.eclipse.jdt.core.dom.*;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>a ? (f,g,h) : c(d,e)</code> into
 * <code>a ? c(d,e) : f(g,h)</code>
 *
 * @author Yossi Gil
 * @since 2015-08-14
 */
public final class TernaryShortestFirst extends Wring.ReplaceCurrentNode<ConditionalExpression> {
  @Override ConditionalExpression replacement(final ConditionalExpression e) {
    final ConditionalExpression $ = Subject.pair(core(e.getElseExpression()), core(e.getThenExpression())).toCondition(logicalNot(e.getExpression()));
    final Expression then = $.getElseExpression();
    final Expression elze = $.getThenExpression();
    if (!Is.conditional(then) && Is.conditional(elze))
      return null;
    if (Is.conditional(then) && !Is.conditional(elze))
      return $;
    final ConditionalExpression parent = asConditionalExpression(e.getParent());
    if (parent != null && parent.getElseExpression() == e && compatibleCondition(parent.getExpression(), e.getExpression())) {
      final Expression alignTo = parent.getThenExpression();
      final int a1 = align(elze, alignTo);
      final int a2 = align(then, alignTo);
      if (a1 != a2)
        return a1 > a2 ? $ : null;
    }
    final Expression condition = logicalNot($.getExpression());
    return Wrings.length(condition, then) > Wrings.length(logicalNot(condition), elze) ? $ : null;
  }
  private static int align(final Expression e1, final Expression e2) {
    return e1.getNodeType() == e2.getNodeType() ? 1 : 0;
  }
  private static boolean compatibleCondition(final Expression e1, final Expression e2) {
    return compatible(e1, e2) || compatible(e1, logicalNot(e2));
  }
  private static boolean compatible(final Expression e1, final Expression e2) {
    return e1.getNodeType() == e2.getNodeType() && (e1 instanceof InstanceofExpression || e1 instanceof InfixExpression || e1 instanceof MethodInvocation);
  }
  @Override String description(@SuppressWarnings("unused") final ConditionalExpression _) {
    return "Invert logical condition and exhange order of '?' and ':' operands to conditional expression";
  }
}