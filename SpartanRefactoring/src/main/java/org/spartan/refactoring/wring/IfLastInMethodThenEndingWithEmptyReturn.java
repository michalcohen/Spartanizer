package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.asReturnStatement;
import static org.spartan.refactoring.utils.Funcs.then;
import static org.spartan.utils.Utils.last;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Rewrite;

public class IfLastInMethodThenEndingWithEmptyReturn extends Wring<IfStatement> {
  @Override String description(final IfStatement n) {
    return "Remove redundant in 'then' branch of last in method if statement ";
  }
  @Override Rewrite make(final IfStatement s, final ExclusionManager exclude) {
    final Block b = asBlock(s.getParent());
    if (b == null || !(b.getParent() instanceof MethodDeclaration) || last(b.statements()) != s)
      return null;
    final ReturnStatement deleteMe = asReturnStatement(Extract.lastStatement(then(s)));
    if (deleteMe == null || deleteMe.getExpression() != null)
      return null;
    if (exclude != null)
      exclude.equals(s);
    return new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        r.replace(deleteMe, s.getAST().newEmptyStatement(), g);
      }
    };
  }
}
