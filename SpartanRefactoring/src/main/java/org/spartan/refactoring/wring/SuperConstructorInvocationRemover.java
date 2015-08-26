package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * A wring to remove <code>super()</code> calls which take no arguments, as
 * typically created by Eclipse's template for constructors.
 *
 * @author Yossi Gil
 * @since 2015-08-26
 */
public class SuperConstructorInvocationRemover extends Wring<SuperConstructorInvocation> {
  @Override boolean eligible(@SuppressWarnings("unused") final SuperConstructorInvocation _) {
    return true;
  }
  @Override boolean go(final ASTRewrite r, final SuperConstructorInvocation i) {
    if (eligible(i))
      r.remove(i, null);
    return true;
  }
  @Override ASTNode replacement(@SuppressWarnings("unused") final SuperConstructorInvocation _) {
    return null;
  }
  @Override boolean scopeIncludes(final SuperConstructorInvocation i) {
    return i.arguments().isEmpty();
  }
}
