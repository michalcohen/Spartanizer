package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.ASTNode.ASSIGNMENT;
import static org.eclipse.jdt.core.dom.ASTNode.CLASS_INSTANCE_CREATION;
import static org.eclipse.jdt.core.dom.ASTNode.FIELD_ACCESS;
import static org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.METHOD_INVOCATION;
import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Funcs.makeConditional;
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
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Precedence;

final class CollapseTernary extends Wring.OfConditionalExpression {
  private Expression collapse(final ConditionalExpression e) {
    if (e == null)
      return null;
    Expression $;
    return ($ = collapseOnElse(e)) != null || ($ = collaspeOnThen(e)) != null ? $ : null;
  }
  private Expression collaspeOnThen(ConditionalExpression e) {
    final ConditionalExpression then = asConditionalExpression(getCore(e.getThenExpression()));
    if (then == null)
      return null;
    final Expression elze = getCore(e.getElseExpression());
    final Expression thenThen = getCore(then.getThenExpression());
    final Expression thenElse = getCore(then.getElseExpression());
    if (same(thenElse, elze))
      return makeConditionalExpression(Wrings.makeAND(e.getExpression(), then.getExpression()), thenThen, elze);
    if (same(thenThen, elze))
      return makeConditionalExpression(Wrings.makeAND((e.getExpression()), Wrings.not(then.getExpression())), thenElse, elze);
    return null;
  }
  private Expression collapseOnElse(ConditionalExpression e) {
    final ConditionalExpression elze = asConditionalExpression(getCore(e.getElseExpression()));
    if (elze == null)
      return null;
    final Expression then = getCore(e.getThenExpression());
    final Expression elseThen = getCore(elze.getThenExpression());
    final Expression elseElse = getCore(elze.getElseExpression());
    if (same(then, elseThen))
      return makeConditionalExpression(Wrings.makeAND(Wrings.not(e.getExpression()), elze.getExpression()), elseElse, elze);
    if (same(then, elseElse))
      return makeConditionalExpression(Wrings.makeAND(Wrings.not(e.getExpression()), Wrings.not(elze.getExpression())), elseThen, elseElse);
    return null;
  }
  @Override boolean _eligible(@SuppressWarnings("unused") final ConditionalExpression _) {
    return true;
  }
  @Override Expression _replacement(final ConditionalExpression e) {
    return collapse(e);
  }
  @Override boolean scopeIncludes(final ConditionalExpression e) {
    return collapse(e) != null;
  }
}