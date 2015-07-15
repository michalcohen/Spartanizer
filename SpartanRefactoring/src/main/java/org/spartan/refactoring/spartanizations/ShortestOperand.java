package org.spartan.refactoring.spartanizations;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.PLUS;
import static org.spartan.refacotring.utils.Funcs.countNodes;
import static org.spartan.refacotring.utils.Funcs.duplicate;
import static org.spartan.refacotring.utils.Funcs.duplicateLeft;
import static org.spartan.refacotring.utils.Funcs.duplicateRight;
import static org.spartan.refacotring.utils.Funcs.flip;
import static org.spartan.refacotring.utils.Funcs.remake;
import static org.spartan.utils.Utils.hasNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refacotring.utils.Is;
import org.spartan.utils.Range;

/**
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         24.05.2014)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         24.05.2014)
 * @since 2014/05/24
 */
public class ShortestOperand extends Spartanization {
  // Option flags
  /**
   * Enumeration for null and boolean swap
   */
  public static enum RepositionBoolAndNull {
    /** a == null */
    MoveRight, /** null == a */
    MoveLeft, /** Don't interrupt user choice */
    None
  }

  /**
   * Enumeration for right literal rule options
   */
  public static enum RepositionRightLiteral {
    /** When right can be swapped - do it */
    All, /** Swap literal only when it is not boolean or null */
    AllButBooleanAndNull, /**
                           * When the literal appears to the right - do not swap
                           */
    None
  }

  /**
   * Enumeration for both side literals rule options
   */
  public static enum RepositionLiterals {
    /** Swap literals */
    All, /** Do not swap literals */
    None
  }

  /**
   * Enumeration for ranges of messages
   */
  public static enum MessagingOptions {
    /** Swap literals */
    Union, /** Do not swap literals */
    ShowAll
  }

  /** Instantiates this class */
  public ShortestOperand() {
    super("Shortest operand first", "Make the shortest operand first in a binary commutative or semi-commutative operator");
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (!inRange(m, e) || outOfScope(e))
          return true;
        r.replace(e, transpose(t, e), null);
        return true;
      }
    });
  }
  static boolean outOfScope(final InfixExpression e) {
    return e == null //
        || hasNull(e.getLeftOperand(), e.getRightOperand()) //
        || ComparisonWithSpecific.withinDomain(e) //
        || stringReturningMethod(e) //
        || containsStringLiteral(e)//
        ;
  }
  static boolean invalid(final InfixExpression n) {
    return n == null || n.getLeftOperand() == null || n.getRightOperand() == null || stringReturningMethod(n)
        || containsStringLiteral(n);
  }
  static boolean containsStringLiteral(final ASTNode n) {
    if (n == null || !Is.infix(n))
      return false;
    final InfixExpression e = (InfixExpression) n;
    if (EQUALS == e.getOperator()) // The only permitted operator on strings
      return false;
    final ASTNode l = e.getLeftOperand();
    final ASTNode r = e.getRightOperand();
    if (Is.stringLiteral(l) || Is.stringLiteral(r))
      return true;
    for (final Object listN : e.extendedOperands())
      if (listN instanceof ASTNode)
        if (Is.stringLiteral((ASTNode) listN))
          return true;
    return containsStringLiteral(l) || containsStringLiteral(r);
  }
  /**
   * Transpose infix expressions recursively. Makes the shortest operand first
   * on every subtree of the node.
   *
   * @param t
   *          The AST - for copySubTree.
   * @param e
   *          The node.
   * @return Number of abstract syntax tree nodes under the parameter.
   */
  public InfixExpression transpose(final AST t, final InfixExpression e) {
    final InfixExpression $ = duplicate(t, e);
    System.out.println("BEGIN OP=" + e.getOperator() + " HIM MU");
    if (eligible(e))
      flip(e);
    sortInfix($, t);
    System.out.println("END OP=" + e.getOperator() + " HIM MU");
    return $;
  }
  private boolean eligible(final InfixExpression e) {
    return Is.flipable(e.getOperator()) && longerFirst(e) && !inInfixExceptions(e);
  }
  void transposeOperands(final InfixExpression ie, final AST t) {
    final Expression l = ie.getLeftOperand();
    // sortInfix($);
    if (Is.infix(l))
      ie.setLeftOperand(transpose(t, (InfixExpression) l));
    final Expression r = ie.getRightOperand();
    if (Is.infix(r))
      ie.setRightOperand(transpose(t, (InfixExpression) r));
  }
  private static boolean inRightOperandExceptions(final ASTNode rN, final Operator o) {
    if (Is.methodInvocation(rN))
      return true;
    if (inOperandExceptions(rN, o) || o == PLUS && (Is.methodInvocation(rN) || Is.stringLiteral(rN)))
      return true;
    return Is.literal(rN);
  }
  private static boolean inOperandExceptions(final ASTNode n, final Operator o) {
    return Is.literal(n) ? true : o == PLUS && Is.stringLiteral(n);
  }
  private static boolean inInfixExceptions(final InfixExpression ie) {
    final Operator $ = ie.getOperator();
    return Is.methodInvocation(ie.getLeftOperand()) && Is.methodInvocation(ie.getRightOperand())
        || inOperandExceptions(ie.getLeftOperand(), $) //
        || inOperandExceptions(ie.getRightOperand(), $) //
        || inRightOperandExceptions(ie.getRightOperand(), $);
  }

  private static final int TOKEN_THRESHOLD = 1;
  private static final int CHARACTER_THRESHOLD = 2;

  protected static boolean includeEachOther(final Range a, final Range b) {
    return includedIn(a, b) || includedIn(b, a);
  }
  private static boolean includedIn(final Range a, final Range b) {
    return a.from < b.from && a.to > b.to;
  }
  /**
   * Determine if the ranges are overlapping in a part of their range
   *
   * @param a
   *          b Ranges to merge
   * @return true - if such an overlap exists
   * @see ShortestOperandTest#merge
   */
  protected static boolean overlapping(final Range a, final Range b) {
    return b.to >= a.from && a.to >= b.from || includeEachOther(a, b);
  }
  /**
   * @param a
   *          b Ranges to merge
   * @return A new merged range.
   * @see areOverlapped
   */
  protected static Range merge(final Range a, final Range b) {
    return new Range(a.from < b.from ? a.from : b.from, a.to > b.to ? a.to : b.to);
  }
  /**
   * Tries to union the given range with one of the elements inside the given
   * list.
   *
   * @param rs
   *          The list of ranges to union with
   * @param rNew
   *          The new range to union
   * @return True - if the list updated and the new range consumed False - the
   *         list remained intact
   * @see areOverlapped
   * @see mergerangeList
   */
  protected static boolean unionRangeWithList(final List<Range> rs, final Range rNew) {
    boolean $ = false;
    for (final Range r : rs)
      if (overlapping(r, rNew)) {
        merge(r, rNew);
        rs.add(r);
        $ = true;
        break;
      }
    removeDuplicates(rs);
    return $;
  }
  protected static <T> void removeDuplicates(final List<T> ts) {
    final Set<T> noDuplicates = new LinkedHashSet<>(ts);
    ts.clear();
    ts.addAll(noDuplicates);
  }
  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        return outOfScope(e) || !eligible(e) ? true : overrideInto(new Range(e), opportunities);
      }
    };
  }
  static boolean overrideInto(final Range r, final List<Range> rs) {
    r.pruneIncluders(rs);
    rs.add(r);
    return true;
  }
  static boolean stringReturningMethod(final InfixExpression n) {
    for (ASTNode parent = n.getParent(); parent != null; parent = parent.getParent())
      if (Is.isReturn(parent) && doesMthdRetString(parent))
        return true;
    return false;
  }
  private static boolean doesMthdRetString(final ASTNode n) {
    for (ASTNode p = n.getParent(); p != null; p = p.getParent())
      if (p.getNodeType() == ASTNode.METHOD_DECLARATION)
        return ((MethodDeclaration) p).getReturnType2().toString().equals("String");
    return false;
  }
  static boolean longerFirst(final InfixExpression n) {
    return isLonger(n.getLeftOperand(), n.getRightOperand());
  }
  static boolean isLonger(final Expression e1, final Expression e2) {
    if (hasNull(e1, e2))
      return false;
    final boolean tokenWiseGreater = countNodes(e1) > TOKEN_THRESHOLD + countNodes(e2);
    final boolean characterWiseGreater = e1.getLength() > CHARACTER_THRESHOLD + e2.getLength();
    if (tokenWiseGreater && characterWiseGreater)
      return true;
    if (!tokenWiseGreater && !characterWiseGreater)
      return false;
    return moreArguments(e1, e2);
  }
  private static boolean moreArguments(final Expression e1, final Expression e2) {
    return Is.methodInvocation(e1) && Is.methodInvocation(e2) && moreArguments((MethodInvocation) e1, (MethodInvocation) e2);
  }
  static boolean moreArguments(final MethodInvocation i1, final MethodInvocation i2) {
    return i1.arguments().size() > i2.arguments().size();
  }
  boolean sortInfix(final InfixExpression e, final AST t) {
    boolean $ = false;
    if (e == null || !Is.flipable(e.getOperator()) || !e.hasExtendedOperands())
      return $;
    final List<Expression> eo = e.extendedOperands();
    // The algorithm is described as line-by-line example
    // Say we have infix expression with (Left operand) (Right operand) and
    // list of extended operands | e1, e2 ,e3...
    // Infix: (Left = a) (Right = e) | d, b, c, f
    eo.add(0, (Expression) ASTNode.copySubtree(t, e.getLeftOperand()));
    eo.add(1, (Expression) ASTNode.copySubtree(t, e.getRightOperand()));
    final Operator o = e.getOperator();
    // | a, e, d, b, c, f - is the list with the operands
    $ = $ | sortExpressionList(eo, t, o);
    // | a, b, c, d, e, f - is the list after sorting
    e.setRightOperand((Expression) ASTNode.copySubtree(t, eo.get(1)));
    e.setLeftOperand((Expression) ASTNode.copySubtree(t, eo.get(0)));
    // (Left = a) (Right = b) | a, b, c, d, e, f - retrieve the operands
    eo.remove(1);
    eo.remove(0);
    // (Left = a) (Right = b) | c, d, e, f
    if (longerFirst(e) && !inInfixExceptions(e)) {
      remake(e, duplicateLeft(e), flip(o), duplicateRight(e));
      $ = true;
    }
    return $;
  }
  private static boolean moveMethodsToTheBack(final List<Expression> es, final AST t, final Operator o) {
    boolean $ = false;
    // Selective bubble sort
    for (int i = 0, size = es.size(); i < size; i++)
      for (int j = 0; size > j + 1; j++) {
        final Expression l = es.get(j);
        final Expression s = es.get(j + 1);
        if (Is.methodInvocation(l) && !Is.methodInvocation(s) && !inOperandExceptions(l, o) && !inOperandExceptions(s, o)) {
          es.remove(j);
          es.add(j + 1, (Expression) ASTNode.copySubtree(t, l));
          $ = true;
        }
      }
    return $;
  }
  private boolean sortOperandList(final List<Expression> es, final AST t, final Operator o) {
    boolean $ = false;
    // Bubble sort
    // We cannot use overridden version of Comparator due to the copy
    // ASTNode.copySubtree necessity
    for (int i = 0, size = es.size(); i < size; i++)
      for (int j = 0; j < size - 1; j++) {
        final Expression l = es.get(j);
        final Expression s = es.get(j + 1);
        if (areExpsValid(o, l, s)) {
          es.remove(j);
          es.add(j + 1, (Expression) ASTNode.copySubtree(t, l));
          $ = true;
        }
      }
    return $;
  }
  private boolean areExpsValid(final Operator o, final Expression l, final Expression s) {
    return isLonger(l, s) && !Is.methodInvocation(l) && !Is.methodInvocation(s) && !inOperandExceptions(l, o)
        && !inOperandExceptions(s, o) && !inRightOperandExceptions(l, o) && !inRightOperandExceptions(s, o);
  }
  private boolean sortExpressionList(final List<Expression> es, final AST t, final Operator o) {
    return moveMethodsToTheBack(es, t, o) | sortOperandList(es, t, o);
  }
}
