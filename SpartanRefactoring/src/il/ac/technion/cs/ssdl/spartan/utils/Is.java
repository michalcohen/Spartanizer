package il.ac.technion.cs.ssdl.spartan.utils;

import static il.ac.technion.cs.ssdl.spartan.utils.As.asExpressionStatement;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.in;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.intIsIn;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.PLUS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.TIMES;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.XOR;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public enum Is {
  ;
  /**
   * @param n
   *          the statement or block to check if it is an assignment
   * @return true if it is an assignment or false if it is not or if the block
   *         Contains more than one statement
   */
  public static boolean assignment(final ASTNode n) {
    return block(n) ? assignment(asExpressionStatement(Funcs.getBlockSingleStmnt((Block) n)))
        : expressionStatement(n) && ASTNode.ASSIGNMENT == ((ExpressionStatement) n).getExpression().getNodeType();
  }

  /**
   * Determined if a node is a return statement
   *
   * @param n
   *          node to check
   * @return true if the given node is a block statement
   */
  public static boolean block(final ASTNode n) {
    return is(n, ASTNode.BLOCK);
  }

  public static boolean booleanLiteral(final ASTNode e) {
    return e != null && ASTNode.BOOLEAN_LITERAL == e.getNodeType();
  }

  /**
   * @param es
   *          expressions to check
   * @return true if one of the expressions is a conditional or parenthesized
   *         conditional expression or false otherwise
   */
  public static boolean conditional(final Expression... es) {
    for (final Expression e : es) {
      if (e == null)
        continue;
      switch (e.getNodeType()) {
        default:
          break;
        case ASTNode.CONDITIONAL_EXPRESSION:
          return true;
        case ASTNode.PARENTHESIZED_EXPRESSION:
          if (ASTNode.CONDITIONAL_EXPRESSION == ((ParenthesizedExpression) e).getExpression().getNodeType())
            return true;
      }
    }
    return false;
  }

  /**
   * Determined if a node is an "expression statement"
   *
   * @param n
   *          node to check
   * @return true if the given node is expression statement
   */
  public static boolean expressionStatement(final ASTNode n) {
    return is(n, ASTNode.EXPRESSION_STATEMENT);
  }

  /**
   * @param o
   *          The operator to check
   * @return True - if the operator have opposite one in terms of operands swap.
   */
  public static boolean flipable(final Operator o) {
    return in(o, //
        AND, //
        EQUALS, //
        GREATER, //
        GREATER_EQUALS, //
        LESS_EQUALS, //
        LESS, //
        NOT_EQUALS, //
        OR, //
        PLUS, // Too risky
        TIMES, //
        XOR, //
        null);
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is an infix expression or false otherwise
   */
  public static boolean infix(final ASTNode n) {
    return is(n, ASTNode.INFIX_EXPRESSION);
  }

  private static boolean is(final ASTNode n, final int type) {
    return n != null && type == n.getNodeType();
  }

  /**
   * Determine if a given node is a boolean literal
   *
   * @param n
   *          node to check
   * @return true if the given node is a boolean literal or false otherwise
   */
  public static boolean isBooleanLiteral(final ASTNode n) {
    return is(n, ASTNode.BOOLEAN_LITERAL);
  }

  /**
   * Determine whether a variable declaration is final or not
   *
   * @param v
   *          some declaration
   * @return true if the variable is declared as final
   */
  public static boolean isFinal(final VariableDeclarationStatement v) {
    return (Modifier.FINAL & v.getModifiers()) != 0;
  }

  /**
   * Determine whether an item is the last one in a list
   *
   * @param t
   *          a list item
   * @param ts
   *          a list
   *
   * @return <code><b>true</b></code> <i>iff</i> the item is found in the list
   *         and it is the last one in it.
   */
  public static <T> boolean isLast(final T t, final List<T> ts) {
    return ts.indexOf(t) == ts.size() - 1;
  }

  /**
   * @param n
   *          node to check
   * @return true if node is an Expression Statement of type Post or Pre
   *         Expression with ++ or -- operator false if node is not an
   *         Expression Statement or its a Post or Pre fix expression that its
   *         operator is not ++ or --
   */
  public static boolean isNodeIncOrDecExp(final ASTNode n) {
    switch (n.getNodeType()) {
      case ASTNode.EXPRESSION_STATEMENT:
        return isNodeIncOrDecExp(((ExpressionStatement) n).getExpression());
      case ASTNode.POSTFIX_EXPRESSION:
        return in(((PostfixExpression) n).getOperator(), PostfixExpression.Operator.INCREMENT,
            PostfixExpression.Operator.DECREMENT);
      case ASTNode.PREFIX_EXPRESSION:
        return in(((PrefixExpression) n).getOperator(), PrefixExpression.Operator.INCREMENT, PrefixExpression.Operator.DECREMENT);
      default:
        return false;
    }
  }

  private static boolean isOneOf(final int i, final int... is) {
    for (final int j : is)
      if (i == j)
        return true;
    return false;
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a prefix expression or false otherwise
   */
  public static boolean isPrefix(final ASTNode n) {
    return is(n, ASTNode.PREFIX_EXPRESSION);
  }

  /**
   * Determined if a node is a return statement
   *
   * @param n
   *          node to check
   * @return true if the given node is a return statement or false otherwise
   */
  public static boolean isReturn(final ASTNode n) {
    return is(n, ASTNode.RETURN_STATEMENT);
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a variable declaration statement or false
   *         otherwise
   */
  public static boolean isVarDeclStmt(final ASTNode n) {
    return is(n, ASTNode.VARIABLE_DECLARATION_STATEMENT);
  }

  /**
   * @param n
   *          Expression node
   * @return true if the Expression is literal
   */
  public static boolean literal(final ASTNode n) {
    return intIsIn(n.getNodeType(), //
        ASTNode.NULL_LITERAL, //
        ASTNode.CHARACTER_LITERAL, //
        ASTNode.NUMBER_LITERAL, //
        ASTNode.STRING_LITERAL, //
        ASTNode.BOOLEAN_LITERAL //
    );
  }

  /**
   * @param r
   *          Return Statement node
   * @return true if the ReturnStatement is of literal type
   */
  public static boolean literal(final ReturnStatement r) {
    return literal(r.getExpression());
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a method invocation or false otherwise
   */
  public static boolean methodInvocation(final ASTNode n) {
    return is(n, ASTNode.METHOD_INVOCATION);
  }

  /**
   * Determine whether the type of an {@link ASTNode} node is one of given list
   *
   * @param n
   *          a node
   * @param types
   *          a list of types
   * @return <code><b>true</b></code> <i>iff</i> function #ASTNode.getNodeType
   *         returns one of the types provided as parameters
   */
  public static boolean oneOf(final ASTNode n, final int... types) {
    return n == null ? false : isOneOf(n.getNodeType(), types);
  }

  /**
   * @param a
   *          the assignment who's operator we want to check
   * @return true is the assignment's operator is assign
   */
  public static boolean plainAssignment(final Assignment a) {
    return a != null && a.getOperator() == Assignment.Operator.ASSIGN;
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a string literal or false otherwise
   */
  public static boolean stringLiteral(final ASTNode n) {
    return n != null && n.getNodeType() == ASTNode.STRING_LITERAL;
  }
}
