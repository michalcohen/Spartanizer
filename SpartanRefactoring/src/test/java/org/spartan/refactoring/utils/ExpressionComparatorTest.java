package org.spartan.refactoring.utils;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThan;
import static org.spartan.hamcrest.OrderingComparison.lessThan;
import static org.spartan.refactoring.utils.Into.*;
import org.eclipse.jdt.core.dom.Expression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Test class for {@link ExpressionComparator}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc", "static-method" }) //
public class ExpressionComparatorTest {
  @Test public void alphabeticalCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.alphabeticalCompare(e1, e2), lessThan(0));
  }
  @Test public void characterCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.characterCompare(e1, e2), is(0));
  }
  @Test public void countStatementsDoLoop() {
    final String statement = "do { f(); g();} while( i++);";
    assertThat(cs(statement), is(11));
  }
  @Test public void countStatementsDoLoopNoBlock() {
    final String statement = "do f();  while( i++);";
    assertThat(cs(statement), is(7));
  }
  @Test public void countStatementsDoLoopTrimmed() {
    final String statement = "do  f();  while( i++);";
    assertThat(cs(statement), is(7));
  }
  @Test public void countStatementsDoLoopTrimmedInBlock() {
    final String statement = "do { f(); } while( i++);";
    assertThat(cs(statement), is(7));
  }
  @Test public void countStatementsFor() {
    assertThat(cs("for (;;) i++;"), is(7));
  }
  @Test public void countStatementsForEnahnced() {
    final String statement = "for (int x : f()) i++;";
    assertThat(cs(statement), is(7));
  }
  @Test public void countStatementsIfPlain() {
    final String statement = "if (f()) g();";
    assertThat(cs(statement), is(7));
  }
  @Test public void countStatementsIfWithElse() {
    final String statement = "if (f()) g(); else h();";
    assertThat(cs(statement), is(11));
  }
  @Test public void countStatementsIfWithElseAndEmptyStatments() {
    final String statement = "if (f()) {;;;g();{}} else h();";
    assertThat(cs(statement), is(11));
  }
  @Test public void countStatementsIfWithElseManyMoreEmptyStatments() {
    final String statement = "if (f()) {;;;g();{}} else {{;;}; {} ; h();;;;}";
    assertThat(cs(statement), is(11));
  }
  @Test public void countStatementsIfWithEMptyBoth() {
    final String statement = "if (f()) ; else ;";
    assertThat(cs(statement), is(5));
  }
  @Test public void countStatementsIfWithEMptyBothIsOk() {
    final String statement = "if (f()) ; ";
    assertThat(cs(statement), is(4));
  }
  @Test public void countStatementsIfWithEMptyElseStatement() {
    final String statement = "if (f()) g(); else ;";
    assertThat(cs(statement), is(8));
  }
  @Test public void countStatementsPlain() {
    assertThat(cs("i++;"), is(3));
  }
  @Test public void countStatementsWithBlock() {
    assertThat(cs("for (;;) { i++; }"), is(7));
  }
  @Test public void literalAndClassConstant() {
    assertThat(ExpressionComparator.ADDITION.compare(e("1"), e("BOB")), greaterThan(0));
  }
  @Test public void literalAndProductAddition() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), greaterThan(0));
  }
  @Test public void literalAndProductMULITIPLICATION() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(ExpressionComparator.MULTIPLICATION.compare(e1, e2), lessThan(0));
  }
  @Test public void literalCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.literalCompare(e1, e2), is(0));
  }
  @Test public void longLiteralShortLiteralAddition() {
    final Expression e1 = e("1");
    final Expression e2 = e("12");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), lessThan(0));
  }
  @Test public void longLiteralShortLiteralMultiplication() {
    final Expression e1 = e("1");
    final Expression e2 = e("12");
    assertThat(ExpressionComparator.MULTIPLICATION.compare(e1, e2), lessThan(0));
  }
  @Test public void nodesCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.nodesCompare(e1, e2), is(0));
  }
  @Test public void twoClassConstants() {
    assertThat(ExpressionComparator.ADDITION.compare(e("SPONGE"), e("BOB")), greaterThan(0));
  }
  @Test public void twoClassConstantsLongExpressionWithClassConstantsWithDigits() {
    assertThat(ExpressionComparator.ADDITION.compare(e("f(a,b,c)"), e("ABC0")), lessThan(0));
  }
  @Test public void twoExpression() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), lessThan(0));
  }
  @Test public void twoFunctionAddition() {
    assertThat(ExpressionComparator.ADDITION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
  @Test public void twoFunctionMultiplication() {
    assertThat(ExpressionComparator.MULTIPLICATION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
  private int cs(final String statement) {
    return ExpressionComparator.lineCount(s(statement));
  }
}
