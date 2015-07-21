package org.spartan.refactoring.spartanizations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
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
public enum MULTIPLICATION_SORTER_Test {
  ;
  @RunWith(Parameterized.class) //
  public static class Noneligible extends AbstractWringTest.Noneligible.Infix {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Plain product of two, sorted", "2*a"), //
        Utils.asArray("Plain product of two, no order", "a*b"), //
        Utils.asArray("Plain product of three, sorted", "2*a*b"), //
        Utils.asArray("Plain product of four, sorted", "2*a*b*c"), //
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
    static Document rewrite(final Spartanization s, final CompilationUnit u, final Document d) {
      try {
        s.createRewrite(u, null).rewriteAST(d, null).apply(d);
        return d;
      } catch (final MalformedTreeException e) {
        fail(e.getMessage());
      } catch (final IllegalArgumentException e) {
        fail(e.getMessage());
      } catch (final BadLocationException e) {
        fail(e.getMessage());
      }
      return null;
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
    @Test public void tryToSort() {
      final InfixExpression e = asInfixExpression();
      assertFalse(Wrings.tryToSort(All.operands(flatten(e)), COMPARATOR));
    }
    @Test public void tryToSortTwice() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(flatten(e));
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
        Utils.asArray("Constant first", "a*2", "2*a"), //
        Utils.asArray("Constant first two arguments", "a*2*b", "2*a*b"), //
        Utils.asArray("Function with fewer arguments first", "f(a,b,c)*f(a,b)*f(a)", "f(a)*f(a,b)*f(a,b,c)"), //
        Utils.asArray("Literals of distinct length", "123*12*1", "1*12*123"), //
        Utils.asArray("Sort expressions by size", "1*f(a,b,c,d) * 2*f(a,b) * 3*f()", "1*2*3*f()*f(a,b)*f(a,b,c,d)"), //
        Utils.asArray("Long alphabetical sorting", "f(t)*g(h1,h2)*y*a*2*b*x", "2*a*b*x*y*f(t)*g(h1,h2)"), //
        Utils.asArray("Plain alphabetical sorting", "f(y)*f(x)", "f(x)*f(y)"), //
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
    @Override @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = flatten(asInfixExpression());
      assertThat(flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void isTimes() {
      final InfixExpression e = asInfixExpression();
      assertTrue(e.getOperator() == Operator.TIMES);
    }
    @Test public void tryToSort() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(flatten(e));
      final boolean tryToSort = Wrings.tryToSort(operands, COMPARATOR);
      assertThat(//
          "Before: " + All.operands(flatten(e)) + "\n" + //
              "After: " + operands + "\n", //
          tryToSort, is(true));
    }
    @Test public void tryToSortTwice() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(flatten(e));
      assertTrue(Wrings.tryToSort(operands, COMPARATOR));
      assertFalse(Wrings.tryToSort(operands, COMPARATOR));
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
  }

  static final Wring WRING = Wrings.MULTIPLICATION_SORTER.inner;
  static final ExpressionComparator COMPARATOR = ExpressionComparator.MULTIPLICATION;
}