package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Extract.core;
import static org.spartan.refactoring.utils.Funcs.*;

import static org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;

/**
 * A {@link Wring} to convert <code>a = 3; b = 3;</code> to
 * <code>a = b = 3</code>
 *
 * @author Yossi Gil
 * @since 2015-08-28
 */
public class AssignmentAndAssignment extends Wring.ReplaceToNextStatement<Assignment> {
  @Override ASTRewrite go(final ASTRewrite r, final Assignment a, final Statement nextStatement, final TextEditGroup g) {
    if (a.getOperator() != ASSIGN)
      return null;
    final Assignment a1 = Extract.assignment(nextStatement);
    if (a1 == null)
      return null;
    if (a1.getOperator() != ASSIGN)
      return null;
    if (!same(core(right(a)), core(right(a1))))
      return null;
    if (!Is.sideEffectFree(right(a)))
      return null;
    r.remove(Extract.statement(a), g);
    r.replace(right(a1), duplicate(a), g);
    return r;
  }
  @Override String description(final Assignment a) {
    return "Inline assignment to " + left(a) + " with its subsequent 'return'";
  }
}
