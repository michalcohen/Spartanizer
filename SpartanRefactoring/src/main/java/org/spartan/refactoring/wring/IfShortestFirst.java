package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Funcs.then;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>a ? (f,g,h) : c(d,e) </code> into
 * <code> a ? c(d,e) : f(g,h) </code>
 *
 * @author Yossi Gil
 * @since 2015-08-15
 */
public final class IfShortestFirst extends Wring.OfIfStatement {
  @Override Statement _replacement(final IfStatement s) {
    final Statement then = then(s);
    final Statement elze = elze(s);
    if (elze == null)
      return null;
    final int n1 = Extract.statements(then).size();
    final int n2 = Extract.statements(elze).size();
    if (n1 < n2)
      return null;
    final Expression notConditional = not(s.getExpression());
    final Statement $ = Subject.pair(elze, then).toIf(notConditional);
    if (n1 > n2)
      return $;
    assert n1 == n2;
    final int l1 = Wrings.length(not(notConditional), then);
    final int l2 = Wrings.length(notConditional, elze);
    return l1 > l2 ? $ : null;
  }
}