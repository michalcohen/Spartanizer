package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.asInfixExpression;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.getCore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

public enum Restructure {
  ;
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
  public static InfixExpression refitOperands(final InfixExpression e, final List<Expression> operands) {
    assert operands.size() >= 2;
    final InfixExpression $ = e.getAST().newInfixExpression();
    $.setOperator(e.getOperator());
    $.setLeftOperand(duplicate(operands.get(0)));
    $.setRightOperand(duplicate(operands.get(1)));
    operands.remove(0);
    operands.remove(0);
    if (!operands.isEmpty())
      for (final Expression operand : operands)
        $.extendedOperands().add(duplicate(operand));
    return $;
  }
}
