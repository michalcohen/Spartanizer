package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.same;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
/**
 * A {@link Wring} to convert
 *
 * <pre>
 * int a; a = 3;
 * </pre>
 *
 * into
 *
 * <pre>
 * int a = 3;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationAssignment extends Wring.OfVariableDeclarationFragmentAndSurrounding {
  @Override ASTRewrite fillReplacement(final VariableDeclarationFragment f, final ASTRewrite r) {
    if (f.getInitializer() != null)
      return null;
    final Assignment a = Extract.nextAssignment(f);
    if (a == null || !same(f.getName(), a.getLeftHandSide()))
      return null;
    r.replace(f, Wrings.makeVariableDeclarationFragement(f, a.getRightHandSide()), null);
    r.remove(Extract.statement(a), null);
    return r;
  }
}