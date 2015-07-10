package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertOneOpportunity;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertSimilar;
import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.compressSpaces;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.removePrefix;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.removeSuffix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import il.ac.technion.cs.ssdl.spartan.utils.As;

/**
 * * Unit tests for the nesting class Unit test for the containing class. Note
 * our naming convention: a) test methods do not use the redundant "test"
 * prefix. b) test methods begin with the name of the method they check.
 *
 * @author Yossi Gil
 * @since 2014-07-10
 *
 */
@FixMethodOrder(MethodSorters.JVM) //
@SuppressWarnings({ "static-method", "javadoc" }) //
public class SimplificationEngineTest {
  public static final String example = "on * notion * of * no * nothion != the * plain + kludge";
  private static final String PRE = //
  "package p; \n" + //
      "public class SpongeBob {\n" + //
      " public boolean squarePants() {\n" + //
      "   return ";
  private static final String POST = //
  "" + //
      ";\n" + //
      " }" + //
      "}" + //
      "";

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

  private void assertNoChange(final String input) {
    assertSimilar(input, peel(apply(new SimplificationEngine(), wrap(input))));
  }

  private void assertSimplifiesTo(final String from, final String expected) {
    final String result = peel(apply(new SimplificationEngine(), wrap(from)));
    if (result.equals(from))
      assertNotEquals("No similification of " + from, from, result);
    if (compressSpaces(result).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + " is just reformatting", compressSpaces(result), compressSpaces(from));
    assertSimilar(expected, result);
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

  @Test public void comparisonWithSpecific() {
    assertSimplifiesTo("this != a", "a != this");
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

  @Test public void twoMultiplication0() {
    assertSimplifiesTo("f(a,b,c,d) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d)");
  }

  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }

  @Test public void threeMultiplication() {
    assertSimplifiesTo("f(a,b,c,d) * f(a,b,c) * f()", "f() * f(a,b,c) * f(a,b,c,d)");
  }

  @Test public void desiredSimplificationOfExample() {
    final String from = example;
    final String to = "on*notion*of*no*nothion != kludge+the*plain";
    assertSimplifiesTo(from, to);
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

  @Test public void testPeel() {
    assertEquals(example, peel(wrap(example)));
  }
}