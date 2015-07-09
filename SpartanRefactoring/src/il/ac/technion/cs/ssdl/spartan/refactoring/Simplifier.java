package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.countNodes;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.flip;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeParenthesizedExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makePrefixExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.hasNull;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.in;
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
   * Determines whether this {@link Simplifier} object is applicable for a given
   * {@InfixExpression} is within the "scope" of this . Note that it could be
   * the case that a {@link Simplifier} is applicable in principle to an object,
   * but that actual application will be vacuous.
   *
   * @param e
   *          arbitrary
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object
   */
  public abstract boolean withinScope(InfixExpression e);

  /**
   * @param e
   *          some arbitrary value
   * @return <code><b>true</b></code> <i>iff</i> the argument is legible for the
   *         simplification offered by this object.
   */
  public abstract boolean eligible(InfixExpression e);

  /**
   * Record a rewrite
   *
   * @param r
   *          John Doe
   * @param e
   *          Hane Doe
   * @return <code><b>true</b></code> <i>iff</i> there is room for further
   *         simplification of this expression.
   */
  public final boolean go(final ASTRewrite r, final InfixExpression e) {
    if (!eligible(e))
      return true;
    r.replace(e, replacement(r, e), null);
    return true;
  }

  protected abstract Expression replacement(ASTRewrite r, final InfixExpression e);

  public static Simplifier[] values() {
    return values;
  }

  private static final Simplifier[] values = new Simplifier[] { //
      // Comparison with boolean
      new Simplifier() {
        @Override public final boolean withinScope(final InfixExpression e) {
          return in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS)
              && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
        }

        @Override public boolean eligible(final InfixExpression e) {
          assert withinScope(e);
          return true;
        }

        @Override protected Expression replacement(final ASTRewrite r, final InfixExpression e) {
          assert eligible(e);
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
      },
      // Compare with specific
      new Simplifier() {
        @Override public boolean withinScope(final InfixExpression e) {
          return isComparison(e) && (hasThisOrNull(e) || hasOneSpecificArgument(e));
        }

        @Override public boolean eligible(final InfixExpression e) {
          assert withinScope(e);
          return isSpecific(e.getLeftOperand());
        }

        @Override protected Expression replacement(final ASTRewrite r, final InfixExpression e) {
          return flip(e);
        }

        boolean hasThisOrNull(final InfixExpression e) {
          return isThisOrNull(e.getLeftOperand()) || isThisOrNull(e.getRightOperand());
        }

        private boolean hasOneSpecificArgument(final InfixExpression e) {
          // One of the arguments must be specific, the other must not be.
          return isSpecific(e.getLeftOperand()) != isSpecific(e.getRightOperand());
        }

        boolean isComparison(final InfixExpression e) {
          return in(e.getOperator(), EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, NOT_EQUALS);
        }

        boolean isSpecific(final Expression e) {
          return Is.oneOf(e, CHARACTER_LITERAL, NUMBER_LITERAL, NULL_LITERAL, THIS_EXPRESSION);
        }

        boolean isThisOrNull(final Expression e) {
          return Is.oneOf(e, NULL_LITERAL, THIS_EXPRESSION);
        }
      },
      // Longest first
      new Simplifier() {
        @Override public final boolean withinScope(final InfixExpression e) {
          return Is.flipable(e.getOperator());
        }

        @Override public boolean eligible(final InfixExpression e) {
          assert withinScope(e);
          return longerFirst(e);
        }

        @Override protected Expression replacement(final ASTRewrite r, final InfixExpression e) {
          assert eligible(e);
          return flip(e);
        }
      }, };
  private static final int TOKEN_THRESHOLD = 1;
  private static final int CHARACTER_THRESHOLD = 2;

  static boolean longerFirst(final InfixExpression n) {
    return isLonger(n.getLeftOperand(), n.getRightOperand());
  }

  static boolean isLonger(final Expression e1, final Expression e2) {
    if (hasNull(e1, e2))
      return false;
    final boolean tokenWiseGreater = countNodes(e1) > TOKEN_THRESHOLD + countNodes(e2);
    final boolean characterWiseGreater = e1.getLength() > CHARACTER_THRESHOLD + e2.getLength();
    if (tokenWiseGreater && characterWiseGreater)
      return true;
    if (!tokenWiseGreater && !characterWiseGreater)
      return false;
    return moreArguments(e1, e2);
  }

  private static boolean moreArguments(final Expression e1, final Expression e2) {
    return Is.methodInvocation(e1) && Is.methodInvocation(e2) && moreArguments((MethodInvocation) e1, (MethodInvocation) e2);
  }

  static boolean moreArguments(final MethodInvocation i1, final MethodInvocation i2) {
    return i1.arguments().size() > i2.arguments().size();
  }
}
