package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.Range;

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
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <boris.van.sosin@gmail.com> (v2)
 * 
 * @since 2013/01/01
 */
public class ShortestBranchRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Negate the expression of a conditional, and change the order of branches so that shortest branch occurs first";
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
      @Override public boolean visit(final IfStatement node) {
        if (!inRange(m, node))
          return true;
        if (node.getElseStatement() == null)
          return true;
        if (countNodes(node.getThenStatement()) - countNodes(node.getElseStatement()) <= -threshold)
          return true;
        final IfStatement newnode = t.newIfStatement();
        final Expression neg = negateExpression(t, r, node.getExpression());
        newnode.setExpression(neg);
        newnode.setThenStatement((org.eclipse.jdt.core.dom.Statement) r.createMoveTarget(node.getElseStatement()));
        newnode.setElseStatement((org.eclipse.jdt.core.dom.Statement) r.createMoveTarget(node.getThenStatement()));
        r.replace(node, newnode, null);
        return true;
      }
      
      @Override public boolean visit(final ConditionalExpression node) {
        if (!inRange(m, node))
          return true;
        if (node.getElseExpression() == null)
          return true;
        if (node.getThenExpression().getLength() - node.getElseExpression().getLength() <= -threshold)
          return true;
        final ConditionalExpression newnode = t.newConditionalExpression();
        final Expression neg = negateExpression(t, r, node.getExpression());
        newnode.setExpression(neg);
        newnode.setThenExpression((Expression) r.createMoveTarget(node.getElseExpression()));
        newnode.setElseExpression((Expression) r.createMoveTarget(node.getThenExpression()));
        r.replace(node, newnode, null);
        return true;
      }
    });
  }
  
  /**
   * @return Returns a prefix expression that is the negation of the provided
   *         expression.
   */
  static Expression negateExpression(final AST ast, final ASTRewrite rewrite, final Expression exp) {
    Expression negatedComparison = null;
    if (exp instanceof InfixExpression && (negatedComparison = tryNegateComparison(ast, rewrite, (InfixExpression) exp)) != null)
      return negatedComparison;
    Expression negatedNot = null;
    if (exp instanceof PrefixExpression && (negatedNot = tryNegatePrefix(rewrite, (PrefixExpression) exp)) != null)
      return negatedNot;
    final ParenthesizedExpression paren = ast.newParenthesizedExpression();
    paren.setExpression((Expression) rewrite.createCopyTarget(exp));
    final PrefixExpression neg = ast.newPrefixExpression();
    neg.setOperand(paren);
    neg.setOperator(PrefixExpression.Operator.NOT);
    return neg;
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
  
  private static Operator invert(Operator o) {
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
        if (n.getElseStatement() == null)
          return true;
        if (countNodes(n.getThenStatement()) - countNodes(n.getElseStatement()) > threshold)
          opportunities.add(new Range(n));
        return true;
      }
      
      @Override public boolean visit(final ConditionalExpression n) {
        if (n.getElseExpression() == null)
          return true;
        if (n.getThenExpression().getLength() - n.getElseExpression().getLength() > threshold)
          opportunities.add(new Range(n));
        return true;
      }
    };
  }
}
