package org.spartan.refactoring.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.spartan.refactoring.spartanizations.TESTUtils.e;

import org.junit.Test;

/**
 * Test class for class {@link Is}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
public class IsTest {
  @Test public void numericLiteralTrue() {
    assertTrue(Is.numericLiteral(e("1")));
  }
  @Test public void numericLiteralFalse1() {
    assertFalse(Is.numericLiteral(e("2*3")));
  }
  @Test public void numericLiteralFalse2() {
    assertFalse(Is.numericLiteral(e("2*3")));
  }
}
