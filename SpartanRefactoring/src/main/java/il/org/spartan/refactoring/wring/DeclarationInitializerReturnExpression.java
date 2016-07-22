package il.org.spartan.refactoring.wring;

import static il.org.spartan.refactoring.utils.Funcs.*;
import il.org.spartan.refactoring.preferences.*;
import il.org.spartan.refactoring.utils.*;
import il.org.spartan.refactoring.wring.LocalInliner.LocalInlineWithValue;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

/**
 * A {@link Wring} to convert <code>int a = 3; return a;</code> into
 * <code>return a;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationInitializerReturnExpression extends Wring.VariableDeclarationFragementAndStatement implements Kind.InlineVariable {
  private static ASTRewrite go(final ASTRewrite $, final VariableDeclarationFragment f, final SimpleName n, final Expression e, final TextEditGroup g, final ReturnStatement s) {
    if (s == null)
      return null;
    final LocalInlineWithValue i = new LocalInliner(n, $, g).byValue(e);
    return go($, f, g, s, i, newReturnValue(n, i, extract.expression(s)));
  }
  private static ASTRewrite go(final ASTRewrite $, final VariableDeclarationFragment f, final TextEditGroup g, final ReturnStatement s, final LocalInlineWithValue v, final Expression e) {
    if (e == null)
      return null;
    $.replace(s.getExpression(), e, g);
    v.inlineInto(e);
    eliminate(f, $, g);
    return $;
  }
  private static Expression newReturnValue(final SimpleName n, final LocalInlineWithValue v, final Expression $) {
    return $ != null && !same(n, $) && v.canInlineInto($) ? $ : null;
  }
  @Override String description(final VariableDeclarationFragment f) {
    return "Eliminate temporary " + f.getName() + " and inline its value into the expression of the subsequent return statement";
  }
  @Override ASTRewrite go(final ASTRewrite $, final VariableDeclarationFragment f, final SimpleName n, final Expression e, final Statement s, final TextEditGroup g) {
    return e == null || hasAnnotation(f) ? null : go($, f, n, e, g, asReturnStatement(s));
  }
}