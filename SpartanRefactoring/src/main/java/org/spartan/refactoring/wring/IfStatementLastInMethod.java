package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.utils.Utils.last;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Rewrite;

public class IfStatementLastInMethod extends Wring<IfStatement> {
  @Override String description(final IfStatement n) {
    return "Invert conditional " + n.getExpression() + " for early return";
  }
  @Override Rewrite make(final IfStatement s) {
    if (Wrings.emptyThen(s) || !Wrings.emptyElse(s))
      return null;
    if (Extract.statements(then(s)).size() < 2)
      return null;
    final Block b = asBlock(s.getParent());
    if (b == null)
      return null;
    @SuppressWarnings("unchecked") final Object last = last(b.statements());
    if (last != s)
      return null;
    if (!(b.getParent() instanceof MethodDeclaration))
      return null;
    return new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        Wrings.insertAfter(s, Extract.statements(then(s)), r, g);
        final IfStatement newIf = duplicate(s);
        newIf.setExpression(logicalNot(s.getExpression()));
        newIf.setThenStatement(s.getAST().newReturnStatement());
        newIf.setElseStatement(null);
        r.replace(s, newIf, g);
      }
    };
  }
}
