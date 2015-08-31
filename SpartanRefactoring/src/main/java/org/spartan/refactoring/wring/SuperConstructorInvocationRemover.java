package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;
import org.spartan.refactoring.utils.*;

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
  @Override boolean scopeIncludes(final SuperConstructorInvocation i) {
    return i.arguments().isEmpty();
  }
  @Override Rewrite make(final SuperConstructorInvocation n) {
    return new Rewrite(description(n), n) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        r.remove(n, g);
      }
    };
  }
  @Override String description(@SuppressWarnings("unused") final SuperConstructorInvocation _) {
    return "Remove empty 'super()' invocation";
  }
}
