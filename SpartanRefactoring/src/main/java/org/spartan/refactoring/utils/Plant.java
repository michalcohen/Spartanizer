package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.duplicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

// TODO: document this class
@SuppressWarnings("javadoc") public class Plant {
  private final Expression inner;
  public Plant(final Expression inner) {
    this.inner = inner;
  }
  public Expression into(final ASTNode host) {
    return Precedence.greater(host, inner) || Precedence.equal(host, inner) && !Is.nonAssociative(host) ? inner : parenthesize(inner);
  }
  private ParenthesizedExpression parenthesize(final Expression e) {
    final ParenthesizedExpression $ = inner.getAST().newParenthesizedExpression();
    $.setExpression(duplicate(e));
    return $;
  }
}
