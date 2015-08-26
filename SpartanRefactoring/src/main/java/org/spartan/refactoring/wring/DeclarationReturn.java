package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.same;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert
 *
 * <pre>
 * int a = 3;
 * return a;
 * </pre>
 *
 * into
 *
 * <pre>
 * return a;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationReturn extends Wring.OfVariableDeclarationFragmentAndSurrounding {
  @Override ASTRewrite fillReplacement(final VariableDeclarationFragment f, final ASTRewrite r) {
    final Expression initializer = f.getInitializer();
    if (initializer == null)
      return null;
    final ReturnStatement s = Extract.nextReturn(f);
    if (s == null)
      return null;
    final Expression returnValue = Extract.expression(s);
    if (returnValue == null || !same(f.getName(), returnValue))
      return null;
    r.remove(Extract.statement(f), null);
    r.replace(s, Subject.operand(initializer).toReturn(), null);
    return r;
  }
}