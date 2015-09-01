package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asReturnStatement;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.same;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>int a = 3;
 * return a;</code> into <code>return a;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationInitializerReturnInlineInto extends Wring.ReplaceToNextStatement<VariableDeclarationFragment> {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final Statement nextStatement, final TextEditGroup g) {
    if (!Is.variableDeclarationStatement(f.getParent()))
      return null;
    final Expression initializer = f.getInitializer();
    if (initializer == null)
      return null;
    final ReturnStatement s = asReturnStatement(nextStatement);
    if (s == null)
      return null;
    final Expression returnValue = Extract.expression(s);
    if (returnValue == null)
      return null;
    final SimpleName name = f.getName();
    if (same(f.getName(), returnValue))
      return null;
    final Expression newReturnValue = duplicate(returnValue);
    if (Search.findDefinitions(name).in(newReturnValue))
      return null;
    final List<Expression> uses = Search.USES_SEMANTIC.of(name).in(newReturnValue);
    if (!Is.sideEffectFree(initializer) && uses.size() > 1)
      return null;
    remove(f, r, g);
    r.replace(returnValue, newReturnValue, g);
    for (final Expression e : uses)
      r.replace(e, new Plant(initializer).into(e.getParent()), g);
    return r;
  }
  @Override String description(final VariableDeclarationFragment f) {
    return "Eliminate temporary " + f.getName() + " and return its value";
  }
}