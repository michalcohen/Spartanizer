package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.countNodes;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.duplicate;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.duplicateLeft;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.duplicateRight;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.flip;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.remake;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.hasNull;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.PLUS;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

import il.ac.technion.cs.ssdl.spartan.utils.Is;
import il.ac.technion.cs.ssdl.spartan.utils.Is;
import il.ac.technion.cs.ssdl.spartan.utils.Range;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

/**
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         24.05.2014)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         24.05.2014)
 * @since 2014/05/24
 */
public class ShortestOperand extends SpartanizationOfInfixExpression {
  /** Instantiates this class */
  public ShortestOperand() {
    super("Shortest operand first", "Make the shortest operand first in a binary commutative or semi-commutative operator");
  }

  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (!inRange(m, e) || outOfScope(e))
          return true;
        final InfixExpression newNode = transpose(t, e);
        r.replace(e, newNode, null);
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

  static boolean containsStringLiteral(final ASTNode n) {
    if (n == null || !Is._final(n))
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
      flip(t, $, e);
    sortInfix($, t);
    System.out.println("END OP=" + e.getOperator() + " HIM MU");
    return $;
  }

  private boolean eligible(final InfixExpression e) {
    return Is.flipable(e.getOperator()) && longerFirst(e) && !inInfixExceptions(e);
  }

  private void transposeOperands(final InfixExpression ie, final AST t, final AtomicBoolean hasChanged) {
    final Expression l = ie.getLeftOperand();
    // sortInfix($);
    if (Is._final(l))
      ie.setLeftOperand(transpose(t, (InfixExpression) l));
    final Expression r = ie.getRightOperand();
    if (Is._final(r))
      ie.setRightOperand(transpose(t, (InfixExpression) r));
  }

  @SuppressWarnings("boxing") // Justification: because ASTNode is a primitive
  // int we can't use the generic "in" function on
  // it
  // without boxing into Integer. Any other
  // solution
  // will cause less readable/maintainable code.
  private boolean inRightOperandExceptions(final ASTNode rN, final Operator o) {
    if (Is.methodInvocation(rN))
      return true;
    if (inOperandExceptions(rN, o) || o == PLUS && (Is.methodInvocation(rN) || Is.stringLiteral(rN)))
      return true;
    return Is.literal(rN);
  }

  private boolean inOperandExceptions(final ASTNode n, final Operator o) {
    return Is.literal(n) ? true : o == PLUS && Is.stringLiteral(n);
  }

  private boolean inInfixExceptions(final InfixExpression ie) {
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
  protected boolean unionRangeWithList(final List<Range> rs, final Range rNew) {
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
    for (;;) {
      final Range parent = findContaining(rs, r);
      if (parent == null)
        break;
      rs.remove(parent);
    }
    rs.add(r);
    return false;
  }

  private static Range findContaining(final Iterable<Range> rs, final Range r) {
    for (final Range $ : rs)
      if (includedIn(r, $))
        return $;
    return null;
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
      remake(e, duplicateLeft(t, e), flip(o), duplicateRight(t, e));
      $ = true;
    }
    return $;
  }

  private boolean moveMethodsToTheBack(final List<Expression> es, final AST t, final Operator o) {
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
