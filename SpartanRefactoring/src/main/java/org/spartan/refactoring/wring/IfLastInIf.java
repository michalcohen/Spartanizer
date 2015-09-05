package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.IfStatement;
import org.spartan.refactoring.utils.Rewrite;

public class IfLastInIf extends Wring<IfStatement> {
  @Override String description(final IfStatement n) {
    return "Invert conditional " + n.getExpression() + " for early return";
  }
  @Override boolean eligible(final IfStatement n) {
    return true;
  }
  @Override Rewrite make(final IfStatement n) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override Rewrite make(final IfStatement n, final ExclusionManager exclude) {
    // TODO Auto-generated method stub
    return super.make(n, exclude);
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return make(s, null) != null;
  }
}
