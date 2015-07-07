package il.ac.technion.cs.ssdl.spartan.utils;

import static il.ac.technion.cs.ssdl.spartan.utils.Utils.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Useful Functions
 */
public enum Funcs {
  ;
  /**
   * Convert variadic list into an array
   *
   * @param os
   *          an unknown number of objects
   * @return the parameter, as an array.
   */
  public static Object[] objects(final Object... os) {
    return os;
  }

  /**
   * @param os
   *          an unknown number of objects
   * @return true if one of the objects is a null or false otherwise
   */
  public static boolean hasNull(final Object... os) {
    for (final Object o : os)
      if (o == null)
        return true;
    return false;
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
   * Retrieve previous item in a list
   *
   * @param i
   *          an index of specific item in a list
   * @param ts
   *          the indexed list
   *
   * @return the previous item in the list, if such an item exists, otherwise,
   *         the last node
   */
  public static <T> T prev(final int i, final List<T> ts) {
    return ts.get(i < 1 ? 0 : i - 1);
  }

  /**
   * Retrieve next item in a list
   *
   * @param i
   *          an index of specific item in a list
   * @param ts
   *          the indexed list
   *
   *
   * @return the following item in the list, if such such an item exists,
   *         otherwise, the last node
   */
  public static <T> T next(final int i, final List<T> ts) {
    return !inRange(i + 1, ts) ? last(ts) : ts.get(i + 1);
  }

  /**
   * @param ts
   *          a list
   * @return the last item in a list
   */
  public static <T> T last(final List<T> ts) {
    return ts.get(ts.size() - 1);
  }

  /**
   * Determine whether an integer is a valid list index
   *
   * @param i
   *          some integer
   * @param ts
   *          a list of things
   * @return <code><b>true</b></code> <i>iff</i> the index is valid index into
   *         the list. and it is the last one in it.
   */
  public static <T> boolean inRange(final int i, final List<T> ts) {
    return i >= 0 && i < ts.size();
  }

  /**
   * @param t
   *          the AST who is to own the new variable declaration fragment
   * @param r
   *          the ASTRewrite for the given AST
   * @param varName
   *          the variable name for the new fragment
   * @param initalizer
   *          the initial value for the new fragment (for the variable)
   * @return the new variable declaration fragment or null if one of the given
   *         parameters was null
   */
  public static VariableDeclarationFragment makeVarDeclFrag(final AST t, final ASTRewrite r, final SimpleName varName,
      final Expression initalizer) {
    if (hasNull(t, r, varName, initalizer))
      return null;
    final VariableDeclarationFragment $ = t.newVariableDeclarationFragment();
    $.setInitializer(initalizer.getParent() == null ? initalizer : (Expression) r.createCopyTarget(initalizer));
    $.setName(varName.getParent() == null ? varName : (SimpleName) r.createCopyTarget(varName));
    return $;
  }

  /**
   * @param t
   *          the AST who is to own the new parenthesized conditional expression
   * @param r
   *          ASTRewrite for the given AST
   * @param cond
   *          the condition
   * @param thenExp
   *          the then statement to set in the conditional
   * @param elseExp
   *          the else statement to set in the conditional
   * @return a parenthesized conditional expression
   */
  public static ParenthesizedExpression makeParenthesizedConditionalExp(final AST t, final ASTRewrite r, final Expression cond,
      final Expression thenExp, final Expression elseExp) {
    if (hasNull(t, r, cond, thenExp, elseExp))
      return null;
    final ConditionalExpression $ = t.newConditionalExpression();
    $.setExpression(cond.getParent() == null ? cond : (Expression) r.createCopyTarget(cond));
    $.setThenExpression(thenExp.getParent() == null ? thenExp : (Expression) r.createCopyTarget(thenExp));
    $.setElseExpression(elseExp.getParent() == null ? elseExp : (Expression) r.createCopyTarget(elseExp));
    return makeParenthesizedExpression(t, r, $);
  }

  /**
   * @param t
   *          the AST who is to own the new If Statement
   * @param r
   *          ASTRewrite for the given AST
   * @param cond
   *          the condition
   * @param thenStmnt
   *          the then statement to set in the If Statement
   * @param elseStmnt
   *          the else statement to set in the If Statement
   * @return a new if Statement
   */
  public static IfStatement makeIfStmnt(final AST t, final ASTRewrite r, final Expression cond, final Statement thenStmnt,
      final Statement elseStmnt) {
    if (hasNull(t, r, cond, thenStmnt, elseStmnt))
      return null;
    final IfStatement $ = t.newIfStatement();
    $.setExpression(cond.getParent() == null ? cond : (Expression) r.createCopyTarget(cond));
    $.setThenStatement(thenStmnt.getParent() == null ? thenStmnt : (Statement) r.createCopyTarget(thenStmnt));
    $.setElseStatement(elseStmnt.getParent() == null ? elseStmnt : (Statement) r.createCopyTarget(elseStmnt));
    return $;
  }

  /**
   * @param t
   *          the AST who is to own the new return statement
   * @param r
   *          ASTRewrite for the given AST
   * @param e
   *          the expression to return in the return statement
   * @return the new return statement
   */
  public static ReturnStatement makeReturnStatement(final AST t, final ASTRewrite r, final Expression e) {
    if (hasNull(t, r))
      return null;
    final ReturnStatement $ = t.newReturnStatement();
    $.setExpression(e == null || e.getParent() == null ? e : (Expression) r.createCopyTarget(e));
    return $;
  }

  /**
   * @param t
   *          the AST who is to own the new return statement
   * @param r
   *          ASTRewrite for the given AST
   * @param o
   *          the operator for the new infix expression
   * @param left
   *          the left expression
   * @param right
   *          the right expression
   * @return the new infix expression
   */
  public static InfixExpression makeInfixExpression(final AST t, final ASTRewrite r, final InfixExpression.Operator o,
      final Expression left, final Expression right) {
    if (hasNull(t, r, o, right, left))
      return null;
    final InfixExpression $ = t.newInfixExpression();
    $.setOperator(o);
    $.setRightOperand(right.getParent() == null ? right : (Expression) r.createCopyTarget(right));
    $.setLeftOperand(left.getParent() == null ? left : (Expression) r.createCopyTarget(left));
    return $;
  }

  /**
   * @param t
   *          the AST who is to own the new return statement
   * @param r
   *          ASTRewrite for the given AST
   * @param o
   *          the assignment operator
   * @param right
   *          right side of the assignment, usually an expression
   * @param left
   *          left side of the assignment, usually a variable name
   * @return the new assignment
   */
  public static Assignment makeAssigment(final AST t, final ASTRewrite r, final Operator o, final Expression right,
      final Expression left) {
    if (hasNull(t, r, o, right, left))
      return null;
    final Assignment $ = t.newAssignment();
    $.setOperator(o);
    $.setRightHandSide(right.getParent() == null ? right : (Expression) r.createCopyTarget(right));
    $.setLeftHandSide(left.getParent() == null ? left : (Expression) r.createCopyTarget(left));
    return $;
  }

  /**
   * @param t
   *          the AST who is to own the new return statement
   * @param r
   *          ASTRewrite for the given AST
   * @param operand
   *          the operand for the new prefix Expression
   * @param o
   *          the operator for the new prefix Expression
   * @return the new prefix expression or null if one of the given parameters
   *         was null
   */
  public static PrefixExpression makePrefixExpression(final AST t, final ASTRewrite r, final Expression operand,
      final PrefixExpression.Operator o) {
    if (hasNull(t, operand, o))
      return null;
    final PrefixExpression $ = t.newPrefixExpression();
    $.setOperator(o);
    $.setOperand(operand.getParent() == null ? operand : (Expression) r.createCopyTarget(operand));
    return $;
  }

  /**
   * @param t
   *          the AST who is to own the new return statement
   * @param r
   *          ASTRewrite for the given AST
   * @param exp
   *          the expression to put in parenthesis
   * @return the given expression with parenthesis
   */
  public static ParenthesizedExpression makeParenthesizedExpression(final AST t, final ASTRewrite r, final Expression exp) {
    if (hasNull(t, r, exp))
      return null;
    final ParenthesizedExpression $ = t.newParenthesizedExpression();
    $.setExpression(exp.getParent() == null ? exp : (Expression) r.createCopyTarget(exp));
    return $;
  }

  /**
   * @param node
   *          a node to extract an expression from
   * @return null if the statement is not an expression or return statement or
   *         the expression if they are
   */
  public static Expression getExpression(final ASTNode node) {
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
   * @param s
   *          a statement or a block to extract the expression statement from
   * @return the expression statement if n is a block or an expression statement
   *         or null if it not an expression statement or if the block contains
   *         more than one statement
   */
  public static ExpressionStatement asExpressionStatement(final Statement s) {
    if (s == null)
      return null;
    final ASTNode $ = !isBlock(s) ? s : getBlockSingleStmnt(s);
    return !isExpressionStatement($) ? null : (ExpressionStatement) $;
  }

  /**
   * @param s
   *          a statement or block to extract the assignment from
   * @return null if the block contains more than one statement or if the
   *         statement is not an assignment or the assignment if it exists
   */
  public static Assignment getAssignment(final Statement s) {
    final ExpressionStatement $ = asExpressionStatement(s);
    return $ == null || ASTNode.ASSIGNMENT != $.getExpression().getNodeType() ? null : (Assignment) $.getExpression();
  }

  /**
   * @param s
   *          the statement or block to extract the method invocation from
   * @return the method invocation if it exists or null if it doesn't or if the
   *         block contains more than one statement
   */
  public static MethodInvocation getMethodInvocation(final Statement s) {
    final ExpressionStatement $ = asExpressionStatement(s);
    return $ == null || ASTNode.METHOD_INVOCATION != $.getExpression().getNodeType() ? null : (MethodInvocation) $.getExpression();
  }

  /**
   * @param n
   *          the statement or block to check if it is an assignment
   * @return true if it is an assignment or false if it is not or if the block
   *         Contains more than one statement
   */
  public static boolean isAssignment(final ASTNode n) {
    return isBlock(n) ? isAssignment(asExpressionStatement(getBlockSingleStmnt((Block) n)))
        : isExpressionStatement(n) && ASTNode.ASSIGNMENT == ((ExpressionStatement) n).getExpression().getNodeType();
  }

  /**
   * @param b
   *          the block to check
   * @return true if a return statement exists in the block or false otherwise
   */
  public static boolean hasReturn(final Block b) {
    if (b == null)
      return false;
    for (int i = 0; i < b.statements().size(); i++)
      if (ASTNode.RETURN_STATEMENT == ((ASTNode) b.statements().get(i)).getNodeType())
        return true;
    return false;
  }

  /**
   * @param b
   *          the block to get the statement from
   * @return if b is a block with just 1 statement it returns that statement, if
   *         b is statement it returns b and if b is null it returns a null
   */
  public static Statement getBlockSingleStmnt(final Statement b) {
    return b == null || ASTNode.BLOCK != b.getNodeType() ? b : getBlockSingleStmnt((Block) b);
  }

  private static Statement getBlockSingleStmnt(final Block b) {
    return b.statements().size() != 1 ? null : (Statement) b.statements().get(0);
  }

  /**
   * Determine whether a given statement is return or has return in it.
   *
   * @param s
   *          the statement or block to check
   * @return true iff s contains a return statement
   */
  public static boolean hasReturn(final Statement s) {
    if (s == null)
      return false;
    switch (s.getNodeType()) {
      case ASTNode.RETURN_STATEMENT:
        return true;
      case ASTNode.BLOCK: {
        for (final Object node : ((Block) s).statements())
          if (ASTNode.RETURN_STATEMENT == ((ASTNode) node).getNodeType())
            return true;
        break;
      }
      default:
        break;
    }
    return false;
  }

  /**
   * @param node
   *          the node to get the number of statements in
   * @return 0 is s is null, 1 if s is a statement or the number of statement in
   *         the block is s is a block
   */
  public static int statementsCount(final ASTNode node) {
    if (node == null)
      return 0;
    switch (node.getNodeType()) {
      case ASTNode.BLOCK:
        return statements(node).size();
      default:
        return 1;
    }
  }

  /**
   * @param s
   *          The node from which to return statement.
   * @return null if it is not possible to extract the return statement.
   */
  public static ReturnStatement asReturn(final ASTNode s) {
    if (s == null)
      return null;
    switch (s.getNodeType()) {
      case ASTNode.BLOCK:
        return asReturn((Block) s);
      case ASTNode.RETURN_STATEMENT:
        return (ReturnStatement) s;
      default:
        return null;
    }
  }

  private static ReturnStatement asReturn(final Block b) {
    return b.statements().size() != 1 ? null : asReturn((Statement) b.statements().get(0));
  }

  /**
   * @param n
   *          the node from which to extract the proper fragment
   * @param name
   *          the name by which to look for the fragment
   * @return the fragment if such with the given name exists or null otherwise
   *         (or if s or name are null)
   */
  public static VariableDeclarationFragment getVarDeclFrag(final ASTNode n, final Expression name) {
    return hasNull(n, name) || n.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT
        || name.getNodeType() != ASTNode.SIMPLE_NAME ? null
            : getVarDeclFrag(((VariableDeclarationStatement) n).fragments(), (SimpleName) name);
  }

  private static VariableDeclarationFragment getVarDeclFrag(final List<VariableDeclarationFragment> frags, final SimpleName name) {
    for (final VariableDeclarationFragment o : frags)
      if (same(name, o.getName()))
        return o;
    return null;
  }

  /**
   * String wise comparison of all the given SimpleNames
   *
   * @param cmpTo
   *          a string to compare all names to
   * @param names
   *          SimplesNames to compare by their string value to cmpTo
   * @return true if all names are the same (string wise) or false otherwise
   */
  public static boolean compatibleNames(final Expression cmpTo, final Expression... names) {
    if (hasNull(cmpTo, names) || cmpTo.getNodeType() != ASTNode.SIMPLE_NAME)
      return false;
    for (final Expression name : names)
      if (name == null || name.getNodeType() != ASTNode.SIMPLE_NAME
          || !((SimpleName) name).getIdentifier().equals(((SimpleName) cmpTo).getIdentifier()))
        return false;
    return true;
  }

  /**
   * @param cmpTo
   *          the assignment operator to compare all to
   * @param op
   *          A unknown number of assignments operators
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
   * the function checks if all the given assignments has the same left hand
   * side(variable) and operator
   *
   * @param base
   *          The assignment to compare all others to
   * @param asgns
   *          The assignments to compare
   * @return true if all assignments has the same left hand side and operator as
   *         the first one or false otherwise
   */
  public static boolean compatible(final Assignment base, final Assignment... asgns) {
    if (hasNull(base, asgns))
      return false;
    for (final Assignment asgn : asgns)
      if (asgn == null || !compatibleOps(base.getOperator(), asgn.getOperator())
          || !compatibleNames(base.getLeftHandSide(), asgn.getLeftHandSide()))
        return false;
    return true;
  }

  /**
   * the function receives a condition and the then boolean value and returns
   * the proper condition (its negation if thenValue is false)
   *
   * @param t
   *          the AST who is to own the new return statement
   * @param r
   *          ASTRewrite for the given AST
   * @param cond
   *          the condition to try to negate
   * @param thenValue
   *          the then value
   * @return the original condition if thenValue was true or its negation if it
   *         was false (or null if any of the given parameter were null)
   */
  public static Expression tryToNegateCond(final AST t, final ASTRewrite r, final Expression cond, final boolean thenValue) {
    if (hasNull(t, cond))
      return null;
    return thenValue ? cond : makePrefixExpression(t, r, makeParenthesizedExpression(t, r, cond), PrefixExpression.Operator.NOT);
  }

  /**
   * Counts the number of nodes in the tree of which node is root.
   *
   * @param n
   *          The node.
   * @return Number of abstract syntax tree nodes under the parameter.
   */
  public static int countNodes(final ASTNode n) {
    final AtomicInteger $ = new AtomicInteger(0);
    n.accept(new ASTVisitor() {
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
       * @param _
       *          ignored
       */
      @Override public void preVisit(@SuppressWarnings("unused") final ASTNode _) {
        $.incrementAndGet();
      }
    });
    return $.get();
  }

  /**
   * @param root
   *          the node whose children we return
   * @return A list containing all the nodes in the given root's sub tree
   */
  public static List<ASTNode> getChildren(final ASTNode root) {
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
   * @param es
   *          expressions to check
   * @return true if one of the expressions is a conditional or parenthesized
   *         conditional expression or false otherwise
   */
  public static boolean isConditional(final Expression... es) {
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
   * @param n
   *          the potential block who's statements list we return
   * @return the list of statements in n if it is a block or null otherwise
   */
  public static List<ASTNode> statements(final ASTNode n) {
    return statements(asBlock(n));
  }

  /**
   * Convert, is possible, an {@link ASTNode} to a {@link Block}
   *
   * @param n
   *          what to convert
   * @return the argument, but downcasted to a {@link Block}, or
   *         <code><b>null</b></code> otherwise.
   */
  public static Block asBlock(final ASTNode n) {
    return !(n instanceof Block) ? null : (Block) n;
  }

  private static List<ASTNode> statements(final Block b) {
    return b == null ? null : b.statements();
  }

  /**
   * Get the containing node by type. Say we want to find the first block that
   * wraps our node: getContainerByNodeType(node, ASTNode.BLOCK);
   *
   * @param n
   *          Node to find its container
   * @param ASTNodeType
   *          The type of the containing node we want to find
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
   * @param nodes
   *          unknown number of nodes to check
   * @return true if one of the nodes is an Expression Statement of type Post or
   *         Pre Expression with ++ or -- operator. false if none of them are or
   *         if the given parameter is null.
   */
  public static boolean containIncOrDecExp(final ASTNode... nodes) {
    if (nodes == null)
      return false;
    for (final ASTNode n : nodes)
      if (n != null && isNodeIncOrDecExp(n))
        return true;
    return false;
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
   * @param n
   *          node to check
   * @return true if the given node is a string literal or false otherwise
   */
  public static boolean isStringLiteral(final ASTNode n) {
    return n != null && n.getNodeType() == ASTNode.STRING_LITERAL;
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a boolean or null literal or false
   *         otherwise
   */
  public static boolean isBoolOrNull(final ASTNode n) {
    return is(n, ASTNode.BOOLEAN_LITERAL) || is(n, ASTNode.NULL_LITERAL);
  }

  /**
   * Determined if a node is an "expression statement"
   *
   * @param n
   *          node to check
   * @return true if the given node is expression statement
   */
  public static boolean isExpressionStatement(final ASTNode n) {
    return is(n, ASTNode.EXPRESSION_STATEMENT);
  }

  /**
   * Determine if an item can be found in a list of values
   *
   * @param candidate
   *          what to search for
   * @param ts
   *          where to search
   * @return true if the the item is found in the list
   */
  @SafeVarargs public static <T> boolean in(final T candidate, final T... ts) {
    for (final T t : ts)
      if (t != null && t.equals(candidate))
        return true;
    return false;
  }

  /**
   * Determine if an integer can be found in a list of values
   *
   * @param candidate
   *          what to search for
   * @param is
   *          where to search
   * @return true if the the item is found in the list
   */
  @SafeVarargs public static boolean in(final int candidate, final int... is) {
    for (final int i : is)
      if (i == candidate)
        return true;
    return false;
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
   * Determined if a node is a return statement
   *
   * @param n
   *          node to check
   * @return true if the given node is a block statement
   */
  public static boolean isBlock(final ASTNode n) {
    return is(n, ASTNode.BLOCK);
  }

  private static boolean is(final ASTNode n, final int type) {
    return n != null && type == n.getNodeType();
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
   *          node to check
   * @return true if the given node is an infix expression or false otherwise
   */
  public static boolean isInfix(final ASTNode n) {
    return is(n, ASTNode.INFIX_EXPRESSION);
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a method invocation or false otherwise
   */
  public static boolean isMethodInvocation(final ASTNode n) {
    return is(n, ASTNode.METHOD_INVOCATION);
  }

  /**
   * @param n
   *          node to check
   * @return true if the given node is a prefix expression or false otherwise
   */
  public static boolean isPrefix(final ASTNode n) {
    return is(n, ASTNode.PREFIX_EXPRESSION);
  }

  private static final int[] literals = sort(new int[] {
      //
      ASTNode.NULL_LITERAL, //
      ASTNode.CHARACTER_LITERAL, //
      ASTNode.NUMBER_LITERAL, //
      ASTNode.STRING_LITERAL, //
      ASTNode.BOOLEAN_LITERAL, //
  });

  /**
   * @param r
   *          Return Statement node
   * @return true if the ReturnStatement is of literal type
   */
  public static boolean isLiteral(final ReturnStatement r) {
    return isLiteral(r.getExpression());
  }

  /**
   * @param n
   *          Expression node
   * @return true if the Expression is literal
   */
  public static boolean isLiteral(final ASTNode n) {
    return Arrays.binarySearch(literals, n.getNodeType()) >= 0;
  }

  /**
   * @param a
   *          the assignment who's operator we want to check
   * @return true is the assignment's operator is assign
   */
  public static boolean isOpAssign(final Assignment a) {
    return a != null && a.getOperator() == Assignment.Operator.ASSIGN;
  }

  /**
   * Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   *
   * @param n1
   *          an arbitrary node
   * @param n2
   *          second node to compare
   * @return are the nodes equal string-wise
   */
  public static boolean same(final ASTNode n1, final ASTNode n2) {
    return n1.toString().equals(n2.toString());
  }

  /**
   * Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   *
   * @param n1
   *          first list to compare
   * @param n2
   *          second list to compare
   * @return are the lists equal string-wise
   */
  public static boolean same(final List<ASTNode> n1, final List<ASTNode> n2) {
    return n1.toString().equals(n2.toString());
  }
}
