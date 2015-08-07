package org.spartan.refactoring.utils;

import static org.eclipse.jdt.core.dom.ASTNode.ARRAY_CREATION;
import static org.eclipse.jdt.core.dom.ASTNode.ASSIGNMENT;
import static org.eclipse.jdt.core.dom.ASTNode.BLOCK;
import static org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.BREAK_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.CONDITIONAL_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.CONTINUE_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.EMPTY_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.EXPRESSION_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.INSTANCEOF_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.METHOD_INVOCATION;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.PARENTHESIZED_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.POSTFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.PREFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.RETURN_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.THIS_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.THROW_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.VARIABLE_DECLARATION_STATEMENT;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
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
import static org.spartan.refactoring.utils.Funcs.asInfixExpression;
import static org.spartan.refactoring.utils.Funcs.asPrefixExpression;
import static org.spartan.utils.Utils.in;
import static org.spartan.utils.Utils.intIsIn;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a sentence phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 */
public enum Is {
  ;
  /**
   * Determine whether a variable declaration is final or not
   *
   * @param v some declaration
   * @return <code><b>true</b></code> <i>iff</i> the variable is declared as
   *         final
   */
  public static boolean _final(final VariableDeclarationStatement v) {
    return (Modifier.FINAL & v.getModifiers()) != 0;
  }
  /**
   * @param n the statement or block to check if it is an assignment
   * @return <code><b>true</b></code> if the parameter an assignment or false if
   *         the parameter not or if the block Contains more than one statement
   */
  public static boolean assignment(final ASTNode n) {
    return is(n, ASSIGNMENT);
  }
  /**
   * Determine whether a node is a {@link Block}
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a block
   *         statement
   */
  public static boolean block(final ASTNode n) {
    return is(n, BLOCK);
  }
  /**
   * Determine whether a node is a {@link Block}
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a block
   *         statement
   */
  public static boolean statement(final ASTNode n) {
    return n instanceof Statement;
  }
  /**
   * Determine whether a node is a boolean literal
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a boolean
   *         literal
   */
  public static boolean booleanLiteral(final ASTNode n) {
    return is(n, BOOLEAN_LITERAL);
  }
  /**
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a comparison
   *         expression.
   */
  public static boolean comparison(final InfixExpression e) {
    return in(e.getOperator(), EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, NOT_EQUALS);
  }
  /**
   * @param es JD
   * @return <code><b>true</b></code> <i>iff</i> one of the parameters is a
   *         conditional or parenthesized conditional expression
   */
  public static boolean conditional(final Expression... es) {
    for (final Expression e : es) {
      if (e == null)
        continue;
      switch (e.getNodeType()) {
        default:
          break;
        case CONDITIONAL_EXPRESSION:
          return true;
        case PARENTHESIZED_EXPRESSION:
          if (CONDITIONAL_EXPRESSION == ((ParenthesizedExpression) e).getExpression().getNodeType())
            return true;
      }
    }
    return false;
  }
  /**
   * Check whether an expression is a "conditional and" (&&)
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an expression
   *         whose operator is
   *         {@link org.eclipse.jdt.core.dom.InfixExpression.Operator#CONDITIONAL_AND}
   */
  public static boolean conditionalAnd(final InfixExpression e) {
    return e.getOperator() == CONDITIONAL_AND;
  }
  /**
   * Check whether an expression is a "conditional or" (||)
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an expression
   *         whose operator is
   *         {@link org.eclipse.jdt.core.dom.InfixExpression.Operator#CONDITIONAL_OR}
   */
  public static boolean conditionalOr(final InfixExpression e) {
    return e.getOperator() == CONDITIONAL_OR;
  }
  /**
   * Check whether the operator of an expression is susceptible for applying one
   * of the two de Morgan laws.
   *
   * @param e InfixExpression
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an operator on
   *         which the de Morgan laws apply.
   */
  public static boolean deMorgan(final InfixExpression e) {
    return Is.deMorgan(e.getOperator());
  }
  /**
   * Check whether an operator is susceptible for applying one of the two de
   * Morgan laws.
   *
   * @param o JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an operator on
   *         which the de Morgan laws apply.
   */
  public static boolean deMorgan(final Operator o) {
    return in(o, CONDITIONAL_AND, CONDITIONAL_OR);
  }
  /**
   * Determine whether a node is an {@link EmptyStatement}
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an
   *         {@link EmptyStatement}
   */
  public static boolean emptyStatement(final ASTNode n) {
    return is(n, EMPTY_STATEMENT);
  }
  /**
   * Determine whether a node is an "expression statement"
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an
   *         {@link ExpressionStatement} statement
   */
  public static boolean expression(final ASTNode n) {
    return n != null && n instanceof Expression;
  }
  /**
   * Determine whether a node is an "expression statement"
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an
   *         {@link ExpressionStatement} statement
   */
  public static boolean expressionStatement(final ASTNode n) {
    return is(n, EXPRESSION_STATEMENT);
  }
  /**
   * @param o The operator to check
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
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an infix
   *         expression.
   */
  public static boolean infix(final ASTNode n) {
    return is(n, INFIX_EXPRESSION);
  }
  /**
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the node is an Expression
   *         Statement of type Post or Pre Expression with ++ or -- operator
   *         false if node is not an Expression Statement or its a Post or Pre
   *         fix expression that its operator is not ++ or --
   */
  public static boolean isNodeIncOrDecExp(final ASTNode n) {
    switch (n.getNodeType()) {
      case EXPRESSION_STATEMENT:
        return isNodeIncOrDecExp(((ExpressionStatement) n).getExpression());
      case POSTFIX_EXPRESSION:
        return in(((PostfixExpression) n).getOperator(), PostfixExpression.Operator.INCREMENT, PostfixExpression.Operator.DECREMENT);
      case PREFIX_EXPRESSION:
        return in(asPrefixExpression(n).getOperator(), PrefixExpression.Operator.INCREMENT, PrefixExpression.Operator.DECREMENT);
      default:
        return false;
    }
  }
  /**
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a variable
   *         declaration statement.
   */
  public static boolean isVarDeclStmt(final ASTNode n) {
    return is(n, VARIABLE_DECLARATION_STATEMENT);
  }
  /**
   * Determine whether an item is the last one in a list
   *
   * @param t a list item
   * @param ts a list
   * @return <code><b>true</b></code> <i>iff</i> the item is found in the list
   *         and it is the last one in it.
   */
  public static <T> boolean last(final T t, final List<T> ts) {
    return ts.indexOf(t) == ts.size() - 1;
  }
  /**
   * @param n Expression node
   * @return <code><b>true</b></code> <i>iff</i> the Expression is literal
   */
  public static boolean literal(final ASTNode n) {
    return intIsIn(n.getNodeType(), //
        NULL_LITERAL, //
        CHARACTER_LITERAL, //
        NUMBER_LITERAL, //
        STRING_LITERAL, //
        BOOLEAN_LITERAL //
    );
  }
  /**
   * @param r Return Statement node
   * @return <code><b>true</b></code> <i>iff</i> the ReturnStatement is of
   *         literal type
   */
  public static boolean literal(final ReturnStatement r) {
    return literal(r.getExpression());
  }
  /**
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a method
   *         invocation.
   */
  public static boolean methodInvocation(final ASTNode n) {
    return is(n, METHOD_INVOCATION);
  }
  /**
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an expression
   *         whose type is provably not of type {@link String}, in the sense
   *         used in applying the <code>+</code> operator to concatenate
   *         strings. concatenation.
   */
  public static boolean notString(final Expression e) {
    return intIsIn(e.getNodeType(), //
        NULL_LITERAL, // null + null is an error, not a string.
        CHARACTER_LITERAL, //
        NUMBER_LITERAL, //
        BOOLEAN_LITERAL, //
        PREFIX_EXPRESSION, //
        INFIX_EXPRESSION, //
        ARRAY_CREATION, //
        INSTANCEOF_EXPRESSION//
    //
    ) || notString(asInfixExpression(e));
  }
  /**
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is an expression
   *         whose type is provably not of type {@link String}, in the sense
   *         used in applying the <code>+</code> operator to concatenate
   *         strings. concatenation.
   */
  public static boolean notString(final InfixExpression e) {
    return e != null && (e.getOperator() != PLUS || Are.notString(All.operands(e)));
  }
  /**
   * Determine whether a node is <code><b>this</b></code> or
   * <code><b>null</b></code>
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a block
   *         statement
   */
  public static boolean numericLiteral(final Expression e) {
    return Is.oneOf(e, CHARACTER_LITERAL, NUMBER_LITERAL);
  }
  /**
   * Determine whether the type of an {@link ASTNode} node is one of given list
   *
   * @param n a node
   * @param types a list of types
   * @return <code><b>true</b></code> <i>iff</i> function #ASTNode.getNodeType
   *         returns one of the types provided as parameters
   */
  public static boolean oneOf(final ASTNode n, final int... types) {
    return n != null && isOneOf(n.getNodeType(), types);
  }
  /**
   * @param a the assignment who's operator we want to check
   * @return true is the assignment's operator is assign
   */
  public static boolean plainAssignment(final Assignment a) {
    return a != null && a.getOperator() == Assignment.Operator.ASSIGN;
  }
  /**
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a prefix
   *         expression.
   */
  public static boolean prefix(final ASTNode n) {
    return is(n, PREFIX_EXPRESSION);
  }
  /**
   * Determine whether a node is a return statement
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a return
   *         statement.
   */
  public static boolean retern(final ASTNode n) {
    return is(n, RETURN_STATEMENT);
  }
  /**
   * Determine whether an {@link Expression} is so basic that it never needs to
   * be placed in parenthesis.
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is so basic that
   *         it never needs to be placed in parenthesis.
   */
  public static boolean simple(final Expression e) {
    return in(e.getClass(), //
        BooleanLiteral.class, //
        CharacterLiteral.class, //
        NullLiteral.class, //
        NumberLiteral.class, //
        StringLiteral.class, //
        TypeLiteral.class, //
        Name.class, //
        QualifiedName.class, //
        SimpleName.class, //
        ParenthesizedExpression.class, //
        SuperMethodInvocation.class, //
        MethodInvocation.class, //
        ClassInstanceCreation.class, //
        SuperFieldAccess.class, //
        FieldAccess.class, //
        ThisExpression.class, //
        null);
  }
  /**
   * Determine whether a node is a "specific", i.e., <code><b>null</b></code> or
   * <code><b>this</b></code> or literal.
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a "specific"
   */
  public static boolean specific(final Expression e) {
    return Is.oneOf(e, CHARACTER_LITERAL, NUMBER_LITERAL, NULL_LITERAL, THIS_EXPRESSION);
  }
  /**
   * Determine whether a node is a "sequencer", i.e., <code><b>return</b></code>
   * , <code><b>break</b></code>, <code><b>continue</b></code> or
   * <code><b>throw</b></code>
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a sequencer
   */
  public static boolean sequencer(final ASTNode n) {
    return Is.oneOf(n, RETURN_STATEMENT, BREAK_STATEMENT, CONTINUE_STATEMENT, THROW_STATEMENT);
  }
  /**
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a string
   *         literal
   */
  public static boolean stringLiteral(final ASTNode n) {
    return n != null && n.getNodeType() == STRING_LITERAL;
  }
  /**
   * Determine whether a node is <code><b>this</b></code> or
   * <code><b>null</b></code>
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a block
   *         statement
   */
  public static boolean thisOrNull(final Expression e) {
    return Is.oneOf(e, NULL_LITERAL, THIS_EXPRESSION);
  }
  private static boolean is(final ASTNode n, final int type) {
    return n != null && type == n.getNodeType();
  }
  private static boolean isOneOf(final int i, final int... is) {
    for (final int j : is)
      if (i == j)
        return true;
    return false;
  }
}
