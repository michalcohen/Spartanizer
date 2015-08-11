package org.spartan.refactoring.utils;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.refactoring.spartanizations.Into.e;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.Test;

@SuppressWarnings({ "javadoc", "static-method" }) public class SubjectTest {
  @Test public void plantIntoNull() {
    final String s = "a?b:c";
    final Expression e = e(s);
    assertThat(e,notNullValue());
    final Expression e1 = Plant.zis(e).into(null);
    assertThat(e1,notNullValue());
    assertThat(e1, iz(s));
  }
}
