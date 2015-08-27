package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.duplicate;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * /** A {@link Wring} to convert
 *
 * <pre>
 * if (x)
 *   return b;
 * else {
 * }
 * </pre>
 *
 * into
 *
 * <pre>
 * if (x)
 *   return b;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-08-01
 */
public final class IfEmptyElse extends Wring.OfIfStatement {
  @Override Statement _replacement(final IfStatement s) {
    final IfStatement $ = duplicate(s);
    $.setElseStatement(null);
    return $;
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && Wrings.degenerateElse(s);
  }
}