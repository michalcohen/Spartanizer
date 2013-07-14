package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <boris.van.sosin@gmail.com> (v2)
 * 
 * @since 2013/01/01
 */
public class ShortestBranchRefactoring extends BaseSpartanization {
  /** Instantiates this class */
  public ShortestBranchRefactoring() {
    super("Shortester first",
        "Negate the expression of a conditional, and change the order of branches so that shortest branch occurs first");
  }
  
  /**
   * Counts the number of nodes in the tree of which node is root.
   * 
   * @param n
   *          The node.
   * @return Number of abstract syntax tree nodes under the parameter.
   */
  static int countNodes(final ASTNode n) {
    final AtomicInteger $ = new AtomicInteger(0);
    n.accept(new ASTVisitor() {
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
       * @param _
       *          ignored
       */
      @Override public void preVisit(final ASTNode _) {
        $.incrementAndGet();
      }
    });
    return $.get();
  }
  
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        if (!inRange(m, n))
          return true;
        if (longerFirst(n))
          r.replace(n, transpose(n), null);
        return true;
      }
      
      @Override public boolean visit(final ConditionalExpression n) {
        if (!inRange(m, n))
          return true;
        if (longerFirst(n))
          r.replace(n, transpose(n), null);
        return true;
      }
      
      private IfStatement transpose(final IfStatement n) {
        final IfStatement $ = t.newIfStatement();
        $.setExpression(negate(t, r, n.getExpression()));
        $.setThenStatement((Statement) r.createMoveTarget(n.getElseStatement()));
        $.setElseStatement((Statement) r.createMoveTarget(n.getThenStatement()));
        return $;
      }
      
      private ConditionalExpression transpose(final ConditionalExpression n) {
        final ConditionalExpression $ = t.newConditionalExpression();
        $.setExpression(negate(t, r, n.getExpression()));
        $.setThenExpression((Expression) r.createMoveTarget(n.getElseExpression()));
        $.setElseExpression((Expression) r.createMoveTarget(n.getThenExpression()));
        return $;
      }
    });
  }
  
  /**
   * @return a prefix expression that is the negation of the provided
   *         expression.
   */
  static Expression negate(final AST t, final ASTRewrite r, final Expression e) {
    Expression negatedComparison = null;
    if (e instanceof InfixExpression && (negatedComparison = tryNegateComparison(t, r, (InfixExpression) e)) != null)
      return negatedComparison;
    Expression negatedNot = null;
    if (e instanceof PrefixExpression && (negatedNot = tryNegatePrefix(r, (PrefixExpression) e)) != null)
      return negatedNot;
    final PrefixExpression $ = t.newPrefixExpression();
    $.setOperand(parenthesize(t, r, e));
    $.setOperator(PrefixExpression.Operator.NOT);
    return $;
  }
  
  private static ParenthesizedExpression parenthesize(final AST t, final ASTRewrite r, final Expression e) {
    final ParenthesizedExpression $ = t.newParenthesizedExpression();
    $.setExpression((Expression) r.createCopyTarget(e));
    return $;
  }
  
  private static Expression tryNegateComparison(final AST ast, final ASTRewrite rewrite, final InfixExpression e) {
    final Operator o = invert(e.getOperator());
    if (o == null)
      return null;
    final InfixExpression $ = ast.newInfixExpression();
    $.setRightOperand((Expression) rewrite.createCopyTarget(e.getRightOperand()));
    $.setLeftOperand((Expression) rewrite.createCopyTarget(e.getLeftOperand()));
    $.setOperator(o);
    return $;
  }
  
  private static Operator invert(final Operator o) {
    if (o.equals(EQUALS))
      return NOT_EQUALS;
    if (o.equals(NOT_EQUALS))
      return EQUALS;
    if (o.equals(LESS))
      return GREATER_EQUALS;
    if (o.equals(GREATER))
      return LESS_EQUALS;
    if (o.equals(LESS_EQUALS))
      return GREATER;
    if (o.equals(GREATER_EQUALS))
      return LESS;
    return null;
  }
  
  private static Expression tryNegatePrefix(final ASTRewrite rewrite, final PrefixExpression exp) {
    if (exp.getOperator().equals(PrefixExpression.Operator.NOT))
      return (Expression) rewrite.createCopyTarget(exp.getOperand());
    return null;
  }
  
  private static final int threshold = 1;
  
  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        if (longerFirst(n))
          opportunities.add(new Range(n));
        return true;
      }
      
      @Override public boolean visit(final ConditionalExpression n) {
        if (longerFirst(n))
          opportunities.add(new Range(n));
        return true;
      }
    };
  }
  
  static boolean longerFirst(final IfStatement n) {
    return n.getElseStatement() != null && countNodes(n.getThenStatement()) > countNodes(n.getElseStatement()) + threshold;
  }
  
  static boolean longerFirst(final ConditionalExpression n) {
    return n.getThenExpression().getLength() > n.getElseExpression().getLength() + threshold;
  }
}
