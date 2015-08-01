package org.spartan.refactoring.utils;

import static org.eclipse.jdt.core.dom.ASTNode.ASSIGNMENT;
import static org.eclipse.jdt.core.dom.ASTNode.BLOCK;
import static org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.EXPRESSION_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.IF_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.METHOD_INVOCATION;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.POSTFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.PREFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.ASTNode.RETURN_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME;
import static org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.VARIABLE_DECLARATION_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.copySubtree;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.spartan.refactoring.utils.Restructure.parenthesize;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;
import static org.spartan.utils.Utils.inRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.utils.Utils;

/**
 * Useful Functions
 */
public enum Funcs {
  ;
  private static Map<Operator, Operator> conjugate = makeConjeguates();
  /**
   * Convert an {@link Expression} into {@link InfixExpression} whose operator
   * is either {@link org.eclipse.jdt.core.dom.InfixExpression.Operator#AND} or
   * {@link org.eclipse.jdt.core.dom.InfixExpression.Operator#OR}.
   *
   * @param e JD
   * @return the parameter thus converted, or <code><b>null</b> if the
   *         conversion is not possible for it
   */
  public static InfixExpression asAndOrOr(final Expression e) {
    return !Is.infix(e) || !Is.deMorgan(asInfixExpression(e).getOperator()) ? null : asInfixExpression(e);
  }
  /**
   * Convert, is possible, an {@link ASTNode} to an {@link Assignment}
   *
   * @param $ JD
   * @return the argument, but down-casted to a {@link Assignment}, or
   *         <code><b>null</b></code> if the downcast is impossible.
   */
  public static Assignment asAssignment(final ASTNode $) {
    return !is($, ASSIGNMENT) ? null : (Assignment) $;
  }
  /**
   * Convert, is possible, an {@link ASTNode} to a {@link Block}
   *
   * @param $ JD
   * @return the argument, but down-casted to a {@link Block}, or
   *         <code><b>null</b></code> if no such down-cast is possible..
   */
  public static Block asBlock(final ASTNode $) {
    return $.getNodeType() != BLOCK ? null : (Block) $;
  }
  /**
   * Down-cast, if possible, to {@link BooleanLiteral}
   *
   * @param e JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static BooleanLiteral asBooleanLiteral(final Expression e) {
    return !(e instanceof BooleanLiteral) ? null : (BooleanLiteral) e;
  }
  /**
   * Convert an {@link Expression} into {@link InfixExpression} whose operator
   * is one of the six comparison operators: <code><</code>, <code><=</code>,
   * <code>></code>, <code>>=</code>, <code>!=</code>, or <code>==</code>.
   *
   * @param e JD
   * @return the parameter thus converted, or <code><b>null</b> if the
   *         conversion is not possible for it
   */
  public static InfixExpression asComparison(final Expression e) {
    return !(e instanceof InfixExpression) ? null : asComparison((InfixExpression) e);
  }
  /**
   * Convert, is possible, an {@link ASTNode} to a {@link ConditionalExpression}
   *
   * @param n JD
   * @return the argument, but down-casted to a {@link ConditionalExpression},
   *         or <code><b>null</b></code> if no such down-cast is possible..
   */
  public static ConditionalExpression asConditionalExpression(final ASTNode n) {
    return !(n instanceof ConditionalExpression) ? null : (ConditionalExpression) n;
  }
  /**
   * Convert, is possible, an {@link ASTNode} to a {@link ConditionalExpression}
   *
   * @param n JD
   * @return the argument, but down-casted to a {@link ConditionalExpression},
   *         or <code><b>null</b></code> if no such down-cast is possible..
   */
  public static ThrowStatement asThrowStatement(final ASTNode n) {
    return !(n instanceof ThrowStatement) ? null : (ThrowStatement) n;
  }
  /**
   * Down-cast, if possible, to {@link ConditionalExpression}
   *
   * @param e JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static ConditionalExpression asConditionalExpression(final Expression e) {
    return !(e instanceof ConditionalExpression) ? null : (ConditionalExpression) e;
  }
  /**
   * Down-cast, if possible, to {@link Expression}
   *
   * @param n JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static Expression asExpression(final ASTNode n) {
    return !(n instanceof Expression) ? null : (Expression) n;
  }
  /**
   * Down-cast, if possible, to {@link ExpressionStatement}
   *
   * @param n JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static ExpressionStatement asExpressionStatement(final ASTNode n) {
    return !(n instanceof ExpressionStatement) ? null : (ExpressionStatement) n;
  }
  /**
   * Down-cast, if possible, to {@link IfStatement}
   *
   * @param $ JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static IfStatement asIfStatement(final ASTNode $) {
    return $.getNodeType() != IF_STATEMENT ? null : (IfStatement) $;
  }
  /**
   * Down-cast, if possible, to {@link InfixExpression}
   *
   * @param e JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static InfixExpression asInfixExpression(final Expression e) {
    return !(e instanceof InfixExpression) ? null : (InfixExpression) e;
  }
  /**
   * Down-cast, if possible, to {@link MethodInvocation}
   *
   * @param e JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static MethodInvocation asMethodInvocation(final Expression e) {
    return !(e instanceof MethodInvocation) ? null : (MethodInvocation) e;
  }
  /**
   * Convert an {@link Expression} into a {@link PrefixExpression} whose
   * operator is <code>!</code>,
   *
   * @param e JD
   * @return the parameter thus converted, or <code><b>null</b> if the
   *         conversion is not possible for it
   */
  public static PrefixExpression asNot(final Expression e) {
    return !(e instanceof PrefixExpression) ? null : asNot(asPrefixExpression(e));
  }
  /**
   * Down-cast, if possible, to {@link PrefixExpression}
   *
   * @param e JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static PrefixExpression asPrefixExpression(final ASTNode e) {
    return !(e instanceof PrefixExpression) ? null : (PrefixExpression) e;
  }
  /**
   * Down-cast, if possible, to {@link ReturnStatement}
   *
   * @param $ JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static ReturnStatement asReturnStatement(final ASTNode $) {
    return $ == null || $.getNodeType() != RETURN_STATEMENT ? null : (ReturnStatement) $;
  }
  /**
   * Down-cast, if possible, to {@link Statement}
   *
   * @param e JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static Statement asStatement(final ASTNode e) {
    return !(e instanceof Statement) ? null : (Statement) e;
  }
  /**
   * @param root the node whose children we return
   * @return A list containing all the nodes in the given root's sub tree
   */
  public static List<ASTNode> collectDescendants(final ASTNode root) {
    if (root == null)
      return null;
    final List<ASTNode> $ = new ArrayList<>();
    root.accept(new ASTVisitor() {
      @Override public void preVisit(final ASTNode node) {
        $.add(node);
      }
    });
    $.remove(0);
    return $;
  }
  /**
   * the function checks if all the given assignments have the same left hand
   * side(variable) and operator
   *
   * @param base The assignment to compare all others to
   * @param as The assignments to compare
   * @return true if all assignments has the same left hand side and operator as
   *         the first one or false otherwise
   */
  public static boolean compatible(final Assignment base, final Assignment... as) {
    if (hasNull(base, as))
      return false;
    for (final Assignment a : as)
      if (a == null || !compatibleOps(base.getOperator(), a.getOperator()) || !compatibleNames(base.getLeftHandSide(), a.getLeftHandSide()))
        return false;
    return true;
  }
  /**
   * String wise comparison of all the given SimpleNames
   *
   * @param cmpTo a string to compare all names to
   * @param names SimplesNames to compare by their string value to cmpTo
   * @return true if all names are the same (string wise) or false otherwise
   */
  public static boolean compatibleNames(final Expression cmpTo, final Expression... names) {
    if (hasNull(cmpTo, names) || cmpTo.getNodeType() != SIMPLE_NAME)
      return false;
    for (final Expression name : names)
      if (name == null || name.getNodeType() != SIMPLE_NAME || !((SimpleName) name).getIdentifier().equals(((SimpleName) cmpTo).getIdentifier()))
        return false;
    return true;
  }
  /**
   * @param cmpTo the assignment operator to compare all to
   * @param op A unknown number of assignments operators
   * @return true if all the operator are the same or false otherwise
   */
  public static boolean compatibleOps(final Assignment.Operator cmpTo, final Assignment.Operator... op) {
    if (hasNull(cmpTo, op))
      return false;
    for (final Assignment.Operator o : op)
      if (o == null || o != cmpTo)
        return false;
    return true;
  }
  /**
   * @param ns unknown number of nodes to check
   * @return true if one of the nodes is an Expression Statement of type Post or
   *         Pre Expression with ++ or -- operator. false if none of them are or
   *         if the given parameter is null.
   */
  public static boolean containIncOrDecExp(final ASTNode... ns) {
    if (ns == null)
      return false;
    for (final ASTNode n : ns)
      if (n != null && Is.isNodeIncOrDecExp(n))
        return true;
    return false;
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static BooleanLiteral duplicate(final BooleanLiteral b) {
    return (BooleanLiteral) copySubtree(b.getAST(), b);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static ConditionalExpression duplicate(final ConditionalExpression e) {
    return (ConditionalExpression) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param a JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static Assignment duplicate(final Assignment a) {
    return (Assignment) copySubtree(a.getAST(), a);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static Expression duplicate(final Expression e) {
    return (Expression) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static ClassInstanceCreation duplicate(final ClassInstanceCreation e) {
    return (ClassInstanceCreation) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static FieldAccess duplicate(final FieldAccess e) {
    return (FieldAccess) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static IfStatement duplicate(final IfStatement e) {
    return (IfStatement) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static InfixExpression duplicate(final InfixExpression e) {
    return (InfixExpression) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static MethodInvocation duplicate(final MethodInvocation i) {
    return (MethodInvocation) copySubtree(i.getAST(), i);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static Statement duplicate(final Statement e) {
    return (Statement) copySubtree(e.getAST(), e);
  }
  /**
   * Make a duplicate of, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static Expression duplicateLeft(final InfixExpression e) {
    return duplicate(e.getLeftOperand());
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the right hand side of the
   * parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static Expression duplicateRight(final InfixExpression e) {
    return duplicate(e.getRightOperand());
  }
  public static InfixExpression flip(final InfixExpression e) {
    final Operator flip = flip(e.getOperator());
    return remake(e.getAST().newInfixExpression(), rightMoveableToLeft(flip, e), flip, leftMoveableToRight(flip, e));
  }
  /**
   * Makes an opposite operator from a given one, which keeps its logical
   * operation after the node swapping. e.g. "&" is commutative, therefore no
   * change needed. "<" isn't commutative, but it has its opposite: ">=".
   *
   * @param o The operator to flip
   * @return The correspond operator - e.g. "<=" will become ">", "+" will stay
   *         "+".
   */
  public static Operator flip(final Operator o) {
    return !conjugate.containsKey(o) ? o : conjugate.get(o);
  }
  public static Expression frugalDuplicate(final Expression e) {
    return e.getParent() == null ? e : (Expression) copySubtree(e.getAST(), e);
  }
  /**
   * Get the containing node by type. Say we want to find the first block that
   * wraps our node: getContainerByNodeType(node, BLOCK);
   *
   * @param n Node to find its container
   * @param ASTNodeType The type of the containing node we want to find
   * @return The containing node
   */
  public static ASTNode getContainerByNodeType(final ASTNode n, final int ASTNodeType) {
    if (n == null)
      return null;
    ASTNode $ = n.getParent();
    for (; $ != null && ASTNodeType != $.getNodeType(); $ = $.getParent())
      if ($.getParent() == $.getRoot())
        break;
    return $;
  }
  /**
   * @param n the node from which to extract the proper fragment
   * @param name the name by which to look for the fragment
   * @return the fragment if such with the given name exists or null otherwise
   *         (or if s or name are null)
   */
  public static VariableDeclarationFragment getVarDeclFrag(final ASTNode n, final Expression name) {
    return hasNull(n, name) || n.getNodeType() != VARIABLE_DECLARATION_STATEMENT || name.getNodeType() != SIMPLE_NAME ? null
        : getVarDeclFrag(((VariableDeclarationStatement) n).fragments(), (SimpleName) name);
  }
  /**
   * @param b the block to check
   * @return true if a return statement exists in the block or false otherwise
   */
  public static boolean hasReturn(final Block b) {
    if (b == null)
      return false;
    for (final Object node : b.statements())
      if (RETURN_STATEMENT == ((ASTNode) node).getNodeType())
        return true;
    return false;
  }
  /**
   * Determine whether a given statement is return or has return in it.
   *
   * @param s the statement or block to check
   * @return true iff s contains a return statement
   */
  public static boolean hasReturn(final Statement s) {
    if (s == null)
      return false;
    switch (s.getNodeType()) {
      case RETURN_STATEMENT:
        return true;
      case BLOCK: {
        for (final Object node : ((Block) s).statements())
          if (RETURN_STATEMENT == ((ASTNode) node).getNodeType())
            return true;
        break;
      }
      default:
        break;
    }
    return false;
  }
  /**
   * Determine whether a node is a return statement
   *
   * @param n node to check
   * @return true if the given node is a block statement
   */
  public static boolean isBlock(final ASTNode n) {
    return is(n, BLOCK);
  }
  /**
   * @param n node to check
   * @return true if the given node is a boolean or null literal or false
   *         otherwise
   */
  public static boolean isBoolOrNull(final ASTNode n) {
    return is(n, BOOLEAN_LITERAL) || is(n, NULL_LITERAL);
  }
  /**
   * Determined if a node is an "expression statement"
   *
   * @param n node to check
   * @return true if the given node is expression statement
   */
  public static boolean isExpressionStatement(final ASTNode n) {
    return is(n, EXPRESSION_STATEMENT);
  }
  /**
   * Determine whether a variable declaration is final or not
   *
   * @param v JD
   * @return true if the variable is declared as final
   */
  public static boolean isFinal(final VariableDeclarationStatement v) {
    return (Modifier.FINAL & v.getModifiers()) != 0;
  }
  /**
   * @param n JD
   * @return true if the given node is an infix expression or false otherwise
   */
  public static boolean isInfix(final ASTNode n) {
    return is(n, INFIX_EXPRESSION);
  }
  /**
   * @param n node to check
   * @return true if the given node is a method invocation or false otherwise
   */
  public static boolean isMethodInvocation(final ASTNode n) {
    return is(n, METHOD_INVOCATION);
  }
  /**
   * @param n node to check
   * @return true if node is an Expression Statement of type Post or Pre
   *         Expression with ++ or -- operator false if node is not an
   *         Expression Statement or its a Post or Pre fix expression that its
   *         operator is not ++ or --
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
   * @param a the assignment who's operator we want to check
   * @return true is the assignment's operator is assign
   */
  public static boolean isOpAssign(final Assignment a) {
    return a != null && a.getOperator() == Assignment.Operator.ASSIGN;
  }
  /**
   * Determined if a node is a return statement
   *
   * @param n node to check
   * @return true if the given node is a return statement or false otherwise
   */
  public static boolean isReturn(final ASTNode n) {
    return is(n, RETURN_STATEMENT);
  }
  /**
   * @param n node to check
   * @return true if the given node is a string literal or false otherwise
   */
  public static boolean isStringLiteral(final ASTNode n) {
    return n != null && n.getNodeType() == STRING_LITERAL;
  }
  /**
   * @param n node to check
   * @return true if the given node is a variable declaration statement or false
   *         otherwise
   */
  public static boolean isVarDeclStmt(final ASTNode n) {
    return is(n, VARIABLE_DECLARATION_STATEMENT);
  }
  /**
   * @param ts a list
   * @return the last item in a list or <code><b>null</b></code> if the
   *         parameter is <code><b>null</b></code> or empty
   */
  public static <T> T last(final List<T> ts) {
    return ts == null || ts.isEmpty() ? null : ts.get(ts.size() - 1);
  }
  public static Expression leftMoveableToRight(final Operator o, final InfixExpression e) {
    final Expression $ = e.getLeftOperand();
    return !Precedence.same(o, $) || !Associativity.isLeftToRight(o) ? duplicate($) : parenthesize($);
  }
  /**
   * @param o the assignment operator
   * @param right right side of the assignment, usually an expression
   * @param left left side of the assignment, usually a variable name
   * @return the new assignment
   */
  public static Assignment makeAssigment(final Assignment.Operator o, final Expression left, final Expression right) {
    if (hasNull(o, right, left))
      return null;
    final Assignment $ = right.getAST().newAssignment();
    $.setOperator(o);
    $.setRightHandSide(frugalDuplicate(right));
    $.setLeftHandSide(frugalDuplicate(left));
    return $;
  }
  /**
   * Create a new {@link ConditionalExpression}
   *
   * @param condition JD
   * @param then JD
   * @param elze JD
   * @return a newly created, unparenthesized {@link ConditionalExpression} with
   *         the specified arguments.
   */
  public static ConditionalExpression makeConditionalExpression(final Expression condition, final Expression then, final Expression elze) {
    final ConditionalExpression $ = condition.getAST().newConditionalExpression();
    $.setExpression(frugalDuplicate(condition));
    $.setThenExpression(frugalDuplicate(then));
    $.setElseExpression(frugalDuplicate(elze));
    return $;
  }
  public static Statement makeExpressionStatement(final Expression e) {
    return e.getAST().newExpressionStatement(frugalDuplicate(e));
  }
  /**
   * @param t the AST who is to own the new If Statement
   * @param r ASTRewrite for the given AST
   * @param cond the condition
   * @param thenStmnt the then statement to set in the If Statement
   * @param elseStmnt the else statement to set in the If Statement
   * @return a new if Statement
   */
  public static IfStatement makeIfStmnt(final AST t, final ASTRewrite r, final Expression cond, final Statement thenStmnt, final Statement elseStmnt) {
    if (hasNull(t, r, cond, thenStmnt, elseStmnt))
      return null;
    final IfStatement $ = t.newIfStatement();
    $.setExpression(frugalDuplicate(cond));
    $.setThenStatement(thenStmnt.getParent() == null ? thenStmnt : (Statement) r.createCopyTarget(thenStmnt));
    $.setElseStatement(elseStmnt.getParent() == null ? elseStmnt : (Statement) r.createCopyTarget(elseStmnt));
    return $;
  }
  /**
   * @param t the AST who is to own the new return statement
   * @param r ASTRewrite for the given AST
   * @param o the operator for the new infix expression
   * @param left the left expression
   * @param right the right expression
   * @return the new infix expression
   */
  public static InfixExpression makeInfixExpression(final AST t, final ASTRewrite r, final InfixExpression.Operator o, final Expression left, final Expression right) {
    if (hasNull(t, r, o, right, left))
      return null;
    final InfixExpression $ = t.newInfixExpression();
    $.setOperator(o);
    $.setRightOperand(frugalDuplicate(right));
    $.setLeftOperand(frugalDuplicate(left));
    return $;
  }
  /**
   * @param r ASTRewrite for the given AST
   * @param t the AST who is to own the new return statement
   * @param left the left expression
   * @param o the operator for the new infix expression
   * @param right the right expression
   * @return the new infix expression
   */
  public static InfixExpression makeInfixExpression(final ASTRewrite r, final AST t, final Expression left, final Operator o, final Expression right) {
    if (hasNull(t, r, o, right, left))
      return null;
    final InfixExpression $ = t.newInfixExpression();
    $.setLeftOperand(frugalDuplicate(left));
    $.setOperator(o);
    $.setRightOperand(frugalDuplicate(right));
    return $;
  }
  /**
   * @param r ASTRewrite for the given AST
   * @param cond the condition
   * @param thenExp the then statement to set in the conditional
   * @param elseExp the else statement to set in the conditional
   * @return a parenthesized conditional expression
   */
  public static ParenthesizedExpression makeParenthesizedConditionalExp(final ASTRewrite r, final Expression cond, final Expression thenExp, final Expression elseExp) {
    if (hasNull(r, cond, thenExp, elseExp))
      return null;
    final ConditionalExpression $ = cond.getAST().newConditionalExpression();
    $.setExpression(frugalDuplicate(cond));
    $.setThenExpression(frugalDuplicate(thenExp));
    $.setElseExpression(frugalDuplicate(elseExp));
    return makeParenthesizedExpression($);
  }
  /**
   * @param e the expression to put in parenthesis
   * @return the given expression with parenthesis
   */
  public static ParenthesizedExpression makeParenthesizedExpression(final Expression e) {
    final ParenthesizedExpression $ = e.getAST().newParenthesizedExpression();
    $.setExpression(frugalDuplicate(e));
    return $;
  }
  /**
   * @param t the AST to own the newly created expression
   * @param e the operand for the new prefix Expression
   * @param o the operator for the new prefix Expression
   * @return the new prefix expression or null if one of the given parameters
   *         was null
   */
  public static PrefixExpression makePrefixExpression(final AST t, final Expression e, final PrefixExpression.Operator o) {
    if (hasNull(t, e, o))
      return null;
    final PrefixExpression $ = t.newPrefixExpression();
    $.setOperator(o);
    $.setOperand(frugalDuplicate(e));
    return $;
  }
  /**
   * @param e the operand for the new prefix Expression
   * @param o the operator for the new prefix Expression
   * @return the new prefix expression or null if one of the given parameters
   *         was null
   */
  public static PrefixExpression makePrefixExpression(final Expression e, final PrefixExpression.Operator o) {
    return makePrefixExpression(e.getAST(), e, o);
  }
  /**
   * @param t the AST who is to own the new return statement
   * @param r ASTRewrite for the given AST
   * @param e the expression to return in the return statement
   * @return the new return statement
   */
  public static Statement makeReturnStatement(final Expression e) {
    final ReturnStatement $ = e.getAST().newReturnStatement();
    $.setExpression(frugalDuplicate(e));
    return $;
  }
  /**
   * @param t the AST who is to own the new return statement
   * @param r ASTRewrite for the given AST
   * @param e the expression to return in the return statement
   * @return the new return statement
   */
  public static ThrowStatement makeThrowStatement(final Expression e) {
    final ThrowStatement $ = e.getAST().newThrowStatement();
    $.setExpression(frugalDuplicate(e));
    return $;
  }
  /**
   * @param t the AST who is to own the new variable declaration fragment
   * @param r the ASTRewrite for the given AST
   * @param varName the variable name for the new fragment
   * @param initalizer the initial value for the new fragment (for the variable)
   * @return the new variable declaration fragment or null if one of the given
   *         parameters was null
   */
  public static VariableDeclarationFragment makeVarDeclFrag(final AST t, final ASTRewrite r, final SimpleName varName, final Expression initalizer) {
    if (hasNull(t, r, varName, initalizer))
      return null;
    final VariableDeclarationFragment $ = t.newVariableDeclarationFragment();
    $.setInitializer(frugalDuplicate(initalizer));
    $.setName(varName.getParent() == null ? varName : (SimpleName) r.createCopyTarget(varName));
    return $;
  }
  /**
   * Retrieve next item in a list
   *
   * @param i an index of specific item in a list
   * @param ts the indexed list
   * @return the following item in the list, if such such an item exists,
   *         otherwise, the last node
   */
  public static <T> T next(final int i, final List<T> ts) {
    return !inRange(i + 1, ts) ? last(ts) : ts.get(i + 1);
  }
  /**
   * Retrieve previous item in a list
   *
   * @param i an index of specific item in a list
   * @param ts the indexed list
   * @return the previous item in the list, if such an item exists, otherwise,
   *         the last node
   */
  public static <T> T prev(final int i, final List<T> ts) {
    return ts.get(i < 1 ? 0 : i - 1);
  }
  /**
   * Make a duplicate, suitable for tree rewrite, of the parameter
   *
   * @param e JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  public static Expression rebase(final Expression e, final AST t) {
    return (Expression) copySubtree(t, e);
  }
  public static InfixExpression remake(final InfixExpression $, final Expression left, final InfixExpression.Operator o, final Expression right) {
    $.setLeftOperand(left);
    $.setOperator(o);
    $.setRightOperand(right);
    return $;
  }
  /**
   * Remove all occurrences of a boolean literal from a list of
   * {@link Expression}s
   *
   * @param b JD
   * @param es JD
   */
  public static void removeAll(final boolean b, final List<Expression> es) {
    for (;;) {
      final Expression e = find(b, es);
      if (e == null)
        return;
      es.remove(e);
    }
  }
  /**
   * Obtain a condensed textual representation of an {@link ASTNode}
   *
   * @param n JD
   * @return the textual representation of the parameter,
   */
  public static String removeWhites(final ASTNode n) {
    return Utils.removeWhites(n.toString());
  }
  public static Expression rightMoveableToLeft(final Operator o, final InfixExpression e) {
    final Expression $ = e.getRightOperand();
    return !Precedence.same(o, $) || !Associativity.isLeftToRight(o) ? duplicate($) : parenthesize($);
  }
  /**
   * Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   *
   * @param n1 JD
   * @param n2 JD
   * @return are the nodes equal string-wise
   */
  public static boolean same(final ASTNode n1, final ASTNode n2) {
    return n1.toString().equals(n2.toString());
  }
  /**
   * Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   *
   * @param n1 first list to compare
   * @param n2 second list to compare
   * @return are the lists equal string-wise
   */
  public static boolean same(final List<ASTNode> n1, final List<ASTNode> n2) {
    return n1.toString().equals(n2.toString());
  }
  /**
   * @param s JD
   * @return 0 is s is null, 1 if s is a statement or the number of statement in
   *         the block is the parameter is
   */
  public static int statementsCount(final ASTNode s) {
    return Extract.statements(s).size();
  }
  /**
   * the function receives a condition and the then boolean value and returns
   * the proper condition (its negation if thenValue is false)
   *
   * @param t the AST who is to own the new return statement
   * @param r ASTRewrite for the given AST
   * @param cond the condition to try to negate
   * @param thenValue the then value
   * @return the original condition if thenValue was true or its negation if it
   *         was false (or null if any of the given parameter were null)
   */
  public static Expression tryToNegateCond(final AST t, final ASTRewrite r, final Expression cond, final boolean thenValue) {
    if (hasNull(t, cond))
      return null;
    return thenValue ? cond : makePrefixExpression(t, makeParenthesizedExpression(cond), PrefixExpression.Operator.NOT);
  }
  private static InfixExpression asComparison(final InfixExpression e) {
    return in(e.getOperator(), //
        GREATER, //
        GREATER_EQUALS, //
        LESS, //
        LESS_EQUALS, //
        EQUALS, //
        NOT_EQUALS //
    ) ? e : null;
  }
  private static Expression find(final boolean b, final List<Expression> es) {
    for (final Expression e : es)
      if (Is.booleanLiteral(e) && b == asBooleanLiteral(e).booleanValue())
        return e;
    return null;
  }
  private static VariableDeclarationFragment getVarDeclFrag(final List<VariableDeclarationFragment> frags, final SimpleName name) {
    for (final VariableDeclarationFragment o : frags)
      if (same(name, o.getName()))
        return o;
    return null;
  }
  private static boolean is(final ASTNode n, final int type) {
    return n != null && type == n.getNodeType();
  }
  private static Map<Operator, Operator> makeConjeguates() {
    final Map<Operator, Operator> $ = new HashMap<>();
    $.put(GREATER, LESS);
    $.put(LESS, GREATER);
    $.put(GREATER_EQUALS, LESS_EQUALS);
    $.put(LESS_EQUALS, GREATER_EQUALS);
    return $;
  }
  static PrefixExpression asNot(final PrefixExpression e) {
    return NOT.equals(e.getOperator()) ? e : null;
  }
}
