package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Funcs.then;
import java.util.ArrayList;
import java.util.List;

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
public final class IfThenFooHumpyBarElseFooDumptyBar extends Wring<IfStatement> {
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Factor out commmon prefix of then and else branches to just before if statement";
  }
  @Override boolean eligible(@SuppressWarnings("unused") final IfStatement _) {
    return true;
  }
  @Override Rewrite make(final IfStatement s) {
    final List<Statement> then = Extract.statements(then(s));
    if (then.isEmpty())
      return null;
    final List<Statement> elze = Extract.statements(elze(s));
    if (elze.isEmpty())
      return null;
    final List<Statement> commonPrefix = commonPrefix(then, elze);
    return commonPrefix.isEmpty() ? null : new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final IfStatement newIf = replacement();
        if (!Is.block(s.getParent())) {
          if (newIf != null)
            commonPrefix.add(newIf);
          final Block b = Subject.ss(commonPrefix).toBlock();
          r.replace(s, b, g);
        } else {
          final ListRewrite lr = insertBefore(s, commonPrefix, r, g);
          if (newIf != null)
            lr.insertBefore(newIf, s, g);
          lr.remove(s, g);
        }
      }
      private IfStatement replacement() {
        return replacement(s.getExpression(), Subject.ss(then).toOneStatementOrNull(), Subject.ss(elze).toOneStatementOrNull());
      }
      private IfStatement replacement(final Expression condition, final Statement trimmedThen, final Statement trimmedElse) {
        return trimmedThen == null && trimmedElse == null ? null//
            : trimmedThen == null ? Subject.pair(trimmedElse, null).toNot(condition)//
                : Subject.pair(trimmedThen, trimmedElse).toIf(condition);
      }
      private ListRewrite insertBefore(final Statement where, final List<Statement> what, final ASTRewrite r, final TextEditGroup g) {
        final ListRewrite $ = r.getListRewrite(where.getParent(), Block.STATEMENTS_PROPERTY);
        for (final Statement s : what)
          $.insertBefore(s, where, g);
        return $;
      }
    };
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
  @Override Rewrite make(final IfStatement n, final ExclusionManager exclude) {
    return super.make(n, exclude);
  }
  @Override boolean scopeIncludes(final IfStatement n) {
    return make(n) != null;
  }
}