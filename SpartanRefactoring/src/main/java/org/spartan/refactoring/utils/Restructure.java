package org.spartan.refactoring.utils;

import static org.eclipse.jdt.core.dom.ASTNode.PARENTHESIZED_EXPRESSION;
import static org.spartan.refactoring.utils.Funcs.asInfixExpression;
import static org.spartan.refactoring.utils.Funcs.duplicate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a word phrase.
 *
 * @author Yossi Gil
 * @since 2015-MM-DD
 *
 */
public enum Restructure {
  ;
  /**
   * Flatten the list of arguments to an {@link InfixExpression}, i.e., convert
   *
   * <pre>
   * (a + b) + c
   * </pre>
   *
   * to
   *
   * <pre>
   * a + b + c
   * </pre>
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
}
