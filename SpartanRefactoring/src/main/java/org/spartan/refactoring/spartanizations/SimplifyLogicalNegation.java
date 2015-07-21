package org.spartan.refactoring.spartanizations;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.spartan.refactoring.utils.Funcs.asAndOrOr;
import static org.spartan.refactoring.utils.Funcs.asComparison;
import static org.spartan.refactoring.utils.Funcs.asNot;
import static org.spartan.refactoring.utils.Funcs.getCore;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Range;

/**
 * Simplifies a negated boolean expression using De-Morgan laws and laws of
 * arithmetics.
 *
 * @author Yossi Gil
 * @since 2014/06/15
 */
public class SimplifyLogicalNegation extends Spartanization {
  /** Instantiates this class */
  public SimplifyLogicalNegation() {
    super("Simplify logical negation", "Simplify logical negation");
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final PrefixExpression e) {
        return !inRange(m, e) ? true : simplifyNot(asNot(e));
      }
      private boolean simplifyNot(final PrefixExpression e) {
        return e == null ? true : simplifyNot(e, getCore(e.getOperand()));
      }
      private boolean simplifyNot(final PrefixExpression e, final Expression inner) {
        return perhapsDoubleNegation(e, inner) //
            || perhapsDeMorgan(e, inner) //
            || perhapsComparison(e, inner) //
            || true;
      }
      boolean perhapsDoubleNegation(final Expression e, final Expression inner) {
        return perhapsDoubleNegation(e, asNot(inner));
      }
      boolean perhapsDoubleNegation(final Expression e, final PrefixExpression inner) {
        return inner != null && replace(e, inner.getOperand());
      }
      boolean perhapsDeMorgan(final Expression e, final Expression inner) {
        return perhapsDeMorgan(e, asAndOrOr(inner));
      }
      boolean perhapsDeMorgan(final Expression e, final InfixExpression inner) {
        return inner != null && deMorgan(e, inner, getCoreLeft(inner), getCoreRight(inner));
      }
      boolean deMorgan(final Expression e, final InfixExpression inner, final Expression left, final Expression right) {
        return deMorgan1(e, inner, parenthesize(left), parenthesize(right));
      }
      boolean deMorgan1(final Expression e, final InfixExpression inner, final Expression left, final Expression right) {
        return replace(e, //
            parenthesize( //
                addExtendedOperands(inner, //
                    makeInfixExpression(not(left), conjugate(inner.getOperator()), not(right)))));
      }
      InfixExpression addExtendedOperands(final InfixExpression from, final InfixExpression $) {
        if (from.hasExtendedOperands())
          addExtendedOperands(from.extendedOperands(), $.extendedOperands());
        return $;
      }
      void addExtendedOperands(final List<Expression> from, final List<Expression> to) {
        for (final Expression e : from)
          to.add(not(e));
      }
      boolean perhapsComparison(final Expression e, final Expression inner) {
        return perhapsComparison(e, asComparison(inner));
      }
      boolean perhapsComparison(final Expression e, final InfixExpression inner) {
        return inner != null && comparison(e, inner);
      }
      boolean comparison(final Expression e, final InfixExpression inner) {
        return replace(e, cloneInfixChangingOperator(inner, ShortestBranchFirst.negate(inner.getOperator())));
      }
      InfixExpression cloneInfixChangingOperator(final InfixExpression e, final Operator o) {
        return e == null ? null : makeInfixExpression(getCoreLeft(e), o, getCoreRight(e));
      }
      Expression parenthesize(final Expression e) {
        if (isSimple(e))
          return (Expression) ASTNode.copySubtree(t, e);
        final ParenthesizedExpression $ = t.newParenthesizedExpression();
        $.setExpression((Expression) ASTNode.copySubtree(t, getCore(e)));
        return $;
      }
      boolean isSimple(final Expression e) {
        return isSimple(e.getClass());
      }
      boolean isSimple(final Class<? extends Expression> c) {
        return in(c, BooleanLiteral.class, //
            CharacterLiteral.class, //
            NullLiteral.class, //
            NumberLiteral.class, //
            StringLiteral.class, //
            TypeLiteral.class, //
            Name.class, //
            QualifiedName.class, //
            SimpleName.class, //
            ParenthesizedExpression.class, //
            SuperMethodInvocation.class, //
            MethodInvocation.class, //
            ClassInstanceCreation.class, //
            SuperFieldAccess.class, //
            FieldAccess.class, //
            ThisExpression.class, //
            null);
      }
      private PrefixExpression not(final Expression e) {
        final PrefixExpression $ = t.newPrefixExpression();
        $.setOperator(NOT);
        $.setOperand(parenthesize(e));
        return $;
      }
      private InfixExpression makeInfixExpression(final Expression left, final Operator o, final Expression right) {
        final InfixExpression $ = t.newInfixExpression();
        $.setLeftOperand((Expression) ASTNode.copySubtree(t, left));
        $.setOperator(o);
        $.setRightOperand((Expression) ASTNode.copySubtree(t, right));
        return $;
      }
      private boolean replace(final ASTNode original, final ASTNode replacement) {
        if (!hasNull(original, replacement))
          r.replace(original, replacement, null);
        return true;
      }
    });
  }
  static Expression getCoreRight(final InfixExpression e) {
    return getCore(e.getRightOperand());
  }
  static Expression getCoreLeft(final InfixExpression e) {
    return getCore(e.getLeftOperand());
  }
  static Operator conjugate(final Operator o) {
    assert Is.deMorgan(o);
    return o.equals(CONDITIONAL_AND) ? CONDITIONAL_OR : CONDITIONAL_AND;
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final PrefixExpression e) {
        if (hasOpportunity(asNot(e)))
          $.add(new Range(e));
        return true;
      }
      private boolean hasOpportunity(final PrefixExpression e) {
        return e == null ? false : hasOpportunity(getCore(e.getOperand()));
      }
      private boolean hasOpportunity(final Expression inner) {
        return asNot(inner) != null || asAndOrOr(inner) != null || asComparison(inner) != null;
      }
    };
  }
}