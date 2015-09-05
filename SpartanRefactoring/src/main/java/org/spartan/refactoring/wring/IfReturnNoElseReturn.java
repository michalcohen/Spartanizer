package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asReturnStatement;
import static org.spartan.refactoring.utils.Funcs.then;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert <code>if (x) {
 *   ;
 *   f();
 *   return a;
 * } else {
 *   ;
 *   g();
 *   {
 *   }
 * }</code> into <code>if (x) {
 *   f();
 *   return a;
 * }
 * g();</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfReturnNoElseReturn extends Wring.ReplaceToNextStatement<IfStatement> {
  @Override ASTRewrite go(final ASTRewrite r, final IfStatement s, final Statement nextStatement, final TextEditGroup g) {
    final ReturnStatement then = Extract.returnStatement(then(s));
    final ReturnStatement elze = asReturnStatement(nextStatement);
    return Wrings.replaceTwoStatements(r, s, Subject.operand(Subject.pair(Extract.expression(then), Extract.expression(elze)).toCondition(s.getExpression())).toReturn(), g);
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    final ReturnStatement then = Extract.returnStatement(then(s));
    return Wrings.emptyElse(s) && then != null && Extract.nextReturn(s) != null;
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Consolidate into a single 'return'";
  }
}