package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertOneOpportunity;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertSimilar;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.compressSpaces;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.in;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.removePrefix;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.removeSuffix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import il.ac.technion.cs.ssdl.spartan.utils.As;
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
public class SimplificationEngineTest {
  public static final String example = "on * notion * of * no * nothion != the * plain + kludge";
  private static final String POST = //
  "" + //
      ";\n" + //
      " }" + //
      "}" + //
      "";
  private static final String PRE = //
  "package p; \n" + //
      "public class SpongeBob {\n" + //
      " public boolean squarePants() {\n" + //
      "   return ";

  public static final String peel(final String s) {
    return removeSuffix(removePrefix(s, PRE), POST);
  }

  public static final String wrap(final String s) {
    return PRE + s + POST;
  }

  private String apply(final SimplificationEngine s, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    final Document d = new Document(from);
    return TESTUtils.rewrite(s, u, d).get();
  }

  private InfixExpression asExpression(final String expression) {
    return (InfixExpression) As.EXPRESSION.ast(expression);
  }

  private void assertLegible(final String name, final String expression) {
    assertTrue(Simplifier.find(name).eligible(asExpression(expression)));
  }

  private void assertNoChange(final String input) {
    assertSimilar(input, peel(apply(new SimplificationEngine(), wrap(input))));
  }

  private void assertNotLegible(final Simplifier s, final InfixExpression e) {
    assertFalse(s.eligible(e));
  }

  private void assertNotLegible(final String name, final String expression) {
    final Simplifier s = Simplifier.find(name);
    final InfixExpression e = asExpression(expression);
    assertNotLegible(s, e);
  }

  private void assertNotWithinScope(final Simplifier s, final InfixExpression e) {
    assertFalse(s.withinScope(e));
  }

  private void assertNotWithinScope(final String name, final String expression) {
    final Simplifier s = Simplifier.find(name);
    final InfixExpression e = asExpression(expression);
    assertNotWithinScope(s, e);
  }

  private void assertSimplifiesTo(final String from, final String expected) {
    final String wrap = wrap(from);
    assertEquals(from, peel(wrap));
    final String unpeeled = apply(new SimplificationEngine(), wrap);
    final String peeled = peel(unpeeled);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + " is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }

  private void assertWithinScope(final Simplifier s, final InfixExpression e) {
    assertTrue(s.withinScope(e));
  }

  private void assertWithinScope(final String name, final String expression) {
    final Simplifier s = Simplifier.find(name);
    final InfixExpression e = asExpression(expression);
    assertWithinScope(s, e);
  }

  private void asserWithinScope(final String name, final String expression) {
    final Simplifier s = Simplifier.find(name);
    final InfixExpression e = asExpression(expression);
    assertNotWithinScope(s, e);
  }

  @Test public void chainComparison() {
    assertSimplifiesTo("a == true == b == c", "a == b == c");
  }

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
    assertNotWithinScope("Comparison with Boolean", "this != a");
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
    assertNotLegible("Shortest operand first", "this != a");
  }

  @Test public void comparisonWithSpecific0Legibiliy1withinScope() {
    assertNotWithinScope("Comparison with Boolean", "this != a");
  }

  @Test public void comparisonWithSpecific0Legibiliy2() {
    assertTrue(Is.specific(asExpression("this != a").getLeftOperand()));
    assertLegible("Comparison with specific", "this != a");
  }

  @Test public void comparisonWithSpecific0z0() {
    assertWithinScope("Comparison with specific", "this != a");
  }

  @Test public void comparisonWithSpecific0z1() {
    assertLegible("Comparison with specific", "this != a");
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
    assertLegible("Shortest operand first", "a * b * c * d * e * f * g * h== b == c");
  }

  @Test public void longChainComparison() {
    assertSimplifiesTo("a == b == c == d", "a == b == c == d");
  }

  @Test public void longChainParenthesisComparison() {
    assertSimplifiesTo("(a == b == c) == d", "d == (a == b == c == d)");
  }

  @Test public void longChainParenthesisNotComparison() {
    assertSimplifiesTo("(a == b == c) != d", "d != (a == b == c )");
  }

  @Test public void longerChainParenthesisComparison() {
    assertSimplifiesTo("(a == b == c == d == e) == d", "d == (a == b == c == d)");
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
    assertEquals("a != null", replacement);
  }

  @Test public void rightSipmlificatioForNulNNVariable() {
    assertEquals(Simplifier.find("Comparison with specific"), Simplifier.find(asExpression("null != a")));
  }

  @Test public void shorterChainParenthesisComparison() {
    assertSimplifiesTo("a == b == c", "c == (a == b)");
  }

  @Test public void shorterChainParenthesisComparisonLast() {
    assertSimplifiesTo("a * b * c * d * e * f * g * h== b == c", "b == a * b * c * d * e * f * g * h == c");
  }

  @Test public void testPeel() {
    assertEquals(example, peel(wrap(example)));
  }

  @Test public void threeMultiplication() {
    assertSimplifiesTo("f(a,b,c,d) * f(a,b,c) * f()", "f() * f(a,b,c) * f(a,b,c,d)");
  }

  @Test public void twoMultiplication0() {
    assertSimplifiesTo("f(a,b,c,d) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d)");
  }

  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }
}