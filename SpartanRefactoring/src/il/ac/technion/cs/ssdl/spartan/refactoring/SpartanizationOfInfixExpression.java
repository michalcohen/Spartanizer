package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.hasNull;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Yossi Gil
 * @since 2015/07/04
 */
abstract class SpartanizationOfInfixExpression extends Spartanization {
  public SpartanizationOfInfixExpression(final String name, final String message) {
    super(name, message);
  }

  public SpartanizationOfInfixExpression(final String name) {
    super(name);
  }

  /**
   * Makes an opposite operator from a given one, which keeps its logical
   * operation after the node swapping. e.g. "&" is commutative, therefore no
   * change needed. "<" isn't commutative, but it has its opposite: ">=".
   *
   * @param o
   *          The operator to flip
   * @return The correspond operator - e.g. "<=" will become ">", "+" will stay
   *         "+".
   */
  public static Operator flip(final Operator o) {
    return !conjugate.containsKey(o) ? o : conjugate.get(o);
  }

  private static Map<Operator, Operator> conjugate = makeConjeguates();

  private static Map<Operator, Operator> makeConjeguates() {
    final Map<Operator, Operator> $ = new HashMap<>();
    $.put(GREATER, LESS);
    $.put(LESS, GREATER);
    $.put(GREATER_EQUALS, LESS_EQUALS);
    $.put(LESS_EQUALS, GREATER_EQUALS);
    return $;
  }

  protected static InfixExpression flip(final AST t, final InfixExpression $, final InfixExpression e) {
    return remake($, duplicateLeft(t, e), flip(e.getOperator()), duplicateRight(t, e));
  }

  protected static void flip(final ASTRewrite r, final AST t, final InfixExpression e) {
    final InfixExpression $ = t.newInfixExpression();
    r.replace(e, flip(t, $, e), null); // Replace old tree with
  }

  protected static InfixExpression remake(final InfixExpression $, final Expression left, final InfixExpression.Operator o,
      final Expression right) {
    $.setRightOperand(left);
    $.setOperator(o);
    $.setLeftOperand(right);
    return $;
  }

  protected static InfixExpression duplicate(final AST t, final InfixExpression e) {
    return (InfixExpression) ASTNode.copySubtree(t, e);
  }

  protected static Expression duplicateLeft(final AST t, final InfixExpression e) {
    return duplicate(t, e.getLeftOperand());
  }

  protected static Expression duplicateRight(final AST t, final InfixExpression e) {
    return duplicate(t, e.getRightOperand());
  }

  protected static Expression duplicate(final AST t, final Expression e) {
    return (Expression) ASTNode.copySubtree(t, e);
  }

  /**
   * @param r
   *          ASTRewrite for the given AST
   * @param t
   *          the AST who is to own the new return statement
   * @param left
   *          the left expression
   * @param o
   *          the operator for the new infix expression
   * @param right
   *          the right expression
   * @return the new infix expression
   */
  public static InfixExpression makeInfixExpression(final ASTRewrite r, final AST t, final Expression left, final Operator o,
      final Expression right) {
    if (hasNull(t, r, o, right, left))
      return null;
    final InfixExpression $ = t.newInfixExpression();
    $.setLeftOperand(left.getParent() == null ? left : (Expression) r.createCopyTarget(left));
    $.setOperator(o);
    $.setRightOperand(right.getParent() == null ? right : (Expression) r.createCopyTarget(right));
    return $;
  }
}
