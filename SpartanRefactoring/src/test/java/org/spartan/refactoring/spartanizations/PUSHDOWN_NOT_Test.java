package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertNotNull;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;

import java.util.Collection;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Noneligible;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Noneligible.Infix;
import org.spartan.refactoring.spartanizations.AbstractWringTest.OutOfScope;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Wringed;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc" }) //
public enum PUSHDOWN_NOT_Test {
  ;
  static final Wring WRING = Wrings.PUSHDOWN_NOT.inner;

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Summation", "a+b"), //
        Utils.asArray("Multiplication", "a*b"), //
        Utils.asArray("OR", "a||b"), //
        Utils.asArray("END", "a&&b"), //
        null);

    /** Instantiates the enclosing class ({@link OutOfScope}) */
    public OutOfScope() {
      super(WRING);
    }
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
  }

  @RunWith(Parameterized.class) //
  public static class Noneligible extends AbstractWringTest.Noneligible {
    static String[][] cases = Utils.asArray(//
        // Literal
        Utils.asArray("Simple not", "!a"), //
        Utils.asArray("Simple not of function", "!f(a)"), //
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

    static abstract class Infix extends Noneligible {
      /** Instantiates the enclosing class ({@link Infix})@param simplifier */
      Infix(final Wring w) {
        super(w);
      }
      @Test public void inputIsInfixExpression() {
        final InfixExpression e = asInfixExpression();
        assertNotNull(e);
      }
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.Wringed {
    private static String[][] cases = Utils.asArray(//
        Utils.asArray("not of AND", "!(f() && f(5))", "(!f() || !f(5))"), //
        Utils.asArray("not of EQ", "!(3 == 5)", "3 != 5"), //
        Utils.asArray("not of AND nested", "!(f() && (f(5)))", "(!f() || !f(5))"), //
        Utils.asArray("not of EQ nested", "!((((3 == 5))))", "3 != 5"), //
        Utils.asArray("not of GE", "!(3 >= 5)", "3 < 5"), //
        Utils.asArray("not of GT", "!(3 > 5)", "3 <= 5"), //
        Utils.asArray("not of NE", "!(3 != 5)", "3 == 5"), //
        Utils.asArray("not of LE", "!(3 <= 5)", "3 > 5"), //
        Utils.asArray("not of LT", "!(3 < 5)", "3 >= 5"), //
        Utils.asArray("not of AND", "!(a && b && c)", "(!a || !b || !c)"), //
        Utils.asArray("not of OR", "!(a || b || c)", "(!a && !b && !c)"), //
        Utils.asArray("double not", "!!f()", "f()"), //
        Utils.asArray("double not nested", "!(!f())", "f()"), //
        Utils.asArray("double not deeply nested", "!(((!f())))", "f()"), //
        Utils.asArray("not of OR 2", "!(f() || f(5))", "(!f() && !f(5))"), //
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
    @Test public void inputIsPrefixExpression() {
      final PrefixExpression e = asPrefixExpression();
      assertNotNull(e);
    }
  }
}