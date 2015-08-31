package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to covert <code>b || false</code> to <code>b</code>
 *
 * @author Yossi Gil
 * @since 2015-07-20
 */
public final class InfixConditionalOrFalse extends Wring.Replacing<InfixExpression> {
  @Override Expression replacement(final InfixExpression e) {
    return Wrings.eliminateLiteral(e, false);
  }
  @Override boolean scopeIncludes(final InfixExpression e) {
    return e != null && Is.conditionalOr(e) && Have.falseLiteral(Extract.allOperands(e));
  }
  @Override String description(@SuppressWarnings("unused") final InfixExpression _) {
    return "Remove 'false' argument to '||'";
  }
}