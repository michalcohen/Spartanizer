package il.org.spartan.refactoring.wring;

import il.org.spartan.refactoring.preferences.*;
import il.org.spartan.refactoring.utils.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import static il.org.spartan.refactoring.utils.Funcs.*;
import static il.org.spartan.refactoring.wring.Wrings.*;

/** A {@link Wring} to convert <code>if (X) {bar(); foo();} else {baz();
 * foo();}</code> into <code>if (X) bar(); else baz(); foo();</code>
 * @author Yossi Gil
 * @since 2015-09-05 */
public final class IfBarFooElseBazFoo extends Wring<IfStatement> implements Kind.ConsolidateStatements {
  private static List<Statement> commmonSuffix(final List<Statement> ss1, final List<Statement> ss2) {
    final List<Statement> $ = new ArrayList<>();
    while (!ss1.isEmpty() && !ss2.isEmpty()) {
      final Statement s1 = ss1.get(ss1.size() - 1);
      final Statement s2 = ss2.get(ss2.size() - 1);
      if (!same(s1, s2))
        break;
      $.add(s1);
      ss1.remove(ss1.size() - 1);
      ss2.remove(ss2.size() - 1);
    }
    return $;
  }
  @Override String description(final IfStatement s) {
    return "Consolidate commmon suffix of then and else branches of if(" + s.getExpression() + ") to just after if statement";
  }
  @Override Rewrite make(final IfStatement s) {
    final List<Statement> then = extract.statements(then(s));
    if (then.isEmpty())
      return null;
    final List<Statement> elze = extract.statements(elze(s));
    if (elze.isEmpty())
      return null;
    final List<Statement> commmonSuffix = commmonSuffix(then, elze);
    for (final Statement st : commmonSuffix) {
      final DefinitionsCollector c = new DefinitionsCollector(then);
      st.accept(c);
      if (c.notAllDefined())
        return null;
    }
    return then.isEmpty() && elze.isEmpty() || commmonSuffix.isEmpty() ? null : new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final IfStatement newIf = replacement();
        if (Is.block(s.getParent())) {
          final ListRewrite lr = insertAfter(s, commmonSuffix, r, g);
          lr.insertAfter(newIf, s, g);
          lr.remove(s, g);
        } else {
          if (newIf != null)
            commmonSuffix.add(0, newIf);
          r.replace(s, Subject.ss(commmonSuffix).toBlock(), g);
        }
      }
      private IfStatement replacement() {
        return replacement(s.getExpression(), Subject.ss(then).toOneStatementOrNull(), Subject.ss(elze).toOneStatementOrNull());
      }
      private IfStatement replacement(final Expression condition, final Statement trimmedThen, final Statement trimmedElse) {
        return trimmedThen == null && trimmedElse == null ? null : trimmedThen == null ? Subject.pair(trimmedElse, null).toNot(condition) : Subject.pair(trimmedThen, trimmedElse).toIf(condition);
      }
    };
  }
  @Override Rewrite make(final IfStatement s, final ExclusionManager exclude) {
    return super.make(s, exclude);
  }

  private class DefinitionsCollector extends ASTVisitor {
    public DefinitionsCollector(final List<Statement> l) {
      notAllDefined = false;
      this.l = l.toArray(new Statement[l.size()]);
    }
    public boolean notAllDefined() {
      return notAllDefined;
    }
    @Override public boolean visit(final SimpleName n) {
      if (!Collect.declarationsOf(n).in(l).isEmpty())
        notAllDefined = true;
      return false;
    }

    private final Statement[] l;
    private boolean notAllDefined;
  }
}