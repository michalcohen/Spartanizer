package org.spartan.refactoring.wring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.Into.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.spartanizations.Wrap.POST_EXPRESSION;
import static org.spartan.refactoring.spartanizations.Wrap.PRE_EXPRESSION;
import static org.spartan.refactoring.wring.ExpressionComparator.TOKEN_THRESHOLD;
import static org.spartan.refactoring.wring.ExpressionComparator.countNodes;
import static org.spartan.refactoring.wring.Wrings.COMPARISON_WITH_SPECIFIC;
import static org.spartan.refactoring.wring.Wrings.MULTIPLICATION_SORTER;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;
import static org.spartan.utils.Utils.removePrefix;
import static org.spartan.utils.Utils.removeSuffix;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jface.text.Document;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.spartanizations.TESTUtils;
import org.spartan.refactoring.spartanizations.Wrap;
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
  public static void assertNoChange(final String input) {
    assertSimilar(input, removeSuffix(removePrefix(apply(new Trimmer(), Wrap.Expression.on(input)), PRE_EXPRESSION), POST_EXPRESSION));
  }
  public static int countOpportunities(final Spartanization s, final CompilationUnit u) {
    return s.findOpportunities(u).size();
  }
  protected static int countOppportunities(final Spartanization s, final String input) {
    return s.findOpportunities((CompilationUnit) As.COMPILIATION_UNIT.ast(input)).size();
  }
  public void parenthesizeOfPushdownTernary() {
    assertSimplifiesTo("a ? b+x+e+f:b+y+e+f", "b+(a ? x : y)+e+f");
  }
  static String apply(final Trimmer t, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(t, u, d).get();
  }
  static void assertSimplifiesTo(final String from, final String expected) {
    final String wrap = Wrap.Expression.on(from);
    assertEquals(from, removeSuffix(removePrefix(wrap, PRE_EXPRESSION), POST_EXPRESSION));
    final String unpeeled = apply(new Trimmer(), wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = removeSuffix(removePrefix(unpeeled, PRE_EXPRESSION), POST_EXPRESSION);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + " is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }
  static int countOppportunities(final Spartanization s, final File f) {
    return countOppportunities(s, As.string(f));
  }
  @Test public void bugIntroducingMISSINGWordTry2() {
    assertSimplifiesTo(
  "!(intent.getBooleanExtra(EXTRA_FROM_SHORTCUT, false) && !K9.FOLDER_NONE.equals(mAccount.getAutoExpandFolderName()))",//
  "!intent.getBooleanExtra(EXTRA_FROM_SHORTCUT,false)||K9.FOLDER_NONE.equals(mAccount.getAutoExpandFolderName())");
  }
  @Test public void bugIntroducingMISSINGWordTry3() {
    assertSimplifiesTo(
  "!(f.g(X, false) && !a.b.e(m.h()))",//
  "!f.g(X,false)||a.b.e(m.h())");
  }
  @Test public void bugIntroducingMISSINGWordTry1() {
    assertSimplifiesTo(
        "name.endsWith(testSuffix) && -1 == As.stringBuilder(f).indexOf(testKeyword) ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)",
        "name.endsWith(testSuffix) && As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord1() {
    assertSimplifiesTo(//
        "b.f(a) && -1 == As.g(f).h(c) ? o(s, b, g(f)) : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)",
        "b.f(a) && As.g(f).h(c) == -1 ? o(s,b,g(f)) : b.f(\".in\") && !y(d,b)? o(b.z(u,v),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord1a() {
    assertSimplifiesTo("-1 == As.g(f).h(c)", "As.g(f).h(c)==-1");
  }
  @Test public void bugOfMissingTry() {
    assertSimplifiesTo("!(A && B && C && true && D)", "!A||!B||!C||false||!D");
  }
  @Test public void bugIntroducingMISSINGWord1b() {
    assertSimplifiesTo("b.f(a) && X ? o(s, b, g(f)) : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)",
        "b.f(a)&&X?o(s,b,g(f)):b.f(\".in\")&&!y(d,b)?o(b.z(u,v),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord1c() {
    assertSimplifiesTo("Y ? o(s, b, g(f)) : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)", //
        "Y?o(s,b,g(f)):b.f(\".in\")&&!y(d,b)?o(b.z(u,v),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord1d() {
    assertSimplifiesTo("Y ? Z : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)", //
        "Y?Z:b.f(\".in\")&&!y(d,b)?o(b.z(u,v),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord1e() {
    assertSimplifiesTo("Y ? Z : R ? null : S ? null : T", "Y?Z:!R&&!S?T:null");
  }
  @Test public void bugIntroducingMISSINGWord2() {
    assertSimplifiesTo(//
        "name.endsWith(testSuffix) &&  As.stringBuilder(f).indexOf(testKeyword) == -1? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)", //
        "name.endsWith(testSuffix)&&As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2a() {
    assertSimplifiesTo(//
        "name.endsWith(testSuffix) &&  As.stringBuilder(f).indexOf(testKeyword) == -1? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)", //
        "name.endsWith(testSuffix)&&As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2b() {
    assertSimplifiesTo( //
        "name.endsWith(testSuffix) &&  T ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)", //
        "name.endsWith(testSuffix) && T ? objects(s,name,makeInFile(f)): name.endsWith(\".in\") && !dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2c() {
    assertSimplifiesTo( //
        "X && T ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)", //
        "X && T ? objects(s,name,makeInFile(f)) : name.endsWith(\".in\") && !dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2d() {
    assertSimplifiesTo(//
        "X && T ? E : Y ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)", //
        "X && T ? E : !Y && !dotOutExists(d,name) ? objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2e() {
    assertSimplifiesTo(//
        "X &&  T ? E : Y ? null : Z ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)", //
        "X &&  T ? E : !Y && !Z ? objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2e1() {
    assertSimplifiesTo(//
        "X &&  T ? E : Y ? null : Z ? null : objects(name.replaceAll(x, \"\"), s, f)", //
        "X &&  T ? E : !Y && !Z ? objects(name.replaceAll(x,\"\"),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2e2() {
    assertSimplifiesTo(//
        "X &&  T ? E : Y ? null : Z ? null : objects(name.replaceAll(g, \"\"), s, f)", //
        "X &&  T ? E : !Y && !Z ? objects(name.replaceAll(g,\"\"),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2f() {
    assertSimplifiesTo("X &&  T ? E : Y ? null : Z ? null : F", "X&&T?E:!Y&&!Z?F:null");
  }
  @Test public void bugIntroducingMISSINGWord3() {
    assertSimplifiesTo(
        "name.endsWith(testSuffix) && -1 == As.stringBuilder(f).indexOf(testKeyword) ? objects(s, name, makeInFile(f)) : !name.endsWith(x) ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(3, 56), s, f)",
        "name.endsWith(testSuffix)&&As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(x)&&!dotOutExists(d,name)?objects(name.replaceAll(3,56),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord3a() {
    assertSimplifiesTo("!name.endsWith(x) ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(3, 56), s, f)",
        "name.endsWith(x)&&!dotOutExists(d,name)?objects(name.replaceAll(3,56),s,f):null");
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
  @Test public void comparisonWithSpecific0Legibiliy00() {
    final InfixExpression e = i("this != a");
    assertTrue(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS));
    assertFalse(Is.booleanLiteral(e.getRightOperand()));
    assertFalse(Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS) && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand())));
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
  @Test public void desiredSimplificationOfExample() {
    assertSimplifiesTo("on * notion * of * no * nothion != the * plain + kludge", "no*of*on*notion*nothion!=the*plain+kludge");
  }
  @Test public void doNotIntroduceDoubleNegation() {
    assertSimplifiesTo("!Y ? null :!Z ? null : F", "Y&&Z?F:null");
  }
  @Test public void isGreaterTrue() {
    final InfixExpression e = i("f(a,b,c,d,e) * f(a,b,c)");
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
    assertEquals("f(a,b,c) * f(a,b,c,d,e)", replacement.toString());
  }
  @Test public void isGreaterTrueButAlmostNot() {
    final InfixExpression e = i("f(a,b,c,d) * f(a,b,c)");
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
    assertEquals("f(a,b,c) * f(a,b,c,d)", replacement.toString());
  }
  @Test public void longChainComparison() {
    assertNoChange("a == b == c == d");
  }
  @Test public void longChainParenthesisComparison() {
    assertNoChange("(a == b == c) == d");
  }
  @Test public void longChainParenthesisNotComparison() {
    assertNoChange("(a == b == c) != d");
  }
  @Test public void longerChainParenthesisComparison() {
    assertNoChange("(a == b == c == d == e) == d");
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
  @Test public void notOfAnd() {
    assertSimplifiesTo("!(A && B)", "!A || !B");
  }
  @Test public void oneMultiplication() {
    assertSimplifiesTo("f(a,b,c,d) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d)");
  }
  @Test public void oneMultiplicationAlternate() {
    assertSimplifiesTo("f(a,b,c,d,e) * f(a,b,c)", "f(a,b,c) * f(a,b,c,d,e)");
  }
  @Test public void oneOpportunityExample() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(Wrap.Expression.on(example));
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
  @Test public void shorterChainParenthesisComparison() {
    assertNoChange("a == b == c");
  }
  @Test public void shorterChainParenthesisComparisonLast() {
    assertNoChange("b == a * b * c * d * e * f * g * h == a");
  }
  @Test public void simplifiesTo() {
    assertSimplifiesTo("plain * the + kludge", "the*plain+kludge");
  }
  @Test public void testPeel() {
    assertEquals(example, removeSuffix(removePrefix(Wrap.Expression.on(example), PRE_EXPRESSION), POST_EXPRESSION));
  }
  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }
}
