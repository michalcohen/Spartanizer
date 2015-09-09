package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.utils.Utils.last;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Rewrite;

public final class ReturnLastInMethod extends Wring<ReturnStatement> {
  @Override String description(@SuppressWarnings("unused") final ReturnStatement _) {
    return "Remove redundant return statement";
  }
  @Override Rewrite make(final ReturnStatement s) {
    if (s.getExpression() != null)
      return null;
    final Block b = asBlock(s.getParent());
    return b == null || last(b.statements()) != s || !(b.getParent() instanceof MethodDeclaration) ? null //
        : new Rewrite(description(s), s) {
          @Override public void go(final ASTRewrite r, final TextEditGroup g) {
            r.remove(s, g);
          }
        };
  }
}
