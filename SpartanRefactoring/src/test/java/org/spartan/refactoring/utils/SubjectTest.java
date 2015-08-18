package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.refactoring.utils.Into.e;
import static org.spartan.refactoring.utils.Into.s;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.List;

import org.eclipse.jdt.core.dom.*;

import org.eclipse.jdt.core.dom.Statement;
import org.junit.Test;
import org.junit.Test;
import org.spartan.refactoring.utils.Subject.Pair;
import org.spartan.refactoring.wring.ExpressionComparator;
import org.spartan.refactoring.wring.Wrings;

@SuppressWarnings({ "javadoc", "static-method" }) public class SubjectTest {
  private static final InfixExpression pulus = null;
  @Test public void vanilla() {
    Subject.operand(e("a")).to(PrefixExpression.Operator.NOT);
  }
  @Test public void vanillaCorrectResult() {
    assertThat(Subject.operand(e("a")).to(PrefixExpression.Operator.NOT), iz("!a"));
  }
  @Test public void notPeels() {
    assertThat(Subject.operand(e("((a))")).to(PrefixExpression.Operator.NOT), iz("!a"));
  }
  @Test public void multiplicationOfAddition() {
    assertThat(Subject.pair(e("a+B"), e("c+d")).to(InfixExpression.Operator.TIMES), iz("(a + B) * (c + d)"));
  }
  @Test public void subjectOperands() {
    final Expression e = Into.e("2 + a < b");
    assertTrue(Is.notString(e));
    final InfixExpression plus = Extract.firstPlus(e);
    assertTrue(Is.notString(plus));
    final List<Expression> operands = All.operands(flatten(plus));
    assertThat(operands.size(), is(2));
    final boolean b = Wrings.sort(operands, ExpressionComparator.ADDITION);
    assertThat(b, is(true));
    final InfixExpression r = Subject.operands(operands).to(plus.getOperator());
    assertThat(r, iz("a +2"));
  }
  @Test public void subjectOperandsWithParenthesis() {
    final Expression e = Into.e("(2 + a) * b");
    assertTrue(Is.notString(e));
    final InfixExpression plus = Extract.firstPlus(e);
    assertTrue(Is.notString(plus));
    final List<Expression> operands = All.operands(flatten(plus));
    assertThat(operands.size(), is(2));
    final boolean b = Wrings.sort(operands, ExpressionComparator.ADDITION);
    assertThat(b, is(true));
    final InfixExpression r = Subject.operands(operands).to(plus.getOperator());
    assertThat(r, iz("a +2"));
  }
  @Test public void multiplicationOfMultiplicatoin() {
    assertThat(Subject.pair(e("a*B"), e("c*d")).to(InfixExpression.Operator.TIMES), iz("a * B * c * d"));
  }
  @Test public void assignment() {
    assertThat(Subject.pair(e("a"), e("b")).to(Assignment.Operator.ASSIGN), iz("a=b"));
    assertThat(Subject.pair(e("a"), e("b")).to(Assignment.Operator.PLUS_ASSIGN), iz("a+=b"));
    assertThat(Subject.pair(e("a"), e("b")).to(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN), iz("a>>>=b"));
  }
  @Test public void substractionsDoesntAssociate() {
    assertThat(Subject.pair(e("a-B"), e("c-d")).to(InfixExpression.Operator.MINUS), iz("(a - B) - (c - d)"));
  }
  @Test public void divisionDoesntAssociate() {
    assertThat(Subject.pair(e("a*B"), e("c*d")).to(InfixExpression.Operator.DIVIDE), iz("(a * B) / (c * d)"));
  }
  @Test public void remainderDoesntAssociate() {
    assertThat(Subject.pair(e("a*B"), e("c*d")).to(InfixExpression.Operator.REMAINDER), iz("(a * B) % (c * d)"));
  }
  @Test public void extractcoreLeft() {
    assertThat(Subject.pair(e("((a-B))"), e("c-d")).to(InfixExpression.Operator.PLUS), iz("a - B + c - d"));
  }
  @Test public void extractcoreRight() {
    assertThat(Subject.pair(e("a-B"), e("(c-d)")).to(InfixExpression.Operator.PLUS), iz("a - B + c - d"));
  }
  @Test public void conditionalSimple() {
    final Pair pair = Subject.pair(e("a-B"), e("(c-d)"));
    assertThat(pair, notNullValue());
    assertThat(pair.toCondition(e("x")), iz("x ? a-B : c-d"));
  }
  @Test public void conditionalExtract() {
    final Pair pair = Subject.pair(e("a-B"), e("(c-d)"));
    assertThat(pair, notNullValue());
    assertThat(pair.toCondition(e("(x)")), iz("x ? a-B : c-d"));
  }
  @Test public void postfix() {
    assertThat(Subject.operand(e("a")).to(PostfixExpression.Operator.INCREMENT), iz("a++"));
    assertThat(Subject.operand(e("a")).to(PostfixExpression.Operator.DECREMENT), iz("a--"));
  }
  @Test public void postfixPeels() {
    assertThat(Subject.operand(e("((a))")).to(PostfixExpression.Operator.INCREMENT), iz("a++"));
    assertThat(Subject.operand(e("((a))")).to(PostfixExpression.Operator.DECREMENT), iz("a--"));
  }
  @Test public void operandsVanilla() {
    assertThat(Subject.operands(e("((a))"), e("b"), e("c")).to(InfixExpression.Operator.PLUS), iz("a+b+c"));
  }
  @Test public void operandsParenthesisLeft() {
    assertThat(Subject.operands(e("((a+b))"), e("b"), e("c")).to(InfixExpression.Operator.TIMES), iz("(a+b)*b*c"));
  }
  @Test public void operandsParenthesisRight() {
    assertThat(Subject.operands(e("((a))"), e("b+c"), e("c")).to(InfixExpression.Operator.TIMES), iz("a*(b+c)*c"));
  }
  @Test public void operandsParenthesisRest() {
    assertThat(Subject.operands(e("((a))"), e("b+c"), e("c+d")).to(InfixExpression.Operator.TIMES), iz("a*(b+c)*(c+d)"));
  }
  @Test public void operandsNoParenthesisRest() {
    assertThat(Subject.operands(e("((a))"), e("b+c"), e("c+d")).to(InfixExpression.Operator.PLUS), iz("a+b+c+c+d"));
  }
  @Test public void makeIfStatement() {
    final Statement s = s("s();");
    assertThat(s, iz("{s();}"));
    assertThat(Subject.pair(s, s("f();")).toIf(e("a")), iz("if(a)s(); else f();"));
  }
}
