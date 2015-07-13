package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.asExpression;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertLegible;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertNoChange;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertNotLegible;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertNotWithinScope;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertOneOpportunity;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertSimplifiesTo;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertWithinScope;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.peel;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.wrap;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.countNodes;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.hasNull;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.in;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import il.ac.technion.cs.ssdl.spartan.utils.Is;

/**
 * * Unit tests for the nesting class Unit test for the containing class. Note
 * our naming convention: a) test methods do not use the redundant "test"
 * prefix. b) test methods begin with the name of the method they check.
 *
 * @author Yossi Gil
 * @since 2014-07-10
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "static-method", "javadoc" }) //
public class SimplificationEngineTestTrivial {
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
    assertNotWithinScope(Simplifier.comparisionWithBoolean, "this != a");
  }
  @Test public void comparisonWithSpecific0Legibiliy00() {
    final InfixExpression e = asExpression("this != a");
    assertTrue(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS));
    assertFalse(Is.booleanLiteral(e.getRightOperand()));
    assertFalse(Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS)
        && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand())));
  }
  @Test public void comparisonWithSpecific0Legibiliy1() {
    assertTrue(Is.specific(asExpression("this != a").getLeftOperand()));
    assertNotLegible(Simplifier.shortestOperandFirst, "this != a");
  }
  @Test public void comparisonWithSpecific0Legibiliy1withinScope() {
    assertNotWithinScope(Simplifier.comparisionWithBoolean, "this != a");
  }
  @Test public void comparisonWithSpecific0Legibiliy2() {
    assertTrue(Is.specific(asExpression("this != a").getLeftOperand()));
    assertLegible(Simplifier.comparisionWithSpecific, "this != a");
  }
  @Test public void comparisonWithSpecific0z0() {
    assertWithinScope(Simplifier.comparisionWithSpecific, "this != a");
  }
  @Test public void comparisonWithSpecific0z1() {
    assertLegible(Simplifier.comparisionWithSpecific, "this != a");
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
    final String from = example;
    final String to = "on*notion*of*no*nothion != kludge+the*plain";
    assertSimplifiesTo(from, to);
  }
  @Test public void legibleOnShorterChainParenthesisComparisonLast() {
    assertLegible(Simplifier.shortestOperandFirst, "a * b * c * d * e * f * g * h== b == c");
  }
  @Test public void noChange() {
    assertNoChange("12");
    assertNoChange("true");
    assertNoChange("null");
    assertNoChange("on * notion * of * no * notion");
  }
  @Test public void noChange0() {
    assertSimplifiesTo("the * plain + kludge", "kludge + the * plain ");
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
  @Test public void oneMultiplicationAlternate() {
    assertSimplifiesTo("f(a,b,c,d,e) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d,e)");
  }
  @Test public void oneMultiplication0() {
    final InfixExpression e = asExpression("f(a,b,c,d) * f(a,b,c)");
    assertEquals("f(a,b,c)", e.getRightOperand().toString());
    final Simplifier s = Simplifier.find(e);
    assertEquals(s, Simplifier.shortestOperandFirst);
    assertNotNull(s);
    assertTrue(s.withinScope(e));
    assertTrue(s.eligible(e));
    final Expression replacement = s.replacement(ASTRewrite.create(e.getAST()), e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) * f(a,b,c,d)", replacement.toString());
  }
  @Test public void oneOpportunityExample() {
    assertOneOpportunity(new SimplificationEngine(), wrap(example));
  }
  @Test public void rightSimplificatioForNulNNVariableReplacement() {
    final InfixExpression e = asExpression("null != a");
    final Simplifier s = Simplifier.find(e);
    assertNotNull(s);
    assertTrue(s.withinScope(e));
    assertTrue(s.eligible(e));
    final Expression replacement = s.replacement(ASTRewrite.create(e.getAST()), e);
    assertNotNull(replacement);
    assertEquals("a != null", replacement.toString());
  }
  @Test public void rightSipmlificatioForNulNNVariable() {
    assertEquals(Simplifier.comparisionWithSpecific, Simplifier.find(asExpression("null != a")));
  }
  @Test public void shorterChainParenthesisComparisonLast() {
    assertSimplifiesTo("a * b * c * d * e * f * g * h == b == c", "c == a * b * c * d * e * f * g * h == b");
  }
  @Test public void testPeel() {
    assertEquals(example, peel(wrap(example)));
  }
  @Test public void isGreaterTrueButAlmostNot() {
    final InfixExpression e = asExpression("f(a,b,c,d) * f(a,b,c)");
    assertEquals("f(a,b,c)", e.getRightOperand().toString());
    assertEquals("f(a,b,c,d)", e.getLeftOperand().toString());
    final Simplifier s = Simplifier.find(e);
    assertEquals(Simplifier.shortestOperandFirst, s);
    assertNotNull(s);
    assertTrue(s.withinScope(e));
    final Expression e1 = e.getLeftOperand();
    final Expression e2 = e.getRightOperand();
    assertFalse(hasNull(e1, e2));
    final boolean tokenWiseGreater = countNodes(e1) > Simplifier.TOKEN_THRESHOLD + countNodes(e2);
    assertFalse(tokenWiseGreater);
    assertTrue(Simplifier.moreArguments(e1, e2));
    assertTrue(Simplifier.longerFirst(e));
    assertTrue(e.toString(), s.eligible(e));
    final Expression replacement = s.replacement(ASTRewrite.create(e.getAST()), e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) * f(a,b,c,d)", replacement.toString());
  }
  @Test public void isGreaterTrue() {
    final InfixExpression e = asExpression("f(a,b,c,d,e) * f(a,b,c)");
    assertEquals("f(a,b,c)", e.getRightOperand().toString());
    assertEquals("f(a,b,c,d,e)", e.getLeftOperand().toString());
    final Simplifier s = Simplifier.find(e);
    assertEquals(Simplifier.shortestOperandFirst, s);
    assertNotNull(s);
    assertTrue(s.withinScope(e));
    final Expression e1 = e.getLeftOperand();
    final Expression e2 = e.getRightOperand();
    assertFalse(hasNull(e1, e2));
    final boolean tokenWiseGreater = countNodes(e1) > Simplifier.TOKEN_THRESHOLD + countNodes(e2);
    assertTrue(tokenWiseGreater);
    assertTrue(Simplifier.moreArguments(e1, e2));
    assertTrue(Simplifier.longerFirst(e));
    assertTrue(e.toString(), s.eligible(e));
    final Expression replacement = s.replacement(ASTRewrite.create(e.getAST()), e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) * f(a,b,c,d,e)", replacement.toString());
  }
  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }
}