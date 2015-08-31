package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.utils.Utils.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>if (X)
 *   return A;
 * if (Y)
 *   return A;</code> into <code>if (X || Y)
 *   return A;</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfCommandsSequencerIfSameCommandsSequencer extends Wring.ReplaceToNextStatement<IfStatement> {
  private static IfStatement makeIfWithoutElse(final Statement s, final InfixExpression condition) {
    final IfStatement $ = condition.getAST().newIfStatement();
    $.setExpression(condition);
    $.setThenStatement(s);
    $.setElseStatement(null);
    return $;
  }
  @Override ASTRewrite go(final ASTRewrite r, final IfStatement s, final Statement nextStatement, final TextEditGroup g) {
    if (s == null || !Wrings.emptyElse(s))
      return null;
    final IfStatement s2 = Extract.nextIfStatement(s);
    if (s2 == null || !Wrings.emptyElse(s2))
      return null;
    final Statement then = then(s);
    final List<Statement> ss1 = Extract.statements(then);
    final List<Statement> ss2 = Extract.statements(then(s2));
    return !same(ss1, ss2) || !Is.sequencer(last(ss1)) ? null
        : Wrings.replaceTwoStatements(r, s,
            makeIfWithoutElse(BlockSimplify.reorganizeNestedStatement(then), Subject.pair(s.getExpression(), s2.getExpression()).to(CONDITIONAL_OR)), g);
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Consolidate two 'if' statements with identical body";
  }
}