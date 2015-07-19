package org.spartan.refactoring.spartanizations;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThan;
import static org.spartan.hamcrest.OrderingComparison.lessThan;
import static org.spartan.refactoring.spartanizations.TESTUtils.e;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Test class for {@link ExpressionComparator}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc", "static-method" }) //
public class ExpressionComparatorTest {
  @Test public void twoFunctionAddition() {
    assertThat(ExpressionComparator.ADDITION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
  @Test public void twoFunctionMultiplication() {
    assertThat(ExpressionComparator.MULITIPLICATION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
  @Test public void literalAndProductAddition() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), greaterThan(0));
  }
  @Test public void literalAndProductMULITIPLICATION() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(ExpressionComparator.MULITIPLICATION.compare(e1, e2), lessThan(0));
  }
  @Test public void longLiteralShortLiteralMultiplication() {
    final Expression e1 = e("1");
    final Expression e2 = e("12");
    assertThat(ExpressionComparator.MULITIPLICATION.compare(e1, e2), lessThan(0));
  }
  @Test public void longLiteralShortLiteralAddition() {
    final Expression e1 = e("1");
    final Expression e2 = e("12");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), lessThan(0));
  }
}
