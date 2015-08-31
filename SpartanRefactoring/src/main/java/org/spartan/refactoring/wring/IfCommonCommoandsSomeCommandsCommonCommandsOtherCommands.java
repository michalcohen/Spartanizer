package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;
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
        asBlock(n.getParent());
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
  @Override Rewrite make(final IfStatement n, final Set<ASTNode> exclude) {
    // TODO Auto-generated method stub
    return super.make(n, exclude);
  }
  @Override boolean scopeIncludes(final IfStatement n) {
    return make(n) != null;
  }
}