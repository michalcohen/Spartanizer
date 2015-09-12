package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.utils.Utils.lastIn;
import static org.spartan.utils.Utils.penultimateIn;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Search;
import org.spartan.refactoring.wring.LocalInliner.LocalInlineWithValue;

/**
 * A {@link Wring} to convert <code>int a = 3;
 * b = a;</code> into <code>b = a</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationInitializerStatementTerminatingScope extends Wring.VariableDeclarationFragementAndStatement {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final SimpleName n, final Expression initializer, final Statement nextStatement,
      final TextEditGroup g) {
    if (initializer == null)
      return null;
    final Statement s = Extract.statement(f);
    if (s == null)
      return null;
    final Block parent = asBlock(s.getParent());
    if (parent == null)
      return null;
    final List<Statement> ss = parent.statements();
    if (!lastIn(nextStatement, ss) || !penultimateIn(s, ss))
      return null;
    final List<SimpleName> in = Search.forDefinitions(n).in(nextStatement);
    if (!in.isEmpty())
      return null;
    final List<SimpleName> uses = Search.forAllOccurencesOf(f.getName()).in(nextStatement);
    if (uses.size() > 1 && !Is.sideEffectFree(initializer))
      return null;
    final LocalInlineWithValue i = new LocalInliner(n, r, g).byValue(initializer);
    final Statement newStatement = duplicate(nextStatement);
    final int addedSize = i.addedSize(newStatement);
    final int removalSaving = removalSaving(f);
    if (addedSize - removalSaving > 0)
      return null;
    r.replace(nextStatement, newStatement, g);
    i.inlineInto(newStatement);
    remove(f, r, g);
    return r;
  }
  @Override String description(final VariableDeclarationFragment f) {
    return "Inline local " + f.getName() + " into subsequent statement";
  }
}