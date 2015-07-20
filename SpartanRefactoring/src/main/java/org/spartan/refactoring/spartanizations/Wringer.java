package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.asAndOrOr;
import static org.spartan.refactoring.utils.Funcs.asComparison;
import static org.spartan.refactoring.utils.Funcs.asNot;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.duplicateLeft;
import static org.spartan.refactoring.utils.Funcs.duplicateRight;
import static org.spartan.refactoring.utils.Funcs.flip;
import static org.spartan.refactoring.utils.Funcs.getCore;
import static org.spartan.refactoring.utils.Funcs.remake;

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
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Range;

/**
 * Applies the suite of {@link Wring} objects found in <ode><b>enum</b></code>
 * {@link Wrings} to a tree.
 *
 * @author Yossi Gil
 * @since 2015/07/10
 */
public class Wringer extends Spartanization {
  /** Instantiates this class */
  public Wringer() {
    super("Expression simplifier", "Make the shortest operand first in a binary commutative or semi-commutative operator");
  }
  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      // TODO: this must be a bug.
      @Override public boolean visit(final PrefixExpression e) {
        if (hasOpportunity(asNot(e)))
          opportunities.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final InfixExpression e) {
        final Wring w = Wrings.find(e);
        return w != null && w.noneligible(e) ? true : overrideInto(e, opportunities);
      }
      private boolean hasOpportunity(final PrefixExpression e) {
        return e == null ? false : hasOpportunity(getCore(e.getOperand()));
      }
      private boolean hasOpportunity(final Expression inner) {
        return Is.booleanLiteral(inner) || asNot(inner) != null || asAndOrOr(inner) != null || asComparison(inner) != null;
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring s = Wrings.find(e);
        if (s != null)
          return s.go(r, e);
        return true;
      }
      @Override public boolean visit(final PrefixExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring s = Wrings.find(e);
        if (s != null)
          return s.go(r, e);
        return true;
      }
    });
  }
  static boolean overrideInto(final InfixExpression e, final List<Range> rs) {
    return overrideInto(new Range(e), rs);
  }
  private static boolean overrideInto(final Range r, final List<Range> rs) {
    r.pruneIncluders(rs);
    rs.add(r);
    return true;
  }
  /**
   * Transpose infix expressions recursively. Makes the shortest operand first
   * on every subtree of the node.
   *
   * @param e
   *          The node.
   * @return Number of abstract syntax tree nodes under the parameter.
   */
  public static InfixExpression transpose(final AST t, final InfixExpression e) {
    final InfixExpression $ = flip(e);
    sortInfix($, t);
    return $;
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
      if (r.overlapping(rNew)) {
        rs.add(r.merge(rNew));
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
  static boolean moreArguments(final MethodInvocation i1, final MethodInvocation i2) {
    return i1.arguments().size() > i2.arguments().size();
  }
  static boolean sortInfix(final InfixExpression e, final AST t) {
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
    // $ = $ | sortExpressionList(eo, t, o);
    // | a, b, c, d, e, f - is the list after sorting
    e.setRightOperand(duplicate(eo.get(1)));
    e.setLeftOperand(duplicate(eo.get(0)));
    // (Left = a) (Right = b) | a, b, c, d, e, f - retrieve the operands
    eo.remove(1);
    eo.remove(0);
    // (Left = a) (Right = b) | c, d, e, f
    // if (longerFirst(e) && !inInfixExceptions(e)) {
    remake(e, duplicateLeft(e), flip(o), duplicateRight(e));
    $ = true;
    // }
    return $;
  }
}
