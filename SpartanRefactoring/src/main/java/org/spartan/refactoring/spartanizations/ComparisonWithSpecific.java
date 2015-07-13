package org.spartan.refactoring.spartanizations;

import static org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.THIS_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.spartan.refacotring.utils.Funcs.flip;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refacotring.utils.Is;
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
  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (withinDomain(e) && applicable(e))
          opportunities.add(new Range(e));
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
    return isSpecific(e.getLeftOperand());
  }
  static boolean withinDomain(final InfixExpression e) {
    return e != null && isComparison(e) && (hasThisOrNull(e) || hasOneSpecificArgument(e));
  }
  private static boolean hasThisOrNull(final InfixExpression e) {
    return isThisOrNull(e.getLeftOperand()) || isThisOrNull(e.getRightOperand());
  }
  private static boolean hasOneSpecificArgument(final InfixExpression e) {
    // One of the arguments must be specific, the other must not be.
    return isSpecific(e.getLeftOperand()) != isSpecific(e.getRightOperand());
  }
  static boolean isComparison(final InfixExpression e) {
    return in(e.getOperator(), EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, NOT_EQUALS);
  }
  static boolean isSpecific(final Expression e) {
    return Is.oneOf(e, CHARACTER_LITERAL, NUMBER_LITERAL, NULL_LITERAL, THIS_EXPRESSION);
  }
  static boolean isThisOrNull(final Expression e) {
    return Is.oneOf(e, NULL_LITERAL, THIS_EXPRESSION);
  }
}
