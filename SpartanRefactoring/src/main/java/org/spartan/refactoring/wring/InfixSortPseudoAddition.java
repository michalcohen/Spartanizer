package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.spartan.utils.Utils.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} that sorts the arguments of an expression using the same
 * sorting order as {@link Operator#PLUS} expression, except that we do not
 * worry about commutativity. Unlike {@link InfixSortAddition}, we know that the
 * reordering is always possible.
 *
 * @see InfixSortAddition
 * @author Yossi Gil
 * @since 2015-07-17
 */
public final class InfixSortPseudoAddition extends Wring.InfixSorting {
  @Override boolean sort(final List<Expression> es) {
    return ExpressionComparator.ADDITION.sort(es);
  }
  @Override boolean scopeIncludes(final InfixExpression e) {
    return in(e.getOperator(), OR, XOR, AND);
  }
  @Override String description(final InfixExpression e) {
    return "Reorder operands of " + e.getOperator();
  }
}