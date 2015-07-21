package org.spartan.refactoring.utils;

import static org.eclipse.jdt.core.dom.ASTNode.PARENTHESIZED_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.spartan.refactoring.utils.Funcs.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * An empty <code><b>enum</b></code> with a variety of <code>public
 * static</code> functions for restructuring expressions.
 *
 * @author Yossi Gil
 * @since 2015-07-21
 *
 */
public enum Restructure {
  ;
  /**
   * Compute the list of statements within a statement.
   *
   * @param s
   *          JD
   * @return a flattened list of all {@link Statement}s found within the
   *         parameter.
   */
  public static List<Statement> flatten(final Statement s) {
    final ArrayList<Statement> $ = new ArrayList<>();
    return flattenInto(s, $);
  }
  private static List<Statement> flattenInto(final Statement s, final List<Statement> $) {
    if (Is.block(s))
      return flattenInto(asBlock(s), $);
    if (Is.emptyStatement(s))
      return $;
    return add(s, $);
  }
  private static List<Statement> add(final Statement s, final List<Statement> $) {
    $.add(s);
    return $;
  }
  private static List<Statement> flattenInto(final Block b, final List<Statement> $) {
    for (final Object o : b.statements())
      flattenInto((Statement) o, $);
    return $;
  }
  /**
   * Flatten the list of arguments to an {@link InfixExpression}, e.g., convert
   * an expression such as <code>(a + b) + c</code> whose inner form is roughly
   * "+(+(a,b),c)", into <code> a + b + c </code>, whose inner form is (roughly)
   * "+(a,b,c)".
   *
   * @param $
   *          JD
   * @return a duplicate of the argument, with the a flattened list of operands.
   */
  public static InfixExpression flatten(final InfixExpression $) {
    return refitOperands(duplicate($), flattenInto($.getOperator(), All.operands($), new ArrayList<Expression>()));
  }
  private static List<Expression> flattenInto(final Operator o, final List<Expression> es, final List<Expression> $) {
    for (final Expression e : es)
      flattenInto(o, e, $);
    return $;
  }
  private static List<Expression> flattenInto(final Operator o, final Expression e, final List<Expression> $) {
    final Expression core = getCore(e);
    if (Is.infix(core) && asInfixExpression(core).getOperator() == o)
      return flattenInto(o, All.operands(asInfixExpression(core)), $);
    return add(Is.simple(core) ? core : e, $);
  }
  private static List<Expression> add(final Expression e, final List<Expression> $) {
    $.add(e);
    return $;
  }
  /**
   * Replace the list of arguments of a given @link {@link InfixExpression}
   *
   * @param e
   *          JD
   * @param es
   *          JD
   * @return a duplicate of the {@link InfixExpression} parameter, whose
   *         operands are the {@link List} of {@link Expression} parameter.
   */
  public static InfixExpression refitOperands(final InfixExpression e, final List<Expression> es) {
    assert es.size() >= 2;
    final InfixExpression $ = e.getAST().newInfixExpression();
    $.setOperator(e.getOperator());
    $.setLeftOperand(duplicate(es.get(0)));
    $.setRightOperand(duplicate(es.get(1)));
    es.remove(0);
    es.remove(0);
    if (!es.isEmpty())
      for (final Expression operand : es)
        $.extendedOperands().add(duplicate(operand));
    return $;
  }
  /**
   * Find the "core" of a given {@link Expression}, by peeling of any
   * parenthesis that may wrap it.
   *
   * @param $
   *          JD
   * @return the parameter itself, if not parenthesized, or the result of
   *         applying this function (@link {@link #getClass()}) to whatever is
   *         wrapped in these parenthesis.
   */
  public static Expression getCore(final Expression $) {
    return PARENTHESIZED_EXPRESSION != $.getNodeType() ? $ : getCore(((ParenthesizedExpression) $).getExpression());
  }
  /**
   * Parenthesize an expression (if necessary).
   *
   * @param e
   *          JD
   * @return a {@link Funcs#duplicate(Expression)} of the parameter wrapped in
   *         parenthesis.
   */
  public static Expression parenthesize(final Expression e) {
    if (Is.simple(e))
      return duplicate(e);
    final ParenthesizedExpression $ = e.getAST().newParenthesizedExpression();
    $.setExpression(e.getParent() == null ? e : duplicate(e));
    return $;
  }
  /**
   * Compute the "de Morgan" conjugate of an operator.
   *
   * @param o
   *          must be either {@link Operator#CONDITIONAL_AND} or
   *          {@link Operator#CONDITIONAL_OR}
   * @return {@link Operator#CONDITIONAL_AND} if the parameter is
   *         {@link Operator#CONDITIONAL_OR}, or {@link Operator#CONDITIONAL_OR}
   *         if the parameter is {@link Operator#CONDITIONAL_AND}
   * @see Restructure#conjugate(InfixExpression)
   */
  public static Operator conjugate(final Operator o) {
    assert Is.deMorgan(o);
    return o.equals(CONDITIONAL_AND) ? CONDITIONAL_OR : CONDITIONAL_AND;
  }
  /**
   * Compute the "de Morgan" conjugate of the operator present on an
   * {@link InfixExpression}.
   *
   * @param e
   *          an expression whose operator is either
   *          {@link Operator#CONDITIONAL_AND} or
   *          {@link Operator#CONDITIONAL_OR}
   * @return {@link Operator#CONDITIONAL_AND} if the operator present on the
   *         parameter is {@link Operator#CONDITIONAL_OR}, or
   *         {@link Operator#CONDITIONAL_OR} if this operator is
   *         {@link Operator#CONDITIONAL_AND}
   * @see Restructure#conjugate(Operator)
   */
  public static Operator conjugate(final InfixExpression e) {
    return conjugate(e.getOperator());
  }
}
