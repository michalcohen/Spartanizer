package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asIfStatement;

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
 *   return b;
 * else
 *   return c;
 * </pre>
 *
 * into
 *
 * <pre>
 * return  x? b : c
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfReturnFooElseReturnBar extends Wring.OfIfStatement {
  @Override Statement _replacement(final IfStatement i) {
    final Expression condition = i.getExpression();
    final Expression then = Extract.returnExpression(i.getThenStatement());
    final Expression elze = Extract.returnExpression(i.getElseStatement());
    return then == null || elze == null ? null : Subject.operand(Subject.pair(then, elze).toCondition(condition)).toReturn();
  }
  @Override boolean scopeIncludes(final IfStatement e) {
    final IfStatement i = asIfStatement(e);
    return i != null && Extract.returnExpression(i.getThenStatement()) != null && Extract.returnExpression(i.getElseStatement()) != null;
  }
}