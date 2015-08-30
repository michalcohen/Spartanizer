package org.spartan.refactoring.wring;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.utils.Into.es;
import static org.spartan.refactoring.wring.Wrings.mixedLiteralKind;

import org.junit.Test;

@SuppressWarnings({ "javadoc", "static-method" }) public class WringsTest {
  @Test public void mixedLiteralKindEmptyList() {
    assertThat(mixedLiteralKind(es()), is(false));
  }
  @Test public void mixedLiteralKindSingletonList() {
    assertThat(mixedLiteralKind(es("1")), is(false));
  }
  @Test public void mixedLiteralKindnPairList() {
    assertThat(mixedLiteralKind(es("1", "1.0")), is(false));
  }
  @Test public void mixedLiteralKindnTripleList() {
    assertThat(mixedLiteralKind(es("1", "1.0", "a")), is(true));
  }
}
