package org.spartan.refactoring.wring;

import static org.spartan.refactoring.wring.Wrings.*;
import static org.spartan.refactoring.utils.Funcs.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>if (X)
 *   return A;
 * if (Y)
 *   return A;</code> into <code>if (X || Y)
 *   return A;</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfCommonCommoandsSomeCommandsCommonCommandsOtherCommands extends Wring<IfStatement> {
  @Override String description(final IfStatement n) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override boolean eligible(@SuppressWarnings("unused") final IfStatement _) {
    return true;
  }
  @Override Rewrite make(final IfStatement n) {
    final List<Statement> then = Extract.statements(then(n));
    if (then.isEmpty())
      return null;
    final List<Statement> elze = Extract.statements(elze(n));
    if (elze.isEmpty())
      return null;
    final List<Statement> commonPrefix = commonPrefix(then, elze);
    if (commonPrefix.isEmpty())
      return null;
    return new Rewrite("Factor out commmon prefix of then and else branches to just before if statement", n) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final IfStatement newIf = replacement();
        if (Is.block(n.getParent())) {
          final ListRewrite lr = insertBefore(n, commonPrefix, r, g);
          if (newIf != null)
            lr.insertBefore(newIf, n, g);
          lr.remove(n, g);
        } else {
          final Block b = Subject.ss(commonPrefix).toBlock();
          if (newIf != null)
            b.statements().add(newIf);
          r.replace(n, b, g);
        }
      }
      private IfStatement replacement() {
        return replacement(n.getExpression(), Subject.ss(then).toOneStatementOrNull(), Subject.ss(elze).toOneStatementOrNull());
      }
      private IfStatement replacement(final Expression condition, final Statement trimmedThen, final Statement trimmedElse) {
        if (trimmedThen == null && trimmedElse == null)
          return null;
        final IfStatement $ = Subject.pair(trimmedThen, trimmedElse).toIf(condition);
        return trimmedThen != null ? $ : invert($);
      }
      private ListRewrite insertBefore(final Statement where, final List<Statement> what, final ASTRewrite r, final TextEditGroup g) {
        final ListRewrite $ = r.getListRewrite(where.getParent(), Block.STATEMENTS_PROPERTY);
        for (final Statement s : what)
          $.insertBefore(s, where, g);
        return $;
      }
    };
  }
  protected Block makeContainingBlock(final Statement s, final ASTRewrite r, final TextEditGroup g) {
    final ASTNode parent = s.getParent();
    if (Is.block(parent))
      return (Block) parent;
    final Block b = Subject.statement(s).toBlock();
    r.replace(s, b, g);
    return b;
  }
  private static List<Statement> commonPrefix(final List<Statement> ss1, final List<Statement> ss2) {
    final List<Statement> $ = new ArrayList<>();
    while (!ss1.isEmpty() && !ss2.isEmpty()) {
      final Statement s1 = ss1.get(0);
      final Statement s2 = ss2.get(0);
      if (!same(s1, s2))
        break;
      $.add(s1);
      ss1.remove(0);
      ss2.remove(0);
    }
    return $;
  }
  @Override Rewrite make(final IfStatement n, final Set<ASTNode> exclude) {
    return super.make(n, exclude);
  }
  @Override boolean scopeIncludes(final IfStatement n) {
    return make(n) != null;
  }
}