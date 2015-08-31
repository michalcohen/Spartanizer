package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.*;
import static org.spartan.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;

@SuppressWarnings({ "javadoc", "static-method" }) public class IntoTest {
  @Test public void dNotNull() {
    final MethodDeclaration d = Into.d("int f() { return a; }");
    assertThat(d, notNullValue());
  }
  @Test(expected = AssertionError.class) public void dOnNull() {
    Into.d(null);
  }
  @Test public void dCorrect() {
    final String input = "int f() { return a; }";
    final MethodDeclaration d = Into.d(input);
    assertThat(d, iz(input));
  }
}
