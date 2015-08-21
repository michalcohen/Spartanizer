package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
/**
 * <code>
 * a ? b : c
 * </code> is the same as <code>
 * (a && b) || (!a && c)
 * </code> if b is false than: <code>
 * (a && false) || (!a && c) == (!a && c)
 * </code> if b is true than: <code>
 * (a && true) || (!a && c) == a || (!a && c) == a || c
 * </code> if c is false than: <code>
 * (a && b) || (!a && false) == (!a && c)
 * </code> if c is true than <code>
 * (a && b) || (!a && true) == (a && b) || (!a) == !a || b
 * </code> keywords <code><b>this</b></code> or <code><b>null</b></code>.
 *
 * @author Yossi Gil
 * @since 2015-07-20
 */
public final class TernaryBooleanLiteral extends Wring.OfConditionalExpression {
  @Override Expression _replacement(final ConditionalExpression e) {
    return Wrings.simplifyTernary(e);
  }
  @Override boolean scopeIncludes(final ConditionalExpression e) {
    return Wrings.isTernaryOfBooleanLitreral(e);
  }
}