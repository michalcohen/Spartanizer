package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.Collection;

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
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#MULTIPLCATION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public enum ANDOR_TRUE {
  ;
  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope.Infix {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Product is not AND/OR", "2*a"), //
        Utils.asArray("AND without boolean", "b && a"), //
        Utils.asArray("OR without boolean", "b || a"), //
        Utils.asArray("OR of 3 without boolean", "x || a || b"), //
        Utils.asArray("OR of 4 without boolean", "x || a || b || c"), //
        Utils.asArray("OR of 5 without boolean", "x || a || b || c || d"), //
        Utils.asArray("OR of 6 without boolean", "x || a || b || c || d || e"), //
        Utils.asArray("OR of 6 without boolean with parenthesis", "x || (a || b) || (c || (d || e))"), //
        Utils.asArray("AND of 3 without boolean", "x && a && b"), //
        Utils.asArray("AND of 4 without boolean", "x && a && b && c"), //
        Utils.asArray("AND of 5 without boolean", "x && a && b && c && d"), //
        Utils.asArray("AND of 6 without boolean", "x && a && b && c && d && e"), //
        Utils.asArray("AND of 6 without boolean with parenthesis", "(x && (a && b)) && (c && (d && e))"), //
        Utils.asArray("AND with false", "b && a"), //
        Utils.asArray("OR false with something", "false || a"), //
        Utils.asArray("OR something with false", "a || false"), //
        Utils.asArray("OR of 3 without boolean", "a || b"), //
        Utils.asArray("OR of 4 without boolean", "a || b || c"), //
        Utils.asArray("OR of 5 without boolean", "a || b || c || d"), //
        Utils.asArray("OR of 6 without boolean", "a || b || c || d || e"), //
        Utils.asArray("OR of 6 without boolean with parenthesis", "(a || b) || (c || (d || e))"), //
        Utils.asArray("AND of 3 without boolean", "a && b && false"), //
        Utils.asArray("AND of 4 without boolean", "a && b && c && false"), //
        Utils.asArray("AND of 5 without boolean", "false && a && b && c && d"), //
        Utils.asArray("AND of 6 without boolean", "a && b && c && false && d && e"), //
        Utils.asArray("AND of 7 without boolean with parenthesis", "(a && b) && (c && (d && (e && false)))"), //
        Utils.asArray("AND of 7 without boolean and multiple false value",
            "(a && (b && false)) && (c && (d && (e && (false && false))))"), //
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
    public OutOfScope() {
      super(WRING);
    }
  }

  @RunWith(Parameterized.class) //
  public static class Noneligible extends AbstractWringTest.Noneligible.Infix {
    static String[][] cases = Utils.asArray();

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
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void isTimes() {
      final InfixExpression e = asInfixExpression();
      assertTrue(e.getOperator() == Operator.TIMES);
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.Wringed.Infix {
    static String[][] cases = Utils.asArray(//
        new String[] { "Many parenthesis", "a || (((true))) || b", "a || b" }, //
        Utils.asArray("true && true", "true && true", "true"), //
        Utils.asArray("true || true", "true ||true", "true"), //
        Utils.asArray("3 OR TRUE", "true || true || true", "true"), //
        Utils.asArray("4 OR TRUE", "true || true || true || true", "true"), //
        Utils.asArray("OR of 3 with true", "x || true || b", "x || b"), //
        Utils.asArray("OR of 4 with true", "x || a || b || c || true", "x || a || b || c"), //
        Utils.asArray("OR of 5 with true", "x || a || true || c || d", "x || a || c || d"), //
        Utils.asArray("OR of 6 with true", "true || x || a || b || c || d || e", "x || a || b || c || d || e"), //
        Utils.asArray("OR of 6 with true with parenthesis", "x || (a || (true) || b) || (c || (d || e))",
            "x || a || b || c || d || e"), //
        Utils.asArray("AND of 3 with true", "true && x && true && a && b", "x && a && b"), //
        Utils.asArray("AND of 4 with true", "x && true && a && b && c", "x && a && b && c"), //
        Utils.asArray("AND of 5 with true", "x && a && b && c && true && true && true && d", "x && a && b && c && d"), //
        Utils.asArray("AND of 6 with true", "x && a && true && b && c && d && e", "x && a && b && c && d && e"), //
        Utils.asArray("AND of 6 with true with parenthesis", "x && (true && (a && b && true)) && (c && (d && e))",
            "x && a && b && c && d && e"), //
        Utils.asArray("AND with true", "true && b && a", "b && a"), //
        Utils.asArray("OR true with something", "true || a || true", "a"), //
        Utils.asArray("OR something with true", "true || a || true", "a"), //
        Utils.asArray("OR of 3 with true", "true || a || b || true", "a || b"), //
        Utils.asArray("OR of 4 with true", "a || b || true || c", "a || b || c"), //
        Utils.asArray("OR of 5 with true", "a || b || c || d || true", "a || b || c || d"), //
        Utils.asArray("OR of 6 with two trues", "a || true || b || true || c || d || e", "a || b || c || d || e"), //
        Utils.asArray("OR of 6 with true with parenthesis", "(a || b) || true || (c || true || (d || e || true))",
            "a || b || c || d || e "), //
        Utils.asArray("AND of 3 with true", "a && b && true", "a && b"), //
        Utils.asArray("AND of 4 with true", "a && b && c && true", "a && b && c"), //
        Utils.asArray("AND of 5 with true", "true && a && b && c && d", "a && b && c && d"), //
        Utils.asArray("AND of 6 with true", "a && b && c && true && d && e", "a && b && c && d && e"), //
        Utils.asArray("AND of 7 with true with parenthesis", "true && (a && b) && (c && (d && (e && true)))",
            "a && b && c && d && e"), //
        Utils.asArray("AND of 7 with multiple true value", "(a && (b && true)) && (c && (d && (e && (true && true))))",
            "a&&b&&c&&d&&e"), //
        null);

    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the expected output
     */
    @Parameters(name = DESCRIPTION) //
    public static Collection<Object[]> cases() {
      return collect(cases);
    }
    /** Instantiates the enclosing class ({@link Wringed}) */
    public Wringed() {
      super(WRING);
    }
    @Override @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = flatten(asInfixExpression());
      assertThat(flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void isANDorOR() {
      final InfixExpression e = asInfixExpression();
      assertThat(e.getOperator(),
          is(anyOf(is(InfixExpression.Operator.CONDITIONAL_OR), is(InfixExpression.Operator.CONDITIONAL_AND))));
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
  }

  static final Wring WRING = Wrings.ANDOR_TRUE.inner;
}
