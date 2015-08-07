package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.ASTNode.*;
import static org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.METHOD_INVOCATION;
import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Restructure.getCore;
import static org.spartan.refactoring.utils.Restructure.parenthesize;
import static org.spartan.refactoring.utils.Restructure.refitOperands;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Precedence;

final class PushdownTernary extends Wring.OfConditionalExpression {
  private static int findSingleDifference(List<Expression> es1, List<Expression> es2) {
    int $ = -1;
    for (int i = 0; i < es1.size(); i++)
      if (!Wrings.same(es1.get(i), es2.get(i)))
        if ($ < 0)
          $ = i;
        else
          return -1;
    return $;
  }
  @SuppressWarnings("unchecked") private static <T extends Expression> T p(ASTNode e, T $) {
    return !Precedence.Is.legal(Precedence.of(e)) || Precedence.of(e) >= Precedence.of($) ? $ : (T) parenthesize($);
  }
  private static Expression pushdown(ConditionalExpression e, Assignment e1, Assignment e2) {
    if (e1.getOperator() != e2.getOperator() || !Wrings.same(e1.getLeftHandSide(), e2.getLeftHandSide()))
      return null;
    final Assignment $ = duplicate(e1);
    $.setRightHandSide(makeConditional(e, e1.getRightHandSide(), e2.getRightHandSide()));
    return p(e.getParent(), $);
  }
  private static Expression pushdown(ConditionalExpression e, FieldAccess e1, FieldAccess e2) {
    if (!Wrings.same(e1.getName(), e2.getName()))
      return null;
    System.out.println("Field access" + e1 + e2);
    final FieldAccess $ = duplicate(e1);
    $.setExpression(parenthesize(makeConditional(e, e1.getExpression(), e2.getExpression())));
    return $;
  }
  private static Expression pushdown(ConditionalExpression e, InfixExpression e1, InfixExpression e2) {
    if (e1.getOperator() != e2.getOperator())
      return null;
    List<Expression> es1 = All.operands(e1);
    List<Expression> es2 = All.operands(e2);
    if (es1.size() != es2.size())
      return null;
    int i = findSingleDifference(es1, es2);
    if (i < 0)
      return null;
    InfixExpression $ = duplicate(e1);
    List<Expression> operands = All.operands($);
    operands.remove(i);
    operands.add(i, p($, makeConditional(e, es1.get(i), es2.get(i))));
    return p(e, refitOperands($, operands));
  }
  private static Expression pushdown(ConditionalExpression e, MethodInvocation e1, MethodInvocation e2) {
    if (!Wrings.same(e1.getName(), e2.getName()) || !Wrings.same(e1.getExpression(), e2.getExpression()))
      return null;
    List<Expression> es1 = e1.arguments();
    List<Expression> es2 = e2.arguments();
    if (es1.size() != es2.size())
      return null;
    int i = findSingleDifference(es1, es2);
    if (i < 0)
      return null;
    MethodInvocation $ = duplicate(e1);
    $.arguments().remove(i);
    $.arguments().add(i, makeConditional(e, es1.get(i), es2.get(i)));
    return $;
  }
  private static Expression pushdown(ConditionalExpression e, ClassInstanceCreation e1, ClassInstanceCreation e2) {
    if (!Wrings.same(e1.getType(), e2.getType()) || !Wrings.same(e1.getExpression(), e2.getExpression()))
      return null;
    List<Expression> es1 = e1.arguments();
    List<Expression> es2 = e2.arguments();
    if (es1.size() != es2.size())
      return null;
    int i = findSingleDifference(es1, es2);
    if (i < 0)
      return null;
    ClassInstanceCreation $ = duplicate(e1);
    $.arguments().remove(i);
    $.arguments().add(i, makeConditional(e, es1.get(i), es2.get(i)));
    return $;
  }
  private Expression pushdown(ConditionalExpression e) {
    if (e == null)
      return null;
    Expression then = getCore(e.getThenExpression());
    Expression elze = getCore(e.getElseExpression());
    return Wrings.same(then, elze) ? null : pushdown(e, then, elze);
  }
  private Expression pushdown(ConditionalExpression e, Expression e1, Expression e2) {
    if (e1.getNodeType() != e2.getNodeType())
      return null;
    switch (e1.getNodeType()) {
      case METHOD_INVOCATION:
        return pushdown(e, (MethodInvocation) e1, (MethodInvocation) e2);
      case INFIX_EXPRESSION:
        return pushdown(e, (InfixExpression) e1, (InfixExpression) e2);
      case ASSIGNMENT:
        return pushdown(e, (Assignment) e1, (Assignment) e2);
      case FIELD_ACCESS:
        return pushdown(e, (FieldAccess) e1, (FieldAccess) e2);
      case CLASS_INSTANCE_CREATION:
        return pushdown(e, (ClassInstanceCreation) e1, (ClassInstanceCreation) e2);
      default:
        return null;
    }
  }
  @Override boolean _eligible(@SuppressWarnings("unused") final ConditionalExpression _) {
    return true;
  }
  @Override Expression _replacement(final ConditionalExpression e) {
    return pushdown(e);
  }
  @Override boolean scopeIncludes(final ConditionalExpression e) {
    return pushdown(e) != null;
  }
}