package org.spartan.refactoring.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.refactoring.spartanizations.Into.e;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.junit.Test;
import org.junit.Test;

@SuppressWarnings({ "javadoc", "static-method" }) public class SubjectTest {
  @Test public void vanilla() {
    Subject.operand(e("a")).to(PrefixExpression.Operator.NOT);
  }
  @Test public void vanillaCorrectResult() {
    assertThat(Subject.operand(e("a")).to(PrefixExpression.Operator.NOT).toString(), is("!a"));
  }
  @Test public void multiplicationOfAddition() {
    assertThat(new Subject.Pair(e("a+B"), e("c+d")).to(InfixExpression.Operator.TIMES).toString(), is("(a + B) * (c + d)"));
  }
  @Test public void multiplicationOfMultiplicatoin() {
    assertThat(new Subject.Pair(e("a*B"), e("c*d")).to(InfixExpression.Operator.TIMES).toString(), is("a * B * c * d"));
  }
  @Test public void assignment() {
    assertThat(new Subject.Pair(e("a"), e("b")).to(Assignment.Operator.ASSIGN).toString(), is("a=b"));
    assertThat(new Subject.Pair(e("a"), e("b")).to(Assignment.Operator.PLUS_ASSIGN).toString(), is("a+=b"));
    assertThat(new Subject.Pair(e("a"), e("b")).to(Assignment.Operator. RIGHT_SHIFT_UNSIGNED_ASSIGN).toString(), is("a>>>=b"));

  }
}
