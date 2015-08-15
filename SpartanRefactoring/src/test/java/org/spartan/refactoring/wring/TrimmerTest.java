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
  static void assertConvertsTo(final String from, final String expected) {
    assertWrappedTranslation(from, expected, Wrap.Statement);
  }
  static void assertSimplifiesTo(final String from, final String expected) {
    assertWrappedTranslation(from, expected, Wrap.Expression);
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
  static void assertWrappedTranslation(final String from, final String expected, final Wrap w) {
    final String wrap = w.on(from);
    assertEquals(from, w.off(wrap));
    final String unpeeled = apply(new Trimmer(), wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = w.off(unpeeled);
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
    assertSimplifiesTo("2 + a < b", "a + 2 < b");
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
  /* End of the already good tests */
  /* Begin of converted test files */
  @Test public void comaprisonWithBoolean1() {
    assertSimplifiesTo("s.equals(\"yada\")==true", "s.equals(\"yada\")");
  }
  @Test public void comaprisonWithBoolean2() {
    assertSimplifiesTo("s.equals(\"yada\")==false ", "!s.equals(\"yada\")");
  }
  @Test public void comaprisonWithBoolean3() {
    assertSimplifiesTo("(false==s.equals(\"yada\"))", "(!s.equals(\"yada\"))");
  }
  @Test public void comaprisonWithSpecific0() {
    assertSimplifiesTo("this != a", "a != this");
  }
  @Test public void comaprisonWithSpecific0Legibiliy00() {
    final InfixExpression e = i("this != a");
    assertTrue(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS));
    assertFalse(Is.booleanLiteral(e.getRightOperand()));
    assertFalse(Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand()));
    assertFalse(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS) && (Is.booleanLiteral(e.getRightOperand()) || Is.booleanLiteral(e.getLeftOperand())));
  }
  @Test public void comaprisonWithSpecific1() {
    assertSimplifiesTo("null != a", "a != null");
  }
  @Test public void comaprisonWithSpecificInParenthesis() {
    assertSimplifiesTo("(null==a)", "(a==null)");
  }
  @Test public void comaprisonWithSpecific2() {
    assertSimplifiesTo("null != a", "a != null");
    assertSimplifiesTo("this == a", "a == this");
    assertSimplifiesTo("null == a", "a == null");
    assertSimplifiesTo("this >= a", "a <= this");
    assertSimplifiesTo("null >= a", "a <= null");
    assertSimplifiesTo("this <= a", "a >= this");
    assertSimplifiesTo("null <= a", "a >= null");
  }
  @Test public void comaprisonWithSpecific2a() {
    assertSimplifiesTo("s.equals(\"yada\")==false", "!s.equals(\"yada\")");
  }
  @Test public void comaprisonWithSpecific3() {
    assertSimplifiesTo("(this==s.equals(\"yada\"))", "(s.equals(\"yada\")==this)");
  }
  @Test public void comaprisonWithSpecific4() {
    assertSimplifiesTo("(0 < a)", "(a>0)");
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
  @Test public void desiredSimplificationOfExample() {
    assertSimplifiesTo("on * notion * of * no * nothion != the * plain + kludge", "no*of*on*notion*nothion!=the*plain+kludge");
  }
  @Test public void doNotIntroduceDoubleNegation() {
    assertSimplifiesTo("!Y ? null :!Z ? null : F", "Y&&Z?F:null");
  }
  @Test public void extractMethodSplitDifferentStories() {
    assertSimplifiesTo("", "");
  }
  @Test public void forwardDeclaration1() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = 6;   int j = 2;   int k = i+2;   System.out.println(i-j+k); ",
        "  /*    * This is a comment    */      int j = 2;   int i = 6;   int k = i+2;   System.out.println(i-j+k); ");
  }
  @Test public void forwardDeclaration2() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = 6, h = 7;   int j = 2;   int k = i+2;   System.out.println(i-j+k); ",
        "  /*    * This is a comment    */      int h = 7;   int j = 2;   int i = 6;   int k = i+2;   System.out.println(i-j+k); ");
  }
  @Test public void forwardDeclaration3() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = 6;   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   yada3(i);   yada3(i+m); ",
        "  /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   int i = 6;   yada3(i);   yada3(i+m); ");
  }
  @Test public void forwardDeclaration4() {
    assertSimplifiesTo(
        "  /*    * This is a comment    */      int i = 6;   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   final BlahClass bc = new BlahClass(i);   yada3(i+m+bc.j);    private static class BlahClass {   public BlahClass(int i) {    j = 2*i;      public final int j; ",
        "  /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   int i = 6;   final BlahClass bc = new BlahClass(i);   yada3(i+m+bc.j);    private static class BlahClass {   public BlahClass(int i) {    j = 2*i;      public final int j; ");
  }
  @Test public void forwardDeclaration5() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = yada3(0);   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ",
        "  /*    * This is a comment    */      int j = 3;   int k = j+2;   int i = yada3(0);   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ");
  }
  @Test public void forwardDeclaration6() {
    assertSimplifiesTo(
        "  /*    * This is a comment    */      int i = yada3(0);   int h = 8;   int j = 3;   int k = j+2 + yada3(i);   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ",
        "  /*    * This is a comment    */      int h = 8;   int i = yada3(0);   int j = 3;   int k = j+2 + yada3(i);   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ");
  }
  @Test public void forwardDeclaration7() {
    assertSimplifiesTo(
        "   j = 2*i;   }      public final int j;    private BlahClass yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   System.out.println(res.j);   return res; ",
        "   j = 2*i;   }      public final int j;    private BlahClass yada6() {   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   final BlahClass res = new BlahClass(6);   System.out.println(res.j);   return res; ");
  }
  @Test public void inlineSingleUse01() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = yada3(0);   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + i); ",
        "  /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + (yada3(0))); ");
  }
  @Test public void inlineSingleUse02() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = 5,j=3;   int k = j+2;   int m = k + j -19 +i;   yada3(k); ",
        "  /*    * This is a comment    */      int j=3;   int k = j+2;   int m = k + j -19 +(5);   yada3(k); ");
  }
  @Test public void inlineSingleUse03() {
    assertSimplifiesTo("  /*    * This is a comment    */      int i = 5;   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + i); ",
        "  /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + (5)); ");
  }
  @Test public void inlineSingleUse04() {
    assertSimplifiesTo("  int x = 6;   final BlahClass b = new BlahClass(x);   int y = 2+b.j;   yada3(y-b.j);   yada3(y*2); ",
        "  final BlahClass b = new BlahClass((6));   int y = 2+b.j;   yada3(y-b.j);   yada3(y*2); ");
  }
  @Test public void inlineSingleUse05() {
    assertSimplifiesTo("  int x = 6;   final BlahClass b = new BlahClass(x);   int y = 2+b.j;   yada3(y+x);   yada3(y*x); ",
        "  int x = 6;   int y = 2+(new BlahClass(x)).j;   yada3(y+x);   yada3(y*x); ");
  }
  @Test public void inlineSingleUse06() {
    assertNoChange(
        "    final Collection<Integer> outdated = new ArrayList<>();     int x = 6, y = 7;     System.out.println(x+y);     final Collection<Integer> coes = new ArrayList<>();     for (final Integer pi : coes)      if (pi.intValue() < x - y)       outdated.add(pi);     for (final Integer pi : outdated)      coes.remove(pi);     System.out.println(coes.size()); ");
  }
  @Test public void inlineSingleUse07() {
    assertNoChange(
        "    final Collection<Integer> outdated = new ArrayList<>();     int x = 6, y = 7;     System.out.println(x+y);     final Collection<Integer> coes = new ArrayList<>();     for (final Integer pi : coes)      if (pi.intValue() < x - y)       outdated.add(pi);     System.out.println(coes.size()); ");
  }
  @Test public void inlineSingleUse08() {
    assertNoChange(
        "    final Collection<Integer> outdated = new ArrayList<>();     int x = 6, y = 7;     System.out.println(x+y);     final Collection<Integer> coes = new ArrayList<>();     for (final Integer pi : coes)      if (pi.intValue() < x - y)       outdated.add(pi);     System.out.println(coes.size());     System.out.println(outdated.size()); ");
  }
  @Test public void inlineSingleUse09() {
    assertNoChange(
        "  final Application a = new DuplicateCurrent().new Application(&quot;{\nABRA\n{\nCADABRA\n{&quot;);   assertEquals(5, a.new Context().lineCount());   final PureIterable&lt;Mutant&gt; ms = a.generateMutants();   assertEquals(2, count(ms));   final PureIterator&lt;Mutant&gt; i = ms.iterator();   assertTrue(i.hasNext());   assertEquals(&quot;{\nABRA\nABRA\n{\nCADABRA\n{\n&quot;, i.next().text);   assertTrue(i.hasNext());   assertEquals(&quot;{\nABRA\n{\nCADABRA\nCADABRA\n{\n&quot;, i.next().text);   assertFalse(i.hasNext());  ");
  }
  @Test public void inlineSingleUse10() {
    assertNoChange(
        "       final Application a = new Application(\"{\nABRA\n{\nCADABRA\n{\");        assertEquals(5, a.new Context().lineCount());        final PureIterable<Mutant> ms = a.mutantsGenerator();        assertEquals(2, count(ms));        final PureIterator<Mutant> i = ms.iterator();        assertTrue(i.hasNext());        assertEquals(\"{\nABRA\nABRA\n{\nCADABRA\n{\n\", i.next().text);        assertTrue(i.hasNext());        assertEquals(\"{\nABRA\n{\nCADABRA\nCADABRA\n{\n\", i.next().text);        assertFalse(i.hasNext());");
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
  @Test public void orFalse3ORTRUE() {
    assertSimplifiesTo("false || false || false", "false");
  }
  @Test public void orFalse4ORTRUE() {
    assertSimplifiesTo("false || false || false || false", "false");
  }
  @Test public void orFalseANDOf3WithoutBoolean() {
    assertNoChange("a && b && false");
  }
  @Test public void orFalseANDOf3WithoutBooleanA() {
    assertNoChange("x && a && b");
  }
  @Test public void orFalseANDOf3WithTrue() {
    assertSimplifiesTo("true && x && true && a && b", "x && a && b");
  }
  @Test public void orFalseANDOf3WithTrueA() {
    assertSimplifiesTo("a && b && true", "a && b");
  }
  @Test public void orFalseANDOf4WithoutBoolean() {
    assertNoChange("a && b && c && false");
  }
  @Test public void orFalseANDOf4WithoutBooleanA() {
    assertNoChange("x && a && b && c");
  }
  @Test public void orFalseANDOf4WithTrue() {
    assertSimplifiesTo("x && true && a && b && c", "x && a && b && c");
  }
  @Test public void orFalseANDOf4WithTrueA() {
    assertSimplifiesTo("a && b && c && true", "a && b && c");
  }
  @Test public void orFalseANDOf5WithoutBoolean() {
    assertNoChange("false && a && b && c && d");
  }
  @Test public void orFalseANDOf5WithoutBooleanA() {
    assertNoChange("x && a && b && c && d");
  }
  @Test public void orFalseANDOf5WithTrue() {
    assertSimplifiesTo("x && a && b && c && true && true && true && d", "x && a && b && c && d");
  }
  @Test public void orFalseANDOf5WithTrueA() {
    assertSimplifiesTo("true && a && b && c && d", "a && b && c && d");
  }
  @Test public void orFalseANDOf6WithoutBoolean() {
    assertNoChange("a && b && c && false && d && e");
  }
  @Test public void orFalseANDOf6WithoutBooleanA() {
    assertNoChange("x && a && b && c && d && e");
  }
  @Test public void orFalseANDOf6WithoutBooleanWithParenthesis() {
    assertNoChange("(x && (a && b)) && (c && (d && e))");
  }
  @Test public void orFalseANDOf6WithTrue() {
    assertSimplifiesTo("x && a && true && b && c && d && e", "x && a && b && c && d && e");
  }
  @Test public void orFalseANDOf6WithTrueA() {
    assertSimplifiesTo("a && b && c && true && d && e", "a && b && c && d && e");
  }
  @Test public void orFalseANDOf6WithTrueWithParenthesis() {
    assertSimplifiesTo("x && (true && (a && b && true)) && (c && (d && e))", "x && a && b && c && d && e");
  }
  @Test public void orFalseANDOf7WithMultipleTrueValue() {
    assertSimplifiesTo("(a && (b && true)) && (c && (d && (e && (true && true))))", "a &&b &&c &&d &&e ");
  }
  @Test public void orFalseANDOf7WithoutBooleanAndMultipleFalseValue() {
    assertNoChange("(a && (b && false)) && (c && (d && (e && (false && false))))");
  }
  @Test public void orFalseANDOf7WithoutBooleanWithParenthesis() {
    assertNoChange("(a && b) && (c && (d && (e && false)))");
  }
  @Test public void orFalseANDOf7WithTrueWithParenthesis() {
    assertSimplifiesTo("true && (a && b) && (c && (d && (e && true)))", "a &&b &&c &&d &&e ");
  }
  @Test public void orFalseANDWithFalse() {
    assertNoChange("b && a");
  }
  @Test public void orFalseANDWithoutBoolean() {
    assertNoChange("b && a");
  }
  @Test public void orFalseANDWithTrue() {
    assertSimplifiesTo("true && b && a", "b && a");
  }
  @Test public void orFalseFalseOrFalse() {
    assertSimplifiesTo("false ||false", "false");
  }
  @Test public void orFalseORFalseWithSomething() {
    assertNoChange("true || a");
  }
  @Test public void orFalseORFalseWithSomethingB() {
    assertSimplifiesTo("false || a || false", "a");
  }
  @Test public void orFalseOROf3WithFalse() {
    assertSimplifiesTo("x || false || b", "x || b");
  }
  @Test public void orFalseOROf3WithFalseB() {
    assertSimplifiesTo("false || a || b || false", "a || b");
  }
  @Test public void orFalseOROf3WithoutBoolean() {
    assertNoChange("a || b");
  }
  @Test public void orFalseOROf3WithoutBooleanA() {
    assertNoChange("x || a || b");
  }
  @Test public void orFalseOROf4WithFalse() {
    assertSimplifiesTo("x || a || b || c || false", "x || a || b || c");
  }
  @Test public void orFalseOROf4WithFalseB() {
    assertSimplifiesTo("a || b || false || c", "a || b || c");
  }
  @Test public void orFalseOROf4WithoutBoolean() {
    assertNoChange("a || b || c");
  }
  @Test public void orFalseOROf4WithoutBooleanA() {
    assertNoChange("x || a || b || c");
  }
  @Test public void orFalseOROf5WithFalse() {
    assertSimplifiesTo("x || a || false || c || d", "x || a || c || d");
  }
  @Test public void orFalseOROf5WithFalseB() {
    assertSimplifiesTo("a || b || c || d || false", "a || b || c || d");
  }
  @Test public void orFalseOROf5WithoutBoolean() {
    assertNoChange("a || b || c || d");
  }
  @Test public void orFalseOROf5WithoutBooleanA() {
    assertNoChange("x || a || b || c || d");
  }
  @Test public void orFalseOROf6WithFalse() {
    assertSimplifiesTo("false || x || a || b || c || d || e", "x || a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithFalseWithParenthesis() {
    assertSimplifiesTo("x || (a || (false) || b) || (c || (d || e))", "x || a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithFalseWithParenthesisB() {
    assertSimplifiesTo("(a || b) || false || (c || false || (d || e || false))", "a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithoutBoolean() {
    assertNoChange("a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithoutBooleanA() {
    assertNoChange("x || a || b || c || d || e");
  }
  @Test public void orFalseOROf6WithoutBooleanWithParenthesis() {
    assertNoChange("(a || b) || (c || (d || e))");
  }
  @Test public void orFalseOROf6WithoutBooleanWithParenthesisA() {
    assertNoChange("x || (a || b) || (c || (d || e))");
  }
  @Test public void orFalseOROf6WithTwoFalse() {
    assertSimplifiesTo("a || false || b || false || c || d || e", "a || b || c || d || e");
  }
  @Test public void orFalseORSomethingWithFalse() {
    assertSimplifiesTo("false || a || false", "a");
  }
  @Test public void orFalseORSomethingWithTrue() {
    assertNoChange("a || true");
  }
  @Test public void orFalseORWithoutBoolean() {
    assertNoChange("b || a");
  }
  @Test public void orFalseProductIsNotANDDivOR() {
    assertNoChange("2*a");
  }
  @Test public void orFalseTrueAndTrueA() {
    assertSimplifiesTo("true && true", "true");
  }
  public void parenthesizeOfpushdownTernary() {
    assertSimplifiesTo("a ? b+x+e+f:b+y+e+f", "b+(a ? x : y)+e+f");
  }
  @Test public void pushdownNot2LevelNotOfFalse() {
    assertSimplifiesTo("!!false", "false");
  }
  @Test public void pushdownNot2LevelNotOfTrue() {
    assertSimplifiesTo("!!true", "true");
  }
  @Test public void pushdownNotActualExample() {
    assertNoChange("!inRange(m, e)");
  }
  @Test public void pushdownNotDoubleNot() {
    assertSimplifiesTo("!!f()", "f()");
  }
  @Test public void pushdownNotDoubleNotDeeplyNested() {
    assertSimplifiesTo("!(((!f())))", "f()");
  }
  @Test public void pushdownNotDoubleNotNested() {
    assertSimplifiesTo("!(!f())", "f()");
  }
  @Test public void pushdownNotEND() {
    assertNoChange("a&&b");
  }
  @Test public void pushdownNotMultiplication() {
    assertNoChange("a*b");
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
  @Test public void pushdownNotNotOfOR() {
    assertSimplifiesTo("!(a || b || c)", "!a && !b && !c");
  }
  @Test public void pushdownNotNotOfOR2() {
    assertSimplifiesTo("!(f() || f(5))", "!f() && !f(5)");
  }
  @Test public void pushdownNotNotOfTrue() {
    assertSimplifiesTo("!true", "false");
  }
  @Test public void pushdownNotNotOfTrue2() {
    assertSimplifiesTo("!!true", "true");
  }
  @Test public void pushdownNotNotOfWrappedOR() {
    assertSimplifiesTo("!((a) || b || c)", "!a && !b && !c");
  }
  @Test public void pushdownNotOR() {
    assertNoChange("a||b");
  }
  @Test public void pushdownNotSimpleNot() {
    assertNoChange("!a");
  }
  @Test public void pushdownNotSimpleNotOfFunction() {
    assertNoChange("!f(a)");
  }
  @Test public void pushdownNotSummation() {
    assertNoChange("a+b");
  }
  @Test public void pushdownTernaryActualExample() {
    assertNoChange("next < values().length");
  }
  @Test public void pushdownTernaryActualExample2() {
    assertSimplifiesTo("!inRange(m, e) ? true : inner.go(r, e)", "!inRange(m, e) || inner.go(r, e)");
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
  @Test public void pushdownTernaryAlmostIdenticalAssignment() {
    assertSimplifiesTo("a ? (b=c) :(b=d)", "b = a ? c : d");
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
  @Test public void pushdownTernaryAMethodCall() {
    assertNoChange("a ? y.f(c,b) :y.f(c)");
  }
  @Test public void pushdownTernaryAMethodCallDistinctReceiver() {
    assertNoChange("a ? x.f(c) : y.f(d)");
  }
  @Test public void pushdownTernaryDifferentTargetFieldRefernce() {
    assertSimplifiesTo("a ? 1 + x.a : 1 + y.a", "1+(a ? x.a : y.a)");
  }
  @Test public void pushdownTernaryExpressionVsExpression() {
    assertSimplifiesTo(" 6 - 7 < 2 + 1   ", "6 -7 < 1 + 2");
  }
  @Test public void pushdownTernaryFieldRefernece() {
    assertNoChange("externalImage ? R.string.webview_contextmenu_image_download_action : R.string.webview_contextmenu_image_save_action");
  }
  @Test public void pushdownTernaryFunctionCall() {
    assertNoChange("a ? f(b,c) : f(c)");
  }
  @Test public void pushdownTernaryFX() {
    assertSimplifiesTo("a ? false : c", "!a && c");
  }
  @Test public void pushdownTernaryIdenticalAddition() {
    assertSimplifiesTo("a ? b+d :b+ d", "b+d");
  }
  @Test public void pushdownTernaryIdenticalAssignment() {
    assertSimplifiesTo("a ? (b=c) :(b=c)", "b = c");
  }
  @Test public void pushdownTernaryIdenticalFunctionCall() {
    assertSimplifiesTo("a ? f(b) :f(b)", "f(b)");
  }
  @Test public void pushdownTernaryIdenticalIncrement() {
    assertSimplifiesTo("a ? b++ :b++", "b++");
  }
  @Test public void pushdownTernaryIdenticalMethodCall() {
    assertSimplifiesTo("a ? y.f(b) :y.f(b)", "y.f(b)");
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
  @Test public void pushdownTernaryIntoConstructorNotSameArity() {
    assertNoChange("a ? new S(a,new Integer(4),b) : new S(new Ineger(3))");
  }
  @Test public void pushdownTernaryLiteralVsLiteral() {
    assertNoChange("1 < 102333");
  }
  @Test public void pushdownTernaryMethodInvocationFirst() {
    assertNoChange("a?b():c");
  }
  @Test public void pushdownTernaryNoBoolean() {
    assertNoChange("a?b:c");
  }
  @Test public void pushdownTernaryNotOnMINUS() {
    assertNoChange("a ? -c :-d");
  }
  @Test public void pushdownTernaryNotOnMINUSMINUS1() {
    assertNoChange("a ? --c :--d");
  }
  @Test public void pushdownTernaryNotOnMINUSMINUS2() {
    assertNoChange("a ? c-- :d--");
  }
  @Test public void pushdownTernaryNotOnNOT() {
    assertNoChange("a ? !c :!d");
  }
  @Test public void pushdownTernaryNotOnPLUS() {
    assertNoChange("a ? +x : +y");
  }
  @Test public void pushdownTernaryNotOnPLUSPLUS() {
    assertNoChange("a ? x++ :y++");
  }
  @Test public void pushdownTernaryNotSameFunctionInvocation() {
    assertNoChange("a?b(x):d(x)");
  }
  @Test public void pushdownTernaryNotSameFunctionInvocation2() {
    assertNoChange("a?x.f(x):x.d(x)");
  }
  @Test public void pushdownTernaryParFX() {
    assertSimplifiesTo("a ?( false):true", "!a && true");
  }
  @Test public void pushdownTernaryParTX() {
    assertSimplifiesTo("a ? (((true ))): c", "a || c");
  }
  @Test public void pushdownTernaryParXF() {
    assertSimplifiesTo("a ? b : (false)", "a && b");
  }
  @Test public void pushdownTernaryParXT() {
    assertSimplifiesTo("a ? b : ((true))", "!a || b");
  }
  @Test public void pushdownTernaryTX() {
    assertSimplifiesTo("a ? true : c", "a || c");
  }
  @Test public void pushdownTernaryXF() {
    assertSimplifiesTo("a ? b : false", "a && b");
  }
  @Test public void pushdownTernaryXT() {
    assertSimplifiesTo("a ? b : true", "!a || b");
  }
  @Test public void reanmeReturnVariableToDollar01() {
    assertSimplifiesTo(
        "  public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   System.out.println(res.j);   return res; ",
        "  public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   System.out.println($.j);   return $; ");
  }
  @Test public void reanmeReturnVariableToDollar02() {
    assertSimplifiesTo(
        "  int res = blah.length();   if (blah.contains(\"blah\"))    return res * 2;   if (res % 2 ==0)    return ++res;   if (blah.startsWith(\"y\")) {    return yada3(res);   int x = res + 6;   if (x>1)    return res + x;   res -= 1;   return res; ",
        "  int $ = blah.length();   if (blah.contains(\"blah\"))    return $ * 2;   if ($ % 2 ==0)    return ++$;   if (blah.startsWith(\"y\")) {    return yada3($);   int x = $ + 6;   if (x>1)    return $ + x;   $ -= 1;   return $; ");
  }
  @Test public void reanmeReturnVariableToDollar03() {
    assertSimplifiesTo(
        "  public BlahClass(int i) {    j = 2*i;      public final int j;   public int yada7(final String blah) {   final BlahClass res = new BlahClass(blah.length());   if (blah.contains(\"blah\"))    return res.j;   int x = blah.length()/2;   if (x==3)    return x;   x = yada3(res.j - x);   return x; ",
        "  public BlahClass(int i) {    j = 2*i;      public final int j;   public int yada7(final String blah) {   final BlahClass res = new BlahClass(blah.length());   if (blah.contains(\"blah\"))    return res.j;   int $ = blah.length()/2;   if ($==3)    return $;   $ = yada3(res.j - $);   return $; ");
  }
  @Test public void reanmeReturnVariableToDollar04() {
    assertNoChange("  int res = 0;   String $ = blah + \" yada\";   yada3(res + $.length());   return res + $.length();");
  }
  @Test public void reanmeReturnVariableToDollar05() {
    assertSimplifiesTo(
        "   j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass(res.j);     System.out.println(res2.j);     doStuff(res2);        private void doStuff(final BlahClass res) {     System.out.println(res.j);   System.out.println(res.j);   return res; ",
        "   j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass($.j);     System.out.println(res2.j);     doStuff(res2);        private void doStuff(final BlahClass res) {     System.out.println(res.j);   System.out.println($.j);   return $; ");
  }
  @Test public void reanmeReturnVariableToDollar06() {
    assertSimplifiesTo(
        "   j = 2*i;   }      public final int j;    public void yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass(res.j);     System.out.println(res2.j);     doStuff(res2);        private int doStuff(final BlahClass r) {     final BlahClass res = new BlahClass(r.j);     return res.j + 1;   System.out.println(res.j); ",
        "   j = 2*i;   }      public final int j;    public void yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass(res.j);     System.out.println(res2.j);     doStuff(res2);        private int doStuff(final BlahClass r) {     final BlahClass $ = new BlahClass(r.j);     return $.j + 1;   System.out.println(res.j); ");
  }
  @Test public void reanmeReturnVariableToDollar07() {
    assertSimplifiesTo(
        "   j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   System.out.println(res.j);   return res; ",
        "   j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   System.out.println($.j);   return $; ");
  }
  @Test public void reanmeReturnVariableToDollar08() {
    assertSimplifiesTo(
        "  public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   if (res.j == 0)    return null;   System.out.println(res.j);   return res; ",
        "  public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   if ($.j == 0)    return null;   System.out.println($.j);   return $; ");
  }
  @Test public void reanmeReturnVariableToDollar09() {
    assertNoChange(
        "  public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   if (res.j == 0)    return null;   System.out.println(res.j);   return null;");
  }
  @Test public void reanmeReturnVariableToDollar10() {
    assertSimplifiesTo(
        " @Override public IMarkerResolution[] getResolutions(final IMarker m) {   try {    final Spartanization s = All.get((String) m.getAttribute(Builder.SPARTANIZATION_TYPE_KEY)); ",
        " @Override public IMarkerResolution[] getResolutions(final IMarker m) {   try {    final Spartanization $ = All.get((String) m.getAttribute(Builder.SPARTANIZATION_TYPE_KEY)); ");
  }
  @Test public void reanmeReturnVariableToDollar11() {
    assertNoChange("");
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
  @Test public void shortestBranchFirst01() {
    assertConvertsTo(
        "  if (s.equals(\"blah\")) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (!(s.equals(\"blah\"))) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst02() {
    assertConvertsTo(
        "  if (!s.equals(\"blah\")) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.equals(\"blah\")) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst03() {
    assertConvertsTo(
        "  if (s.length()>6) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.length() <= 6) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst04() {
    assertConvertsTo(
        "  if (s.length()>=6) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.length() < 6) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst05() {
    assertConvertsTo(
        "  if (s.length()<6) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.length() >= 6) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst06() {
    assertConvertsTo(
        "  if (s.length()<=6) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.length() > 6) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst07() {
    assertConvertsTo(
        "  if (s.length()==6) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.length() != 6) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst08() {
    assertConvertsTo(
        "  if (s.length()!=6) {    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res;   else {    return 8; ",
        "  if (s.length() == 6) {    return 8;    int res=0;    for (int i=0; i<s.length(); ++i)     if (s.charAt(i)=='a')      res += 2;     else if (s.charAt(i)=='d')      res -= 1;    return res; ");
  }
  @Test public void shortestBranchFirst09() {
    assertSimplifiesTo("   s.equals(\"yada\") ? 9 * yada3(s.length()) : 6;  } ", "   (!(s.equals(\"yada\")) ? 6 : 9 * yada3(s.length())) ");
  }
  @Test public void shortestBranchFirst10() {
    assertSimplifiesTo(
        "      for (final String s : contents.split(\"\\n\"))\n" + "        if (!foundPackage && s.contains(Strings.JAVA_PACKAGE)) {\n" + //
            "          $.append(s.replace(\";\", \".\" + folderName + \";\") + \"\\n\" + imports);\n" + //
            "          foundPackage = true;\n" + //
            "        } else\n" + //
            "          $.append(replaceClassName(s, className, newClassName) + \"\\n\");\n" + //
            "      return asString($);",
        "      for (final String s : contents.split(\"\\n\"))\n" + //
            "        if (foundPackage || !s.contains(Strings.JAVA_PACKAGE))\n" + //
            "          $.append(replaceClassName(s, className, newClassName) " + //
            " \"\\n\");\n" + //
            "        else {\n" + "          $.append(s.replace(\";\", \".\" + folderName + \";\") + \"\\n\" + imports);\n" + //
            "          foundPackage = true;\n" + //
            "        }\n" + //
            "      return asString($);");
  }
  @Test public void shortestBranchFirst11() {
    assertConvertsTo("   b != null && b.getNodeType() == ASTNode.BLOCK ? getBlockSingleStmnt((Block) b) : b ",
        "   (!(b != null) || !(b.getNodeType() == ASTNode.BLOCK) ? b : getBlockSingleStmnt((Block) b)) ");
  }
  @Test public void shortestBranchFirst12() {
    assertSimplifiesTo("  if (FF() && TT()){    foo1();    foo2();     shorterFoo(); ", //
        "  if (!FF() || !TT()) {     shorterFoo();    foo1();    foo2(); ");
  }
  @Test public void shortestBranchFirst13() {
    assertNoChange("  int a=0;   if (a > 0)    return 6;   else {    int b=9;    b*=b;    return b;");
  }
  @Test public void shortestBranchFirst14() {
    assertSimplifiesTo("  int a=0;   if (a > 0){    int b=9;    b*=b;    return 6;    a = 5;    return b; ",
        "  int a=0;   if (a <= 0){    a = 5;    return b;    int b=9;    b*=b;    return 6;");
  }
  @Test public void shortestOperand01() {
    assertSimplifiesTo(" x + y > z; ", "  return z < x + y");
  }
  @Test public void shortestOperand02() {
    assertSimplifiesTo("  k = k + 4;   if (2 * 6 + 4 == k) return true; ", "  k = k + 4;   if (k == 2 * 6 + 4) return true; ");
  }
  @Test public void shortestOperand03() {
    assertSimplifiesTo(//
        "  k = k * 4;   if ( 1 + 2 - 3 - 4 + 5 / 6 - 7 + 8 * 9 > k + 4) return true; ", //
        "  k = k * 4;   if ( k + 4 < 1 + 2 - 3 - 4 + 5 / 6 - 7 + 8 * 9) return true; ");
  }
  @Test public void shortestOperand04() {
    assertSimplifiesTo("  return (1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1);  } ", "  return (3 > 1 + 2 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1);  } ");
  }
  @Test public void shortestOperand05() {
    assertSimplifiesTo(//
        "  StringBuilder s = \"bob\";   return s.append(\"-ha\").append(\"-ba\").toString() == \"bob-ha-banai\"; ",
        "  StringBuilder s = \"bob\";   return \"bob-ha-banai\" == s.append(\"-ha\").append(\"-ba\").toString(); ");
  }
  @Test public void shortestOperand06() {
    assertSimplifiesTo("  int a,b,c;   String t = \"eureka!\";   if (2 * 3.1415 * 180 > a || t.concat(\"<!>\") == \"1984\" && t.length() > 3)    return c > 5; ",
        "  int a,b,c;   String t = \"eureka!\";   if (a < 2 * 3.1415 * 180 || \"1984\" == t.concat(\"<!>\") && 3 < t.length())    return c > 5; ");
  }
  @Test public void shortestOperand07() {
    assertSimplifiesTo("  int y,o,g,i,s;   return ( y + o + s > s + i |  g > 42); ", "  int y,o,g,i,s;   return ( g > 42 | y + o + s > s + i); ");
  }
  @Test public void shortestOperand08() {
    assertSimplifiesTo(
        "  human father;   human mother;   int age;  public boolean squarePants() {   human bob;   if (bob.father.age > 42 && bob.mother.father.age > bob.age ) return true;  ",
        "  human father;   human mother;   int age;  public boolean squarePants() {   human bob;   if (bob.father.age ? 42 && bob.age < bob.mother.father.age ) return true;  ");
  }
  @Test public void shortestOperand09() {
    assertNoChange("  return 2 - 4 < 50 - 20 - 10 - 5;  } ");
  }
  @Test public void shortestOperand10() {
    assertNoChange("  return b == true;  } ");
  }
  @Test public void shortestOperand11() {
    assertNoChange(" int h,u,m,a,n;   return b == true && n + a > m - u || h > u; ");
  }
  @Test public void shortestOperand12() {
    assertNoChange("  int k = 15;   return 7 < k; ");
  }
  @Test public void shortestOperand13() {
    assertNoChange("  return (2 > 2 + a) == true;     } ");
  }
  @Test public void shortestOperand14() {
    assertNoChange(" Integer t = new Integer(5);   return (t.toString() == null);    ");
  }
  @Test public void shortestOperand15() {
    assertNoChange(" String t = \"Bob \" + \"Wants \" + \"To \" + \"Sleep \";   return (\"right now...\" + t);    ");
  }
  @Test public void shortestOperand16() {
    assertNoChange("  String t = \"\";   t = t.concat(\"a\").concat(\"b\") + t.concat(\"c\");   return (t + \"...\");    ");
  }
  @Test public void shortestOperand17() {
    assertSimplifiesTo("  SomeClass a;   return a.getNum() ^ 5; ", "  SomeClass a;   return 5 ^ a.getNum();");
  }
  @Test public void shortestOperand18() {
    assertSimplifiesTo("  SomeClass a;   return k.get().parent() & a; ", "  SomeClass a;   return a & k.get().parent();");
  }
  @Test public void shortestOperand19() {
    assertNoChange("  SomeClass a;   return k.get().operand() ^ a.get(); ");
  }
  @Test public void shortestOperand20() {
    assertNoChange("  SomeClass a;   String k = k.Concat(\"mmm...\") + a.get().sum().toString();   return k.get() ^ a.get(); ");
  }
  @Test public void shortestOperand21() {
    assertSimplifiesTo("        return f(a, b, c, d, e) + 3333 + 222 + a + spongeBob + f(g, c, d) + f(a) + tt; ",
        "     return a+tt+222+3333+spongeBob+f(a,b,c,d,e)+f(g,c,d)+f(a); ");
  }
  @Test public void shortestOperand22() {
    assertNoChange("    return f(a,b,c,d,e) + f(a,b,c,d) + f(a,b,c) + f f(a,b) + f(a) + f();     } ");
  }
  @Test public void shortestOperand23() {
    assertNoChange("    return f() + \".\";     }");
  }
  @Test public void shortestOperand24() {
    assertSimplifiesTo("    return f(a,b,c,d) & 175 & 0;   } ", "    return 0 & 175 & f(a,b,c,d);   }");
  }
  @Test public void shortestOperand25() {
    assertSimplifiesTo("    return f(a,b,c,d) & bob & 0;   } ", "    return 0 & bob & f(a,b,c,d);   }");
  }
  @Test public void shortestOperand26() {
    assertSimplifiesTo("    return f(a,b,c,d) | f() | 0;     } ", "    return 0 | f(a,b,c,d) | f();     }");
  }
  @Test public void shortestOperand27() {
    assertNoChange("    return f(a,b,c,d) + f(a,b,c) + f();     } ");
  }
  @Test public void shortestOperand28() {
    assertNoChange("    return f(a,b,c,d) * f(a,b,c) * f();     } ");
  }
  @Test public void shortestOperand29() {
    assertSimplifiesTo("    return f(a,b,c,d) ^ f() ^ 0;     } ", "    return 0 ^ f(a,b,c,d) ^ f();     }");
  }
  @Test public void shortestOperand30() {
    assertNoChange("    return f(a,b,c,d) & f();     } ");
  }
  @Test public void shortestOperand31() {
    assertNoChange("    return f(a,b,c,d) | \".\";     }");
  }
  @Test public void shortestOperand32() {
    assertNoChange("    return f(a,b,c,d) && f();     }");
  }
  @Test public void shortestOperand33() {
    assertNoChange("    return f(a,b,c,d) || f();     }");
  }
  @Test public void shortestOperand34() {
    assertSimplifiesTo("    return f(a,b,c,d) + someVar;     } ", "    return someVar + f(a,b,c,d);    } ");
  }
  @Test public void shortestOperand35() {
    assertSimplifiesTo("    return f(a,b,c,d) * moshe;     } ", "    return moshe * f(a,b,c,d);     } ");
  }
  @Test public void shortestOperand36() {
    assertSimplifiesTo("f(a,b,c,d) ^ bob", "bob ^ f(a,b,c,d)");
  }
  @Test public void shortestOperand37() {
    assertNoChange("");
  }
  @Test public void shortestOperandFarStringLiteral() {
    assertNoChange("");
  }
  @Test public void simplifiesTo() {
    assertSimplifiesTo("plain * the + kludge", "the*plain+kludge");
  }
  @Test public void simplifyBlockComplexEmpty() {
    assertSimplifiesTo("{;;{;{{}}}{;}{};}", "", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockComplexEmpty0() {
    assertSimplifiesTo("{}", "", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockComplexSingleton() {
    assertSimplifiesTo("{;{{;;return b; }}}", " return b;", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockDeeplyNestedReturn() {
    assertSimplifiesTo(" {{{;return c;};;};}", " return c;", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  /* Begin of already good tests */
  @Test public void simplifyBlockEmpty() {
    assertSimplifiesTo("{;;}", "", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyBlockExpressionVsExpression() {
    assertSimplifiesTo(" 6 - 7 < a * 3", "6 - 7 < 3 * a");
    // Note that we also need to generalize NoChange to work with other
    // Wrings\Wrap types
  }
  @Test public void simplifyBlockLiteralVsLiteral() {
    assertNoChange("if (a) return b; else c;");
  }
  @Test public void simplifyBlockThreeStatements() {
    assertSimplifiesTo("{i++;{{;;return b; }}j++;}", " i++;return b;j++;", Wrings.SIMPLIFY_BLOCK.inner, Wrap.Statement);
  }
  @Test public void simplifyLogicalNegationOfAnd() {
    assertSimplifiesTo("!(f() && f(5))", "!f() || !f(5)");
  }
  @Test public void simplifyLogicalNegationOfEquality() {
    assertSimplifiesTo("!(3 == 5)", "3!=5");
  }
  @Test public void simplifyLogicalNegationOfGreater() {
    assertSimplifiesTo("!(3 > 5)", "3 <= 5");
  }
  @Test public void simplifyLogicalNegationOfGreaterEquals() {
    assertSimplifiesTo("!(3 >= 5)", "3 < 5");
  }
  @Test public void simplifyLogicalNegationOfInequality() {
    assertSimplifiesTo("!(3 != 5)", "3 == 5");
  }
  @Test public void simplifyLogicalNegationOfLess() {
    assertSimplifiesTo("!(3 < 5)", "3 >= 5");
  }
  @Test public void simplifyLogicalNegationOfLessEquals() {
    assertSimplifiesTo("!(3 <= 5)", "3 > 5");
  }
  @Test public void simplifyLogicalNegationOfMultipleAnd() {
    assertSimplifiesTo("!(a && b && c)", "!a || !b || !c");
  }
  @Test public void simplifyLogicalNegationOfMultipleOr() {
    assertSimplifiesTo("!(a || b || c)", "!a && !b && !c");
  }
  @Test public void simplifyLogicalNegationOfNot() {
    assertSimplifiesTo("!!f()", "f()");
  }
  @Test public void simplifyLogicalNegationOfOr() {
    assertSimplifiesTo("!(f() || f(5))", "!f() && !f(5)");
  }
  @Test public void simplifyLogicalNegationONested() {
    assertSimplifiesTo("!((a || b == c) && (d || !(!!c)))", "!a && b != c || !d && c");
  }
  @Test public void ternarize01() {
    assertSimplifiesTo("  String res = s;   if (s.equals(\"yada\")==true)    res = s + \" blah\";   else    res = \"spam\";   System.out.println(res); ",
        "  String res = (s.equals(\"yada\")==true ? s + \" blah\" : \"spam\");   System.out.println(res); ");
  }
  @Test public void ternarize02() {
    assertSimplifiesTo("  String res = s;   if (s.equals(\"yada\")==true)    res = s + \" blah\";   System.out.println(res); ",
        "  String res = (s.equals(\"yada\")==true ? s + \" blah\" : s);   System.out.println(res); ");
  }
  @Test public void ternarize03() {
    assertSimplifiesTo("  if (s.equals(\"yada\"))    return 6;   return 9; ", "  return (s.equals(\"yada\") ? 6 : 9);  } ");
  }
  @Test public void ternarize04() {
    assertSimplifiesTo(
        "  int res = 0;   if (s.equals(\"yada\"))    res += 6;   else    res += 9;      /*   if (s.equals(\"yada\"))    res += 6;   else    res += 9;    */   return res; ",
        "  int res = 0;   res += (s.equals(\"yada\") ? 6 : 9);      /*   if (s.equals(\"yada\"))    res += 6;   else    res += 9;    */   return res; ");
  }
  @Test public void ternarize05() {
    assertNoChange(
        "  int res = 0;   if (s.equals(\"yada\"))    res += 6;   else    res -= 9;      /*   if (s.equals(\"yada\"))    res += 6;   else    res += 9;    */   return res; ");
  }
  @Test public void ternarize06() {
    assertSimplifiesTo("  String res;   res = s;   if (s.equals(\"yada\")==true)    res = s + \" blah\";   System.out.println(res); ",
        "  String res = (s.equals(\"yada\")==true ? s + \" blah\" : s);   System.out.println(res); ");
  }
  @Test public void ternarize07() {
    assertNoChange("  String res;   res = s;   if (res.equals(\"yada\")==true)    res = s + \" blah\";   System.out.println(res); ");
  }
  @Test public void ternarize08() {
    assertSimplifiesTo(
        "  int res = 0;   if (s.equals(\"yada\")) {    res += 6;   else {    res += 9;      /*   if (s.equals(\"yada\"))    res += 6;   else    res += 9;    */   return res; ",
        "  int res = 0;   res += (s.equals(\"yada\") ? 6 : 9);      /*   if (s.equals(\"yada\"))    res += 6;   else    res += 9;    */   return res; ");
  }
  @Test public void ternarize09() {
    assertSimplifiesTo("  if (s.equals(\"yada\")) {    return 6;   else {    return 9; ", "  return (s.equals(\"yada\") ? 6 : 9);  } ");
  }
  @Test public void ternarize10() {
    assertNoChange("  String res = s, foo = \"bar\";   if (res.equals(\"yada\")==true)    res = s + \" blah\";   System.out.println(res); ");
  }
  @Test public void ternarize11() {
    assertSimplifiesTo("  String res = s, foo = \"bar\";   if (s.equals(\"yada\")==true)    res = s + \" blah\";   System.out.println(res); ",
        "  String res = (s.equals(\"yada\")==true ? s + \" blah\" : s), foo = \"bar\";   System.out.println(res); ");
  }
  @Test public void ternarize12() {
    assertNoChange("  String res = s;   if (s.equals(\"yada\")==true)    res = res + \" blah\";   System.out.println(res); ");
  }
  @Test public void ternarize13() {
    assertNoChange(" String res = mode, foo;  if (mode.equals(\"TEST-MODE\")==true)   foo = \"test-bob\"; ");
  }
  @Test public void ternarize14() {
    assertNoChange(
        "  String res = mode, foo = \"Not in test mode\";   if (res.equals(\"TEST-MODE\")==true){    foo = \"test-bob\";    int k = 2;    k = 8;   System.out.println(foo); ");
  }
  @Test public void ternarize15() {
    assertSimplifiesTo(
        "  String res = mode, foo = \"Not in test mode\"; int k;   k = 1984;   if (mode.equals(\"TEST-MODE\")==true)    foo = \"test-bob\";   foo = \"sponge-bob\"; ",
        "  String res = mode, foo = \"Not in test mode\"; int k;   k = 1984;   foo = \"sponge-bob\"; ");
  }
  @Test public void ternarize16() {
    assertNoChange(" String res = mode;  int num1, num2, num3;  if (mode.equals(\"TEST-MODE\"))   num2 = 2; ");
  }
  @Test public void ternarize17() {
    assertSimplifiesTo(
        "  return 6;  }  public int y(){   return 4;  public void yada(final String s) {      int a, b;      a = 3;      b = 5;      if (a == 4)        if (b == 3)          b = r();        else          b = a;     else       if (b == 3)         b = y();       else         b = a; ",
        "  return 6;  }  public int y(){   return 4;  public void yada(final String s) {      int a, b;      a = 3;      b = 5;      if (b == 3)          b = (a == 4 ? r() : y());        else          b = a; ");
  }
  @Test public void ternarize18() {
    assertSimplifiesTo("  String res = s;   int a=0;   if (s.equals(\"yada\"))    System.out.println(\"hey\" + res);   else    System.out.println(\"ho\" + res + a); ",
        "  String res = s;   int a=0;   System.out.println((s.equals(\"yada\") ? \"hey\" + res : \"ho\" + res + a)); ");
  }
  @Test public void ternarize19() {
    assertSimplifiesTo("  if (s.equals(\"yada\"))    System.out.close();   else    System.out.close(); ", "  System.out.close();  } ");
  }
  @Test public void ternarize20() {
    assertSimplifiesTo("  return 0;  }  public int y(int b){   return 1;  public int yada(final String s) {   if (s.equals(\"yada\")){    return 2 + r(2);    return 3 + f(4); ",
        "  return 0;  }  public int y(int b){   return 1;  public int yada(final String s) {   return (s.equals(\"yada\") ? 2 + r(2) : 3 + f(4)); ");
  }
  @Test public void ternarize21() {
    assertNoChange("  if (s.equals(\"yada\")){    System.out.println(\"g\");    System.out.append(\"k\"); ");
  }
  @Test public void ternarize22() {
    assertNoChange("  int a=0;   if (s.equals(\"yada\")){    System.console();    a=3; ");
  }
  @Test public void ternarize23() {
    assertSimplifiesTo("  return 0;  }  public int y(int b){   return 1;  public void yada(final String s) {   int a=0;   if (s.equals(\"yada\")){    a+=y(2)+10;    a+=r(3)-6; ",
        "  return 0;  }  public int y(int b){   return 1;  public void yada(final String s) {   int a=0;   a+=(s.equals(\"yada\") ? y(2)+10 : r(3)-6); ");
  }
  @Test public void ternarize24() {
    assertSimplifiesTo("  boolean c;   if (s.equals(\"yada\")){    c=false;    c=true; ", "  boolean c = !(s.equals(\"yada\"));  } ");
  }
  @Test public void ternarize25() {
    assertNoChange("  return 2;  }  public void yada(final String s) {   int a, b=0, c=0;   if (b==3){    a+=2+r();    c=6;   a+=6; ");
  }
  @Test public void ternarize26() {
    assertNoChange("  int a=0;   if (s.equals(\"yada\")){    a+=2;   a-=2; ");
  }
  @Test public void ternarize27() {
    assertSimplifiesTo("  int a=0;   int b=0;   a=5;   if (s.equals(\"yada\")){    a=4;   a=3; ", "  int a=0;   int b=0;   a=3; ");
  }
  @Test public void ternarize28() {
    assertSimplifiesTo("  int a=0;   a=5;   if (s.equals(\"yada\")){    a=4;   a=3; ", "  int a=3;  } ");
  }
  @Test public void ternarize29() {
    assertNoChange("  int a=0;   int b=0;   a=5;   if (a==3){    a=4; ");
  }
  @Test public void ternarize30() {
    assertSimplifiesTo("  int a=0, b=0;   a=5;   if (b==3){    a=a+4; ", "  int a=0, b=0;   a = (b==3 ? a+4 : 5); ");
  }
  @Test public void ternarize31() {
    assertSimplifiesTo("  int a=0;   a=5;   if (a==3){    a=a+4;   a=7; ", "  int a=7;  } ");
  }
  @Test public void ternarize32() {
    assertSimplifiesTo("  int a=0, b=0;   if (b==3){    a=4; ", "  int a = (b==3 ? 4 : 0), b=0;  } ");
  }
  @Test public void ternarize33() {
    assertNoChange("  int a, b=0;   if (b==3){    a=4; ");
  }
  @Test public void ternarize34() {
    assertSimplifiesTo("  int b=0;   if (b==3){    return true;   return false; ", "  int b=0;   return b==3; ");
  }
  @Test public void ternarize35() {
    assertNoChange("  int a, b=0, c=0;   a=4;   if (c==3){    b=2; ");
  }
  @Test public void ternarize36() {
    assertNoChange("  int a, b=0, c=0;   a=4;   if (c==3){    b=2;   a=6; ");
  }
  @Test public void ternarize37() {
    assertSimplifiesTo("  return 2;  }  public void yada(final String s) {   int a, b=0, c=0;   if (b==3){    a+=2+r()+c;   a+=6; ",
        "  return 2;  }  public void yada(final String s) {   int a, b=0, c=0;   a += (b==3 ? 2+r()+c + 6 : 6); ");
  }
  @Test public void ternarize38() {
    assertNoChange("  return 2;  }  public void yada(final String s) {   int a, b=0;   if (b==3){    a+=2+r();   a-=6; ");
  }
  @Test public void ternarize39() {
    assertSimplifiesTo(
        "     int a, b;      a = 3;      b = 5;      if (a == 4)        if (b == 3)          b = 2;        else          b = a;     else       if (b == 3)         b = 2;       else         b = a*a; ",
        "     int a, b;      a = 3;      b = 5;      if (b == 3)          b = 2;        else          b = (a == 4 ? a : a*a); ");
  }
  @Test public void ternarize40() {
    assertSimplifiesTo("  int a, b, c;   a = 3;   b = 5;   if (a == 4)     while (b == 3)     c = a;   else    while (b == 3)     c = a*a; ",
        "  int a, b, c;   a = 3;   b = 5;   while (b == 3)    c = (a == 4 ? a : a*a); ");
  }
  @Test public void ternarize41() {
    assertNoChange("  int a, b, c, d;   a = 3;   b = 5;   d = 7;   if (a == 4)     while (b == 3)     c = a;   else    while (d == 3)     c = a*a; ");
  }
  @Test public void ternarize42() {
    assertNoChange(
        "  int a, b;      a = 3;      b = 5;      if (a == 4)        if (b == 3)          b = 2;        else{          b = a;          b=3;     else       if (b == 3)         b = 2;       else{         b = a*a;         b=3; ");
  }
  @Test public void ternarize43() {
    assertSimplifiesTo("  if (mode.equals(\"TEST-MODE\")==true)    if (b==3){     return 3;     b=5;   else    if (b==3){     return 2;     b=4; ",
        "  if (b==3){    return (mode.equals(\"TEST-MODE\")==true ? 3 : 2);    b=(mode.equals(\"TEST-MODE\")==true ? 5 : 4); ");
  }
  @Test public void ternarize44() {
    assertSimplifiesTo("  if (mode.equals(\"TEST-MODE\")==true)    if (b==3){     return 3;     return 7;   else    if (b==3){     return 2;     return 7; ",
        "  if (b==3){    return (mode.equals(\"TEST-MODE\")==true ? 3 : 2);    return 7; ");
  }
  @Test public void ternarize45() {
    assertNoChange("  if (mode.equals(\"TEST-MODE\")==true)    if (b==3){     return 3;     return 7;   else    if (b==3){     return 2;     a=7; ");
  }
  @Test public void ternarize46() {
    assertNoChange("  int a , b=0;   if (mode.equals(\"TEST-MODE\")==true)    if (b==3){     return 3;     a+=7;   else    if (b==3){     return 2;     a=7; ");
  }
  @Test public void ternarize47() {
    assertSimplifiesTo("  int size = 0, a;   if (mode.equals(\"TEST-MODE\")==true)    for (int i=0; i < size; i++){     a+=7;   else    for (int i=0; i < size; i++){     a+=8; ",
        "  int size = 0, a;   for (int i=0; i < size; i++){    a+=(mode.equals(\"TEST-MODE\")==true ? 7 : 8); ");
  }
  @Test public void ternarize48() {
    assertNoChange(
        "  int size = 0, a, b;   if (mode.equals(\"TEST-MODE\")==true)    for (int i=0; i < size; i++){     a+=7;     b=2;   else    for (int i=0; i < size; i++){     a+=8; ");
  }
  @Test public void ternarize49() {
    assertNoChange(
        "  int size = 0;   if (mode.equals(\"TEST-MODE\")==true)    for (int i=0; i < size; i++){     System.out.println(\"Hey\");   else    for (int i=0; i < size; i++){     System.out.append('f'); ");
  }
  @Test public void ternarize50() {
    assertSimplifiesTo(
        "  int size = 0;   if (mode.equals(\"TEST-MODE\")==true)    for (int i=0; i < size; i++){     System.out.println(\"Hey\");   else    for (int i=0; i < size; i++){     System.out.println(\"Ho\"); ",
        "  int size = 0;   for (int i=0; i < size; i++){    System.out.println((mode.equals(\"TEST-MODE\")==true ? \"Hey\" : \"Ho\")); ");
  }
  @Test public void ternarize51() {
    assertSimplifiesTo("  int a=0,b = 0,d = 0,e = 0,c;   if (a < b) {    c = d;     c = e; ", "  int a=0,b = 0,d = 0,e = 0,c = (a < b ? d : e);  } ");
  }
  @Test public void ternarize52() {
    assertSimplifiesTo("  int a=0,b = 0,c,d = 0,e = 0;   if (a < b) {    c = d;     c = e; ", "  int a=0,b = 0,c,d = 0,e = 0;   c = (a < b ? d : e); ");
  }
  @Test public void ternarize53() {
    assertNoChange("  int $, xi=0, xj=0, yi=0, yj=0;   if (xi > xj == yi > yj)    $++;   else    $--;");
  }
  @Test public void ternarize54() {
    assertSimplifiesTo("", "");
  }
  @Test public void ternarize55() {
    assertNoChange("");
  }
  @Test public void ternarize56() {
    assertNoChange(
        "     if (target == 0) {        progressBarCurrent.setString(\"0/0\");        progressBarCurrent.setValue(0);        progressBarCurrent.setString(current + \"/\" + target);        progressBarCurrent.setValue(current * 100 / target);");
  }
  /* End of converted test files */
  @Test public void testPeel() {
    assertEquals(example, Wrap.Expression.off(Wrap.Expression.on(example)));
  }
  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }
  @Test public void vanillaShortestFirstConditionalNoChange() {
    assertNoChange("literal ? CONDITIONAL_OR : CONDITIONAL_AND");
  }
}
