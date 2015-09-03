package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.wring.Wrings.*;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

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
public final class IfCommandsSequencerNoElseSingletonSequencer extends Wring.ReplaceToNextStatement<IfStatement> {
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Invert conditional and use next statement)";
  }
  @Override ASTRewrite go(final ASTRewrite r, final IfStatement s, final Statement nextStatement, final TextEditGroup g) {
    if (!emptyElse(s) || !Is.sequencer(nextStatement) || !endsWithSequencer(then(s)))
      return null;
    final IfStatement asVirtualIf = Subject.pair(then(s), nextStatement).toIf(s.getExpression());
    if (same(then(asVirtualIf), elze(asVirtualIf))) {
      r.replace(s, then(asVirtualIf), g);
      r.remove(nextStatement, g);
      return r;
    }
    if (!shoudlInvert(asVirtualIf))
      return null;
    final IfStatement canonicalIf = invert(asVirtualIf);
    final List<Statement> ss = Extract.statements(elze(canonicalIf));
    canonicalIf.setElseStatement(null);
    if (!Is.block(s.getParent())) {
      ss.add(0, canonicalIf);
      final Block b = Subject.ss(ss).toBlock();
      r.replace(s, b, g);
      r.remove(nextStatement, g);
    } else {
      final ListRewrite lr = insertAfter(s, ss, r, g);
      lr.replace(s, canonicalIf, g);
      lr.remove(nextStatement, g);
    }
    return r;
  }
  private static ListRewrite insertAfter(final Statement where, final List<Statement> what, final ASTRewrite r, final TextEditGroup g) {
    final ListRewrite $ = r.getListRewrite(where.getParent(), Block.STATEMENTS_PROPERTY);
    for (int i = what.size() - 1; i >= 0; i--) {
      final Statement s = what.get(i);
      $.insertAfter(s, where, g);
    }
    return $;
  }
}