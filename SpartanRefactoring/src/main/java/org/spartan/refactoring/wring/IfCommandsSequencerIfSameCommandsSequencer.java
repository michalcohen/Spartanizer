package org.spartan.refactoring.wring;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Funcs.then;
import static org.spartan.utils.Utils.last;

import java.util.List;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;

/**
 * A {@link Wring} to convert
 *
 * <pre>
 * if (X)
 *   return A;
 * if (Y)
 *   return A;
 * </pre>
 *
 * into
 *
 * <pre>
 * if (X || Y)
 *   return A;
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfCommandsSequencerIfSameCommandsSequencer extends Wring.OfIfStatementAndSubsequentStatement {
  private static IfStatement makeIfWithoutElse(final Statement s, final InfixExpression condition) {
    final IfStatement $ = condition.getAST().newIfStatement();
    $.setExpression(condition);
    $.setThenStatement(s);
    $.setElseStatement(null);
    return $;
  }
  @Override ASTRewrite fillReplacement(final IfStatement s1, final ASTRewrite r) {
    if (s1 == null || !Wrings.elseIsEmpty(s1))
      return null;
    final IfStatement s2 = Extract.nextIfStatement(s1);
    if (s2 == null || !Wrings.elseIsEmpty(s2))
      return null;
    final Statement then = then(s1);
    final List<Statement> ss1 = Extract.statements(then);
    final List<Statement> ss2 = Extract.statements(then(s2));
    return !same(ss1, ss2) || !Is.sequencer(last(ss1)) ? null
        : Wrings.replaceTwoStatements(r, s1, makeIfWithoutElse(BlockSimplify.reorganizeNestedStatement(then), Subject.pair(s1.getExpression(), s2.getExpression()).to(CONDITIONAL_OR)));
  }
}