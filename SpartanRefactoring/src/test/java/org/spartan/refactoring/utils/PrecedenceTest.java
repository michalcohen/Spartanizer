package org.spartan.refactoring.utils;

import static org.spartan.refactoring.spartanizations.TESTUtils.e;

import org.junit.Test;

/**
 * @author Yossi Gil
 * @since 2015-07-17
 *
 */
@SuppressWarnings({ "static-method", "javadoc" }) //
public class PrecedenceTest {
  @Test public void exists() {
    Precedence.of(e("A+3"));
  }
}
