package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.wring.Wrings.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
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
    if (!emptyElse(s) || !Is.sequencer(nextStatement))
      return null;
    final IfStatement t = Subject.pair(then(s), nextStatement).toIf(s.getExpression());
    if (!shoudlInvert(t))
      return null;
    replaceWithShorterIf(s, invert(t), r, g);
    return r;
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return elze(s) != null && (endsWithSequencer(then(s)) || endsWithSequencer(elze(s)));
  }
}