package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.utils.Funcs.flip;
import static org.spartan.refactoring.utils.Into.i;
import static org.spartan.refactoring.utils.Restructure.flatten;
import static org.spartan.utils.Utils.hasNull;

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
import org.spartan.refactoring.utils.ExpressionComparator;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class InfixComparisonShortestFirstTest extends AbstractWringTest<InfixExpression> {
  static final Wring<InfixExpression> WRING = new InfixComparisonShortestFirst();
  /** Instantiates this class */
  public InfixComparisonShortestFirstTest() {
    super(WRING);
  }
  @Test public void comparisonWithSpecific0Legibiliy1() {
    assertThat(WRING, notNullValue());
  }
  @Test public void t3() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void t4() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1")));
  }
  @Test public void t5() {
    assertFalse(WRING.scopeIncludes(i(" 6 - 7 < 2 + 1")));
  }
  @Test public void t6() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void t7() {
    assertTrue(WRING.scopeIncludes(i("1 + 2  + s < 3 ")));
  }
  @Test public void t8() {
    final InfixExpression e = i("1 + 2  + 3 < 3 ");
    assertTrue(WRING.scopeIncludes(e));
  }
  @Test public void t9() {
    final InfixExpression e = i("1 + 2  + 3 < 3 -4");
    assertNotNull(e);
    assertFalse(hasNull(e.getLeftOperand(), e.getRightOperand()));
  }
  @Test public void withinDomainFalse0() {
    assertFalse(WRING.scopeIncludes(i("13455643294 < 22")));
  }
  @Test public void withinDomainFalse1() {
    assertFalse(WRING.scopeIncludes(i("1 < 102333")));
  }
  @Test public void withinDomainFalse2() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void withinDomainFalse3() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1")));
  }
  @Test public void withinDomainFalse4() {
    assertFalse(WRING.scopeIncludes(i(" 6 - 7 < 2 + 1   ")));
  }
  @Test public void withinDomainFalse5() {
    assertFalse(WRING.scopeIncludes(i("13455643294 < 22")));
  }
  @Test public void withinDomainFalse6() {
    assertFalse(WRING.scopeIncludes(i("1 < 102333")));
  }
  @Test public void withinDomainFalse7() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void withinDomainFalse8() {
    assertFalse(WRING.scopeIncludes(i("1 + 2 < 3 & 7 + 4 > 2 + 1")));
  }
  @Test public void withinDomainFalse9() {
    assertFalse(WRING.scopeIncludes(i(" 6 - 7 < 2 + 1   ")));
  }
  @Test public void withinDomainTrue1() {
    assertTrue(WRING.scopeIncludes(i("a == this")));
  }
  @Test public void withinDomainTrue2() {
    assertTrue(WRING.scopeIncludes(i("this == null")));
  }
  @Test public void withinDomainTrue3() {
    assertTrue(WRING.scopeIncludes(i("12 == this")));
  }
  @Test public void withinDomainTrue4() {
    assertFalse(WRING.scopeIncludes(i("a == 11")));
  }
  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope.Exprezzion.Infix {
    static String[][] cases = Utils.asArray(//
         new String[] {"Literal vs. Literal", "1 < 102333"}, //
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
  public static class Wringed extends AbstractWringTest.WringedExpression.Infix {
    private static String[][] cases = Utils.asArray(//
        new String[] { "Vanialla", "f(2) < a", "a > f(2)" }, null);
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
     * Instantiates the enclosing class ({@link WringedExpression})
     */
    public Wringed() {
      super(WRING);
    }
    @Override @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = flatten(asInfixExpression());
      assertThat(flatten(flatten).toString(), is(flatten.toString()));
    }
    @Test public void flipIsNotNull() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(Subject.pair(e.getRightOperand(), e.getLeftOperand()).to(flip(e.getOperator())));
    }
    @Override @Test public void inputIsInfixExpression() {
      assertNotNull(asInfixExpression());
    }
    @Test public void sortTwiceADDITION() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = Extract.operands(flatten(e));
      Wrings.sort(operands, ExpressionComparator.ADDITION);
      assertFalse(Wrings.sort(operands, ExpressionComparator.ADDITION));
    }
    @Test public void sortTwiceMULTIPLICATION() {
      final List<Expression> operands = Extract.operands(flatten(asInfixExpression()));
      Wrings.sort(operands, ExpressionComparator.MULTIPLICATION);
      assertFalse(Wrings.sort(operands, ExpressionComparator.MULTIPLICATION));
    }
    @Test public void twoOrMoreArguments() {
      assertThat(Extract.operands(asInfixExpression()).size(), greaterThanOrEqualTo(2));
    }
  }
}
