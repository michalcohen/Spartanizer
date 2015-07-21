package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.flip;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Range;

/**
 * Comparison with this, null, or literals (either character or integer) is
 * flipped so that these specific values are placed on the right.
 *
 * @author Yossi Gil
 * @since 2015-07-04
 */
public final class ComparisonWithSpecific extends SpartanizationOfInfixExpression {
  /** Instantiates this class */
  public ComparisonWithSpecific() {
    super("Specific comparison", "Specific values: 'null', 'this' and numerical literals should appear last in comparisons");
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (withinDomain(e) && applicable(e))
          $.add(new Range(e));
        return true;
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (inRange(m, e) && withinDomain(e) && applicable(e))
          r.replace(e, flip(e), null);
        return true;
      }
    });
  }
  static boolean applicable(final InfixExpression e) {
    return Is.specific(e.getLeftOperand());
  }
  static boolean withinDomain(final InfixExpression e) {
    return e != null && Is.comparison(e) && (hasThisOrNull(e) || hasOneSpecificArgument(e));
  }
  private static boolean hasThisOrNull(final InfixExpression e) {
    return Is.thisOrNull(e.getLeftOperand()) || Is.thisOrNull(e.getRightOperand());
  }
  private static boolean hasOneSpecificArgument(final InfixExpression e) {
    // One of the arguments must be specific, the other must not be.
    return Is.specific(e.getLeftOperand()) != Is.specific(e.getRightOperand());
  }
}
