package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.asIfStatement;
import static org.spartan.refactoring.utils.Funcs.compatible;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert
 *
 * <pre>
 * if (x)
 *   a += 3;
 * else
 *   a += 9;
 * </pre>
 *
 * into
 *
 * <pre>
 * a += x ? 3 : 9;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfAssignToFooElseAssignToFoo extends Wring.OfIfStatement {
  @Override Statement _replacement(final IfStatement s) {
    asBlock(s);
    final IfStatement i = asIfStatement(s);
    if (i == null)
      return null;
    final Assignment then = Extract.assignment(i.getThenStatement());
    final Assignment elze = Extract.assignment(i.getElseStatement());
    if (!compatible(then, elze))
      return null;
    final ConditionalExpression e = Subject.pair(then.getRightHandSide(), elze.getRightHandSide()).toCondition(i.getExpression());
    return Subject.pair(then.getLeftHandSide(), e).toStatement(then.getOperator());
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && compatible(Extract.assignment(s.getThenStatement()), Extract.assignment(s.getElseStatement()));
  }
}