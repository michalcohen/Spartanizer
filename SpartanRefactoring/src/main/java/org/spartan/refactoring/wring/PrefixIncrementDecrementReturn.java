package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.*;
import static org.spartan.refactoring.utils.Extract.*;
import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.utils.Utils.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>a = 3;return a;</code> to
 * <code>return a = 3;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-28
 */
public class PrefixIncrementDecrementReturn extends Wring.ReplaceToNextStatement<PrefixExpression> {
  @Override ASTRewrite go(final ASTRewrite r, final PrefixExpression e, final Statement nextStatement, final TextEditGroup g) {
    if (!in(e.getOperator(), INCREMENT, DECREMENT))
      return null;
    final Statement parent = asStatement(e.getParent());
    if (parent == null)
      return null;
    final ReturnStatement s = Extract.nextReturn(e);
    if (s == null || !same(e.getOperand(), core(s.getExpression())))
      return null;
    r.remove(parent, g);
    r.replace(s, Subject.operand(e).toReturn(), g);
    return r;
  }
  @Override String description(final PrefixExpression n) {
    return "Consolidate " + n + " with subsequent 'return' of " + n.getOperand();
  }
}
