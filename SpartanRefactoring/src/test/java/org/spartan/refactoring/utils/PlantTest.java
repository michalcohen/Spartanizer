package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.refactoring.utils.Into.e;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.Test;

@SuppressWarnings({ "javadoc", "static-method" }) public class PlantTest {
  @Test public void plantIntoNull() {
    final String s = "a?b:c";
    final Expression e = e(s);
    assertThat(e, notNullValue());
    final Expression e1 = Plant.zis(e).into(null);
    assertThat(e1, notNullValue());
    assertThat(e1, iz(s));
  }
  @Test public void plus() {
    final Expression e = Into.e("a + 2 < b");
    final Expression plus = Extract.firstPlus(e);
    assertThat(plus.toString(), Is.notString(plus), is(true));
    assertThat(e.toString(), Is.notString(plus), is(true));
  }

}
