package org.spartan.refactoring.wring;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.hamcrest.OrderingComparison.greaterThan;
import static org.spartan.hamcrest.OrderingComparison.lessThan;
import static org.spartan.hamcrest.OrderingComparison.lessThanOrEqualTo;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "static-method", "javadoc" }) public class GenericArgumentNameTest {
  // TODO: Daniel Mittelman
  @Test public void testSomething() {
    assertThat(2, greaterThan(1));
    assertThat(1, lessThan(2));
    assertThat(1, lessThanOrEqualTo(1));
  }
  @Test public void testSomethingElse() {
    assertThat(true, is(true));
  }
  @Test public void testWorksWithApproximateMatch() {
    assertThat(this.getClass().getSimpleName(), iz("GenericArgumentNameTest"));
  }
}
