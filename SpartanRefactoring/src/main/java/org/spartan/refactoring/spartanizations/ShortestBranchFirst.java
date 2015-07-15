package org.spartan.refactoring.spartanizations;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.spartan.refacotring.utils.Funcs.countNodes;
import static org.spartan.refacotring.utils.Funcs.getBlockSingleStmnt;
import static org.spartan.refacotring.utils.Funcs.makeIfStmnt;
import static org.spartan.refacotring.utils.Funcs.makeInfixExpression;
import static org.spartan.refacotring.utils.Funcs.makeParenthesizedConditionalExp;
import static org.spartan.refacotring.utils.Funcs.makeParenthesizedExpression;
import static org.spartan.refacotring.utils.Funcs.makePrefixExpression;
import static org.spartan.refacotring.utils.Funcs.statementsCount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
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
import org.spartan.refacotring.utils.Is;
import org.spartan.utils.Range;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3)
 * @since 2013/01/01
 */
public class ShortestBranchFirst extends SpartanizationOfInfixExpression {
  /** Instantiates this class */
  public ShortestBranchFirst() {
    super("Shortest Branch First",
        "Negate the expression of a conditional, and change the order of branches so that shortest branch occurs first");
  }

  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        if (!inRange(m, n) || !longerFirst(n))
          return true;
        final IfStatement newIfStmnt = transpose(n);
        if (newIfStmnt != null)
          r.replace(n, newIfStmnt, null);
        return true;
      }

      @Override public boolean visit(final ConditionalExpression n) {
        if (!inRange(m, n) || !longerFirst(n))
          return true;
        final ParenthesizedExpression newCondExp = transpose(n);
        if (newCondExp != null)
          r.replace(n, newCondExp, null);
        return true;
      }

      private IfStatement transpose(final IfStatement n) {
        final Expression negatedOp = negate(t, r, n.getExpression());
        if (negatedOp == null)
          return null;
        final Statement elseStmnt = n.getElseStatement();
        final Statement thenStmnt = n.getThenStatement();
        if (statementsCount(elseStmnt) == 1 && ASTNode.IF_STATEMENT == getBlockSingleStmnt(elseStmnt).getNodeType()) {
          final Block newElseBlock = t.newBlock();
          newElseBlock.statements().add(r.createCopyTarget(elseStmnt));
          return makeIfStmnt(t, r, negatedOp, newElseBlock, thenStmnt);
        }
        return makeIfStmnt(t, r, negatedOp, elseStmnt, thenStmnt);
      }

      private ParenthesizedExpression transpose(final ConditionalExpression n) {
        return n == null ? null
            : makeParenthesizedConditionalExp(t, r, negate(t, r, n.getExpression()), n.getElseExpression(), n.getThenExpression());
      }
    });
  }

  /**
   * @return a prefix expression that is the negation of the provided
   *         expression.
   */
  static Expression negate(final AST t, final ASTRewrite r, final Expression e) {
    if (e instanceof InfixExpression)
      return tryNegateComparison(t, r, (InfixExpression) e);
    return e instanceof PrefixExpression ? tryNegatePrefix(r, (PrefixExpression) e)
        : makePrefixExpression(t, makeParenthesizedExpression(t, e), NOT);
  }

  private static Expression tryNegateComparison(final AST t, final ASTRewrite r, final InfixExpression e) {
    final Operator op = negate(e.getOperator());
    if (op == null)
      return null;
    return op != CONDITIONAL_AND && op != CONDITIONAL_OR //
        ? makeInfixExpression(r, t, e.getLeftOperand(), op, e.getRightOperand())//
        : makeInfixExpression(r, t, negateExp(t, r, e.getLeftOperand()), op, negateExp(t, r, e.getRightOperand()));
  }

  private static Expression negateExp(final AST t, final ASTRewrite r, final Expression e) {
    if (Is.infix(e))
      return makePrefixExpression(t, makeParenthesizedExpression(t, e), NOT);
    return !Is.isPrefix(e) || !((PrefixExpression) e).getOperator().equals(NOT) //
        ? makePrefixExpression(t, e, NOT) //
        : (Expression) r.createCopyTarget(((PrefixExpression) e).getOperand());
  }

  static Operator negate(final Operator o) {
    return !negate.containsKey(o) ? null : negate.get(o);
  }

  private static Map<Operator, Operator> makeNegation() {
    final Map<Operator, Operator> $ = new HashMap<>();
    $.put(EQUALS, NOT_EQUALS);
    $.put(NOT_EQUALS, EQUALS);
    $.put(LESS_EQUALS, GREATER);
    $.put(GREATER, LESS_EQUALS);
    $.put(LESS, GREATER_EQUALS);
    $.put(GREATER_EQUALS, LESS);
    $.put(CONDITIONAL_AND, CONDITIONAL_OR);
    $.put(CONDITIONAL_OR, CONDITIONAL_AND);
    return $;
  }

  private static Map<Operator, Operator> negate = makeNegation();

  private static Expression tryNegatePrefix(final ASTRewrite r, final PrefixExpression exp) {
    return !exp.getOperator().equals(NOT) ? null : (Expression) r.createCopyTarget(exp.getOperand());
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
    return n.getElseStatement() != null && countNodes(n.getThenStatement()) > threshold + countNodes(n.getElseStatement());
  }

  static boolean longerFirst(final ConditionalExpression n) {
    return n.getThenExpression().getLength() > threshold + n.getElseExpression().getLength();
  }
}
