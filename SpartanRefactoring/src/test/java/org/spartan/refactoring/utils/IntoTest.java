package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

@SuppressWarnings({ "javadoc", "static-method" }) public class IntoTest {
  @Test public void dCorrect() {
    final String input = "int f() { return a; }";
    final MethodDeclaration d = Into.d(input);
    assertThat(d, iz(input));
  }
  @Test public void dNotNull() {
    final MethodDeclaration d = Into.d("int f() { return a; }");
    assertThat(d, notNullValue());
  }
  @Test(expected = AssertionError.class) public void dOnNull() {
    Into.d(null);
  }
}
