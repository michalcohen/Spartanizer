package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.not;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.comparesEqualTo;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.c;
import static org.spartan.refactoring.spartanizations.TESTUtils.e;

import org.junit.Test;

/**
 * @author Yossi Gil
 * @since 2015-07-17
 */
@SuppressWarnings({ "static-method", "javadoc" }) //
public class PrecedenceTest {
  @Test public void exists() {
    Precedence.of(e("A+3"));
  }
  @Test public void existsTernary() {
    Precedence.of(c("A?b:c"));
  }
  @Test public void ternaryIsNotNegative() {
    assertThat(Precedence.of(c("A?b:c")), greaterThanOrEqualTo(0));
  }
  @Test public void methodInvocationIsNotNegative() {
    assertThat(Precedence.of(e("f(a,b,c)")), greaterThanOrEqualTo(0));
  }
  @Test public void methodInvocationIsNotTernary() {
    assertThat(Precedence.of(e("f(a,b,c)")), not(comparesEqualTo(Precedence.of(e("a?b:c")))));
  }
}
