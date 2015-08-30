package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.utils.ExpressionComparator.NODES_THRESHOLD;
import static org.spartan.refactoring.utils.ExpressionComparator.nodesCount;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Into.i;
import static org.spartan.refactoring.utils.Into.s;
import static org.spartan.utils.Utils.hasNull;
import static org.spartan.utils.Utils.in;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.Document;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.spartanizations.TESTUtils;
import org.spartan.refactoring.spartanizations.Wrap;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.ExpressionComparator;
import org.spartan.refactoring.utils.Extract;
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
@SuppressWarnings({ "static-method", "javadoc" }) public class TrimmerTest {
  public static int countOpportunities(final Spartanization s, final CompilationUnit u) {
    return s.findOpportunities(u).size();
  }
  static String apply(final Trimmer t, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(t, u, d).get();
  }
  private static String apply(final Wring<? extends ASTNode> w, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(new AsSpartanization(w, "Tested Refactoring"), u, d).get();
  }
  private static void assertConvertsTo(final String from, final String expected) {
    assertWrappedTranslation(from, expected, Wrap.Statement);
  }
  private static void assertNoChange(final String input) {
    assertSimilar(input, Wrap.Expression.off(apply(new Trimmer(), Wrap.Expression.on(input))));
  }
  private static void assertNoConversion(final String input) {
    assertSimilar(input, Wrap.Statement.off(apply(new Trimmer(), Wrap.Statement.on(input))));
  }
  private static void assertSimplifiesTo(final String from, final String expected) {
    assertWrappedTranslation(from, expected, Wrap.Expression);
  }
  private static void assertSimplifiesTo(final String from, final String expected, final Wring<? extends ASTNode> wring, final Wrap wrapper) {
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
  private static void assertWrappedTranslation(final String from, final String expected, final Wrap w) {
    final String wrap = w.on(from);
    assertEquals(from, w.off(wrap));
    final String unpeeled = apply(new Trimmer(), wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = w.off(unpeeled);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + "is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }
  @Test public void actualExampleForSortAddition() {
    assertNoChange("1 + b.statements().indexOf(declarationStmt)");
  }
  @Test public void actualExampleForSortAdditionInContext() {
    final String from = "2 + a < b";
    final String expected = "a + 2 < b";
    final String from1 = from;
    final String expected1 = expected;
    final Wrap w = Wrap.Expression;
    final String wrap = w.on(from1);
    assertEquals(from1, w.off(wrap));
    final Trimmer t = new Trimmer();
    final String unpeeled = TrimmerTest.apply(t, wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from1);
    final String peeled = w.off(unpeeled);
    if (peeled.equals(from1))
      assertNotEquals("No similification of " + from1, from1, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from1)))
      assertNotEquals("Simpification of " + from1 + " is just reformatting", compressSpaces(peeled), compressSpaces(from1));
    assertSimilar(expected1, peeled);
  }
  @Test public void assignmentReturn0() {
    assertConvertsTo("a = 3; return a;", "return a = 3;");
  }
  @Test public void assignmentReturn1() {
    assertConvertsTo("a = 3; return (a);", "return a = 3;");
  }
  @Test public void assignmentReturn2() {
    assertConvertsTo("a += 3; return a;", "return a += 3;");
  }
  @Test public void assignmentReturn3() {
    assertConvertsTo("a *= 3; return a;", "return a *= 3;");
  }
  @Test public void assignmentReturniNo() {
    assertNoConversion("b = a = 3; return a;");
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
        "name.endsWith(testSuffix) &&  As.stringBuilder(f).indexOf(testKeyword) == -1? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)", //
        "name.endsWith(testSuffix)&&As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",Z2),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2a() {
    assertSimplifiesTo(//
        "name.endsWith(testSuffix) &&  As.stringBuilder(f).indexOf(testKeyword) == -1? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)", //
        "name.endsWith(testSuffix)&&As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",Z2),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2b() {
    assertSimplifiesTo( //
        "name.endsWith(testSuffix) &&  T ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)", //
        "name.endsWith(testSuffix) && T ? objects(s,name,makeInFile(f)): name.endsWith(\".in\") && !dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",Z2),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2c() {
    assertSimplifiesTo( //
        "X && T ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)", //
        "X && T ? objects(s,name,makeInFile(f)) : name.endsWith(\".in\") && !dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",Z2),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWord2d() {
    assertSimplifiesTo(//
        "X && T ? E : Y ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)", //
        "X && T ? E : !Y && !dotOutExists(d,name) ? objects(name.replaceAll(\"\\\\.in$\",Z2),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2e() {
    assertSimplifiesTo(//
        "X &&  T ? E : Y ? null : Z ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)", //
        "X &&  T ? E : !Y && !Z ? objects(name.replaceAll(\"\\\\.in$\",Z2),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2e1() {
    assertSimplifiesTo(//
        "X &&  T ? E : Y ? null : Z ? null : objects(name.replaceAll(x, Z2), s, f)", //
        "X &&  T ? E : !Y && !Z ? objects(name.replaceAll(x,Z2),s,f) : null");
  }
  @Test public void bugIntroducingMISSINGWord2e2() {
    assertSimplifiesTo(//
        "X &&  T ? E : Y ? null : Z ? null : objects(name.replaceAll(g, Z2), s, f)", //
        "X &&  T ? E : !Y && !Z ? objects(name.replaceAll(g,Z2),s,f) : null");
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
    assertSimplifiesTo(//
        "!name.endsWith(x) ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(3, 56), s, f)",
        "name.endsWith(x)&&!dotOutExists(d,name)?objects(name.replaceAll(3,56),s,f):null");
  }
  @Test public void bugIntroducingMISSINGWordTry1() {
    assertSimplifiesTo(
        "name.endsWith(testSuffix) && -1 == As.stringBuilder(f).indexOf(testKeyword) ? objects(s, name, makeInFile(f)) : !name.endsWith(\".in\") ? null : dotOutExists(d, name) ? null : objects(name.replaceAll(\"\\\\.in$\", Z2), s, f)",
        "name.endsWith(testSuffix) && As.stringBuilder(f).indexOf(testKeyword)==-1?objects(s,name,makeInFile(f)):name.endsWith(\".in\")&&!dotOutExists(d,name)?objects(name.replaceAll(\"\\\\.in$\",Z2),s,f):null");
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
    final InfixExpression e = i("a == true == b == c");
    assertEquals("c", right(e).toString());
    assertSimplifiesTo("a == true == b == c", "a == b == c");
  }
  @Test public void chainCOmparisonTrueLast() {
    assertSimplifiesTo("a == b == c == true", "a == b == c");
  }
  @Test public void comaprisonWithBoolean1() {
    assertSimplifiesTo("s.equals(532)==true", "s.equals(532)");
  }
  @Test public void comaprisonWithBoolean2() {
    assertSimplifiesTo("s.equals(532)==false ", "!s.equals(532)");
  }
  @Test public void comaprisonWithBoolean3() {
    assertSimplifiesTo("(false==s.equals(532))", "(!s.equals(532))");
  }
  @Test public void comaprisonWithSpecific0() {
    assertSimplifiesTo("this != a", "a != this");
  }
  @Test public void comaprisonWithSpecific0Legibiliy00() {
    final InfixExpression e = i("this != a");
    assertTrue(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS));
    assertFalse(Is.booleanLiteral(right(e)));
    assertFalse(Is.booleanLiteral(left(e)));
    assertTrue(in(e.getOperator(), Operator.EQUALS, Operator.NOT_EQUALS));
  }
  @Test public void comaprisonWithSpecific1() {
    assertSimplifiesTo("null != a", "a != null");
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
    assertSimplifiesTo("s.equals(532)==false", "!s.equals(532)");
  }
  @Test public void comaprisonWithSpecific3() {
    assertSimplifiesTo("(this==s.equals(532))", "(s.equals(532)==this)");
  }
  @Test public void comaprisonWithSpecific4() {
    assertSimplifiesTo("(0 < a)", "(a>0)");
  }
  @Test public void comaprisonWithSpecificInParenthesis() {
    assertSimplifiesTo("(null==a)", "(a==null)");
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
  @Test public void comparison01() {
    assertNoChange("1+2+3<3");
  }
  @Test public void comparison02() {
    assertNoChange("f(2)<a");
  }
  @Test public void comparison03() {
    assertNoChange("this==null");
  }
  @Test public void comparison04() {
    assertSimplifiesTo("6-7<2+1", "6-7<1+2");
  }
  @Test public void comparison05() {
    assertNoChange("a==11");
  }
  @Test public void comparison06() {
    assertNoChange("1<102333");
  }
  @Test public void comparison08() {
    assertNoChange("a==this");
  }
  @Test public void comparison09() {
    assertSimplifiesTo("1+2<3&7+4>2+1", "1+2<3&4+7>1+2");
  }
  @Test public void comparison10() {
    assertNoChange("1+2+3<3-4");
  }
  @Test public void comparison11() {
    assertSimplifiesTo("12==this", "this==12");
  }
  @Test public void comparison12() {
    assertSimplifiesTo("1+2<3&7+4>2+1||6-7<2+1", "1+2<3&4+7>1+2||6-7<1+2");
  }
  @Test public void comparison13() {
    assertNoChange("13455643294<22");
  }
  @Test public void comparisonWithCharacterConstant() {
    assertSimplifiesTo("'d' == s.charAt(i)", "s.charAt(i)=='d'");
  }
  @Test public void compreaeExpressionToExpression() {
    assertSimplifiesTo("6 - 7 < 2 + 1   ", "6 -7 < 1 + 2");
  }
  @Test public void declarationIfAssignment() {
    assertConvertsTo( //
        "" + //
            "    final String y = empty;\n" + //
            "    final String s = empty;\n" + //
            "    String res = s;\n" + //
            "    if (s.equals(y))\n" + //
            "      res = s + blah;\n" + //
            "    System.out.println(res);",
        "" + //
            "    final String y = empty;\n" + //
            "    final String s = empty;\n" + //
            "    String res = s.equals(y) ? s + blah :s;\n" + //
            "    System.out.println(res);");
  }
  @Test public void declarationIfUsesLaterVariable() {
    assertNoConversion("int a=0, b=0;if (b==3)   a=4;");
  }
  @Test public void delcartionIfAssignmentNotPlain() {
    assertNoConversion("int a=0;   if (y) a+=3; ");
  }
  @Ignore @Test public void doNotIntroduceDoubleNegation() {
    assertSimplifiesTo("!Y ? null :!Z ? null : F", "Y&&Z?F:null");
  }
  @Test public void duplicateBothIfBranches() {
    assertConvertsTo("if (s.equals(532))    System.out.close();else    System.out.close();", " System.out.close();} ");
  }
  @Test public void duplicatePartialIfBranches() {
    assertConvertsTo(
        "" + //
            "    if (a) {\n" + //
            "      f();\n" + //
            "      g();\n" + //
            "      ++i;\n" + //
            "    } else {\n" + //
            "      f();\n" + //
            "      g();\n" + //
            "      --i;\n" + //
            "    }",
        "" + ////
            "   f();\n" + //
            "   g();\n" + //
            "    if (a) \n" + //
            "      ++i;\n" + //
            "    else \n" + //
            "      --i;" //
    );
  }
  @Test public void emptyElse() {
    assertConvertsTo("if (x) b = 3; else ;", "if (x) b = 3;");
  }
  @Test public void emptyElseBlock() {
    assertConvertsTo("if (x) b = 3; else { ;}", "if (x) b = 3;");
  }
  @Test public void emptyIsNotChangedExpression() {
    assertNoConversion("");
  }
  @Test public void emptyIsNotChangedStatement() {
    assertNoChange("");
  }
  @Test public void emptyThen1() {
    assertConvertsTo("if (b) ; else x();", "if (!b) x();");
  }
  @Test public void emptyThen2() {
    assertConvertsTo("if (b) {;;} else {x() ;}", "if (!b) x();");
  }
  @Ignore @Test public void extractMethodSplitDifferentStories() {
    assertSimplifiesTo("", "");
  }
  @Ignore @Test public void forwardDeclaration1() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = 6;   int j = 2;   int k = i+2;   System.out.println(i-j+k); ",
        " /*    * This is a comment    */      int j = 2;   int i = 6;   int k = i+2;   System.out.println(i-j+k); ");
  }
  @Ignore @Test public void forwardDeclaration2() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = 6, h = 7;   int j = 2;   int k = i+2;   System.out.println(i-j+k); ",
        " /*    * This is a comment    */      int h = 7;   int j = 2;   int i = 6;   int k = i+2;   System.out.println(i-j+k); ");
  }
  @Ignore @Test public void forwardDeclaration3() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = 6;   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   yada3(i);   yada3(i+m); ",
        " /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   int i = 6;   yada3(i);   yada3(i+m); ");
  }
  @Ignore @Test public void forwardDeclaration4() {
    assertSimplifiesTo(
        " /*    * This is a comment    */      int i = 6;   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   final BlahClass bc = new BlahClass(i);   yada3(i+m+bc.j);    private static class BlahClass {   public BlahClass(int i) {    j = 2*i;      public final int j; ",
        " /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m);   int i = 6;   final BlahClass bc = new BlahClass(i);   yada3(i+m+bc.j);    private static class BlahClass {   public BlahClass(int i) {    j = 2*i;      public final int j; ");
  }
  @Ignore @Test public void forwardDeclaration5() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = yada3(0);   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ",
        " /*    * This is a comment    */      int j = 3;   int k = j+2;   int i = yada3(0);   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ");
  }
  @Ignore @Test public void forwardDeclaration6() {
    assertSimplifiesTo(
        " /*    * This is a comment    */      int i = yada3(0);   int h = 8;   int j = 3;   int k = j+2 + yada3(i);   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ",
        " /*    * This is a comment    */      int h = 8;   int i = yada3(0);   int j = 3;   int k = j+2 + yada3(i);   int m = k + j -19;   yada3(m*2 - k/m + i);   yada3(i+m); ");
  }
  @Ignore @Test public void forwardDeclaration7() {
    assertSimplifiesTo(
        "  j = 2*i;   }      public final int j;    private BlahClass yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   System.out.println(res.j);   return res; ",
        "  j = 2*i;   }      public final int j;    private BlahClass yada6() {   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   final BlahClass res = new BlahClass(6);   System.out.println(res.j);   return res; ");
  }
  @Test public void ifBugSecondTry() {
    assertConvertsTo(
        "" + //
            " final int c = 2;\n" + //
            "    if (c == c + 1) {\n" + //
            "      if (c == c + 2)\n" + //
            "        return null;\n" + //
            "      c = f().charAt(3);\n" + //
            "    } else if (Character.digit(c, 16) == -1)\n" + //
            "      return null;\n" + //
            "    return null;",
        "" + //
            "    final int c = 2;\n" + //
            "    if (c != c + 1) {\n" + //
            "      if (Character.digit(c, 16) == -1)\n" + //
            "        return null;\n" + //
            "    } else {\n" + //
            "      if (c == c + 2)\n" + //
            "        return null;\n" + //
            "      c = f().charAt(3);\n" + //
            "    }\n" + //
            "    return null;");//
  }
  @Test public void ifBugSimplified() {
    assertConvertsTo(
        "" + //
            "    if (x) {\n" + //
            "      if (z)\n" + //
            "        return null;\n" + //
            "      c = f().charAt(3);\n" + //
            "    } else if (y)\n" + //
            "      return;\n" + //
            "",
        "" + //
            "    if (!x) {\n" + //
            "      if (y)\n" + //
            "        return;\n" + //
            "    } else {\n" + //
            "      if (z)\n" + //
            "        return null;\n" + //
            "      c = f().charAt(3);\n" + //
            "    }\n" + //
            "");//
  }
  @Test public void ifBugWithPlainEmptyElse() {
    assertConvertsTo(
        "" + //
            "      if (z)\n" + //
            "        f();\n" + //
            "      else\n" + //
            "         ; \n" + //
            "",
        "" + //
            "      if (z)\n" + //
            "        f();\n" + //
            "");//
  }
  @Test public void ifEmptyElsewWithinIf() {
    assertConvertsTo("if (a) if (b) f(); else ;", "if (a) if (b) f();");
  }
  @Test public void ifFunctionCall() {
    assertConvertsTo("if (x) f(a); else f(b);", "f(x ? a: b);");
  }
  @Test public void ifPlusPlusPost() {
    assertConvertsTo("if (x) a++; else b++;", "if(x)++a;else++b;");
  }
  @Test public void ifPlusPlusPostExpression() {
    assertNoChange("x? a++:b++");
  }
  @Test public void ifPlusPlusPre() {
    assertNoConversion("if (x) ++a; else ++b;");
  }
  @Test public void ifPlusPlusPreExpression() {
    assertNoChange("x? ++a:++b");
  }
  @Ignore @Test public void inlineSingleUse01() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = yada3(0);   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + i); ",
        " /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + (yada3(0))); ");
  }
  @Ignore @Test public void inlineSingleUse02() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = 5,j=3;   int k = j+2;   int m = k + j -19 +i;   yada3(k); ",
        " /*    * This is a comment    */      int j=3;   int k = j+2;   int m = k + j -19 +(5);   yada3(k); ");
  }
  @Ignore @Test public void inlineSingleUse03() {
    assertSimplifiesTo("/*    * This is a comment    */      int i = 5;   int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + i); ",
        " /*    * This is a comment    */      int j = 3;   int k = j+2;   int m = k + j -19;   yada3(m*2 - k/m + (5)); ");
  }
  @Ignore @Test public void inlineSingleUse04() {
    assertSimplifiesTo("int x = 6;   final BlahClass b = new BlahClass(x);   int y = 2+b.j;   yada3(y-b.j);   yada3(y*2); ",
        " final BlahClass b = new BlahClass((6));   int y = 2+b.j;   yada3(y-b.j);   yada3(y*2); ");
  }
  @Ignore @Test public void inlineSingleUse05() {
    assertSimplifiesTo("int x = 6;   final BlahClass b = new BlahClass(x);   int y = 2+b.j;   yada3(y+x);   yada3(y*x); ",
        " int x = 6;   int y = 2+(new BlahClass(x)).j;   yada3(y+x);   yada3(y*x); ");
  }
  @Ignore @Test public void inlineSingleUse06() {
    assertNoChange(
        "   final Collection<Integer> outdated = new ArrayList<>();     int x = 6, y = 7;     System.out.println(x+y);     final Collection<Integer> coes = new ArrayList<>();     for (final Integer pi : coes)      if (pi.intValue() < x - y)       outdated.add(pi);     for (final Integer pi : outdated)      coes.remove(pi);     System.out.println(coes.size()); ");
  }
  @Test public void inlineSingleUse07() {
    assertNoChange(
        "   final Collection<Integer> outdated = new ArrayList<>();     int x = 6, y = 7;     System.out.println(x+y);     final Collection<Integer> coes = new ArrayList<>();     for (final Integer pi : coes)      if (pi.intValue() < x - y)       outdated.add(pi);     System.out.println(coes.size()); ");
  }
  @Test public void inlineSingleUse08() {
    assertNoChange(
        "   final Collection<Integer> outdated = new ArrayList<>();     int x = 6, y = 7;     System.out.println(x+y);     final Collection<Integer> coes = new ArrayList<>();     for (final Integer pi : coes)      if (pi.intValue() < x - y)       outdated.add(pi);     System.out.println(coes.size());     System.out.println(outdated.size()); ");
  }
  @Test public void inlineSingleUse09() {
    assertNoChange(
        " final Application a = new DuplicateCurrent().new Application(&quot;{\nABRA\n{\nCADABRA\n{&quot;);   assertEquals(5, a.new Context().lineCount());   final PureIterable&lt;Mutant&gt; ms = a.generateMutants();   assertEquals(2, count(ms));   final PureIterator&lt;Mutant&gt; i = ms.iterator();   assertTrue(i.hasNext());   assertEquals(&quot;{\nABRA\nABRA\n{\nCADABRA\n{\n&quot;, i.next().text);   assertTrue(i.hasNext());   assertEquals(&quot;{\nABRA\n{\nCADABRA\nCADABRA\n{\n&quot;, i.next().text);   assertFalse(i.hasNext());  ");
  }
  @Test public void inlineSingleUse10() {
    assertNoChange(
        "      final Application a = new Application(\"{\nABRA\n{\nCADABRA\n{\");        assertEquals(5, a.new Context().lineCount());        final PureIterable<Mutant> ms = a.mutantsGenerator();        assertEquals(2, count(ms));        final PureIterator<Mutant> i = ms.iterator();        assertTrue(i.hasNext());        assertEquals(\"{\nABRA\nABRA\n{\nCADABRA\n{\n\", i.next().text);        assertTrue(i.hasNext());        assertEquals(\"{\nABRA\n{\nCADABRA\nCADABRA\n{\n\", i.next().text);        assertFalse(i.hasNext());");
  }
  @Test public void isGreaterTrue() {
    final InfixExpression e = i("f(a,b,c,d,e) * f(a,b,c)");
    assertEquals("f(a,b,c)", right(e).toString());
    assertEquals("f(a,b,c,d,e)", left(e).toString());
    final Wring<InfixExpression> s = Toolbox.instance.find(e);
    assertThat(s, instanceOf(InfixSortMultiplication.class));
    assertNotNull(s);
    assertTrue(s.scopeIncludes(e));
    final Expression e1 = left(e);
    final Expression e2 = right(e);
    assertFalse(hasNull(e1, e2));
    final boolean tokenWiseGreater = nodesCount(e1) > nodesCount(e2) + NODES_THRESHOLD;
    assertTrue(tokenWiseGreater);
    assertTrue(ExpressionComparator.moreArguments(e1, e2));
    assertTrue(ExpressionComparator.longerFirst(e));
    assertTrue(e.toString(), s.eligible(e));
    final ASTNode replacement = ((Wring.Replacing<InfixExpression>) s).replacement(e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) * f(a,b,c,d,e)", replacement.toString());
  }
  @Test public void isGreaterTrueButAlmostNot() {
    final InfixExpression e = i("f(a,b,c,d) * f(a,b,c)");
    assertEquals("f(a,b,c)", right(e).toString());
    assertEquals("f(a,b,c,d)", left(e).toString());
    final Wring<InfixExpression> s = Toolbox.instance.find(e);
    assertThat(s, instanceOf(InfixSortMultiplication.class));
    assertNotNull(s);
    assertTrue(s.scopeIncludes(e));
    final Expression e1 = left(e);
    final Expression e2 = right(e);
    assertFalse(hasNull(e1, e2));
    final boolean tokenWiseGreater = nodesCount(e1) > nodesCount(e2) + NODES_THRESHOLD;
    assertFalse(tokenWiseGreater);
    assertTrue(ExpressionComparator.moreArguments(e1, e2));
    assertTrue(ExpressionComparator.longerFirst(e));
    assertTrue(e.toString(), s.eligible(e));
    final ASTNode replacement = ((Wring.Replacing<InfixExpression>) s).replacement(e);
    assertNotNull(replacement);
    assertEquals("f(a,b,c) * f(a,b,c,d)", replacement.toString());
  }
  @Test public void linearTransformation() {
    assertSimplifiesTo("plain * the + kludge", "the*plain+kludge");
  }
  @Test public void literalVsLiteral() {
    assertNoChange("1 < 102333");
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
  @Test public void parenthesizeOfpushdownTernary() {
    assertSimplifiesTo("a ? b+x+e+f:b+y+e+f", "b+(a ? x : y)+e+f");
  }
  @Test public void postDecreementReturn() {
    assertConvertsTo("a--; return a;", "--a;return a;");
  }
  @Test public void postIncrementReturn() {
    assertConvertsTo("a++; return a;", "++a;return a;");
  }
  @Test public void preDecreementReturn() {
    assertConvertsTo("--a.b.c; return a.b.c;", "return--a.b.c;");
  }
  @Test public void preDecrementReturn() {
    assertConvertsTo("--a; return a;", "return --a;");
  }
  @Test public void preDecrementReturn1() {
    assertConvertsTo("--this.a; return this.a;", "return --this.a;");
  }
  @Test public void prefixToPosfixIncreementSimple() {
    assertSimplifiesTo("i++", "++i");
  }
  @Test public void prefixToPostfixDecrement() {
    final String from = "for (int i = 0; i < 100;  i--)  i--;";
    final Statement s = s(from);
    assertThat(s, iz("{" + from + "}"));
    assertNotNull(s);
    final PostfixExpression e = Extract.findFirstPostfix(s);
    assertNotNull(e);
    assertThat(e, iz("i--"));
    final ASTNode parent = e.getParent();
    assertThat(parent, notNullValue());
    assertThat(parent, iz(from));
    assertThat(parent, is(not(instanceOf(Expression.class))));
    assertThat(new PostfixToPrefix().scopeIncludes(e), is(true));
    assertThat(new PostfixToPrefix().eligible(e), is(true));
    final Expression r = new PostfixToPrefix().replacement(e);
    assertThat(r, iz("--i"));
    assertConvertsTo(from, "for(int i=0;i<100;--i)--i;");
  }
  @Test public void prefixToPostfixIncreement() {
    assertConvertsTo("for (int i = 0; i < 100; i++) i++;", "for(int i=0;i<100;++i)++i;");
  }
  @Test public void preIncrementReturn() {
    assertConvertsTo("++a; return a;", "return ++a;");
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
  @Test public void pushdownTernaryAMethodCallDistinctReceiver() {
    assertNoChange("a ? x.f(c) : y.f(d)");
  }
  @Test public void pushdownTernaryDifferentTargetFieldRefernce() {
    assertSimplifiesTo("a ? 1 + x.a : 1 + y.a", "1+(a ? x.a : y.a)");
  }
  @Test public void pushdownTernaryFieldReferneceShort() {
    assertNoChange("a ? R.b.c : R.b.d");
  }
  @Test public void pushdownTernaryFunctionCall() {
    assertSimplifiesTo("a ? f(b,c) : f(c)", "!a?f(c):f(b,c)");
  }
  @Test public void pushdownTernaryFX() {
    assertSimplifiesTo("a ? false : c", "!a && c");
  }
  @Test public void pushdownTernaryIdenticalAddition() {
    assertSimplifiesTo("a ? b+d :b+ d", "b+d");
  }
  @Test public void pushdownTernaryIdenticalAdditionWtihParenthesis() {
    assertSimplifiesTo("a ? (b+d) :(b+ d)", "b+d");
  }
  @Test public void pushdownTernaryIdenticalAssignment() {
    assertSimplifiesTo("a ? (b=c) :(b=c)", "b = c");
  }
  @Test public void pushdownTernaryIdenticalAssignmentVariant() {
    assertSimplifiesTo("a ? (b=c) :(b=d)", "b=a?c:d");
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
    assertSimplifiesTo("a ? new S(a,new Integer(4),b) : new S(new Ineger(3))",
        "!a?new S(new Ineger(3)):new S(a,new Integer(4),b)                                                                                                                  ");
  }
  @Test public void pushdownTernaryIntoPrintln() {
    assertConvertsTo(//
        "    if (s.equals(t))\n" + "      System.out.println(Hey + res);\n" + "    else\n" + "      System.out.println(Ho + x + a);", //
        "System.out.println(s.equals(t)?Hey+res:Ho+x+a);");
  }
  @Test public void pushdownTernaryLongFieldRefernece() {
    assertSimplifiesTo(//
        "externalImage ? R.string.webview_contextmenu_image_download_action : R.string.webview_contextmenu_image_save_action", //
        "!externalImage ? R.string.webview_contextmenu_image_save_action : R.string.webview_contextmenu_image_download_action");
  }
  @Test public void pushdownTernaryMethodInvocationFirst() {
    assertSimplifiesTo("a?b():c", "!a?c:b()");
  }
  @Test public void pushdownTernaryNoBoolean() {
    assertNoChange("a?b:c");
  }
  @Test public void pushdownTernaryNoReceiverReceiver() {
    assertNoChange("a < b ? f() : a.f()");
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
  @Test public void pushdownTernaryOnMethodCall() {
    assertSimplifiesTo("a ? y.f(c,b) :y.f(c)", "!a?y.f(c):y.f(c,b)");
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
  @Test public void pushdownTernaryReceiverNoReceiver() {
    assertSimplifiesTo("a < b ? a.f() : f()", "a>=b?f():a.f()");
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
  @Ignore @Test public void reanmeReturnVariableToDollar01() {
    assertSimplifiesTo(
        " public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   System.out.println(res.j);   return res; ",
        " public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   System.out.println($.j);   return $; ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar02() {
    assertSimplifiesTo(
        " int res = blah.length();   if (blah.contains(0xDEAD))    return res * 2;   if (res % 2 ==0)    return ++res;   if (blah.startsWith(\"y\")) {    return yada3(res);   int x = res + 6;   if (x>1)    return res + x;   res -= 1;   return res; ",
        " int $ = blah.length();   if (blah.contains(0xDEAD))    return $ * 2;   if ($ % 2 ==0)    return ++$;   if (blah.startsWith(\"y\")) {    return yada3($);   int x = $ + 6;   if (x>1)    return $ + x;   $ -= 1;   return $; ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar03() {
    assertSimplifiesTo(
        " public BlahClass(int i) {    j = 2*i;      public final int j;   public int yada7(final String blah) {   final BlahClass res = new BlahClass(blah.length());   if (blah.contains(0xDEAD))    return res.j;   int x = blah.length()/2;   if (x==3)    return x;   x = yada3(res.j - x);   return x; ",
        " public BlahClass(int i) {    j = 2*i;      public final int j;   public int yada7(final String blah) {   final BlahClass res = new BlahClass(blah.length());   if (blah.contains(0xDEAD))    return res.j;   int $ = blah.length()/2;   if ($==3)    return $;   $ = yada3(res.j - $);   return $; ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar04() {
    assertNoChange("int res = 0;   String $ = blah + known;   yada3(res + $.length());   return res + $.length();");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar05() {
    assertSimplifiesTo(
        "  j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass(res.j);     System.out.println(res2.j);     doStuff(res2);        private void doStuff(final BlahClass res) {     System.out.println(res.j);   System.out.println(res.j);   return res; ",
        "  j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass($.j);     System.out.println(res2.j);     doStuff(res2);        private void doStuff(final BlahClass res) {     System.out.println(res.j);   System.out.println($.j);   return $; ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar06() {
    assertSimplifiesTo(
        "  j = 2*i;   }      public final int j;    public void yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass(res.j);     System.out.println(res2.j);     doStuff(res2);        private int doStuff(final BlahClass r) {     final BlahClass res = new BlahClass(r.j);     return res.j + 1;   System.out.println(res.j); ",
        "  j = 2*i;   }      public final int j;    public void yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     final BlahClass res2 = new BlahClass(res.j);     System.out.println(res2.j);     doStuff(res2);        private int doStuff(final BlahClass r) {     final BlahClass $ = new BlahClass(r.j);     return $.j + 1;   System.out.println(res.j); ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar07() {
    assertSimplifiesTo(
        "  j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   System.out.println(res.j);   return res; ",
        "  j = 2*i;   }      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   final Runnable r = new Runnable() {        @Override    public void run() {     res = new BlahClass(8);     System.out.println(res.j);     doStuff(res);        private void doStuff(BlahClass res2) {     System.out.println(res2.j);        private BlahClass res;   System.out.println($.j);   return $; ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar08() {
    assertSimplifiesTo(
        " public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   if (res.j == 0)    return null;   System.out.println(res.j);   return res; ",
        " public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass $ = new BlahClass(6);   if ($.j == 0)    return null;   System.out.println($.j);   return $; ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar09() {
    assertNoChange(
        " public BlahClass(int i) {    j = 2*i;      public final int j;    public BlahClass yada6() {   final BlahClass res = new BlahClass(6);   if (res.j == 0)    return null;   System.out.println(res.j);   return null;");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar10() {
    assertSimplifiesTo(
        "@Override public IMarkerResolution[] getResolutions(final IMarker m) {   try {    final Spartanization s = All.get((String) m.getAttribute(Builder.SPARTANIZATION_TYPE_KEY)); ",
        "@Override public IMarkerResolution[] getResolutions(final IMarker m) {   try {    final Spartanization $ = All.get((String) m.getAttribute(Builder.SPARTANIZATION_TYPE_KEY)); ");
  }
  @Ignore @Test public void reanmeReturnVariableToDollar11() {
    assertNoChange("");
  }
  @Test public void removeSuper() {
    assertConvertsTo("class T { T() { super(); }", "class T { T() { }");
  }
  @Test public void removeSuperWithArgument() {
    assertNoConversion("class T { T() { super(a); a();}");
  }
  @Test public void removeSuperWithStatemen() {
    assertConvertsTo("class T { T() { super(); a++;}", "class T { T() { ++a;}");
  }
  @Test public void rightSimplificatioForNulNNVariableReplacement() {
    final InfixExpression e = i("null != a");
    final Wring<InfixExpression> w = Toolbox.instance.find(e);
    assertNotNull(w);
    assertTrue(w.scopeIncludes(e));
    assertTrue(w.eligible(e));
    final ASTNode replacement = ((Wring.Replacing<InfixExpression>) w).replacement(e);
    assertNotNull(replacement);
    assertEquals("a != null", replacement.toString());
  }
  @Test public void rightSipmlificatioForNulNNVariable() {
    assertThat(Toolbox.instance.find(i("null != a")), instanceOf(InfixComparisonSpecific.class));
  }
  @Test public void sequencerFirstInElse() {
    assertConvertsTo(//
        "if (a) {b++; c++; ++d;} else { f++; g++; return x;}", //
        "if (!a) {f++; g++; return x;} b++; c++; ++d; " //
    );
  }
  @Test public void shortestBranchInIf() {
    assertConvertsTo("   int a=0;\n" + //
        "   if (s.equals(known)){\n" + //
        "     System.console();\n" + //
        "   } else {\n" + //
        "     a=3;\n" + //
        "   }\n" + //
        "", "int a=0; if(!s.equals(known))a=3;else System.console();");
  }
  @Test public void shortestIfBranchFirst01() {
    assertConvertsTo(""//
        + "if (s.equals(0xDEAD)) {\n"//
        + " int res=0; "//
        + " for (int i=0; i<s.length(); ++i)     "//
        + " if (s.charAt(i)=='a')      "//
        + "   res += 2;    "//
        + "} else "//
        + " if (s.charAt(i)=='d') "//
        + "  res -= 1;  "//
        + "return res;  "//
        ,
        ""//
            + "if (!s.equals(0xDEAD)) {"//
            + " if(s.charAt(i)=='d')"//
            + "  res-=1;"//
            + "} else {"//
            + "  int res=0;"//
            + "  for(int i=0;i<s.length();++i)"//
            + "   if(s.charAt(i)=='a')"//
            + "     res+=2;"//
            + " }"//
            + " return res;");
  }
  @Test public void shortestOperand02() {
    assertNoConversion("k = k + 4;if (2 * 6 + 4 == k) return true;");
  }
  @Test public void shortestOperand05() {
    assertNoConversion(//
        "    final StringBuilder s = new StringBuilder(\"bob\");\n" + //
            "    return s.append(\"-ha\").append(\"-ba\").toString() == \"bob-ha-banai\";");
  }
  @Test public void shortestOperand12() {
    assertConvertsTo("int k = 15;   return 7 < k; ", "int k = 15; return k > 7;");
  }
  @Test public void shortestOperand13() {
    assertConvertsTo("return (2 > 2 + a) == true;", "return 2>a+2;");
  }
  @Test public void shortestOperand13a() {
    assertSimplifiesTo("(2 > 2 + a) == true", "2>a+2 ");
  }
  @Test public void shortestOperand13b() {
    assertSimplifiesTo("(2) == true", "2 ");
  }
  @Test public void shortestOperand13c() {
    assertSimplifiesTo("2 == true", "2 ");
  }
  @Test public void shortestOperand14() {
    assertNoConversion("Integer t = new Integer(5);   return (t.toString() == null);    ");
  }
  @Test public void shortestOperand15() {
    assertNoConversion("String t = Bob + Wants + To + \"Sleep \";   return (right_now + t);    ");
  }
  @Test public void shortestOperand16() {
    assertNoConversion("String t = Z2;   t = t.concat(A).concat(\"b\") + t.concat(\"c\");   return (t + \"...\");    ");
  }
  @Test public void shortestOperand17() {
    assertSimplifiesTo("5 ^ a.getNum()", "a.getNum() ^ 5");
  }
  @Test public void shortestOperand19() {
    assertSimplifiesTo(//
        "k.get().operand() ^ a.get()", //
        "a.get() ^ k.get().operand()");
  }
  @Test public void shortestOperand20() {
    assertSimplifiesTo(//
        "k.get() ^ a.get()", //
        "a.get() ^ k.get()");
  }
  @Test public void shortestOperand22() {
    assertNoConversion("return f(a,b,c,d,e) + f(a,b,c,d) + f(a,b,c) + f f(a,b) + f(a) + f();     } ");
  }
  @Test public void shortestOperand23() {
    assertNoConversion("return f() + \".\";     }");
  }
  @Test public void shortestOperand24() {
    assertSimplifiesTo("f(a,b,c,d) & 175 & 0", //
        "f(a,b,c,d) & 0 & 175");
  }
  @Test public void shortestOperand25() {
    assertSimplifiesTo(//
        "f(a,b,c,d) & bob & 0 ", "bob & f(a,b,c,d) & 0");
  }
  @Test public void shortestOperand27() {
    assertNoConversion("return f(a,b,c,d) + f(a,b,c) + f();     } ");
  }
  @Test public void shortestOperand28() {
    assertNoChange("return f(a,b,c,d) * f(a,b,c) * f();     } ");
  }
  @Test public void shortestOperand29() {
    assertSimplifiesTo( //
        "f(a,b,c,d) ^ f() ^ 0", //
        "f() ^ f(a,b,c,d) ^ 0");
  }
  @Test public void shortestOperand30() {
    assertSimplifiesTo(//
        "f(a,b,c,d) & f()", //
        "f() & f(a,b,c,d)" //
    );
  }
  @Test public void shortestOperand31() {
    assertNoConversion("return f(a,b,c,d) | \".\";     }");
  }
  @Test public void shortestOperand32() {
    assertNoConversion("return f(a,b,c,d) && f();     }");
  }
  @Test public void shortestOperand33() {
    assertNoConversion("return f(a,b,c,d) || f();     }");
  }
  @Test public void shortestOperand34() {
    assertNoConversion("return f(a,b,c,d) + someVar; ");
  }
  @Test public void shortestOperand37() {
    assertNoChange("return sansJavaExtension(f) + n + \".\"+ extension(f);");
  }
  @Test public void simplifyBlockComplexEmpty0() {
    assertConvertsTo("{}", "");
  }
  @Test public void simplifyBlockComplexEmpty1() {
    assertConvertsTo("{;;{;{{}}}{;}{};}", "");
  }
  @Test public void simplifyBlockComplexSingleton() {
    assertSimplifiesTo("{;{{;;return b; }}}", "return b;", new BlockSimplify(), Wrap.Statement);
  }
  @Test public void simplifyBlockDeeplyNestedReturn() {
    assertSimplifiesTo("{{{;return c;};;};}", "return c;", new BlockSimplify(), Wrap.Statement);
  }
  /* Begin of already good tests */
  @Test public void simplifyBlockEmpty() {
    assertSimplifiesTo("{;;}", "", new BlockSimplify(), Wrap.Statement);
  }
  @Test public void simplifyBlockExpressionVsExpression() {
    assertSimplifiesTo("6 - 7 < a * 3", "6 - 7 < 3 * a");
  }
  @Test public void simplifyBlockLiteralVsLiteral() {
    assertNoChange("if (a) return b; else c;");
  }
  @Test public void simplifyBlockThreeStatements() {
    assertSimplifiesTo("{i++;{{;;return b; }}j++;}", "i++;return b;j++;", new BlockSimplify(), Wrap.Statement);
  }
  @Test public void simplifyLogicalNegationNested() {
    assertSimplifiesTo("!((a || b == c) && (d || !(!!c)))", "!a && b != c || !d && c");
  }
  @Test public void simplifyLogicalNegationNested1() {
    assertSimplifiesTo("!(d || !(!!c))", "!d && c");
  }
  @Test public void simplifyLogicalNegationNested2() {
    assertSimplifiesTo("!(!d || !!!c)", "d && c");
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
  @Test public void sortAddition1() {
    assertSimplifiesTo(//
        "1 + 2 - 3 - 4 + 5 / 6 - 7 + 8 * 9  + A> k + 4", //
        "8*9+1+2-3-4+5 / 6-7+A>k+4");
  }
  @Test public void sortAddition2() {
    assertSimplifiesTo("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1", "1+2 <3&4+7>1+2||6-7<1+2");
  }
  @Test public void sortAddition3() {
    assertNoChange("6 - 7 < 1 + 2");
  }
  @Test public void sortAddition4() {
    assertSimplifiesTo(//
        "a + 11 + 2 < 3 & 7 + 4 > 2 + 1", //
        "7 + 4 > 2 + 1 & a + 11 + 2 < 3");
  }
  @Test public void sortAdditionClassConstantAndLiteral() {
    assertSimplifiesTo(//
        "1+A< 12", //
        "A+1<12");
  }
  @Test public void sortAdditionFunctionClassConstantAndLiteral() {
    assertSimplifiesTo(//
        "1+A+f()< 12", //
        "f()+A+1<12");
  }
  @Test public void sortAdditionUncertain() {
    assertNoChange("1+a");
  }
  @Test public void sortAdditionVariableClassConstantAndLiteral() {
    assertSimplifiesTo(//
        "1+A+a< 12", //
        "a+A+1<12");
  }
  @Test public void sortConstantMultiplication() {
    assertSimplifiesTo("a*2", "2*a");
  }
  @Test public void sortThreeOperands1() {
    assertNoChange("1.0*2222*3");
  }
  @Test public void sortThreeOperands2() {
    assertNoChange("1.0*1*124*1");
  }
  @Test public void sortThreeOperands3() {
    assertNoChange("1*2F*33*142*1");
  }
  @Test public void sortThreeOperands4() {
    assertNoChange("1*2*'a'");
  }
  @Test public void sortTwoOperands0CheckThatWeSortByLength_a() {
    assertSimplifiesTo("1111*211", "211*1111");
  }
  @Test public void sortTwoOperands0CheckThatWeSortByLength_b() {
    assertNoChange("211*1111");
  }
  @Test public void sortTwoOperands1() {
    assertNoChange("1*2F");
  }
  @Test public void sortTwoOperands2() {
    assertSimplifiesTo("2.0*1", "1*2.0");
  }
  @Test public void sortTwoOperands3() {
    assertNoChange("1*2L");
  }
  @Test public void sortTwoOperands4() {
    assertSimplifiesTo("2L*1", "1*2L");
  }
  @Test public void ternarize05() {
    assertConvertsTo(" int res = 0; "//
        + "if (s.equals(532))    "//
        + "res += 6;   "//
        + "else    "//
        + "res += 9;      ", "int res=0;res+=s.equals(532)?6:9;");
  }
  @Test public void ternarize05a() {
    assertConvertsTo(" int res = 0; " + "if (s.equals(532))    " + "res += 6;   " + "else    " + "res += 9;      " + "return res; ",
        "int res=0;res+=s.equals(532)?6:9;return res;");
  }
  @Test public void ternarize07() {
    assertConvertsTo("String res;   res = s;   if (res.equals(532)==true)    res = s + 0xABBA;   System.out.println(res); ", //
        "String res=s;if(res.equals(532))res=s+0xABBA;System.out.println(res);");
  }
  @Test public void ternarize09() {
    assertConvertsTo("if (s.equals(532)) {    return 6;}else {    return 9;}", //
        "return s.equals(532)?6:9; ");
  }
  @Test public void ternarize10() {
    assertConvertsTo("String res = s, foo = bar;   "//
        + "if (res.equals(532)==true)    " + //
        "res = s + 0xABBA;   "//
        + "System.out.println(res); ", "String res=s,foo=bar;if(res.equals(532))res=s+0xABBA;System.out.println(res);");
  }
  @Test public void ternarize12() {
    assertConvertsTo(//
        "String res = s;   if (s.equals(532)==true)    res = res + 0xABBA;   System.out.println(res); ", "String res=s;if(s.equals(532))res=res+0xABBA;System.out.println(res);");
  }
  @Test public void ternarize13() {
    assertConvertsTo(//
        "String res = mode, foo;  if (mode.equals(f())==true)   foo = M; ", //
        "String res=mode,foo;if(mode.equals(f()))foo=M;");
  }
  @Test public void ternarize14() {
    assertConvertsTo(//
        "String res=mode,foo=GY;if (res.equals(f())==true){foo = M;int k = 2;k = 8;System.out.println(foo);", //
        "String res=mode,foo=GY;if(res.equals(f())){foo=M;int k=2;k=8;System.out.println(foo);");
  }
  @Test public void ternarize16() {
    assertNoConversion(//
        "String res = mode;  int num1, num2, num3;  if (mode.equals(f()))   num2 = 2; ");
  }
  @Test public void ternarize16a() {
    assertConvertsTo(
        "int n1, n2 = 0, n3;\n" + //
            "  if (d)\n" + //
            "    n2 = 2;", //
        "int n1, n2 = d ? 2: 0, n3;");
  }
  @Test public void ternarize21() {
    assertNoConversion("if (s.equals(532)){    System.out.println(gG);    System.out.append(kKz); ");
  }
  @Test public void ternarize21a() {
    assertNoConversion(//
        "   if (s.equals(known)){\n" + //
            "     System.out.println(gG);\n" + //
            "   } else {\n" + //
            "     System.out.append(kKz);\n" + //
            "   }");
  }
  @Test public void ternarize22() {
    assertNoConversion("int a=0;   if (s.equals(532)){    System.console();    a=3; ");
  }
  @Test public void ternarize26() {
    assertNoConversion("int a=0;   if (s.equals(532)){    a+=2;   a-=2; ");
  }
  @Test public void ternarize29() {
    assertNoConversion("int a=0;   int b=0;   a=5;   if (a==3){    a=4; }");
  }
  @Test public void ternarize33() {
    assertNoConversion("int a, b=0;   if (b==3){    a=4; ");
  }
  @Test public void ternarize35() {
    assertNoChange("int a, b=0, c=0;   a=4;   if (c==3){    b=2; ");
  }
  @Test public void ternarize36() {
    assertNoChange("int a, b=0, c=0;   a=4;   if (c==3){    b=2;   a=6; ");
  }
  @Test public void ternarize41() {
    assertConvertsTo(//
        "int a,b,c,d;a = 3;b = 5; d = 7;if (a == 4)while (b == 3) c = a; else while (d == 3)c =a*a; ", //
        "int a=3,b,c,d;b=5;d=7;if(a==4)while(b==3)c=a;else while(d==3)c=a*a;");
  }
  @Test public void ternarize42() {
    assertNoConversion(
        " int a, b;      a = 3;      b = 5;      if (a == 4)        if (b == 3)          b = 2;        else{          b = a;          b=3;     else       if (b == 3)         b = 2;       else{         b = a*a;         b=3; ");
  }
  @Test public void ternarize45() {
    assertNoConversion("if (mode.equals(f())==true)    if (b==3){     return 3;     return 7;   else    if (b==3){     return 2;     a=7; ");
  }
  @Test public void ternarize46() {
    assertConvertsTo(
        "   int a , b=0;\n" + "   if (mode.equals(NG)==true)\n" + "     if (b==3){\n" + "       return 3;\n" + "     } else {\n" + "       a+=7;\n" + "     }\n" + "   else\n"
            + "     if (b==3){\n" + "       return 2;\n" + "     } else {\n" + "       a=7;\n" + "     }",
        "int a,b=0;if(mode.equals(NG)!=true)if(b==3){return 2;}else{a=7;}else if(b==3){return 3;}else{a+=7;}");
  }
  @Test public void ternarize48() {
    assertNoConversion(" int size = 0, a, b;   if (mode.equals(f())==true)    for (int i=0; i < size; i++){     a+=7;     b=2;   else    for (int i=0; i < size; i++){     a+=8; ");
  }
  @Test public void ternarize49() {
    assertNoConversion("if (s.equals(532)){    System.out.println(gG);    System.out.append(kKz); ");
  }
  @Test public void ternarize49a() {
    assertConvertsTo(
        ""//
            + "    int size = 0;\n"//
            + "   if (mode.equals(153)==true)\n"//
            + "     for (int i=0; i < size; i++){\n"//
            + "       System.out.println(HH);\n"//
            + "     }\n"//
            + "   else\n"//
            + "     for (int i=0; i < size; i++){\n"//
            + "       System.out.append('f');\n"//
            + "     }",
        ""//
            + "int size=0;"//
            + "if(mode.equals(153))"//
            + "for(int i=0;i<size;++i){"//
            + "  System.out.println(HH);"//
            + "} "//
            + "else "//
            + "  for(int i=0;i<size;++i){"//
            + "    System.out.append('f');" + "  }");
  }
  @Test public void ternarize52() {
    assertNoConversion("int a=0,b = 0,c,d = 0,e = 0;if (a < b) {    c = d;c = e;");
  }
  @Test public void ternarize53() {
    assertConvertsTo(//
        "int $, xi=0, xj=0, yi=0, yj=0;   if (xi > xj == yi > yj)    $++;   else    $--;", //
        "int $, xi=0, xj=0, yi=0, yj=0;   if (xi > xj == yi > yj)    ++$;   else    --$;"//
    );
  }
  @Test public void ternarize55() {
    assertConvertsTo(//
        "if (key.equals(markColumn))\n" + //
            " to.put(key, a.toString());\n" + //
            "else\n" + //
            "  to.put(key, missing(key, a) ? Z2 : get(key, a));", //
        "to.put(key,key.equals(markColumn)?a.toString():missing(key,a)?Z2:get(key,a));");
  }
  @Test public void ternarize56() {
    assertConvertsTo(
        "if (target == 0) {progressBarCurrent.setString(X); progressBarCurrent.setValue(0); progressBarCurrent.setString(current + \"/\"+ target); progressBarCurrent.setValue(current * 100 / target);", //
        "if(target==0){progressBarCurrent.setString(X);progressBarCurrent.setValue(0);progressBarCurrent.setString(current+\"/\"+target);progressBarCurrent.setValue(100*current / target);");
  }
  @Test public void ternaryPushdownOfReciever() {
    assertSimplifiesTo("a ? b.f():c.f()", "(a?b:c).f()");
  }
  @Test public void testPeel() {
    final String example = "on * notion * of * no * nothion != the * plain + kludge";
    assertEquals(example, Wrap.Expression.off(Wrap.Expression.on(example)));
  }
  @Test public void twoMultiplication1() {
    assertSimplifiesTo("f(a,b,c,d) * f()", "f() * f(a,b,c,d)");
  }
  @Test public void twoOpportunityExample() {
    final String example = "on * notion * of * no * nothion != the * plain + kludge";
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(Wrap.Expression.on(example));
    assertThat(countOpportunities(new Trimmer(), u), is(2));
  }
  @Test public void useOutcontextToManageStringAmbiguity() {
    assertSimplifiesTo("1+2+s<3", "s+1+2<3");
  }
  @Test public void vanillaShortestFirstConditionalNoChange() {
    assertNoChange("literal ? CONDITIONAL_OR : CONDITIONAL_AND");
  }
  @Test public void xorSortClassConstantsAtEnd() {
    assertNoChange("f(a,b,c,d) ^ BOB");
  }
  @Test public void postfixToPrefixAvoidChangeOnVariableDeclaration() {
    // We expect to print 2, but ++s will make it print 3
    assertNoConversion(//
    "int s = 2;" + //
    "int n = s++;" + //
    "System.out.print(n);" //
    );
  }
}
