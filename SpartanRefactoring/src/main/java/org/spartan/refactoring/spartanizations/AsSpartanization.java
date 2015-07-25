package org.spartan.refactoring.spartanizations;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.utils.Range;

/**
 * An adapter which makes it possible to use a single @{link Wring} as a
 * {@link Spartanization}
 *
 * @author Yossi Gil
 * @since 2015/07/25
 */
public class AsSpartanization extends Spartanization {
  final Wring inner;
  /** Instantiates this class */
  AsSpartanization(final Wring wring, final String name, final String description) {
    super(name, description);
    inner = wring;
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final PrefixExpression e) {
        if (!inner.scopeIncludes(e))
          return true;
        if (inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      private boolean addTo(final Expression e, final List<Range> $) {
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final InfixExpression e) {
        if (!inner.scopeIncludes(e))
          return true;
        if (inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final ConditionalExpression e) {
        if (!inner.scopeIncludes(e))
          return true;
        if (!inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        return !inRange(m, e) || !true || inner.go(r, e);
      }
      @Override public boolean visit(final PrefixExpression e) {
        return !inRange(m, e) ? true : inner.go(r, e);
      }
      @Override public boolean visit(final ConditionalExpression e) {
        return !inRange(m, e) ? true : inner.go(r, e);
      }
    });
  }
}
