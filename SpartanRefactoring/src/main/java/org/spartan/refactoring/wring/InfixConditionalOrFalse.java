package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Have;
import org.spartan.refactoring.utils.Is;
/**
 * A {@link Wring} that eliminate Boolean literals, when possible present on
 * logical AND an logical OR.
 *
 * @author Yossi Gil
 * @since 2015-07-20
 */

public final class InfixConditionalOrFalse extends Wring.OfInfixExpression {
  @Override Expression _replacement(final InfixExpression e) {
    return Wrings.eliminateLiteral(e, false);
  }
  @Override boolean scopeIncludes(final InfixExpression e) {
    return e != null && Is.conditionalOr(e) && Have.falseLiteral(Extract.operands(e));
  }
}