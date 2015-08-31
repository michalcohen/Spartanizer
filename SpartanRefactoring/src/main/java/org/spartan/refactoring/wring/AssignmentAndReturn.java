package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Extract.core;
import static org.spartan.refactoring.utils.Funcs.asStatement;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.same;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>a = 3;return a;</code> to
 * <code>return a = 3;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-28
 */
public class AssignmentAndReturn extends Wring.ReplaceToNextStatement<Assignment> {
  @Override ASTRewrite go(final ASTRewrite r, final Assignment a, final Statement s1, final TextEditGroup g) {
    final Statement parent = asStatement(a.getParent());
    if (parent == null)
      return null;
    final ReturnStatement s = Extract.nextReturn(a);
    if (s == null || !same(left(a), core(s.getExpression())))
      return null;
    r.remove(parent, g);
    r.replace(s, Subject.operand(a).toReturn(), g);
    return r;
  }
  @Override String description(final Assignment n) {
    return "Consolidate assignment to " + left(n) + " with its subsequent 'return'";
  }
}
