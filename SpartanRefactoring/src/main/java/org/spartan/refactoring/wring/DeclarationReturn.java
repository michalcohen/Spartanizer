package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.same;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>int a = 3;
 * return a;</code> into <code>return a;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationReturn extends Wring.ReplaceToNextStatement<VariableDeclarationFragment> {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final Statement nextStatement, final TextEditGroup g) {
    final Expression initializer = f.getInitializer();
    if (initializer == null)
      return null;
    final ReturnStatement s = Extract.nextReturn(f);
    if (s == null)
      return null;
    final Expression returnValue = Extract.expression(s);
    if (returnValue == null || !same(f.getName(), returnValue))
      return null;
    r.remove(Extract.statement(f), g);
    r.replace(s, Subject.operand(initializer).toReturn(), g);
    return r;
  }
  @Override String description(final VariableDeclarationFragment f) {
    return "Eliminate temporary " + f.getName() + " and return its value";
  }
}