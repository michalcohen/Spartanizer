package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.removeWhites;
import static org.spartan.utils.Utils.hasNull;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.spartan.refactoring.utils.Is;

/**
 * Various methods for comparing
 *
 * @author Yossi Gil
 * @since 2015-07-19
 */
public enum ExpressionComparator implements Comparator<Expression> {
  /**
   * Order on terms in addition: literals must be last. Sort literals by length.
   *
   * @author Yossi Gil
   * @since 2015-07-19
   */
  ADDITION {
    @Override public int compare(final Expression e1, final Expression e2) {
      int $;
      return ($ = literalCompare(e1, e2)) != 0 || ($ = nodesCompare(e1, e2)) != 0 || ($ = characterCompare(e1, e2)) != 0 || ($ = alphabeticalCompare(e1, e2)) != 0 ? $ : 0;
    }
  },
  /**
   * Order on terms in multiplication: literals must be last. Sort literals by
   * length.
   *
   * @author Yossi Gil
   * @since 2015-07-19
   */
  MULTIPLICATION {
    @Override public int compare(final Expression e1, final Expression e2) {
      int $;
      return ($ = literalCompare(e2, e1)) == 0 && ($ = nodesCompare(e1, e2)) == 0 && ($ = characterCompare(e1, e2)) == 0 && ($ = alphabeticalCompare(e1, e2)) == 0 ? 0 : $;
    }
  };
  static int literalCompare(final Expression e1, final Expression e2) {
    return asBit(Is.literal(e1)) - asBit(Is.literal(e2));
  }
  static int nodesCompare(final Expression e1, final Expression e2) {
    return round(countNodes(e1) - countNodes(e2), TOKEN_THRESHOLD);
  }
  static int argumentsCompare(final Expression e1, final Expression e2) {
    return !Is.methodInvocation(e1) || !Is.methodInvocation(e2) ? 0 : argumentsCompare((MethodInvocation) e1, (MethodInvocation) e2);
  }
  static int argumentsCompare(final MethodInvocation i1, final MethodInvocation i2) {
    return i1.arguments().size() - i2.arguments().size();
  }
  static boolean moreArguments(final Expression e1, final Expression e2) {
    return argumentsCompare(e1, e2) > 0;
  }
  static int characterCompare(final Expression e1, final Expression e2) {
    return countNonWhites(e1) - countNonWhites(e2);
  }
  static int alphabeticalCompare(final Expression e1, final Expression e2) {
    return removeWhites(e1).compareTo(removeWhites(e2));
  }
  static int round(final int $, final int threshold) {
    return Math.abs($) > threshold ? $ : 0;
  }
  static int asBit(final boolean b) {
    return b ? 1 : 0;
  }
  static boolean longerFirst(final InfixExpression e) {
    return isLonger(e.getLeftOperand(), e.getRightOperand());
  }
  static boolean isLonger(final Expression e1, final Expression e2) {
    return !hasNull(e1, e2) && (//
        countNodes(e1) > TOKEN_THRESHOLD + countNodes(e2) || //
        countNodes(e1) >= countNodes(e2) && moreArguments(e1, e2)//
        );
  }
  static final int TOKEN_THRESHOLD = 1;
  /**
   * Counts the number of non-space characters in a tree rooted at a given node
   *
   * @param n JD
   * @return Number of abstract syntax tree nodes under the parameter.
   */
  public static int countNonWhites(final ASTNode n) {
    return removeWhites(n).length();
  }
  /**
   * Counts the number of nodes in a tree rooted at a given node
   *
   * @param n JD
   * @return Number of abstract syntax tree nodes under the parameter.
   */
  public static int countNodes(final ASTNode n) {
    final AtomicInteger $ = new AtomicInteger(0);
    n.accept(new ASTVisitor() {
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
       * @param _ ignored
       */
      @Override public void preVisit(@SuppressWarnings("unused") final ASTNode _) {
        $.incrementAndGet();
      }
    });
    return $.get();
  }
}