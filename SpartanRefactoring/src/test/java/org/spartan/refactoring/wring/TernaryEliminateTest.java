package org.spartan.refactoring.wring;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings({ "javadoc", }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class TernaryEliminateTest
{
  static final Wring<ConditionalExpression> WRING = new TernaryEliminate();

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope.Exprezzion<ConditionalExpression> {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("No boolean", "a?b:c"), //
        Utils.asArray("F X", "a ? false : c"), //
        Utils.asArray("T X", "a ? true : c"), //
        Utils.asArray("X F", "a ? b : false"), //
        Utils.asArray("X T", "a ? b : true"), //
        Utils.asArray("() F X", "a ?( false):true"), //
        Utils.asArray("() T X", "a ? (((true ))): c"), //
        Utils.asArray("() X F", "a ? b : (false)"), //
        Utils.asArray("() X T", "a ? b : ((true))"), //
        Utils.asArray("Actual example", "!inRange(m, e) ? true : inner.go(r, e)"), //
        Utils.asArray("Method invocation first", "a?b():c"), //
        Utils.asArray("Not same function invocation ", "a?b(x):d(x)"), //
        Utils.asArray("Not same function invocation ", "a?x.f(x):x.d(x)"), //
        new String[] { "function call", "a ? f(b) : f(c)" }, //
        new String[] { "a method call", "a ? y.f(b) :y.f(c)" }, //
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
    /** Instantiates the enclosing class ({@link OutOfScope}) */
    public OutOfScope() {
      super(WRING);
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.WringedExpression.Conditional {
    private static String[][] cases = Utils.asArray(//
        new String[] { "identical method call", "a ? y.f(b) :y.f(b)", "y.f(b)" }, //
        new String[] { "identical function call", "a ? f(b) :f(b)", "f(b)" }, //
        new String[] { "identical assignment", "a ? (b=c) :(b=c)", "b = c" }, //
        new String[] { "identical increment", "a ? b++ :b++", "b++" }, //
        new String[] { "identical addition", "a ? b+d :b+ d", "b+d" }, //
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
    /** Instantiates the enclosing class ({@link WringedExpression}) */
    public Wringed() {
      super(WRING);
    }
    @Test public void inputIsConditionalfixExpression() {
      assertNotNull(asConditionalExpression());
    }
  }
}
