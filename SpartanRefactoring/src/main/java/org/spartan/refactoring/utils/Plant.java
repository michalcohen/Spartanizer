package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.duplicate;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

public enum Plant {
  ;
  public static Hook zis(final Expression $) {
    return new Hook($);
  }

  public static class Hook {
    private final Expression inner;
    public Hook(final Expression inner) {
      this.inner = inner;
    }
    Expression into(final Expression host) {
      return Precedence.greater(host, inner) || Precedence.equal(host, inner) && !Is.nonAssociative(host) ? inner : parenthesize(inner);
    }
    private ParenthesizedExpression parenthesize(final Expression e) {
      final ParenthesizedExpression $ = inner.getAST().newParenthesizedExpression();
      $.setExpression(duplicate(e));
      return $;
    }
  }
}
