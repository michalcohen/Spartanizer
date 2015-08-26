package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.then;

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
  @Override Statement _replacement(final IfStatement s) {
    final Expression condition = s.getExpression();
    final Expression then = Extract.returnExpression(then(s));
    final Expression elze = Extract.returnExpression(elze(s));
    return then == null || elze == null ? null : Subject.operand(Subject.pair(then, elze).toCondition(condition)).toReturn();
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && Extract.returnExpression(then(s)) != null && Extract.returnExpression(elze(s)) != null;
  }
}