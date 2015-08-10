package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.rebase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
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
  public static Several operands(final Expression... es) {
    return new Several(Arrays.asList(es));
  }
  public static Several operands(final List<Expression> es) {
    return new Several(es);
  }


  static class Claimer {
    protected final AST ast;
    public Claimer(final ASTNode n) {
      ast = n.getAST();
    }
    Expression claim(final Expression e) {
      return rebase(duplicate(Extract.core(e)), ast);
    }
    private  ParenthesizedExpression parenthesize(final Expression e) {
      final ParenthesizedExpression $ = ast.newParenthesizedExpression();
      $.setExpression(duplicate(e));
      return $;
    }
     Expression parenthesize(final Expression host, final Expression $) {
      if (!Precedence.known($))
        return $;
      if (Precedence.of(host) > Precedence.of($))
        return $;
      if (Precedence.of(host) < Precedence.of($))
        return parenthesize($);
      if (Is.nonAssociative(host))
        return parenthesize($);
      return $;
    }
  }

  public static class Operand extends Claimer {
    private final Expression inner;
    Operand(final Expression inner) {
      super(inner);
      this.inner = claim(inner);
    }
    public Expression to(final PostfixExpression.Operator o) {
      final PostfixExpression $ = ast.newPostfixExpression();
      $.setOperator(o);
      $.setOperand(parenthesize($, inner));
      return $;
    }
    public PrefixExpression to(final PrefixExpression.Operator o) {
      final PrefixExpression $ = ast.newPrefixExpression();
      $.setOperator(o);
      $.setOperand(parenthesize($, inner));
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

  public static class Several extends Claimer {
    private final List<Expression> operands;

    public Several(final List<Expression> operands) {
      super(operands.get(0));
      this.operands = new ArrayList<>();
      for (final Expression e: operands)
        this.operands.add(claim(e));
    }
    public InfixExpression to(final InfixExpression.Operator o) {
      assert operands.size() >= 2;
      final InfixExpression $ = Subject.pair(operands.get(0), operands.get(1)).to(o);
        for (int i = 2; i < operands.size(); ++i)
          $.extendedOperands().add(operands.get(i));
      return $;
    }
  }
}
