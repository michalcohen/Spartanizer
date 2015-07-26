package org.spartan.refactoring.spartanizations;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.PLUS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.TIMES;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.XOR;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.spartan.refactoring.utils.Funcs.asAndOrOr;
import static org.spartan.refactoring.utils.Funcs.asBooleanLiteral;
import static org.spartan.refactoring.utils.Funcs.asComparison;
import static org.spartan.refactoring.utils.Funcs.asConditionalExpression;
import static org.spartan.refactoring.utils.Funcs.asInfixExpression;
import static org.spartan.refactoring.utils.Funcs.asNot;
import static org.spartan.refactoring.utils.Funcs.asPrefixExpression;
import static org.spartan.refactoring.utils.Funcs.duplicate;
import static org.spartan.refactoring.utils.Funcs.flip;
import static org.spartan.refactoring.utils.Funcs.makeParenthesizedExpression;
import static org.spartan.refactoring.utils.Funcs.makePrefixExpression;
import static org.spartan.refactoring.utils.Funcs.removeAll;
import static org.spartan.refactoring.utils.Restructure.conjugate;
import static org.spartan.refactoring.utils.Restructure.flatten;
import static org.spartan.refactoring.utils.Restructure.getCore;
import static org.spartan.refactoring.utils.Restructure.refitOperands;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Are;
import org.spartan.refactoring.utils.Have;
import org.spartan.refactoring.utils.Is;

/**
 * This enum represents an ordered list of all {@link Wring} objects.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public enum Wrings {
  /**
   * A {@link Wring} that eliminates redundant comparison with the two boolean
   * literals: <code><b>true</b></code> and <code><b>false</b></code>.
   *
   * @author Yossi Gil
   * @since 2015-07-17
   */
  COMPARISON_WITH_BOOLEAN(new Wring.OfInfixExpression() {
    @Override public final boolean scopeIncludes(final InfixExpression e) {
      return in(e.getOperator(), EQUALS, NOT_EQUALS) && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
    }
    @Override boolean _eligible(final InfixExpression e) {
      assert scopeIncludes(e);
      return true;
    }
    @Override Expression _replacement(final InfixExpression e) {
      Expression nonliteral;
      BooleanLiteral literal;
      if (Is.booleanLiteral(e.getLeftOperand())) {
        literal = asBooleanLiteral(e.getLeftOperand());
        nonliteral = duplicate(e.getRightOperand());
      } else {
        literal = asBooleanLiteral(e.getRightOperand());
        nonliteral = duplicate(e.getLeftOperand());
      }
      return nonNegating(e, literal) ? nonliteral : negate(nonliteral);
    }
    private PrefixExpression negate(final ASTNode e) {
      return makePrefixExpression(makeParenthesizedExpression((Expression) e), NOT);
    }
    private boolean nonNegating(final InfixExpression e, final BooleanLiteral literal) {
      return literal.booleanValue() == (e.getOperator() == EQUALS);
    }
  }), //
  /**
   * A {@link Wring} that reorder comparisons so that the specific value is
   * placed on the right. Specific value means a literal, or any of the two
   * keywords <code><b>this</b></code> or <code><b>null</b></code>.
   *
   * @author Yossi Gil
   * @since 2015-07-17
   */
  COMPARISON_WITH_SPECIFIC(new Wring.OfInfixExpression() {
    @Override public String toString() {
      return "Comparison WITH SPECIFIC";
    }
    @Override public boolean scopeIncludes(final InfixExpression e) {
      return Is.comparison(e) && (hasThisOrNull(e) || hasOneSpecificArgument(e));
    }
    @Override boolean _eligible(final InfixExpression e) {
      return Is.specific(e.getLeftOperand());
    }
    @Override Expression _replacement(final InfixExpression e) {
      return flip(e);
    }
    boolean hasThisOrNull(final InfixExpression e) {
      return Is.thisOrNull(e.getLeftOperand()) || Is.thisOrNull(e.getRightOperand());
    }
    private boolean hasOneSpecificArgument(final InfixExpression e) {
      // One of the arguments must be specific, the other must not be.
      return Is.specific(e.getLeftOperand()) != Is.specific(e.getRightOperand());
    }
  }), //
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
  TERNARY_BOOLEAN_LITERAL(new Wring.OfConditionalExpression() {
    @Override public String toString() {
      return "TERNARY_BOOLEAN_LITERAL";
    }
    @Override boolean scopeIncludes(final ConditionalExpression e) {
      return isTernaryOfBooleanLitreral(e);
    }
    @Override boolean _eligible(@SuppressWarnings("unused") final ConditionalExpression _) {
      return true;
    }
    @Override Expression _replacement(final ConditionalExpression e) {
      return simplifyTernary(e);
    }
  }), //
  /**
   * A {@link Wring} that eliminate Boolean literals, when possible present on
   * logical AND an logical OR.
   *
   * @author Yossi Gil
   * @since 2015-07-20
   */
  AND_TRUE(new Wring.OfInfixExpression() {
    @Override public String toString() {
      return "&& true";
    }
    @Override boolean scopeIncludes(final InfixExpression e) {
      return Is.conditionalAnd(e) && Have.trueLiteral(All.operands(flatten(e)));
    }
    @Override boolean _eligible(@SuppressWarnings("unused") final InfixExpression _) {
      return true;
    }
    @Override Expression _replacement(final InfixExpression e) {
      return eliminateLiteral(e, true);
    }
  }), //
  /**
   * A {@link Wring} that eliminate Boolean literals, when possible present on
   * logical AND an logical OR.
   *
   * @author Yossi Gil
   * @since 2015-07-20
   */
  OR_FALSE(new Wring.OfInfixExpression() {
    @Override public String toString() {
      return "|| true";
    }
    @Override boolean scopeIncludes(final InfixExpression e) {
      return Is.conditionalOr(e) && Have.falseLiteral(All.operands(flatten(e)));
    }
    @Override boolean _eligible(@SuppressWarnings("unused") final InfixExpression _) {
      return true;
    }
    @Override Expression _replacement(final InfixExpression e) {
      return eliminateLiteral(e, false);
    }
  }), //
  /**
   * A {@link Wring} that sorts the arguments of a {@link Operator#PLUS}
   * expression. Extra care is taken to leave intact the use of
   * {@link Operator#PLUS} for the concatenation of {@link String}s.
   *
   * @author Yossi Gil
   * @since 2015-07-17
   */
  ADDITION_SORTER(new Wring.OfInfixExpression() {
    @Override public String toString() {
      return "Addition sorter";
    }
    @Override boolean scopeIncludes(final InfixExpression e) {
      return e.getOperator() == PLUS;
    }
    @Override boolean _eligible(final InfixExpression e) {
      return Are.notString(All.operands(flatten(e))) && tryToSort(e);
    }
    private boolean tryToSort(final InfixExpression e) {
      return tryToSort(All.operands(flatten(e)));
    }
    private boolean tryToSort(final List<Expression> es) {
      return Wrings.tryToSort(es, ExpressionComparator.ADDITION);
    }
    @Override Expression _replacement(final InfixExpression e) {
      final List<Expression> operands = All.operands(flatten(e));
      return !Are.notString(operands) || !tryToSort(operands) ? null : refitOperands(e, operands);
    }
  }), //
  /**
   * A {@link Wring} that sorts the arguments of an expression using the same
   * sorting order as {@link Operator#PLUS} expression, except that we do not
   * worry about commutativity. Unlike {@link #ADDITION_SORTER}, we know that
   * the reordering is always possible.
   *
   * @see #ADDITION_SORTER
   * @author Yossi Gil
   * @since 2015-07-17
   */
  PSEUDO_ADDITION_SORTER(new Wring.OfInfixExpression() {
    @Override public String toString() {
      return "pseudo addition sorter";
    }
    @Override boolean scopeIncludes(final InfixExpression e) {
      return in(e.getOperator(), OR);
    }
    @Override boolean _eligible(final InfixExpression e) {
      return tryToSort(e);
    }
    private boolean tryToSort(final InfixExpression e) {
      return tryToSort(All.operands(flatten(e)));
    }
    private boolean tryToSort(final List<Expression> es) {
      return Wrings.tryToSort(es, ExpressionComparator.ADDITION);
    }
    @Override Expression _replacement(final InfixExpression e) {
      final List<Expression> operands = All.operands(flatten(e));
      return !tryToSort(operands) ? null : refitOperands(e, operands);
    }
  }), //
  /**
   * A {@link Wring} that sorts the arguments of a {@link Operator#PLUS}
   * expression. Extra care is taken to leave intact the use of
   * {@link Operator#PLUS} for the concatenation of {@link String}s.
   *
   * @author Yossi Gil
   * @since 2015-07-17
   */
  MULTIPLICATION_SORTER(new Wring.OfInfixExpression() {
    @Override public String toString() {
      return "Multiplication sorter";
    }
    @Override boolean scopeIncludes(final InfixExpression e) {
      return in(e.getOperator(), TIMES, XOR, AND);
    }
    @Override boolean _eligible(final InfixExpression e) {
      return tryToSort(e);
    }
    private boolean tryToSort(final InfixExpression e) {
      return tryToSort(All.operands(flatten(e)));
    }
    private boolean tryToSort(final List<Expression> es) {
      return Wrings.tryToSort(es, ExpressionComparator.MULTIPLICATION);
    }
    @Override Expression _replacement(final InfixExpression e) {
      final List<Expression> operands = All.operands(flatten(e));
      return !tryToSort(operands) ? null : refitOperands(e, operands);
    }
  }), //
  /**
   * A {@link Wring} that pushes down "<code>!</code>", the negation operator as
   * much as possible, using the de-Morgan and other simplification rules.
   *
   * @author Yossi Gil
   * @since 2015-7-17
   */
  PUSHDOWN_NOT(new Wring.OfPrefixExpression() {
    @Override public String toString() {
      return "Pushdown not";
    }
    @Override public boolean scopeIncludes(final PrefixExpression e) {
      return e != null && asNot(e) != null && hasOpportunity(asNot(e));
    }
    @Override boolean _eligible(final PrefixExpression e) {
      return true;
    }
    @Override Expression _replacement(final PrefixExpression e) {
      return pushdownNot(asNot(e));
    }
  }),
  //
  ;
  final Wring inner;
  Wrings(final Wring inner) {
    this.inner = inner;
  }
  /**
   * Find the first {@link Wring} appropriate for an {@link InfixExpression}
   *
   * @param e JD
   * @return the first {@link Wring} for which the parameter is eligible, or
   *         <code><b>null</b></code>i if no such {@link Wring} is found.
   */
  public static Wring find(final Expression e) {
    Wring $;
    return ($ = find(asInfixExpression(e))) != null//
        || ($ = find(asPrefixExpression(e))) != null//
        || ($ = find(asConditionalExpression(e))) != null//
            //
            ? $ : null;
  }
  /**
   * Find the first {@link Wring} appropriate for an {@link InfixExpression}
   *
   * @param e JD
   * @return the first {@link Wring} for which the parameter is eligible, or
   *         <code><b>null</b></code>i if no such {@link Wring} is found.
   */
  public static Wring find(final ConditionalExpression e) {
    if (e == null)
      return null;
    for (final Wrings s : values())
      if (s.inner.scopeIncludes(e))
        return s.inner;
    return null;
  }
  /**
   * Find the first {@link Wring} appropriate for an {@link InfixExpression}
   *
   * @param e JD
   * @return the first {@link Wring} for which the parameter is eligible, or
   *         <code><b>null</b></code>i if no such {@link Wring} is found.
   */
  public static Wring find(final InfixExpression e) {
    if (e == null)
      return null;
    for (final Wrings s : values())
      if (s.inner.scopeIncludes(e))
        return s.inner;
    return null;
  }
  /**
   * Find the first {@link Wring} appropriate for a {@link PrefixExpression}
   *
   * @param e JD
   * @return the first {@link Wring} for which the parameter is eligible, or
   *         <code><b>null</b></code>i if no such {@link Wring} is found.
   */
  public static Wring find(final PrefixExpression e) {
    if (e == null)
      return null;
    for (final Wrings s : Wrings.values())
      if (s.inner.scopeIncludes(e))
        return s.inner;
    return null;
  }
  static boolean tryToSort(final List<Expression> es, final java.util.Comparator<Expression> c) {
    boolean $ = false;
    // Bubble sort, duplicating in each case of swap
    for (int i = 0, size = es.size(); i < size; i++)
      for (int j = 0; j < size - 1; j++) {
        final Expression e0 = es.get(j);
        final Expression e1 = es.get(j + 1);
        if (c.compare(e0, e1) <= 0)
          continue;
        // Replace locations i,j with e0 and e1
        es.get(j + 1);
        es.remove(j);
        es.remove(j);
        es.add(j, e0);
        es.add(j, e1);
        $ = true;
      }
    return $;
  }
  static Expression pushdownNot(final PrefixExpression e) {
    return e == null ? null : pushdownNot(getCore(e.getOperand()));
  }
  static Expression pushdownNot(final Expression inner) {
    Expression $;
    return ($ = perhapsNotOfLiteral(inner)) != null//
        || ($ = perhapsDoubleNegation(inner)) != null//
        || ($ = perhapsDeMorgan(inner)) != null//
        || ($ = perhapsComparison(inner)) != null //
            ? $ : null;
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
    return simplifyTernary(getCore(e.getThenExpression()), getCore(e.getElseExpression()), duplicate(e.getExpression()));
  }
  private static Expression simplifyTernary(final Expression then, final Expression elze, final Expression main) {
    final boolean takeThen = !Is.booleanLiteral(then);
    final InfixExpression $ = makeWithOther(takeThen ? then : elze);
    final boolean literal = asBooleanLiteral(takeThen ? elze : then).booleanValue();
    $.setOperator(literal ? CONDITIONAL_OR : CONDITIONAL_AND);
    $.setLeftOperand(takeThen != literal ? main : not(main));
    return $;
  }
  private static InfixExpression makeWithOther(final Expression other) {
    final InfixExpression $ = other.getAST().newInfixExpression();
    $.setRightOperand(duplicate(other));
    return $;
  }
  static boolean haveTernaryOfBooleanLitreral(final List<Expression> es) {
    for (final Expression e : es)
      if (isTernaryOfBooleanLitreral(e))
        return true;
    return false;
  }
  static boolean isTernaryOfBooleanLitreral(final Expression e) {
    return isTernaryOfBooleanLitreral(asConditionalExpression(getCore(e)));
  }
  static boolean isTernaryOfBooleanLitreral(final ConditionalExpression e) {
    return e != null && Have.booleanLiteral(getCore(e.getThenExpression()), getCore(e.getElseExpression()));
  }
  static Expression perhapsNotOfLiteral(final Expression inner) {
    return !Is.booleanLiteral(inner) ? null : notOfLiteral(asBooleanLiteral(inner));
  }
  static Expression notOfLiteral(final BooleanLiteral l) {
    final BooleanLiteral $ = duplicate(l);
    $.setBooleanValue(!l.booleanValue());
    return $;
  }
  static Expression perhapsDoubleNegation(final Expression inner) {
    return perhapsDoubleNegation(asNot(inner));
  }
  static Expression perhapsDoubleNegation(final PrefixExpression inner) {
    return inner == null ? null : inner.getOperand();
  }
  static Expression perhapsDeMorgan(final Expression e) {
    return perhapsDeMorgan(asAndOrOr(e));
  }
  static Expression perhapsDeMorgan(final InfixExpression e) {
    return e == null ? null : deMorgan(e, getCoreLeft(e), getCoreRight(e));
  }
  static Expression deMorgan(final InfixExpression inner, final Expression left, final Expression right) {
    return deMorgan1(inner, parenthesize(left), parenthesize(right));
  }
  static Expression deMorgan1(final InfixExpression inner, final Expression left, final Expression right) {
    return parenthesize( //
        addExtendedOperands(inner, //
            makeInfixExpression(not(left), conjugate(inner), not(right))));
  }
  static InfixExpression addExtendedOperands(final InfixExpression from, final InfixExpression $) {
    if (from.hasExtendedOperands())
      addExtendedOperands(from.extendedOperands(), $.extendedOperands());
    return $;
  }
  static void addExtendedOperands(final List<Expression> from, final List<Expression> to) {
    for (final Expression e : from)
      to.add(not(e));
  }
  static Expression perhapsComparison(final Expression inner) {
    return perhapsComparison(asComparison(inner));
  }
  static Expression perhapsComparison(final InfixExpression inner) {
    return inner == null ? null : comparison(inner);
  }
  static Expression comparison(final InfixExpression inner) {
    return cloneInfixChangingOperator(inner, ShortestBranchFirst.negate(inner.getOperator()));
  }
  static InfixExpression cloneInfixChangingOperator(final InfixExpression e, final Operator o) {
    return e == null ? null : makeInfixExpression(getCoreLeft(e), o, getCoreRight(e));
  }
  static Expression parenthesize(final Expression e) {
    if (Is.simple(e))
      return duplicate(e);
    final ParenthesizedExpression $ = e.getAST().newParenthesizedExpression();
    $.setExpression(duplicate(getCore(e)));
    return $;
  }
  static PrefixExpression not(final Expression e) {
    final PrefixExpression $ = e.getAST().newPrefixExpression();
    $.setOperator(NOT);
    $.setOperand(parenthesize(e));
    return $;
  }
  static InfixExpression makeInfixExpression(final Expression left, final Operator o, final Expression right) {
    final InfixExpression $ = left.getAST().newInfixExpression();
    $.setLeftOperand(duplicate(left));
    $.setOperator(o);
    $.setRightOperand(duplicate(right));
    return $;
  }
  static Expression getCoreRight(final InfixExpression e) {
    return getCore(e.getRightOperand());
  }
  static Expression getCoreLeft(final InfixExpression e) {
    return getCore(e.getLeftOperand());
  }
  static boolean hasOpportunity(final PrefixExpression e) {
    return e != null && hasOpportunity(getCore(e.getOperand()));
  }
  static boolean hasOpportunity(final Expression inner) {
    return Is.booleanLiteral(inner) || asNot(inner) != null || asAndOrOr(inner) != null || asComparison(inner) != null;
  }
  static Expression eliminateLiteral(final InfixExpression e, final boolean b) {
    final List<Expression> operands = All.operands(flatten(e));
    removeAll(b, operands);
    switch (operands.size()) {
      case 0:
        return e.getAST().newBooleanLiteral(b);
      case 1:
        return duplicate(operands.get(0));
      default:
        return refitOperands(e, operands);
    }
  }
}
