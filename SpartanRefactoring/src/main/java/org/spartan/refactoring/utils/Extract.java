package org.spartan.refactoring.utils;

import static org.eclipse.jdt.core.dom.ASTNode.*;
import static org.spartan.refactoring.utils.Funcs.*;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a sentence phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-28
 */
public enum Extract {
  ;
  /**
   * @param n a node to extract an expression from
   * @return null if the statement is not an expression or return statement or
   *         the expression if they are
   */
  public static Expression returnExpression(final ASTNode n) {
    final ReturnStatement $ = asReturnStatement(Extract.singleStatement(n));
    return $ == null ? null : $.getExpression();
  }
  /**
   * @param node a node to extract an expression from
   * @return null if the statement is not an expression or return statement or
   *         the expression if they are
   */
  public static Expression expression(final ASTNode node) {
    if (node == null)
      return null;
    switch (node.getNodeType()) {
      case ASTNode.EXPRESSION_STATEMENT:
        return ((ExpressionStatement) node).getExpression();
      case ASTNode.RETURN_STATEMENT:
        return ((ReturnStatement) node).getExpression();
      default:
        return null;
    }
  }
  /**
   * @param n JD
   * @return the method invocation if it exists or null if it doesn't or if the
   *         block contains more than one statement
   */
  public static MethodInvocation methodInvocation(final ASTNode n) {
    return asMethodInvocation(Extract.expressionStatement(n).getExpression());
  }
  /**
   * @param n a statement or block to extract the assignment from
   * @return null if the block contains more than one statement or if the
   *         statement is not an assignment or the assignment if it exists
   */
  public static Assignment assignment(final ASTNode n) {
    return asAssignment(Extract.expressionStatement(n));
  }
  /**
   * Convert, is possible, an {@link ASTNode} to a {@link ExpressionStatement}
   *
   * @param n a statement or a block to extract the expression statement from
   * @return the expression statement if n is a block or an expression statement
   *         or null if it not an expression statement or if the block contains
   *         more than one statement
   */
  public static ExpressionStatement expressionStatement(final ASTNode n) {
    return n == null ? null : asExpressionStatement(Extract.singleStatement(n));
  }
  /**
   * Extract the list of non-empty statements embedded in the first level of a
   * node.
   *
   * @param n JD
   * @return the list of such statements.
   */
  public static List<Statement> statements(final ASTNode n) {
    final List<Statement> $ = new ArrayList<>();
    return n == null || !(n instanceof Statement) ? $ : Extract.statementsInto((Statement) n, $);
  }
  private static List<Statement> statementsInto(final Statement s, final List<Statement> $) {
    int nodeType = s.getNodeType();
    switch (nodeType) {
      case EMPTY_STATEMENT:
        return $;
      case BLOCK:
        return Extract.statementsInto((Block) s, $);
      default:
        $.add(s);
        return $;
    }
  }
  private static List<Statement> statementsInto(final Block b, final List<Statement> $) {
    for (final Object statement : b.statements())
      Extract.statementsInto((Statement) statement, $);
    return $;
  }
  /**
   * @param n JD
   * @return if b is a block with just 1 statement it returns that statement, if
   *         b is statement it returns b and if b is null it returns a null
   */
  public static Statement singleStatement(final ASTNode n) {
    final List<Statement> $ = Extract.statements(n);
    return $.size() != 1 ? null : (Statement) $.get(0);
  }
}
