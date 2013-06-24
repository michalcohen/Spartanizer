package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
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

public class ShortestBranchRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Shortest Conditional Branch First";
  }
  
  /**
   * Count number of nodes in the tree of which node is root.
   * 
   * @param node
   *          The node.
   * @return Number of ast nodes under the node.
   */
  static int countNodes(final ASTNode node) {
    final AtomicInteger $ = new AtomicInteger(0);
    node.accept(new ASTVisitor() {
      @Override public void preVisit(final ASTNode n) {
        $.incrementAndGet();
      }
    });
    return $.get();
  }
  
  @Override protected ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m) {
    if (pm != null)
      pm.beginTask("Creating rewrite operation...", 1);
    final AST ast = cu.getAST();
    final ASTRewrite $ = ASTRewrite.create(ast);
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement node) {
        if (m == null && isNodeOutsideSelection(node))
          return true;
        if (m != null && isNodeOutsideMarker(node, m))
          return true;
        if (node.getElseStatement() == null)
          return true;
        if (countNodes(node.getThenStatement()) - countNodes(node.getElseStatement()) <= -threshold)
          return true;
        final IfStatement newnode = ast.newIfStatement();
        final Expression neg = negateExpression(ast, $, node.getExpression());
        newnode.setExpression(neg);
        newnode.setThenStatement((org.eclipse.jdt.core.dom.Statement) $.createMoveTarget(node.getElseStatement()));
        newnode.setElseStatement((org.eclipse.jdt.core.dom.Statement) $.createMoveTarget(node.getThenStatement()));
        $.replace(node, newnode, null);
        return true;
      }
      
      @Override public boolean visit(final ConditionalExpression node) {
        if (m == null && isNodeOutsideSelection(node))
          return true;
        if (m != null && isNodeOutsideMarker(node, m))
          return true;
        if (node.getElseExpression() == null)
          return true;
        if (node.getThenExpression().getLength() - node.getElseExpression().getLength() <= -threshold)
          return true;
        final ConditionalExpression newnode = ast.newConditionalExpression();
        final Expression neg = negateExpression(ast, $, node.getExpression());
        newnode.setExpression(neg);
        newnode.setThenExpression((Expression) $.createMoveTarget(node.getElseExpression()));
        newnode.setElseExpression((Expression) $.createMoveTarget(node.getThenExpression()));
        $.replace(node, newnode, null);
        return true;
      }
    });
    if (pm != null)
      pm.done();
    return $;
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
  
  private static Expression tryNegateComparison(final AST ast, final ASTRewrite rewrite, final InfixExpression exp) {
    final InfixExpression $ = ast.newInfixExpression();
    $.setRightOperand((Expression) rewrite.createCopyTarget(exp.getRightOperand()));
    $.setLeftOperand((Expression) rewrite.createCopyTarget(exp.getLeftOperand()));
    if (exp.getOperator().equals(Operator.EQUALS)) {
      $.setOperator(Operator.NOT_EQUALS);
      return $;
    }
    if (exp.getOperator().equals(Operator.NOT_EQUALS)) {
      $.setOperator(Operator.EQUALS);
      return $;
    }
    if (exp.getOperator().equals(Operator.GREATER)) {
      $.setOperator(Operator.LESS_EQUALS);
      return $;
    }
    if (exp.getOperator().equals(Operator.GREATER_EQUALS)) {
      $.setOperator(Operator.LESS);
      return $;
    }
    if (exp.getOperator().equals(Operator.LESS)) {
      $.setOperator(Operator.GREATER_EQUALS);
      return $;
    }
    if (exp.getOperator().equals(Operator.LESS_EQUALS)) {
      $.setOperator(Operator.GREATER);
      return $;
    }
    return null;
  }
  
  private static Expression tryNegatePrefix(final ASTRewrite rewrite, final PrefixExpression exp) {
    if (exp.getOperator().equals(PrefixExpression.Operator.NOT))
      return (Expression) rewrite.createCopyTarget(exp.getOperand());
    return null;
  }
  
  private static final int threshold = 1;
  
  @Override public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    final Collection<SpartanizationRange> $ = new ArrayList<SpartanizationRange>();
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement node) {
        if (node.getElseStatement() == null)
          return true;
        if (countNodes(node.getThenStatement()) - countNodes(node.getElseStatement()) > threshold)
          $.add(new SpartanizationRange(node));
        return true;
      }
      
      @Override public boolean visit(final ConditionalExpression node) {
        if (node.getElseExpression() == null)
          return true;
        if (node.getThenExpression().getLength() - node.getElseExpression().getLength() > threshold)
          $.add(new SpartanizationRange(node));
        return true;
      }
    });
    return $;
  }
}
