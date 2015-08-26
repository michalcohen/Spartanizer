package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.makeThrowStatement;
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
  @Override Statement _replacement(final IfStatement s) {
    final Expression condition = s.getExpression();
    final Expression then = Extract.throwExpression(then(s));
    final Expression elze = Extract.throwExpression(elze(s));
    return then == null || elze == null ? null : makeThrowStatement(Subject.pair(then, elze).toCondition(condition));
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && Extract.throwExpression(then(s)) != null && Extract.throwExpression(elze(s)) != null;
  }
}