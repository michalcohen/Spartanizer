package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.wring.Wrings.emptyElse;
import static org.spartan.refactoring.wring.Wrings.emptyThen;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>if (x) ; else  {a;}</code> into
 * <code>if (!x) a;</code>.
 *
 * @author Yossi Gil
 * @since 2015-08-26
 */
public final class IfEmptyThen extends Wring.ReplaceCurrentNode<IfStatement> {
  @Override Statement replacement(final IfStatement s) {
    final IfStatement $ = Subject.pair(elze(s), null).toNot(s.getExpression());
    final IfStatement parent = asIfStatement(s.getParent());
    return parent == null || then(parent) != s ? $ : Subject.statement($).toBlock();
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && emptyThen(s) && !emptyElse(s);
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Invert conditional and remove vacuous 'then' branch";
  }
}