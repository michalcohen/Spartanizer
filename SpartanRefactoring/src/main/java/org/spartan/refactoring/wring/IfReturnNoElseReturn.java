package org.spartan.refactoring.wring;
import static org.spartan.refactoring.utils.Funcs.then;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert
 *
 * <pre>
 * if (x) {
 *   ;
 *   f();
 *   return a;
 * } else {
 *   ;
 *   g();
 *   {
 *   }
 * }
 * </pre>
 *
 * into
 *
 * <pre>
 * if (x) {
 *   f();
 *   return a;
 * }
 * g();
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfReturnNoElseReturn extends Wring.OfIfStatementAndSubsequentStatement {
  @Override ASTRewrite fillReplacement(final IfStatement s, final ASTRewrite r) {
    final ReturnStatement then = Extract.returnStatement(then(s));
    final ReturnStatement elze = Extract.nextReturn(s);
    return Wrings.replaceTwoStatements(r, s, Subject.operand(Subject.pair(Extract.expression(then), Extract.expression(elze)).toCondition(s.getExpression())).toReturn());
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    final ReturnStatement then = Extract.returnStatement(then(s));
    final ReturnStatement elze = Extract.nextReturn(s);
    return Wrings.emptyElse(s) && then != null && elze != null;
  }
}