package org.spartan.refactoring.wring;

import static org.spartan.refactoring.wring.Wrings.emptyElse;
import static org.spartan.refactoring.wring.Wrings.emptyThen;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * A {@link Wring} to degenerate if statements such as
 * <code> if (x) ; else ;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-26
 */
public final class IfEmptyThenEmptyElse extends Wring.Defaults<IfStatement> {
  @Override final boolean eligible(final IfStatement s) {
    assert scopeIncludes(s);
    return true;
  }
  @Override final boolean go(final ASTRewrite r, final IfStatement s) {
    if (eligible(s)) {
      s.setElseStatement(null);
      r.remove(s, null);
    }
    return true;
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && emptyThen(s) && emptyElse(s);
  }
}