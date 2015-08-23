package org.spartan.refactoring.wring;
import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.then;
import static org.spartan.refactoring.wring.TernaryPushdown.pushdown;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert
 *
 * <pre>
 * if (x)
 *   f(a);
 * else
 *   f(b);
 * </pre>
 *
 * into
 *
 * <pre>
 * f(x ? a: b);
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfExpressionStatementElseSimilarExpressionStatement extends Wring.OfIfStatement {
  @Override Statement _replacement(final IfStatement s) {
    final Expression then = Extract.expression(Extract.expressionStatement(then(s)));
    if (then == null)
      return null;
    final Expression elze = Extract.expression(Extract.expressionStatement(elze(s)));
    if (elze == null)
      return null;
    final Expression e = pushdown(Subject.pair(then, elze).toCondition(s.getExpression()));
    if (e == null)
      return null;
    return Subject.operand(e).toStatement();
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return _replacement(s) != null;
  }
}