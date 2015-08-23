package org.spartan.refactoring.wring;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Funcs.then;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Occurrences;
import org.spartan.refactoring.utils.Subject;
/**
 * A {@link Wring} to convert
 *
 * <pre>
 * int a = 2; if (b) a = 3;
 * </pre>
 *
 * into
 *
 * <pre>
 * int a =  b ? 3: 2;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationIfAssginment extends Wring.OfVariableDeclarationFragmentAndSurrounding {
  @Override ASTRewrite fillReplacement(final VariableDeclarationFragment f, final ASTRewrite r) {
    final Expression initializer = f.getInitializer();
    if (initializer == null)
      return null;
    final IfStatement s = Extract.nextIfStatement(f);
    if (s == null || !Wrings.elseIsEmpty(s))
      return null;
    final Assignment a = Extract.assignment(then(s));
    if (a == null || !same(a.getLeftHandSide(), f.getName()))
      return null;
    if (a.getOperator() != Assignment.Operator.ASSIGN)
      return null;
    if (!Occurrences.BOTH_SEMANTIC.of(f).in(s.getExpression(),a.getRightHandSide()).isEmpty())
      return null;
    r.replace(initializer, Subject.pair(a.getRightHandSide(), initializer).toCondition(s.getExpression()), null);
    r.remove(s, null);
    return r;
  }
}