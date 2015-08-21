package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asIfStatement;
import static org.spartan.refactoring.utils.Funcs.makeThrowStatement;

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
 *   throw b;
 * else
 *   throw c;
 * </pre>
 *
 * into
 *
 * <pre>
 * throw x? b : c
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfThrowFooElseThrowBar extends Wring.OfIfStatement {
  @Override Statement _replacement(final IfStatement i) {
    final Expression condition = i.getExpression();
    final Expression then = Extract.throwExpression(i.getThenStatement());
    final Expression elze = Extract.throwExpression(i.getElseStatement());
    return then == null || elze == null ? null : makeThrowStatement(Subject.pair(then, elze).toCondition(condition));
  }
  @Override boolean scopeIncludes(final IfStatement e) {
    final IfStatement i = asIfStatement(e);
    return i != null && Extract.throwExpression(i.getThenStatement()) != null && Extract.throwExpression(i.getElseStatement()) != null;
  }
}