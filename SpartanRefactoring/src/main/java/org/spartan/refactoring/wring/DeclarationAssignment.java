package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;

/**
 * A {@link Wring} to convert <code>int a;
 * a = 3;</code> into <code>int a = 3;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationAssignment extends Wring.ReplaceToNextStatement<VariableDeclarationFragment> {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final Statement nextStatement, final TextEditGroup g) {
    if (f.getInitializer() != null)
      return null;
    final Assignment a = Extract.assignment(nextStatement);
    if (a == null || !same(f.getName(), left(a)))
      return null;
    r.replace(f, makeVariableDeclarationFragement(f, right(a)), g);
    r.remove(Extract.statement(a), g);
    return r;
  }
  private static VariableDeclarationFragment makeVariableDeclarationFragement(final VariableDeclarationFragment f, final Expression e) {
    final VariableDeclarationFragment $ = duplicate(f);
    $.setInitializer(duplicate(e));
    return $;
  }
  @Override String description(final VariableDeclarationFragment n) {
    return "Consolidate declaration of " + n.getName() + " with its subsequent initialization";
  }
}