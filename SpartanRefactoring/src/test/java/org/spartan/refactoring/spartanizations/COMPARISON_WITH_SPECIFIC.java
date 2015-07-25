package org.spartan.refactoring.spartanizations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Noneligible;
import org.spartan.refactoring.spartanizations.AbstractWringTest.OutOfScope;
import org.spartan.refactoring.spartanizations.AbstractWringTest.Wringed;
import org.spartan.refactoring.utils.All;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public enum COMPARISON_WITH_SPECIFIC {
  ;
  static final Wring WRING = Wrings.COMPARISON_WITH_SPECIFIC.inner;

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope.Infix {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Literal vs. Literal", "1 < 102333"), //
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
  public static class Noneligible extends AbstractWringTest.Noneligible.Infix {
    static String[][] cases = Utils.asArray(//
        // Literal
        Utils.asArray("LT/literal", "a<2"), //
        Utils.asArray("LE/literal", "a<=2"), //
        Utils.asArray("GT/literal", "a>2"), //
        Utils.asArray("GE/literal", "a>=2"), //
        Utils.asArray("EQ/literal", "a==2"), //
        Utils.asArray("NE/literal", "a!=2"), //
        // This
        Utils.asArray("LT/this", "a<this"), //
        Utils.asArray("LE/this", "a<=this"), //
        Utils.asArray("GT/this", "a>this"), //
        Utils.asArray("GE/this", "a>=this"), //
        Utils.asArray("EQ/this", "a==this"), //
        Utils.asArray("NE/this", "a!=this"), //
        // Null
        Utils.asArray("LT/null", "a<null"), //
        Utils.asArray("LE/null", "a<=null"), //
        Utils.asArray("GT/null", "a>null"), //
        Utils.asArray("GE/null", "a>=null"), //
        Utils.asArray("EQ/null", "a==null"), //
        Utils.asArray("NE/null", "a!=null"), //
        // Character literal
        Utils.asArray("LT/character literal", "a<'a'"), //
        Utils.asArray("LE/character literal", "a<='a'"), //
        Utils.asArray("GT/character literal", "a>'a'"), //
        Utils.asArray("GE/character literal", "a>='a'"), //
        Utils.asArray("EQ/character literal", "a=='a'"), //
        Utils.asArray("NE/character literal", "a!='a'"), //
        // Misc
        Utils.asArray("Correct order", "1 + 2 < 3 "), //
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
    @Override @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = flatten(asInfixExpression());
      assertThat(flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
    /** Instantiates the enclosing class ({@link Noneligible}) */
    public Noneligible() {
      super(WRING);
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.Wringed.Infix {
    private static String[][] cases = Utils.asArray(//
        // Literal
        Utils.asArray("LT/literal", "2<a", "a>2"), //
        Utils.asArray("LE/literal", "2<=a", "a>=2"), //
        Utils.asArray("GT/literal", "2>a", "a<2"), //
        Utils.asArray("GE/literal", "2>=a", "a<=2"), //
        Utils.asArray("EQ/literal", "2==a", "a==2"), //
        Utils.asArray("NE/literal", "2!=a", "a!=2"), //
        // This
        Utils.asArray("LT/this", "this<a", "a>this"), //
        Utils.asArray("LE/this", "this<=a", "a>=this"), //
        Utils.asArray("GT/this", "this>a", "a<this"), //
        Utils.asArray("GE/this", "this>=a", "a<=this"), //
        Utils.asArray("EQ/this", "this==a", "a==this"), //
        Utils.asArray("NE/this", "this!=a", "a!=this"), //
        // Null
        Utils.asArray("LT/null", "null<a", "a>null"), //
        Utils.asArray("LE/null", "null<=a", "a>=null"), //
        Utils.asArray("GT/null", "null>a", "a<null"), //
        Utils.asArray("GE/null", "null>=a", "a<=null"), //
        Utils.asArray("EQ/null", "null==a", "a==null"), //
        Utils.asArray("NE/null", "null!=a", "a!=null"), //
        // Character literal
        Utils.asArray("LT/character literal", "'b'<a", "a>'b'"), //
        Utils.asArray("LE/character literal", "'b'<=a", "a>='b'"), //
        Utils.asArray("GT/character literal", "'b'>a", "a<'b'"), //
        Utils.asArray("GE/character literal", "'b'>=a", "a<='b'"), //
        Utils.asArray("EQ/character literal", "'b'==a", "a=='b'"), //
        Utils.asArray("NE/character literal", "'b'!=a", "a!='b'"), //
        // Misc
        Utils.asArray("Crazy comparison", "null == this", "this == null"), //
        Utils.asArray("Crazy comparison", "null == 1", "1 == null"), //
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
    @Test public void tryToSortTwice() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(flatten(e));
      Wrings.tryToSort(operands, ExpressionComparator.ADDITION);
      assertFalse(Wrings.tryToSort(operands, ExpressionComparator.ADDITION));
    }
    @Override @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = flatten(asInfixExpression());
      assertThat(flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
  }
}
