package org.spartan.refactoring.wring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.Into.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertNoChange;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.wring.ExpressionComparator.TOKEN_THRESHOLD;
import static org.spartan.refactoring.wring.ExpressionComparator.countNodes;
import static org.spartan.refactoring.wring.Wrings.COMPARISON_WITH_SPECIFIC;
import static org.spartan.refactoring.wring.Wrings.MULTIPLICATION_SORTER;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;

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
import org.spartan.refactoring.wring.AsRefactoring;
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
    assertSimilar(input, Wrap.Expression.off(apply(new Trimmer(), Wrap.Expression.on(input))));
  }
  public static int countOpportunities(final Spartanization s, final CompilationUnit u) {
    return s.findOpportunities(u).size();
  }
  protected static int countOppportunities(final Spartanization s, final String input) {
    return s.findOpportunities((CompilationUnit) As.COMPILIATION_UNIT.ast(input)).size();
  }
  static String apply(final Trimmer t, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(t, u, d).get();
  }
  static String apply(final Wring w, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(new AsRefactoring(w, "Tested Refactoring", ""), u, d).get();
  }
  static void assertSimplifiesTo(final String from, final String expected) {
    final String wrap = Wrap.Expression.on(from);
    assertEquals(from, Wrap.Expression.off(wrap));
    final String unpeeled = apply(new Trimmer(), wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = Wrap.Expression.off(unpeeled);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + " is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }
  static void assertSimplifiesTo(final String from, final String expected, final Wring wring, final Wrap wrapper) {

    final String wrap = wrapper.on(from);
    assertEquals(from, wrapper.off(wrap));

    final String unpeeled = apply(wring, wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = wrapper.off(unpeeled);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + " is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }
  static int countOppportunities(final Spartanization s, final File f) {
    return countOppportunities(s, As.string(f));
  }
  @Test public void actualExampleForSortAddition() {
    assertNoChange("1 + b.statements().indexOf(declarationStmt)");
  }
  @Test public void actualExampleForSortAdditionInContext() {
    assertSimplifiesTo("2 + a < b", //
        "a + 2 < b");
  }
  @Test public void bugIntroducingMISSINGWord1() {
    assertSimplifiesTo(//
        "b.f(a) && -1 == As.g(f).h(c) ? o(s, b, g(f)) : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)",
        "b.f(a) && As.g(f).h(c) == -1 ? o(s,b,g(f)) : b.f(\".in\") && !y(d,b)? o(b.z(u,v),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord1a() {
    assertSimplifiesTo("-1 == As.g(f).h(c)", "As.g(f).h(c)==-1");
  }
  @Test public void bugIntroducingMISSINGWord1b() {
    assertSimplifiesTo("b.f(a) && X ? o(s, b, g(f)) : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)",
        "b.f(a)&&X?o(s,b,g(f)):b.f(\".in\")&&!y(d,b)?o(b.z(u,v),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord1c() {
    assertSimplifiesTo(//
        "Y ? o(s, b, g(f)) : !b.f(\".in\") ? null : y(d, b) ? null : o(b.z(u, v), s, f)", //
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
  @Test public void bugIntroducingMISSINGWordTry1() {
    assertSimplifiesTo(
        "name.endsWith(testSuffix) && -1 == As.stringBuilder(f).indexOf(testKeyword) ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", \"\"), s, f)",
        "name.endsWith(testSuffix) && As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",\"\"),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWordTry2() {
    assertSimplifiesTo("!(intent.getBooleanExtra(EXTRA_FROM_SHORTCUT, false) && !K9.FOLDER_NONE.equals(mAccount.getAutoExpandFolderName()))", //
        "!intent.getBooleanExtra(EXTRA_FROM_SHORTCUT,false)||K9.FOLDER_NONE.equals(mAccount.getAutoExpandFolderName())");
  }
  @Test public void bugIntroducingMISSINGWordTry3() {
    assertSimplifiesTo("!(f.g(X, false) && !a.b.e(m.h()))", //
        "!f.g(X,false)||a.b.e(m.h())");
  }
  @Test public void bugOfMissingTry() {
    assertSimplifiesTo("!(A && B && C && true && D)", "!A||!B||!C||false||!D");
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
    assertSimplifiesTo("a == false", "!a");
  }
  @Test public void compareWithBoolean10() {
    assertSimplifiesTo("true == a", "a");
  }
  @Test public void compareWithBoolean100() {
    assertSimplifiesTo("a != true", "!a");
  }
  @Test public void compareWithBoolean100a() {
    assertSimplifiesTo("(((a))) != true", "!a");
  }
  @Test public void compareWithBoolean101() {
    assertSimplifiesTo("a != false", "a");
  }
  @Test public void compareWithBoolean11() {
    assertSimplifiesTo("false == a", "!a");
  }
  @Test public void compareWithBoolean110() {
    assertSimplifiesTo("true != a", "!a");
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
    assertSimplifiesTo("false == false", "true");
  }
  @Test public void compareWithBoolean5() {
    assertSimplifiesTo("false == true", "false");
  }
  @Test public void compareWithBoolean6() {
    assertSimplifiesTo("false != false", "false");
  }
  @Test public void compareWithBoolean7() {
    assertSimplifiesTo("true != true", "false");
  }
  @Test public void compareWithBoolean8() {
    assertSimplifiesTo("true != false", "true");
  }
  @Test public void compareWithBoolean9() {
    assertSimplifiesTo("true != true", "false");
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
  public void parenthesizeOfpushdownTernary() {
    assertSimplifiesTo("a ? b+x+e+f:b+y+e+f", "b+(a ? x : y)+e+f");
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
    assertEquals(example, Wrap.Expression.off(Wrap.Expression.on(example)));
  }
  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }
  @Test public void simplifyBlockEmpty() {
    assertSimplifiesTo("{;;}", "", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockComplexEmpty() {
    assertSimplifiesTo("{;;{;{{}}}{;}{};}", "", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockDeeplyNestedReturn() {
    assertSimplifiesTo(" {{{;return c;};;};}", " return c;", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockComplexSingleton() {
    assertSimplifiesTo("{;{{;;return b; }}}", " return b;", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockThreeStatements() {
    assertSimplifiesTo("{i++;{{;;return b; }}j++;}", " i++;return b;j++;", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockExpressionVsExpression() {
    assertNoChange(" 6 - 7 < 2 + 1   ");  // Note that we also need to generalize NoChange to work with other Wrings\Wrap types
  }
  @Test public void simplifyBlockLiteralVsLiteral() {
    assertNoChange("if (a) return b; else c;");
  }
  @Test public void pushdownTernaryExpressionVsExpression() {
    assertNoChange(" 6 - 7 < 2 + 1   ");
  }
  @Test public void pushdownTernaryLiteralVsLiteral() {
    assertNoChange("1 < 102333");
  }
  @Test public void pushdownTernaryActualExample() {
    assertNoChange("next < values().length");
  }
  @Test public void pushdownTernaryNoBoolean() {
    assertNoChange("a?b:c");
  }
  @Test public void pushdownTernaryFX() {
    assertSimplifiesTo("a ? false : c","!a && c");
  }
  @Test public void pushdownTernaryTX() {
    assertSimplifiesTo("a ? true : c",, "a || c");
  }
  @Test public void pushdownTernaryXF() {
    assertSimplifiesTo("a ? b : false","a && b");
  }
  @Test public void pushdownTernaryXT() {
    assertSimplifiesTo("a ? b : true","!a || b");
  }
  @Test public void pushdownTernaryParFX() {
    assertNoChange("a ?( false):true");
  }
  @Test public void pushdownTernaryParTX() {
    assertSimplifiesTo("a ? (((true ))): c");
  }
  @Test public void pushdownTernaryParXF() {
    assertSimplifiesTo("a ? b : (false)");
  }
  @Test public void pushdownTernaryParXT() {
    assertSimplifiesTo("a ? b : ((true))","");
  }
  @Test public void pushdownTernaryActualExample2() {
    assertSimplifiesTo("!inRange(m, e) ? true : inner.go(r, e)");
  }
  @Test public void pushdownTernaryMethodInvocationFirst() {
    assertNoChange("a?b():c");
  }
  @Test public void pushdownTernaryNotSameFunctionInvocation() {
    assertNoChange("a?b(x):d(x)");
  }
  @Test public void pushdownTernaryNotSameFunctionInvocation2() {
    assertNoChange("a?x.f(x):x.d(x)");
  }
  @Test public void pushdownTernaryIdenticalMethodCall() {
    assertNoChange("a ? y.f(b) :y.f(b)");
  }
  @Test public void pushdownTernaryIdenticalFunctionCall() {
    assertSimplifiesTo("a ? f(b) :f(b)");
  }
  @Test public void pushdownTernaryIdenticalAssignment() {
    assertNoChange("a ? (b=c) :(b=c)");
  }
  @Test public void pushdownTernaryIdenticalIncrement() {
    assertNoChange("a ? b++ :b++");
  }
  @Test public void pushdownTernaryIdenticalAddition() {
    assertSimplifiesTo("a ? b+d :b+ d");
  }
  @Test public void pushdownTernaryFunctionCall() {
    assertNoChange("a ? f(b,c) : f(c)");
  }
  @Test public void pushdownTernaryAMethodCall() {
    assertNoChange("a ? y.f(c,b) :y.f(c)");
  }
  @Test public void pushdownTernaryAMethodCallDistinctReceiver() {
    assertNoChange("a ? x.f(c) : y.f(d)");
  }
  @Test public void pushdownTernaryNotOnMINUS() {
    assertNoChange("a ? -c :-d");
  }
  @Test public void pushdownTernaryNotOnNOT() {
    assertNoChange("a ? !c :!d");
  }
  @Test public void pushdownTernaryNotOnMINUSMINUS1() {
    assertNoChange("a ? --c :--d");
  }
  @Test public void pushdownTernaryNotOnMINUSMINUS2() {
    assertNoChange("a ? c-- :d--");
  }
  @Test public void pushdownTernaryNotOnPLUSPLUS() {
    assertNoChange("a ? x++ :y++");
  }
  @Test public void pushdownTernaryNotOnPLUS() {
    assertNoChange("a ? +x : +y");
  }
  @Test public void pushdownTernaryIntoConstructorNotSameArity() {
    assertNoChange("a ? new S(a,new Integer(4),b) : new S(new Ineger(3))");
  }
  @Test public void pushdownTernaryFieldRefernece() {
    assertNoChange("externalImage ? R.string.webview_contextmenu_image_download_action : R.string.webview_contextmenu_image_save_action");
  }
  @Test public void pushdownTernaryAlmostIdenticalFunctionCall() {
    assertSimplifiesTo("a ? f(b) :f(c)", "f(a ? b : c)");
  }
  @Test public void pushdownTernaryAlmostIdenticalMethodCall() {
    assertSimplifiesTo("a ? y.f(b) :y.f(c)", "y.f(a ? b : c)");
  }
  @Test public void pushdownTernaryAlmostIdenticalTwoArgumentsFunctionCall1Div2() {
    assertSimplifiesTo("a ? f(b,x) :f(c,x)", "f(a ? b : c,x)");
  }
  @Test public void pushdownTernaryAlmostIdenticalTwoArgumentsFunctionCall2Div2() {
    assertSimplifiesTo("a ? f(x,b) :f(x,c)", "f(x,a ? b : c)");
  }
  @Test public void pushdownTernaryAlmostIdenticalAssignment() {
    assertSimplifiesTo("a ? (b=c) :(b=d)", "b = a ? c : d");
  }
  @Test public void pushdownTernaryAlmostIdentical2Addition() {
    assertSimplifiesTo("a ? b+d :b+ c", "b+(a ? d : c)");
  }
  @Test public void pushdownTernaryAlmostIdentical3Addition() {
    assertSimplifiesTo("a ? b+d +x:b+ c + x", "b+(a ? d : c) + x");
  }
  @Test public void pushdownTernaryAlmostIdentical4AdditionLast() {
    assertSimplifiesTo("a ? b+d+e+y:b+d+e+x", "b+d+e+(a ? y : x)");
  }
  @Test public void pushdownTernaryAlmostIdentical4AdditionSecond() {
    assertSimplifiesTo("a ? b+x+e+f:b+y+e+f", "b+(a ? x : y)+e+f");
  }
  @Test public void pushdownTernaryDifferentTargetFieldRefernce() {
    assertSimplifiesTo("a ? 1 + x.a : 1 + y.a", "1+(a ? x.a : y.a)");
  }
  @Test public void pushdownTernaryIntoConstructor1Div1Location() {
    assertSimplifiesTo("a.equal(b) ? new S(new Integer(4)) : new S(new Ineger(3))", "new S(a.equal(b)? new Integer(4): new Ineger(3))");
  }
  @Test public void pushdownTernaryIntoConstructor1Div3() {
    assertSimplifiesTo("a.equal(b) ? new S(new Integer(4),a,b) : new S(new Ineger(3),a,b)", "new S(a.equal(b)? new Integer(4): new Ineger(3), a, b)");
  }
  @Test public void pushdownTernaryIntoConstructor2Div3() {
    assertSimplifiesTo("a.equal(b) ? new S(a,new Integer(4),b) : new S(a, new Ineger(3), b)", "new S(a,a.equal(b)? new Integer(4): new Ineger(3),b)");
  }
  @Test public void pushdownTernaryIntoConstructor3Div3() {
    assertSimplifiesTo("a.equal(b) ? new S(a,b,new Integer(4)) : new S(a,b,new Ineger(3))", "new S(a, b, a.equal(b)? new Integer(4): new Ineger(3))");
  }
  @Test public void pushdownNotSummation() {
    assertNoChange("a+b");
  }
  @Test public void pushdownNotMultiplication() {
    assertNoChange("a*b");
  }
  @Test public void pushdownNotOR() {
    assertNoChange("a||b");
  }
  @Test public void pushdownNotEND() {
    assertNoChange("a&&b");
  }
  @Test public void pushdownNotSimpleNot() {
    assertNoChange("!a");
  }
  @Test public void pushdownNotSimpleNotOfFunction() {
    assertNoChange("!f(a)");
  }
  @Test public void pushdownNotActualExample() {
    assertNoChange("!inRange(m, e)");
  }
  @Test public void pushdownNot2LevelNotOfFalse() {
    assertSimplifiesTo("!!false", "false");
  }
  @Test public void pushdownNot2LevelNotOfTrue() {
    assertSimplifiesTo("!!true", "true");
  }
  @Test public void pushdownNotDoubleNotDeeplyNested() {
    assertSimplifiesTo("!(((!f())))", "f()");
  }
  @Test public void pushdownNotDoubleNot() {
    assertSimplifiesTo("!!f()", "f()");
  }
  @Test public void pushdownNotDoubleNotNested() {
    assertSimplifiesTo("!(!f())", "f()");
  }
  @Test public void pushdownNotNotOfAND() {
    assertSimplifiesTo("!(a && b && c)", "!a || !b || !c");
  }
  @Test public void pushdownNotNotOfAND2() {
    assertSimplifiesTo("!(f() && f(5))", "!f() || !f(5)");
  }
  @Test public void pushdownNotNotOfANDNested() {
    assertSimplifiesTo("!(f() && (f(5)))", "!f() || !f(5)");
  }
  @Test public void pushdownNotNotOfEQ() {
    assertSimplifiesTo("!(3 == 5)", "3 != 5");
  }
  @Test public void pushdownNotNotOfEQNested() {
    assertSimplifiesTo("!((((3 == 5))))", "3 != 5");
  }
  @Test public void pushdownNotNotOfFalse() {
    assertSimplifiesTo("!false", "true");
  }
  @Test public void pushdownNotNotOfGE() {
    assertSimplifiesTo("!(3 >= 5)", "3 < 5");
  }
  @Test public void pushdownNotNotOfGT() {
    assertSimplifiesTo("!(3 > 5)", "3 <= 5");
  }
  @Test public void pushdownNotNotOfLE() {
    assertSimplifiesTo("!(3 <= 5)", "3 > 5");
  }
  @Test public void pushdownNotNotOfLT() {
    assertSimplifiesTo("!(3 < 5)", "3 >= 5");
  }
  @Test public void pushdownNotNotOfNE() {
    assertSimplifiesTo("!(3 != 5)", "3 == 5");
  }
  @Test public void pushdownNotNotOfOR2() {
    assertSimplifiesTo("!(f() || f(5))", "!f() && !f(5)");
  }
  @Test public void pushdownNotNotOfOR() {
    assertSimplifiesTo("!(a || b || c)", "!a && !b && !c");
  }
  @Test public void pushdownNotNotOfWrappedOR() {
    assertSimplifiesTo("!((a) || b || c)", "!a && !b && !c");
  }
  @Test public void pushdownNotNotOfTrue() {
    assertSimplifiesTo("!true", "false");
  }
  @Test public void pushdownNotNotOfTrue2() {
    assertSimplifiesTo("!!true", "true");
  }
  @Test public void orFalseProductIsNotANDDivOR() {
    assertNoChange("2*a");
  }
  @Test public void orFalseANDWithoutBoolean() {
    assertNoChange("b && a");
  }
  @Test public void orFalseORWithoutBoolean() {
    assertNoChange("b || a");
  }
  @Test public void orFalseOROf3WithoutBooleanA() {
    assertNoChange("x || a || b");
  }
  @Test public void orFalseOROf4WithoutBooleanA() {
    assertNoChange("x || a || b || c");
  }
  @Test public void orFalseOROf5WithoutBooleanA() {
    assertNoChange("x || a || b || c || d");
  }
  @Test public void orFalseOROf6WithoutBooleanA() {
    assertNoChange("x || a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithoutBooleanWithParenthesisA() {
    assertNoChange("x || (a || b) || (c || (d || e))");
  }
  @Test public void orFalseANDOf3WithoutBooleanA() {
    assertNoChange("x && a && b");
  }
  @Test public void orFalseANDOf4WithoutBooleanA() {
    assertNoChange("x && a && b && c");
  }
  @Test public void orFalseANDOf5WithoutBooleanA() {
    assertNoChange("x && a && b && c && d");
  }
  @Test public void orFalseANDOf6WithoutBooleanA() {
    assertNoChange("x && a && b && c && d && e");
  }
  @Test public void orFalseANDOf6WithoutBooleanWithParenthesis() {
    assertNoChange("(x && (a && b)) && (c && (d && e))");
  }
  @Test public void orFalseANDWithFalse() {
    assertNoChange("b && a");
  }
  @Test public void orFalseORFalseWithSomething() {
    assertNoChange("true || a");
  }
  @Test public void orFalseORSomethingWithTrue() {
    assertNoChange("a || true");
  }
  @Test public void orFalseOROf3WithoutBoolean() {
    assertNoChange("a || b");
  }
  @Test public void orFalseOROf4WithoutBoolean() {
    assertNoChange("a || b || c");
  }
  @Test public void orFalseOROf5WithoutBoolean() {
    assertNoChange("a || b || c || d");
  }
  @Test public void orFalseOROf6WithoutBoolean() {
    assertNoChange("a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithoutBooleanWithParenthesis() {
    assertNoChange("(a || b) || (c || (d || e))");
  }
  @Test public void orFalseANDOf3WithoutBoolean() {
    assertNoChange("a && b && false");
  }
  @Test public void orFalseANDOf4WithoutBoolean() {
    assertNoChange("a && b && c && false");
  }
  @Test public void orFalseANDOf5WithoutBoolean() {
    assertNoChange("false && a && b && c && d");
  }
  @Test public void orFalseANDOf6WithoutBoolean() {
    assertNoChange("a && b && c && false && d && e");
  }
  @Test public void orFalseANDOf7WithoutBooleanWithParenthesis() {
    assertNoChange("(a && b) && (c && (d && (e && false)))");
  }
  @Test public void orFalseANDOf7WithoutBooleanAndMultipleFalseValue() {
    assertNoChange("(a && (b && false)) && (c && (d && (e && (false && false))))");
  }
  @Test public void orFalseTrueAndTrueA() {
    assertSimplifiesTo("true && true");
  }
  @Test public void orFalseANDOf3WithTrueA() {
    assertSimplifiesTo("a && b && true", "a && b");
  }
  @Test public void orFalseANDOf4WithTrueA() {
    assertSimplifiesTo("a && b && c && true","");
  }
  @Test public void orFalseANDOf5WithTrueA() {
    assertSimplifiesTo("true && a && b && c && d","");
  }
  @Test public void orFalseANDOf6WithTrueA() {
    assertNoChange("a && b && c && true && d && e");
  }
  @Test public void orFalseANDOf7WithTrueWithParenthesis() {
    assertSimplifiesTo("true && (a && b) && (c && (d && (e && true)))","");
  }
  @Test public void orFalseANDOf7WithMultipleTrueValue() {
    assertSimplifiesTo("(a && (b && true)) && (c && (d && (e && (true && true))))","");
  }
  @Test public void orFalseANDOf3WithTrue() {
    assertSimplifiesTo("true && x && true && a && b", "x && a && b","");
  }
  @Test public void orFalseANDOf4WithTrue() {
    assertSimplifiesTo("x && true && a && b && c","x && a && b && c","");
  }
  @Test public void orFalseANDOf5WithTrue() {
    assertSimplifiesTo("x && a && b && c && true && true && true && d","");
  }
  @Test public void orFalseANDOf6WithTrue() {
    assertSimplifiesTo("x && a && true && b && c && d && e","");
  }
  @Test public void orFalseANDOf6WithTrueWithParenthesis() {
    assertSimplifiesTo("x && (true && (a && b && true)) && (c && (d && e))","");
  }
  @Test public void orFalseANDWithTrue() {
    assertSimplifiesTo("true && b && a","");
  }

  @Test public void orFalseFalseOrFalse() {
    assertSimplifiesTo("false ||false", "false");
  }
  @Test public void orFalse3ORTRUE() {
    assertSimplifiesTo("false || false || false", "false");
  }
  @Test public void orFalse4ORTRUE() {
    assertSimplifiesTo("false || false || false || false", "false");
  }
  @Test public void orFalseOROf3WithFalse() {
    assertSimplifiesTo("x || false || b", "x || b");
  }
  @Test public void orFalseOROf4WithFalse() {
    assertSimplifiesTo("x || a || b || c || false", "x || a || b || c");
  }
  @Test public void orFalseOROf5WithFalse() {
    assertSimplifiesTo("x || a || false || c || d", "x || a || c || d");
  }
  @Test public void orFalseOROf6WithFalse() {
    assertSimplifiesTo("false || x || a || b || c || d || e", "x || a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithFalseWithParenthesis() {
    assertSimplifiesTo("x || (a || (false) || b) || (c || (d || e))", "x || a || b || c || d || e");
  }
  @Test public void orFalseORFalseWithSomethingB() {
    assertSimplifiesTo("false || a || false", "a");
  }
  @Test public void orFalseORSomethingWithFalse() {
    assertSimplifiesTo("false || a || false", "a");
  }
  @Test public void orFalseOROf3WithFalseB() {
    assertSimplifiesTo("false || a || b || false", "a || b");
  }
  @Test public void orFalseOROf4WithFalseB() {
    assertSimplifiesTo("a || b || false || c", "a || b || c");
  }
  @Test public void orFalseOROf5WithFalseB() {
    assertSimplifiesTo("a || b || c || d || false", "a || b || c || d");
  }
  @Test public void orFalseOROf6WithTwoFalse() {
    assertSimplifiesTo("a || false || b || false || c || d || e", "a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithFalseWithParenthesisB() {
    assertSimplifiesTo("(a || b) || false || (c || false || (d || e || false))", "a || b || c || d || e");
  }


}
