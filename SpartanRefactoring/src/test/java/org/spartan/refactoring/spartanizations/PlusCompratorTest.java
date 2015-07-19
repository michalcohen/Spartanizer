package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThan;
import static org.spartan.refactoring.spartanizations.TESTUtils.e;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Test class for {@link Comparator}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc", "static-method" }) //
public class PlusCompratorTest {
  @Test public void twoFunction() {
    assertThat(Comparator.ADDITION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
  @Test public void LiteralAndProduct() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(compare(e1, e2), greaterThan(0));
  }
  private int compare(final Expression e1, final Expression e2) {
    return Comparator.ADDITION.compare(e1, e2);
  }
}
