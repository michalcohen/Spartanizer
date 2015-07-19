package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Noneligible;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Wringed;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Are;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 *
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public enum ADDITION_SORTER_Test {
  ;
  static final Wring WRING = Wrings.ADDITION_SORTER.inner;
  static final ExpressionComparator COMPARATOR = ExpressionComparator.ADDITION;

  @RunWith(Parameterized.class) //
  public static class Noneligible extends AbstractWringTest.Noneligible.Infix {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Add 1", "2*a+1"), //
        Utils.asArray("Add '1'", "2*a+'1'"), //
        Utils.asArray("Add '\\0'", "3*a+'\\0'"), //
        Utils.asArray("Plain addition", "5*a+b*c"), //
        Utils.asArray("Plain addition plust constant", "5*a+b*c+12"), //
        Utils.asArray("Literal addition", "2+3"), //
        null);

    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = DESCRIPTION) //
    public static Collection<Object[]> cases() {
      return collect(cases);
    }
    /** Instantiates the enclosing class ({@link Noneligible}) */
    public Noneligible() {
      super(WRING);
    }
    @Test public void allNotStringArgument() {
      final InfixExpression e = asInfixExpression();
      assertTrue(Are.notString(All.operands(e)));
    }
    @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = Wrings.flatten(asInfixExpression());
      assertThat(Wrings.flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void isPlus() {
      final InfixExpression e = asInfixExpression();
      assertTrue(e.getOperator() == Operator.PLUS);
    }
    @Test public void tryToSort() {
      final InfixExpression e = asInfixExpression();
      assertFalse(Wrings.tryToSort(All.operands(Wrings.flatten(e)), COMPARATOR));
    }
    @Test public void tryToSortTwice() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(Wrings.flatten(e));
      assertFalse(Wrings.tryToSort(operands, COMPARATOR));
      assertFalse(Wrings.tryToSort(operands, COMPARATOR));
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.Wringed.Infix {
    private static String[][] cases = Utils.asArray(//
        Utils.asArray("Add 1 to 2*3", "1+2*3", "2*3+1"), //
        Utils.asArray("Add '1' to a*b", "'1'+a*b", "a*b+'1'"), //
        Utils.asArray("Add '\\0' to a*.b", "'\0'+a*b", "a*b+'\0'"), //
        Utils.asArray("Sort from first to last", "1 + a*b + b*c", "a*b+b*c+1"), //
        Utils.asArray("Sort from second to last", "a*b + 2 + b*c", "a*b+b*c+2"), //
        Utils.asArray("All literals at the end", "1 + a*b + 2 + b*c + 3 + d*e + 4", "a*b + b*c  + d*e + 1 + 2 + 3+4"), //
        Utils.asArray("Add 1", "1+a*b", "a*b+1"), //
        Utils.asArray("Add 1", "1+c*d", "c*d+1"), //
        Utils.asArray("Literals of distinct length", "123+12+1", "1+12+123"), //
        Utils.asArray("Sort expressions by size", "1*f(a,b,c,d) + 2*f(a,b) + 3*f()", "3*f() +2*f(a,b)+ 1*f(a,b,c,d)"), //
        null);

    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = DESCRIPTION) //
    public static Collection<Object[]> cases() {
      return collect(cases);
    }
    /**
     * Instantiates the enclosing class ({@link Wringed})
     */
    public Wringed() {
      super(WRING);
    }
    @Test public void allNotStringArgument() {
      final InfixExpression e = asInfixExpression();
      assertTrue(Are.notString(All.operands(e)));
    }
    @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = Wrings.flatten(asInfixExpression());
      assertThat(Wrings.flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void isPlus() {
      final InfixExpression e = asInfixExpression();
      assertTrue(e.getOperator() == Operator.PLUS);
    }
    @Test public void notString() {
      for (final Expression e : All.operands(Wrings.flatten(asInfixExpression())))
        assertThat(e.toString(), Is.notString(e), is(true));
    }
    @Test public void tryToSort() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(Wrings.flatten(e));
      final boolean tryToSort = Wrings.tryToSort(operands, COMPARATOR);
      assertThat(//
          "Before: " + All.operands(Wrings.flatten(e)) + "\n" + //
              "After: " + operands + "\n", //
          tryToSort, is(true));
    }
    @Test public void tryToSortTwice() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(Wrings.flatten(e));
      assertTrue(Wrings.tryToSort(operands, COMPARATOR));
      assertFalse(Wrings.tryToSort(operands, COMPARATOR));
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
  }
}