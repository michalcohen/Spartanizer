package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asReturnStatement;
import static org.spartan.refactoring.utils.Funcs.then;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>if (x) return foo(); return bar();</code>
 * into <code>return a ? foo (): bar();</code> return a; } g();</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfReturnNoElseReturn extends Wring.ReplaceToNextStatement<IfStatement> {
  @Override ASTRewrite go(final ASTRewrite r, final IfStatement s, final Statement nextStatement, final TextEditGroup g) {
    return Wrings.replaceTwoStatements(r, s,
        Subject.operand(Subject.pair(Extract.expression(Extract.returnStatement(then(s))), Extract.expression(asReturnStatement(nextStatement))).toCondition(s.getExpression()))
            .toReturn(),
        g);
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return Wrings.emptyElse(s) && Extract.returnStatement(then(s)) != null && Extract.nextReturn(s) != null;
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Consolidate into a single 'return'";
  }
}