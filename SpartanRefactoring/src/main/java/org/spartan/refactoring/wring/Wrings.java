package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.spartan.refactoring.utils.Extract.core;
import static org.spartan.refactoring.utils.Funcs.asAndOrOr;
import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.asBooleanLiteral;
import static org.spartan.refactoring.utils.Funcs.asComparison;
import static org.spartan.refactoring.utils.Funcs.asConditionalExpression;
import static org.spartan.refactoring.utils.Funcs.asNot;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.not;
import static org.spartan.refactoring.utils.Funcs.removeAll;
import static org.spartan.refactoring.utils.Restructure.duplicateInto;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Have;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;

/**
 * This enum represents an ordered list of all {@link Wring} objects.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public enum Wrings {
  /**
   * <code>
   * a ? b : c
   * </code> is the same as <code>
   * (a && b) || (!a && c)
   * </code> if b is false than: <code>
   * (a && false) || (!a && c) == (!a && c)
   * </code> if b is true than: <code>
   * (a && true) || (!a && c) == a || (!a && c) == a || c
   * </code> if c is false than: <code>
   * (a && b) || (!a && false) == (!a && c)
   * </code> if c is true than <code>
   * (a && b) || (!a && true) == (a && b) || (!a) == !a || b
   * </code> keywords <code><b>this</b></code> or <code><b>null</b></code>.
   *
   * @author Yossi Gil
   * @since 2015-07-20
   */
  //
  ;



  public static boolean sort(final List<Expression> es, final java.util.Comparator<Expression> c) {
    boolean $ = false;
    // Bubble sort
    for (int i = 0, size = es.size(); i < size; i++)
      for (int j = 0; j < size - 1; j++) {
        final Expression e0 = es.get(j);
        final Expression e1 = es.get(j + 1);
        if (c.compare(e0, e1) <= 0)
          continue;
        // Replace locations i,j with e0 and e1
        es.remove(j);
        es.remove(j);
        es.add(j, e0);
        es.add(j, e1);
        $ = true;
      }
    return $;
  }
  private static Expression simplifyTernary(final Expression then, final Expression elze, final Expression main) {
    final boolean takeThen = !Is.booleanLiteral(then);
    final Expression other = takeThen ? then : elze;
    final boolean literal = asBooleanLiteral(takeThen ? elze : then).booleanValue();
    return Subject.pair(literal != takeThen ? main : not(main), other).to(literal ? CONDITIONAL_OR : CONDITIONAL_AND);
  }
  static Expression eliminateLiteral(final InfixExpression e, final boolean b) {
    final List<Expression> operands = allOperands(e);
    removeAll(b, operands);
    switch (operands.size()) {
      case 0:
        return e.getAST().newBooleanLiteral(b);
      case 1:
        return duplicate(operands.get(0));
      default:
        return Subject.operands(operands).to(e.getOperator());
    }
  }
  static boolean elseIsEmpty(final IfStatement s) {
    return Extract.statements(s.getElseStatement()).size() == 0;
  }
  static boolean existingEmptyElse(final IfStatement s) {
    return s.getElseStatement() != null && elseIsEmpty(s);
  }
  static boolean hasOpportunity(final Expression inner) {
    return Is.booleanLiteral(inner) || asNot(inner) != null || asAndOrOr(inner) != null || asComparison(inner) != null;
  }
  static boolean hasOpportunity(final PrefixExpression e) {
    return e != null && hasOpportunity(core(e.getOperand()));
  }
  static boolean haveTernaryOfBooleanLitreral(final List<Expression> es) {
    for (final Expression e : es)
      if (isTernaryOfBooleanLitreral(e))
        return true;
    return false;
  }
  static boolean isTernaryOfBooleanLitreral(final ConditionalExpression e) {
    return e != null && Have.booleanLiteral(core(e.getThenExpression()), core(e.getElseExpression()));
  }
  static boolean isTernaryOfBooleanLitreral(final Expression e) {
    return isTernaryOfBooleanLitreral(asConditionalExpression(core(e)));
  }
  static int length(final ASTNode... ns) {
    int $ = 0;
    for (final ASTNode n : ns)
      $ += n.toString().length();
    return $;
  }
  static VariableDeclarationFragment makeVariableDeclarationFragement(final VariableDeclarationFragment f, final Expression e) {
    final VariableDeclarationFragment $ = duplicate(f);
    $.setInitializer(duplicate(e));
    return $;
  }
  static ASTRewrite removeStatement(final ASTRewrite r, final Statement s) {
    final Block parent = asBlock(s.getParent());
    final List<Statement> siblings = Extract.statements(parent);
    siblings.remove(siblings.indexOf(s));
    final Block newParent$ = parent.getAST().newBlock();
    duplicateInto(siblings, newParent$.statements());
    r.replace(parent, newParent$, null);
    return r;
  }
  static Statement reorganizeNestedStatement(final Statement s) {
    final List<Statement> ss = Extract.statements(s);
    switch (ss.size()) {
      case 0:
        return s.getAST().newEmptyStatement();
      case 1:
        return duplicate(ss.get(0));
      default:
        return reorganizeStatement(s);
    }
  }
  static Block reorganizeStatement(final Statement s) {
    final List<Statement> ss = Extract.statements(s);
    final Block $ = s.getAST().newBlock();
    duplicateInto(ss, $.statements());
    return $;
  }
  static ASTRewrite replaceTwoStatements(final ASTRewrite r, final Statement what, final Statement by) {
    final Block parent = asBlock(what.getParent());
    final List<Statement> siblings = Extract.statements(parent);
    final int i = siblings.indexOf(what);
    siblings.remove(i);
    siblings.remove(i);
    siblings.add(i, by);
    final Block $ = parent.getAST().newBlock();
    duplicateInto(siblings, $.statements());
    r.replace(parent, $, null);
    return r;
  }
  /**
   * Consider an expression <code> a ? b : c </code>; in a sense it is the same
   * as <code> (a && b) || (!a && c) </code>
   * <ol>
   * <li>if b is false then: <code>
   * (a && false) || (!a && c) == !a && c </code>
   * <li>if b is true then:
   * <code>(a && true) || (!a && c) == a || (!a && c) == a || c </code>
   * <li>if c is false then: <code>(a && b) || (!a && false) == a && b </code>
   * <li>if c is true then <code>(a && b) || (!a && true) == !a || b</code>
   * </ol>
   */
  static Expression simplifyTernary(final ConditionalExpression e) {
    return simplifyTernary(core(e.getThenExpression()), core(e.getElseExpression()), duplicate(e.getExpression()));
  }
  /* <code> a ? b : c </code>
   *
   * is the same as
   *
   * <code> (a && b) || (!a && c) </code>
   *
   * if b is false than:
   *
   * <code> (a && false) || (!a && c) == (!a && c) </code>
   *
   * if b is true than:
   *
   * <code> (a && true) || (!a && c) == a || (!a && c) == a || c </code>
   *
   * if c is false than:
   *
   * <code> (a && b) || (!a && false) == (!a && c) </code>
   *
   * if c is true than
   *
   * <code> (a && b) || (!a && true) == (a && b) || (!a) == !a || b </code> */
  static void simplifyTernary(final List<Expression> es) {
    for (int i = 0; i < es.size(); ++i) {
      final Expression e = es.get(i);
      if (!isTernaryOfBooleanLitreral(e))
        continue;
      es.remove(i);
      es.add(i, simplifyTernary(asConditionalExpression(e)));
    }
  }
  public static List<Expression> allOperands(final InfixExpression e) {
    return All.operands(flatten(e));
  }
  public final Wring inner;
  Wrings(final Wring inner) {
    this.inner = inner;
  }
}
