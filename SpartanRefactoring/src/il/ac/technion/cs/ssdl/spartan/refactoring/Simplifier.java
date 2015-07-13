package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.countNodes;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.flip;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeParenthesizedExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makePrefixExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.hasNull;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.in;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import il.ac.technion.cs.ssdl.spartan.utils.Is;

/**
 * Reifying the notion of a simplifier; all concrete simplification are found in
 * the array returned by {@link #values()}.
 *
 * @author Yossi Gil
 * @since 2015-07-09
 *
 */
public abstract class Simplifier {
  /**
   * Find the first {@link Simplifier} appropriate for an
   * {@link InfixExpression}
   *
   * @param e
   *          JD
   * @return the first {@link Simplifier} for which the parameter is eligible,
   *         or <code><b>null</b></code>i if no such {@link Simplifier} is
   *         found.
   */
  public static Simplifier find(final InfixExpression e) {
    for (final Simplifier s : values())
      if (s.withinScope(e))
        return s;
    return null;
  }
  /**
   * Find the first {@link Simplifier} appropriate for a
   * {@link PrefixExpression}
   *
   * @param e
   *          JD
   * @return the first {@link Simplifier} for which the parameter is eligible,
   *         or <code><b>null</b></code>i if no such {@link Simplifier} is
   *         found.
   */
  public static Simplifier find(final PrefixExpression e) {
    for (final Simplifier s : values())
      if (s.withinScope(e))
        return s;
    return null;
  }
  /**
   * Determines whether this {@link Simplifier} object is applicable for a given
   * {@link InfixExpression} is within the "scope" of this . Note that it could
   * be the case that a {@link Simplifier} is applicable in principle to an
   * object, but that actual application will be vacuous.
   *
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object
   */
  public abstract boolean withinScope(InfixExpression e);
  /**
   * Determines whether this {@link Simplifier} object is applicable for a given
   * {@link PrefixExpression} is within the "scope" of this . Note that a
   * {@link Simplifier} is applicable in principle to an object, but that actual
   * application will be vacuous.
   *
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final InfixExpression e);
  final boolean noneligible(final InfixExpression e) {
    return !eligible(e);
  }
   abstract boolean withinScope(PrefixExpression e);
  /**
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final PrefixExpression e);
  abstract Expression replacement(final ASTRewrite r, final InfixExpression e);
  abstract Expression replacement(final ASTRewrite r, final PrefixExpression e);
  /**
   * Record a rewrite
   *
   * @param r
   *          JD
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> there is room for further
   *         simplification of this expression.
   */
  boolean go(final ASTRewrite r, final InfixExpression e) {
    if (eligible(e))
      r.replace(e, replacement(r, e), null);
    return true;
  }
  public static Simplifier[] values() {
    return values;
  }

  static final Simplifier comparisionWithBoolean = new OfInfixExpression() {
    @Override public final boolean withinScope(final InfixExpression e) {
      return in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS)
          && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
    }
    @Override boolean _eligible(final InfixExpression e) {
      assert withinScope(e);
      return true;
    }
    @Override Expression _replacement(final ASTRewrite r, final InfixExpression e) {
      Expression nonliteral;
      BooleanLiteral literal;
      if (Is.booleanLiteral(e.getLeftOperand())) {
        literal = (BooleanLiteral) e.getLeftOperand();
        nonliteral = (Expression) r.createMoveTarget(e.getRightOperand());
      } else {
        literal = (BooleanLiteral) e.getRightOperand();
        nonliteral = (Expression) r.createMoveTarget(e.getLeftOperand());
      }
      return nonNegating(e, literal) ? nonliteral : negate(r, nonliteral);
    }
    private PrefixExpression negate(final ASTRewrite r, final ASTNode e) {
      return makePrefixExpression(r, makeParenthesizedExpression(r, (Expression) e), PrefixExpression.Operator.NOT);
    }
    private boolean nonNegating(final InfixExpression e, final BooleanLiteral literal) {
      return literal.booleanValue() == (e.getOperator() == Operator.EQUALS);
    }
  };
  static final Simplifier comparisionWithSpecific = new OfInfixExpression() {
    @Override public boolean withinScope(final InfixExpression e) {
      return isComparison(e) && (hasThisOrNull(e) || hasOneSpecificArgument(e));
    }
    @Override boolean _eligible(final InfixExpression e) {
      return Is.specific(e.getLeftOperand());
    }
    @Override Expression _replacement(final ASTRewrite r, final InfixExpression e) {
      return flip(e);
    }
    boolean hasThisOrNull(final InfixExpression e) {
      return Is.thisOrNull(e.getLeftOperand()) || Is.thisOrNull(e.getRightOperand());
    }
    private boolean hasOneSpecificArgument(final InfixExpression e) {
      // One of the arguments must be specific, the other must not be.
      return Is.specific(e.getLeftOperand()) != Is.specific(e.getRightOperand());
    }
    boolean isComparison(final InfixExpression e) {
      return in(e.getOperator(), EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, NOT_EQUALS);
    }
  };
  static final Simplifier shortestOperandFirst = new OfInfixExpression() {
    @Override public final boolean withinScope(final InfixExpression e) {
      return Is.flipable(e.getOperator());
    }
    @Override public boolean _eligible(final InfixExpression e) {
      return longerFirst(e);
    }
    @Override protected Expression _replacement(final ASTRewrite r, final InfixExpression e) {
      return flip(e);
    }
  };
  private static final Simplifier[] values = new Simplifier[] { //
      comparisionWithBoolean, //
      comparisionWithSpecific, //
      shortestOperandFirst,//
  };
  static final int TOKEN_THRESHOLD = 1;

  static boolean longerFirst(final InfixExpression e) {
    return isLonger(e.getLeftOperand(), e.getRightOperand());
  }
  static boolean isLonger(final Expression e1, final Expression e2) {
    return !hasNull(e1, e2) && (//
    countNodes(e1) > TOKEN_THRESHOLD + countNodes(e2) || //
        countNodes(e1) >= countNodes(e2) && moreArguments(e1, e2)//
    );
  }
  static boolean moreArguments(final Expression e1, final Expression e2) {
    return Is.methodInvocation(e1) && Is.methodInvocation(e2) && moreArguments((MethodInvocation) e1, (MethodInvocation) e2);
  }
  static boolean moreArguments(final MethodInvocation i1, final MethodInvocation i2) {
    return i1.arguments().size() > i2.arguments().size();
  }

  static abstract class OfInfixExpression extends Simplifier {
    abstract boolean _eligible(final InfixExpression e);
    abstract Expression _replacement(final ASTRewrite r, final InfixExpression e);
    @Override final boolean eligible(final InfixExpression e) {
      assert withinScope(e);
      return _eligible(e);
    }
    @Override final Expression replacement(final ASTRewrite r, final InfixExpression e) {
      assert eligible(e);
      return _replacement(r, e);
    }
    @Override final boolean go(final ASTRewrite r, final InfixExpression e) {
      if (eligible(e))
        r.replace(e, replacement(r, e), null);
      return true;
    }
    @SuppressWarnings("unused") @Override public final boolean withinScope(final PrefixExpression _) {
      return false;
    }
    @Override final boolean eligible(@SuppressWarnings("unused") final PrefixExpression _) {
      return false;
    }
    @SuppressWarnings("unused") @Override final Expression replacement(final ASTRewrite r, final PrefixExpression _) {
      return null;
    }
  }
}
