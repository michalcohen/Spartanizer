package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.rebase;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

@SuppressWarnings("javadoc") public class Subject {
  public static Subject.Operand operand(final Expression inner) {
    return new Operand(inner);
  }
  public static Subject.Several operands(final Expression... operands) {
    return new Subject.Several(operands);
  }
  private static ParenthesizedExpression parenthesize(final Expression e) {
    final ParenthesizedExpression $ = e.getAST().newParenthesizedExpression();
    $.setExpression(duplicate(e));
    return $;
  }
  static Expression parenthesize(final int precedence, final Expression $) {
    return !Precedence.Is.legal(Precedence.of($)) || precedence >= Precedence.of($) ? duplicate($) : parenthesize($);
  }
  static Expression parenthesize(final Expression host, final Expression $) {
    return parenthesize(Precedence.of(host), $);
  }

  public static class Operand {
    private final Expression inner;
    Operand(final Expression inner) {
      this.inner = inner;
    }
    public Expression to(final PostfixExpression.Operator o) {
      final PostfixExpression $ = inner.getAST().newPostfixExpression();
      $.setOperator(o);
      $.setOperand(parenthesize(Precedence.of($), inner));
      return $;
    }
    public PrefixExpression to(final PrefixExpression.Operator o) {
      final PrefixExpression $ = inner.getAST().newPrefixExpression();
      $.setOperator(o);
      $.setOperand(parenthesize(Precedence.of($), inner));
      return $;
    }
  }

  public static class Pair {
    final Expression left, right;
    public Pair(final Expression left, final Expression right) {
      this.left = left;
      this.right = right;
    }
    public Assignment to(final Assignment.Operator o) {
      final Assignment $ = left.getAST().newAssignment();
      $.setOperator(o);
      $.setLeftHandSide(parenthesize($, left));
      $.setRightHandSide(rebase(parenthesize($, right),$.getAST()));
      return $;
    }
    public InfixExpression to(final InfixExpression.Operator o) {
      final InfixExpression $ = left.getAST().newInfixExpression();
      $.setOperator(o);
      $.setLeftOperand(parenthesize($, left));
      $.setRightOperand(rebase(parenthesize($, right), $.getAST()));
      return $;
    }
    public ConditionalExpression toCondition(final Expression condition) {
      final ConditionalExpression $ = condition.getAST().newConditionalExpression();
      $.setExpression(parenthesize($, condition));
      $.setThenExpression(parenthesize($, left));
      $.setElseExpression(parenthesize($, right));
      return $;
    }
  }

  public static class Several {
    public Several(final Expression... operands) {
    }
  }
}
