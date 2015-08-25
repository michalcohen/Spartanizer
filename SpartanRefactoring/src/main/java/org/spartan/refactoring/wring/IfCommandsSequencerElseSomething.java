package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.then;
import static org.spartan.refactoring.utils.Restructure.duplicateInto;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;
import org.spartan.utils.Range;

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
public final class IfCommandsSequencerElseSomething extends Wring.OfIfStatementAndSubsequentStatement {
  private static void addAllReplacing(final List<Statement> to, final List<Statement> from, final Statement substitute, final Statement by1, final List<Statement> by2) {
    for (final Statement t : from)
      if (t != substitute)
        duplicateInto(t, to);
      else {
        duplicateInto(by1, to);
        duplicateInto(by2, to);
      }
  }
  @Override ASTRewrite fillReplacement(final IfStatement s, final ASTRewrite r) {
    assert scopeIncludes(s);
    final IfStatement shorterIf = endsWithSequencer(then(s)) ? duplicate(s) : Subject.pair(elze(s), then(s)).toIf(not(s.getExpression()));
    final List<Statement> remainder = Extract.statements(elze(shorterIf));
    shorterIf.setElseStatement(null);
    final Block parent = asBlock(s.getParent());
    final Block newParent = s.getAST().newBlock();
    if (parent != null) {
      addAllReplacing(newParent.statements(), parent.statements(), s, shorterIf, remainder);
      r.replace(parent, newParent, null);
    } else {
      newParent.statements().add(shorterIf);
      duplicateInto(remainder, newParent.statements());
      r.replace(s, newParent, null);
    }
    return r;
  }
  private static boolean endsWithSequencer(final Statement s) {
    return Is.sequencer(Extract.lastStatement(s));
  }
  @Override Range range(final ASTNode e) {
    return new Range(e);
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return elze(s) != null && (endsWithSequencer(then(s)) || endsWithSequencer(elze(s)));
  }
}