package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>int a = 2;
 * if (b)
 *   a = 3;</code> into <code>int a = b ? 3 : 2;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationIfAssginment extends Wring.ReplaceToNextStatement<VariableDeclarationFragment> {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final Statement nextStatement, final TextEditGroup g) {
    if (!Is.variableDeclarationStatement(f.getParent()))
      return null;
    final Expression initializer = f.getInitializer();
    if (initializer == null)
      return null;
    final IfStatement s = Extract.nextIfStatement(f);
    if (s == null || !Wrings.emptyElse(s))
      return null;
    s.setElseStatement(null);
    final Assignment a = Extract.assignment(then(s));
    if (a == null || !same(left(a), f.getName()) || a.getOperator() != Assignment.Operator.ASSIGN)
      return null;
    if (useForbiddenSiblings(f, s.getExpression(), right(a)))
      return null;
    r.replace(initializer, Subject.pair(right(a), initializer).toCondition(s.getExpression()), g);
    r.remove(s, g);
    return r;
  }
  @Override public String description(final VariableDeclarationFragment f) {
    return "Consolidate initialization of " + f.getName() + " with the subsequent conditional assignment to it";
  }
}