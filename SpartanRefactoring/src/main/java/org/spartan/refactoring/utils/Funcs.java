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
import static org.eclipse.jdt.core.dom.ASTNode.VARIABLE_DECLARATION_STATEMENT;
import static org.eclipse.jdt.core.dom.ASTNode.copySubtree;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;
import static org.spartan.utils.Utils.inRange;
import static org.spartan.utils.Utils.last;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.spartan.refactoring.wring.PrefixNotPushdown;
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
   * Convert, is possible, an {@link ASTNode} to a {@link Block}
   *
   * @param $ JD
   * @return the argument, but down-casted to a {@link Block}, or
   *         <code><b>null</b></code> if no such down-cast is possible..
   */
  public static SimpleName asSimpleName(final ASTNode $) {
    return $.getNodeType() != SIMPLE_NAME ? null : (SimpleName)$;
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
    return $ == null || $.getNodeType() != IF_STATEMENT ? null : (IfStatement) $;
  }
  /**
   * Down-cast, if possible, to {@link InfixExpression}
   *
   * @param n JD
   * @return the parameter down-casted to the returned type, or
   *         <code><b>null</b></code> if no such down-casting is possible.
   */
  public static InfixExpression asInfixExpression(final ASTNode n) {
    return !(n instanceof InfixExpression) ? null : (InfixExpression) n;
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
   * @param n JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   */
  @SuppressWarnings("unchecked") public static <N extends ASTNode> N duplicate(final N n) {
    return (N) copySubtree(n.getAST(), n);
  }
  public static InfixExpression flip(final InfixExpression e) {
    return Subject.pair(e.getRightOperand(), e.getLeftOperand()).to(flip(e.getOperator()));
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
  // TODO for Yossi review - not sure if funcs is the right place for
  // getCompilationUnit(), getCurrentWorkbenchWindow() - because it serves both
  // spartanization and command handlers but actually kind of part of the
  // builder.
  public static ICompilationUnit getCompilationUnit() {
    return getCompilationUnit(getCurrentWorkbenchWindow().getActivePage().getActiveEditor());
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
  public static IWorkbenchWindow getCurrentWorkbenchWindow() {
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
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
   * @param n node to check
   * @return true if the given node is a variable declaration statement or false
   *         otherwise
   */
  public static boolean isVarDeclStmt(final ASTNode n) {
    return is(n, VARIABLE_DECLARATION_STATEMENT);
  }

  /**
   * @param e the expression to return in the return statement
   * @return the new return statement
   */
  public static ThrowStatement makeThrowStatement(final Expression e) {
    return Subject.operand(e).toThrow();
  }
  /**
   * @param t the AST who is to own the new variable declaration fragment
   * @param r the ASTRewrite for the given AST
   * @param varName the variable name for the new fragment
   * @param initalizer the initial value for the new fragment (for the variable)
   * @return the new variable declaration fragment or null if one of the given
   *         parameters was null
   */
  public static VariableDeclarationFragment makeVariableDeclarationFragment(final AST t, final ASTRewrite r, final SimpleName varName, final Expression initalizer) {
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
   * @param e JD
   * @return the parameter, but logically negated and simplified
   */
  public static Expression not(final Expression e) {
    final PrefixExpression $ = Subject.operand(e).to(NOT);
    final Expression $$ = PrefixNotPushdown.simplifyNot($);
    return $$ == null ? $ : $$;
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
   * @param t JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite
   */
  @SuppressWarnings("unchecked") public static <N extends ASTNode>  N rebase(final N n, final AST t) {
    return (N) copySubtree(t, n);
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
  /**
   * Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   *
   * @param n1 first list to compare
   * @param n2 second list to compare
   * @return are the lists equal string-wise
   */
  public static boolean same(final ASTNode n1, final ASTNode n2) {
    return n1 == n2 || n1.getNodeType() == n2.getNodeType() && n1.toString().equals(n2.toString());
  }
  /**
   * Determine whether two lists of nodes are the same, in the sense that their
   * textual representations is identical.
   *
   * @param ns1 first list to compare
   * @param ns2 second list to compare
   * @return are the lists equal string-wise
   */
  public static <T extends ASTNode> boolean same(final List<T> ns1, final List<T> ns2) {
    if (ns1 == ns2)
      return true;
    if (ns1.size() != ns2.size())
      return false;
    for (int i = 0; i < ns1.size(); ++i)
      if (!same(ns1.get(i), ns2.get(i)))
        return false;
    return true;
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
  private static ICompilationUnit getCompilationUnit(final IEditorPart ep) {
    return ep == null ? null : getCompilationUnit(ep.getEditorInput().getAdapter(IResource.class));
  }
  private static ICompilationUnit getCompilationUnit(final IResource r) {
    return r == null ? null : JavaCore.createCompilationUnitFrom((IFile) r);
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
