package org.spartan.refactoring.spartanizations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.c;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;
import static org.spartan.refactoring.spartanizations.TESTUtils.e;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;
import static org.spartan.refactoring.spartanizations.Wrings.haveTernaryOfBooleanLitreral;
import static org.spartan.refactoring.spartanizations.Wrings.isTernaryOfBooleanLitreral;
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
@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class TERNARY_BOOLEAN_LITERAL {
  @Test public void hasTernaryOrBooleanLiteralFalse() {
    assertFalse(haveTernaryOfBooleanLitreral(All.operands(flatten(i("A||B")))));
  }
  @Test public void hasTernaryOrBooleanLiteral() {
    assertTrue(haveTernaryOfBooleanLitreral(All.operands(flatten(i("A||(e?true:false)")))));
  }
  @Test public void makeSurCoreIsExtracted() {
    assertTrue(isTernaryOfBooleanLitreral(e("(e?true:false)")));
  }
  @Test public void makeSurCoreIsExtracted1() {
    assertTrue(isTernaryOfBooleanLitreral(e("(e?true:false)")));
  }
  @Test public void hasTernaryOrBooleanLiteral1() {
    final InfixExpression flatten = flatten(i("A||(e?true:false)"));
    assertThat(flatten.toString(), is("A||(e?true:false)"));
    final List<Expression> operands = All.operands(flatten);
    assertThat(operands.size(), is(2));
    assertThat(operands.get(0).toString(), is("A"));
    assertThat(operands.get(1).toString(), is("(e ? true : false)"));
    assertFalse(isTernaryOfBooleanLitreral(operands.get(0)));
    assertTrue(isTernaryOfBooleanLitreral(operands.get(1)));
    assertTrue(haveTernaryOfBooleanLitreral(operands));
  }
  @Test public void hasTernaryOrBooleanLiteral2() {
    final List<Expression> operands = All.operands(flatten(i("A||(e?true:false)")));
    assertThat(operands.size(), is(2));
    assertThat(operands.get(0).toString(), is("A"));
    assertThat(operands.get(1).toString(), is("(e ? true : false)"));
    assertFalse(isTernaryOfBooleanLitreral(operands.get(0)));
    assertTrue(isTernaryOfBooleanLitreral(operands.get(1)));
    assertTrue(haveTernaryOfBooleanLitreral(operands));
  }
  @Test public void isTernaryOrBooleanLiteralOnExpression() {
    assertTrue(isTernaryOfBooleanLitreral(e("e?true:false")));
  }
  @Test public void isTernaryOrBooleanLiteralOnConditional() {
    assertTrue(isTernaryOfBooleanLitreral(c("e?true:false")));
  }
  static final Wring WRING = Wrings.TERNARY_BOOLEAN_LITERAL.inner;

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope.Infix {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Literal vs. Literal", "1 < 102333"), //
        Utils.asArray("comparion", "next < values().length;"), //
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
      assertNotNull(asInfixExpression());
    }
    @Test public void twoOrMoreArguments() {
      assertThat(All.operands(asInfixExpression()).size(), greaterThanOrEqualTo(2));
    }
    /** Instantiates the enclosing class ({@link Noneligible}) */
    public Noneligible() {
      super(WRING);
    }
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.Wringed.Conditional {
    private static String[][] cases = Utils.asArray(//
        Utils.asArray("F X", "a ? false : c", "!a && c"), //
        Utils.asArray("T X", "a ? true : c", "a || c"), //
        Utils.asArray("X F", "a ? b : false", "a && b"), //
        Utils.asArray("X T", "a ? b : true", "!a || b"), //
        Utils.asArray("Actual example", "!inRange(m, e) ? true : inner.go(r, e)", "!inRange(m, e) || inner.go(r, e)"), //
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
    @Test public void inputIsConditionalfixExpression() {
      assertNotNull(asConditionalExpression());
    }
    /** Instantiates the enclosing class ({@link Wringed}) */
    public Wringed() {
      super(WRING);
    }
  }
}
