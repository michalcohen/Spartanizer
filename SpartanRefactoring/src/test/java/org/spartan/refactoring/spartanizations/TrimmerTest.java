package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.refactoring.spartanizations.ExpressionComparator.TOKEN_THRESHOLD;
import static org.spartan.refactoring.spartanizations.ExpressionComparator.countNodes;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertLegible;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertNoChange;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertNotLegible;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertNotWithinScope;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimplifiesTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertWithinScope;
import static org.spartan.refactoring.spartanizations.TESTUtils.countOpportunities;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.peel;
import static org.spartan.refactoring.spartanizations.TESTUtils.wrap;
import static org.spartan.refactoring.spartanizations.Wrings.COMPARISON_WITH_BOOLEAN;
import static org.spartan.refactoring.spartanizations.Wrings.COMPARISON_WITH_SPECIFIC;
import static org.spartan.refactoring.spartanizations.Wrings.MULTIPLICATION_SORTER;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.Is;

/**
 * * Unit tests for the nesting class Unit test for the containing class. Note
 * our naming convention: a) test methods do not use the redundant "test"
 * prefix. b) test methods begin with the name of the method they check.
 *
 * @author Yossi Gil
 * @since 2014-07-10
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "static-method", "javadoc" }) //
public class TrimmerTest {
  public static final String example = "on * notion * of * no * nothion != the * plain + kludge";
  @Test public void chainCOmparisonTrueLast() {
    assertSimplifiesTo("a == b == c == true", "a == b == c");
  }
  @Test public void compareWithBoolean00() {
    assertSimplifiesTo("a == true", "a");
  }
  @Test public void compareWithBoolean01() {
    assertSimplifiesTo("a == false", "!(a)");
  }
  @Test public void compareWithBoolean10() {
    assertSimplifiesTo("true == a", "a");
  }
  //
  @Test public void compareWithBoolean100() {
    assertSimplifiesTo("a != true", "!(a)");
  }
  @Test public void compareWithBoolean101() {
    assertSimplifiesTo("a != false", "a");
  }
  @Test public void compareWithBoolean11() {
    assertSimplifiesTo("false == a", "!(a)");
  }
  @Test public void compareWithBoolean110() {
    assertSimplifiesTo("true != a", "!(a)");
  }
  @Test public void compareWithBoolean111() {
    assertSimplifiesTo("false != a", "a");
  }
  @Test public void compareWithBoolean2() {
    assertSimplifiesTo("false != false", "false");
  }
  @Test public void compareWithBoolean3() {
    assertSimplifiesTo("false != true", "true");
  }
  @Test public void compareWithBoolean4() {
    assertSimplifiesTo("false == false", "!(false)");
  }
  @Test public void compareWithBoolean5() {
    assertSimplifiesTo("false == true", "!(true)");
  }
  @Test public void compareWithBoolean6() {
    assertSimplifiesTo("false != false", "false");
  }
  @Test public void compareWithBoolean7() {
    assertSimplifiesTo("true != true", "!(true)");
  }
  @Test public void compareWithBoolean8() {
    assertSimplifiesTo("true != false", "!(false)");
  }
  @Test public void compareWithBoolean9() {
    assertSimplifiesTo("true != true", "!(true)");
  }
  @Test public void comparisonWithSpecific0() {
    assertSimplifiesTo("this != a", "a != this");
  }
  @Test public void comparisonWithSpecific0Legibiliy0() {
    assertNotWithinScope(COMPARISON_WITH_BOOLEAN.inner, "this != a");
  }
  @Test public void comparisonWithSpecific0Legibiliy00() {
    final InfixExpression e = i("this != a");
    assertTrue(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS));
    assertFalse(Is.booleanLiteral(e.getRightOperand()));
    assertFalse(Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS) && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand())));
  }
  @Test public void comparisonWithSpecific0Legibiliy1() {
    assertTrue(Is.specific(i("this != a").getLeftOperand()));
    assertNotLegible(Wrings.COMPARISON_WITH_SPECIFIC.inner, "a != this");
  }
  @Test public void comparisonWithSpecific0Legibiliy1withinScope() {
    assertNotWithinScope(Wrings.COMPARISON_WITH_BOOLEAN.inner, "this != a");
  }
  @Test public void comparisonWithSpecific0Legibiliy2() {
    assertTrue(Is.specific(i("this != a").getLeftOperand()));
    assertLegible(Wrings.COMPARISON_WITH_SPECIFIC.inner, "this != a");
  }
  @Test public void comparisonWithSpecific0z0() {
    assertWithinScope(Wrings.COMPARISON_WITH_SPECIFIC.inner, "this != a");
  }
  @Test public void comparisonWithSpecific0z1() {
    assertLegible(Wrings.COMPARISON_WITH_SPECIFIC.inner, "this != a");
  }
  @Test public void comparisonWithSpecific1() {
    assertSimplifiesTo("null != a", "a != null");
  }
  @Test public void comparisonWithSpecific2() {
    assertSimplifiesTo("null != a", "a != null");
    assertSimplifiesTo("this == a", "a == this");
    assertSimplifiesTo("null == a", "a == null");
    assertSimplifiesTo("this >= a", "a <= this");
    assertSimplifiesTo("null >= a", "a <= null");
    assertSimplifiesTo("this <= a", "a >= this");
    assertSimplifiesTo("null <= a", "a >= null");
  }
  @Test public void comparisonWithSpecificNoChange() {
    assertNoChange("a != this");
    assertNoChange("a != null");
    assertNoChange("a == this");
    assertNoChange("a == null");
    assertNoChange("a <= this");
    assertNoChange("a <= null");
    assertNoChange("a >= this");
    assertNoChange("a >= null");
  }
  @Test public void comparisonWithSpecificNoChangeWithLongEpxressions() {
    assertNoChange("very(complicate,func,-ction,call) != this");
    assertNoChange("very(complicate,func,-ction,call) != null");
    assertNoChange("very(complicate,func,-ction,call) == this");
    assertNoChange("very(complicate,func,-ction,call) == null");
    assertNoChange("very(complicate,func,-ction,call) <= this");
    assertNoChange("very(complicate,func,-ction,call) <= null");
    assertNoChange("very(complicate,func,-ction,call) >= this");
    assertNoChange("very(complicate,func,-ction,call) >= null");
  }
  @Test public void desiredSimplificationOfExample() {
    assertSimplifiesTo("on * notion * of * no * nothion != the * plain + kludge", "no*of*on*notion*nothion!=the*plain+kludge");
  }
  @Test public void isGreaterTrue() {
    final InfixExpression e = i("f(a,b,c,d,e) & f(a,b,c)");
    assertEquals("f(a,b,c)", e.getRightOperand().toString());
    assertEquals("f(a,b,c,d,e)", e.getLeftOperand().toString());
    final Wring s = Wrings.find(e);
    assertEquals(Wrings.MULTIPLICATION_SORTER.inner, s);
    assertNotNull(s);
    assertTrue(s.scopeIncludes(e));
    final Expression e1 = e.getLeftOperand();
    final Expression e2 = e.getRightOperand();
    assertFalse(hasNull(e1, e2));
    final boolean tokenWiseGreater = countNodes(e1) > TOKEN_THRESHOLD + countNodes(e2);
    assertTrue(tokenWiseGreater);
    assertTrue(ExpressionComparator.moreArguments(e1, e2));
    assertTrue(ExpressionComparator.longerFirst(e));
    assertTrue(e.toString(), s.eligible(e));
    final Expression replacement = s.replacement(e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) & f(a,b,c,d,e)", replacement.toString());
  }
  @Test public void isGreaterTrueButAlmostNot() {
    final InfixExpression e = i("f(a,b,c,d) ^ f(a,b,c)");
    assertEquals("f(a,b,c)", e.getRightOperand().toString());
    assertEquals("f(a,b,c,d)", e.getLeftOperand().toString());
    final Wring s = Wrings.find(e);
    assertEquals(MULTIPLICATION_SORTER.inner, s);
    assertNotNull(s);
    assertTrue(s.scopeIncludes(e));
    final Expression e1 = e.getLeftOperand();
    final Expression e2 = e.getRightOperand();
    assertFalse(hasNull(e1, e2));
    final boolean tokenWiseGreater = countNodes(e1) > TOKEN_THRESHOLD + countNodes(e2);
    assertFalse(tokenWiseGreater);
    assertTrue(ExpressionComparator.moreArguments(e1, e2));
    assertTrue(ExpressionComparator.longerFirst(e));
    assertTrue(e.toString(), s.eligible(e));
    final Expression replacement = s.replacement(e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) ^ f(a,b,c,d)", replacement.toString());
  }
  @Test public void legibleOnShorterChainParenthesisComparisonLast() {
    assertLegible(MULTIPLICATION_SORTER.inner, "z * 2 * a * b * c * d * e * f * g * h");
  }
  @Test public void noChange() {
    assertNoChange("12");
    assertNoChange("true");
    assertNoChange("null");
    assertSimplifiesTo("on*of*no*notion*notion", "no*of*on*notion*notion");
  }
  @Test public void noChange0() {
    assertNoChange("kludge + the * plain ");
  }
  @Test public void noChange1() {
    assertNoChange("the * plain");
  }
  @Test public void noChange2() {
    assertNoChange("plain + kludge");
  }
  @Test public void oneMultiplication() {
    assertSimplifiesTo("f(a,b,c,d) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d)");
  }
  @Test public void oneMultiplication0() {
    final InfixExpression e = i("f(a,b,c,d) ^ f(a,b,c)");
    assertEquals("f(a,b,c)", e.getRightOperand().toString());
    final Wring s = Wrings.find(e);
    assertEquals(s, MULTIPLICATION_SORTER.inner);
    assertNotNull(s);
    assertTrue(s.scopeIncludes(e));
    assertTrue(s.eligible(e));
    final Expression replacement = s.replacement(e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) ^ f(a,b,c,d)", replacement.toString());
  }
  @Test public void oneMultiplicationAlternate() {
    assertSimplifiesTo("f(a,b,c,d,e) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d,e)");
  }
  @Test public void oneOpportunityExample() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(wrap(example));
    assertEquals(u.toString(), 1, countOpportunities(new Trimmer(), u));
  }
  @Test public void rightSimplificatioForNulNNVariableReplacement() {
    final InfixExpression e = i("null != a");
    final Wring w = Wrings.find(e);
    assertNotNull(w);
    assertTrue(w.scopeIncludes(e));
    assertTrue(w.eligible(e));
    final Expression replacement = w.replacement(e);
    assertNotNull(replacement);
    assertEquals("a != null", replacement.toString());
  }
  @Test public void rightSipmlificatioForNulNNVariable() {
    assertEquals(COMPARISON_WITH_SPECIFIC.inner, Wrings.find(i("null != a")));
  }
  @Test public void shorterChainParenthesisComparisonLast() {
    assertSimplifiesTo("a * b * c * d * e * f * g * h == b", "b == a * b * c * d * e * f * g * h == a");
  }
  @Test public void simplifiesTo() {
    assertSimplifiesTo("plain * the + kludge", "the*plain+kludge");
  }
  @Test public void testPeel() {
    assertEquals(example, peel(wrap(example)));
  }
  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) & f()", "f() & f(a,b,c,d)");
  }
}