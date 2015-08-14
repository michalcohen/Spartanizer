package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.wring.ExpressionComparator.countNodes;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;
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
    super("Shortest Branch First", "Negate the expression of a conditional, and change the order of branches so that shortest branch occurs first");
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
        final ConditionalExpression newCondExp = transpose(n);
        if (newCondExp != null)
          r.replace(n, newCondExp, null);
        return true;
      }
      private IfStatement transpose(final IfStatement s) {
        final Expression negatedOp = not(s.getExpression());
        if (negatedOp == null)
          return null;
        final Statement elseStmnt = s.getElseStatement();
        final Statement thenStmnt = s.getThenStatement();
        if ( Extract.ifStatement(elseStmnt) != null) {
          final Block newElseBlock = t.newBlock();
          newElseBlock.statements().add(r.createCopyTarget(elseStmnt));
          return Subject.pair(newElseBlock, thenStmnt).toIf(negatedOp);
        }
        return Subject.pair(elseStmnt, thenStmnt).toIf(negatedOp);
      }
      private ConditionalExpression transpose(final ConditionalExpression e) {
        return e == null ? null : Subject.pair(e.getElseExpression(), e.getThenExpression()).toCondition(not(e.getExpression()));
      }
    });
  }

  private static final int threshold = 1;
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        if (longerFirst(n))
          $.add(new Range(n));
        return true;
      }
      @Override public boolean visit(final ConditionalExpression n) {
        if (longerFirst(n))
          $.add(new Range(n));
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
