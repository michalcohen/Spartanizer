package org.spartan.refacotring.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

public enum All {
  ;
  public static List<Expression> operands(final InfixExpression e) {
    final List<Expression> $ = new ArrayList<Expression>();
    $.add(e.getLeftOperand());
    $.add(e.getRightOperand());
    if (e.hasExtendedOperands())
      $.addAll(e.extendedOperands());
    return $;
  }
}
