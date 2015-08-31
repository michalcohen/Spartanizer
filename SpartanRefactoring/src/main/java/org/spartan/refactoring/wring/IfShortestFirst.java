package org.spartan.refactoring.wring;

import static org.spartan.refactoring.wring.Wrings.*;

import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.then;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.spartan.refactoring.utils.Extract;

/**
 * A {@link Wring} to convert <code>a ? (f,g,h) : c(d,e)</code> into
 * <code>a ? c(d,e) : f(g,h)</code>
 *
 * @author Yossi Gil
 * @since 2015-08-15
 */
public final class IfShortestFirst extends Wring.Replacing<IfStatement> {
  @Override Statement replacement(final IfStatement s) {
    final Statement then = then(s);
    final Statement elze = elze(s);
    if (elze == null)
      return null;
    final int n1 = Extract.statements(then).size();
    final int n2 = Extract.statements(elze).size();
    if (n1 < n2)
      return null;
    final IfStatement $ = invert(s);
    if (n1 > n2)
      return $;
    assert n1 == n2;
    return positivePrefixLength($) < positivePrefixLength(invert($)) ? $ : null;
  }
  private static int positivePrefixLength(final IfStatement $) {
    return Wrings.length($.getExpression(), then($));
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Invert logical conditiona and swap branches of 'if' to make the shortest branch first";
  }
}