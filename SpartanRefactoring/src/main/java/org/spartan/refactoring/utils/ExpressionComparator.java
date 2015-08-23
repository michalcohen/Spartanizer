package org.spartan.refactoring.utils;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.removeWhites;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.utils.Utils.hasNull;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
   * Order on terms in addition: except that we do not sort alphabetically
   *
   * @author Yossi Gil
   * @since 2015-07-19
   */
  PRUDENT {
    @Override public int compare(final Expression e1, final Expression e2) {
      int $;
      return ($ = literalCompare(e1, e2)) != 0 || ($ = nodesCompare(e1, e2)) != 0 || ($ = characterCompare(e1, e2)) != 0 ? $ : 0;
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
    return round(nodesCount(e1) - nodesCount(e2), NODES_THRESHOLD);
  }
  static int argumentsCompare(final Expression e1, final Expression e2) {
    return !Is.methodInvocation(e1) || !Is.methodInvocation(e2) ? 0 : argumentsCompare((MethodInvocation) e1, (MethodInvocation) e2);
  }
  static int argumentsCompare(final MethodInvocation i1, final MethodInvocation i2) {
    return i1.arguments().size() - i2.arguments().size();
  }
  /**
   * Compare method invocations by the number of arguments
   *
   * @param e1 JD
   * @param e2 JD
   * @return <code><b>true</b></code> <i>iff</i> the first argument is a method
   *         invocation with more arguments that the second argument
   */
  public static boolean moreArguments(final Expression e1, final Expression e2) {
    return argumentsCompare(e1, e2) > 0;
  }
  /**
   * Compare expressions by their number of characters
   *
   * @param e1 JD
   * @param e2 JD
   * @return an integer which is either negative, zero, or positive, if the
   *         number of characters in the first argument is less than, equal to,
   *         or greater than the number of characters in the second argument.
   */
  static int characterCompare(final Expression e1, final Expression e2) {
    return countNonWhites(e1) - countNonWhites(e2);
  }
  /**
   * Lexicographical comparison expressions by their number of characters
   *
   * @param e1 JD
   * @param e2 JD
   * @return an integer which is either negative, zero, or positive, if the
   *         number of characters in the first argument occurs before, at the
   *         same place, or after then the second argument in lexicographical
   *         order.
   */
  static int alphabeticalCompare(final Expression e1, final Expression e2) {
    return removeWhites(e1).compareTo(removeWhites(e2));
  }
  static int round(final int $, final int threshold) {
    return Math.abs($) > threshold ? $ : 0;
  }
  static int asBit(final boolean b) {
    return b ? 1 : 0;
  }
  /**
   * Compare the length of the left and right arguments of an infix expression
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> if the left operand of the
   *         parameter is is longer than the second argument
   */
  public static boolean longerFirst(final InfixExpression e) {
    return isLonger(left(e), right(e));
  }
  private static boolean isLonger(final Expression e1, final Expression e2) {
    return !hasNull(e1, e2) && (//
    nodesCount(e1) > NODES_THRESHOLD + nodesCount(e2) || //
        nodesCount(e1) >= nodesCount(e2) && moreArguments(e1, e2)//
    );
  }
  /**
   * Threshold for comparing nodes; a difference in the number of nodes between
   * two nodes is considered zero, if it is the less than this value,
   */
  public static final int NODES_THRESHOLD = 1;
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
  public static int nodesCount(final ASTNode n) {
    class Integer {
      int inner = 0;
    }
    final Integer $ = new Integer();
    n.accept(new ASTVisitor() {
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
       * @param _ ignored
       */
      @Override public void preVisit(@SuppressWarnings("unused") final ASTNode _) {
        $.inner++;
      }
    });
    return $.inner;
  }
}