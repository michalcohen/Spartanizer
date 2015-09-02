package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN;
import static org.spartan.refactoring.utils.Funcs.*;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Search;

/**
 * A {@link Wring} to convert <code>int a;
 * a = 3;</code> into <code>int a = 3;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationInitialiazerAssignment extends Wring.VariableDeclarationFragementAndStatement {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final SimpleName n, final Expression initializer, final Statement nextStatement, final TextEditGroup g) {
    if (initializer == null)
      return null;
    final Assignment a = Extract.assignment(nextStatement);
    if (a == null || !same(n, left(a)) || a.getOperator() != ASSIGN)
      return null;
    final Expression secondInitializer = right(a);
    if (useForbiddenSiblings(f, secondInitializer))
      return null;
    final List<Expression> uses = Search.USES_SEMANTIC.of(n).in(secondInitializer);
    if (uses.size() == 1 && Search.noDefinitions(n).in(secondInitializer)) {
      r.remove(Extract.statement(a), g);
      final ASTNode betterInitializer = duplicate(secondInitializer);
      r.replace(initializer, betterInitializer, g);
      r.replace(Search.USES_SEMANTIC.of(n).in(betterInitializer).get(0), initializer, g);
      return r;
    }
    return null;
  }
  @Override String description(final VariableDeclarationFragment n) {
    return "Consolidate declaration of " + n.getName() + " with its subsequent initialization";
  }
}