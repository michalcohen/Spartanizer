package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.rebase;
import static org.spartan.refactoring.utils.Restructure.*;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

@SuppressWarnings("javadoc") public class Subject {
  public static Operand operand(final Expression inner) {
    return new Operand(inner);
  }
  public static Pair pair(final Expression left, final Expression right) {
    return new Pair(left, right);
  }
  public static Several operands(final Expression... operands) {
    return new Several(operands);
  }
  private static ParenthesizedExpression parenthesize(final Expression e) {
    final ParenthesizedExpression $ = e.getAST().newParenthesizedExpression();
    $.setExpression(duplicate(e));
    return $;
  }
  static Expression parenthesize(final int precedence, final Expression $) {
    return !Precedence.Is.legal(Precedence.of($)) || precedence >= Precedence.of($) ? $ : parenthesize($);
  }
  static Expression parenthesize(final Expression host, final Expression $) {
    return parenthesize(Precedence.of(host), $);
  }

  static class Claimer {
    protected final AST ast;
    public Claimer(final ASTNode n) {
      ast = n.getAST();
    }
    Expression claim(final Expression e) {
      return rebase(duplicate(Extract.core(e)), ast);
    }
  }

  public static class Operand  extends Claimer {
    private final Expression inner;
    Operand(final Expression inner) {
      super(inner);
      this.inner = claim(inner);
    }
    public Expression to(final PostfixExpression.Operator o) {
      final PostfixExpression $ = ast.newPostfixExpression();
      $.setOperator(o);
      $.setOperand(parenthesize(Precedence.of($), inner));
      return $;
    }
    public PrefixExpression to(final PrefixExpression.Operator o) {
      final PrefixExpression $ = ast.newPrefixExpression();
      $.setOperator(o);
      $.setOperand(parenthesize(Precedence.of($), inner));
      return $;
    }
  }

  public static class Pair extends Claimer {
    final Expression left, right;
    Pair(final Expression left, final Expression right) {
      super(left);
      this.left = claim(left);
      this.right = claim(right);
    }
    public Assignment to(final Assignment.Operator o) {
      final Assignment $ = ast.newAssignment();
      $.setOperator(o);
      $.setLeftHandSide(parenthesize($, left));
      $.setRightHandSide(parenthesize($, right));
      return $;
    }
    public InfixExpression to(final InfixExpression.Operator o) {
      final InfixExpression $ = ast.newInfixExpression();
      $.setOperator(o);
      $.setLeftOperand(parenthesize($, left));
      $.setRightOperand(parenthesize($, right));
      return $;
    }
    public ConditionalExpression toCondition(final Expression condition) {
      final ConditionalExpression $ = ast.newConditionalExpression();
      $.setExpression(parenthesize($, claim(condition)));
      $.setThenExpression(parenthesize($, left));
      $.setElseExpression(parenthesize($, right));
      return $;
    }
  }

  public static class Several {
    private final Expression[] operands;
    Several(final Expression... operands) {
      this.operands = operands;
    }
  }
}
