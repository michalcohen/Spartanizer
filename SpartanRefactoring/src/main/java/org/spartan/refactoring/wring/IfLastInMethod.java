package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.utils.Utils.isLast;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Rewrite;
import org.spartan.refactoring.utils.Is;

public class IfLastInMethod extends Wring<IfStatement> {
  @Override String description(final IfStatement s) {
    return "Invert conditional " + s.getExpression() + " for early return";
  }
  @Override Rewrite make(final IfStatement s) {
    if (Is.vacuousThen(s) || !Is.vacuousElse(s) || Extract.statements(then(s)).size() < 2)
      return null;
    final Block b = asBlock(s.getParent());
    return b == null || isLast(s, b.statements()) || !(b.getParent() instanceof MethodDeclaration) ? null : new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        Wrings.insertAfter(s, Extract.statements(then(s)), r, g);
        final IfStatement newIf = duplicate(s);
        newIf.setExpression(duplicate(logicalNot(s.getExpression())));
        newIf.setThenStatement(s.getAST().newReturnStatement());
        newIf.setElseStatement(null);
        r.replace(s, newIf, g);
      }
    };
  }
}
