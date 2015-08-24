package org.spartan.refactoring.wring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.spartanizations.TESTUtils;
import org.spartan.refactoring.spartanizations.Wrap;
import org.spartan.refactoring.utils.As;

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
public class FixedPointTest {
  private static String apply(final Trimmer t, final String from) {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(from);
    assertNotNull(u);
    final Document d = new Document(from);
    assertNotNull(d);
    return TESTUtils.rewrite(t, u, d).get();
  }
  private static void assertConvertsTo(final String from, final String expected) {
    assertWrappedTranslation(from, expected, Wrap.Statement);
  }
  private static void assertNoChange(final String input) {
    assertSimilar(input, Wrap.Expression.off(apply(new Trimmer(), Wrap.Expression.on(input))));
  }
  private static void assertSimplifiesTo(final String from, final String expected) {
    assertWrappedTranslation(from, expected, Wrap.Expression);
  }
  private static void assertWrappedTranslation(final String from, final String expected, final Wrap w) {
    final String wrap = w.on(from);
    assertEquals(from, w.off(wrap));
    final String unpeeled = Trimmer.fixedPoint(wrap);
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + from);
    final String peeled = w.off(unpeeled);
    if (peeled.equals(from))
      assertNotEquals("No similification of " + from, from, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Simpification of " + from + "is just reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
  }
  @Test public void bug0() {
    assertConvertsTo(//
        "int a, b;a = 3;b = 5;if (a == 4)  if (b == 3)          b = 2;else          b = a;else if (b == 3)         b = 2;else         b = a*a;",
        "int a=3,b;b=5;if(a==4)b=b==3?2:a;else b=b==3?2:a*a;");
  }
  @Test public void bug1() {
    assertConvertsTo("int a=0, b=0;if (b==3)   a=4;", " int a = (b==3 ? 4 : 0), b=0; ");
  }
  @Test public void chainComparison() {
    assertSimplifiesTo("a == true == b == c", "a == b == c");
  }
  @Test public void desiredSimplificationOfExample() {
    assertSimplifiesTo("on * notion * of * no * nothion < the * plain + kludge", "no*of*on*notion*nothion<kludge+the*plain");
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
  @Test public void shorterChainParenthesisComparison() {
    assertNoChange("a == b == c");
  }
  @Test public void shorterChainParenthesisComparisonLast() {
    assertNoChange("b == a * b * c * d * e * f * g * h == a");
  }
  @Test public void shortestIfBranchFirst02() {
    assertConvertsTo(
        "" //
            + "if (!s.equals(0xDEAD)) { "//
            + " int res=0;"//
            + " for (int i=0;i<s.length();++i)     "//
            + "if (s.charAt(i)=='a')      "//
            + "     res += 2;"//
            + "   else "//
            + "  if (s.charAt(i)=='d')      "//
            + "       res -= 1;"//
            + "  return res;"//
            + "} else {    "//
            + " return 8;"//
            + "}",
        "" //
            + " if (!s.equals(0xDEAD)) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0;i < s.length();++i)\n" + //
            "  if (s.charAt(i) == 'a')\n" + //
            "          res += 2;\n" + //
            "        else if (s.charAt(i) == 'd')\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    }\n" + //
            "    return 8;"//
    );
  }
  @Test public void shortestIfBranchFirst03() {
    assertConvertsTo(
        "  if (s.length() > 6) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0;i < s.length();i++)\n" + //
            "  if ('a' == s.charAt(i))\n" + //
            "          res += 2;\n" + //
            "        else if ('d' == s.charAt(i))\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    } else\n" + //
            "      return 8;", //
        "  if (s.length() > 6) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0;i < s.length();++i)\n" + //
            "  if (s.charAt(i) == 'a')\n" + //
            "          res += 2;\n" + //
            "        else if (s.charAt(i) == 'd')\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    }\n" + //
            "    return 8;");
  }
  @Test public void shortestIfBranchFirst03a() {
    assertConvertsTo(
        "  if ('a' == s.charAt(i))\n" + //
            "          res += 2;\n" + //
            "        else if ('d' == s.charAt(i))\n" + //
            "          res -= 1;\n" + //
            "", //
        "  if (s.charAt(i) == 'a')\n" + //
            "          res += 2;\n" + //
            "        else if (s.charAt(i) == 'd')\n" + //
            "          res -= 1;\n" + //
            "");
  }
  @Test public void shortestIfBranchFirst03b() {
    assertConvertsTo(
        "    if (s.length() > 6) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0; i < s.length(); ++i)\n" + //
            "        if ('a' == s.charAt(i))\n" + //
            "          res += 2;\n" + //
            "        else if ('d' == s.charAt(i))\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    }\n" + "    return 8;", //
        "  if (s.length() > 6) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0;i < s.length();++i)\n" + //
            "  if (s.charAt(i) == 'a')\n" + //
            "          res += 2;\n" + //
            "        else if (s.charAt(i) == 'd')\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    }\n" + //
            "    return 8;");
  }
  @Test public void shortestIfBranchFirst04() {
    assertConvertsTo(
        "    String s;\n" + //
            "    if (s.length() >= 6) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0; i < s.length(); i++)\n" + //
            "        if (s.charAt(i) == 'a')\n" + //
            "          res += 2;\n" + //
            "        else if (s.charAt(i) == 'd')\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    } else {\n" + //
            "      return 8;\n" + //
            "    }",
        "     String s;\n" + //
            "    if (s.length() >= 6) {\n" + //
            "      int res = 0;\n" + //
            "      for (int i = 0; i < s.length(); ++i)\n" + //
            "        if (s.charAt(i) == 'a')\n" + //
            "          res += 2;\n" + //
            "        else if (s.charAt(i) == 'd')\n" + //
            "          res -= 1;\n" + //
            "      return res;\n" + //
            "    }\n" + //
            "    return 8;");
  }
  @Test public void shortestIfBranchFirst09() {
    assertSimplifiesTo("s.equals(532) ? 9 * yada3(s.length()) : 6 ", "!s.equals(532)?6:9*yada3(s.length())");
  }
  @Test public void shortestIfBranchFirst10() {
    assertConvertsTo(
        "      for (final String s : contents.split(\"\\n\"))\n" + "  if (!foundPackage && s.contains(Strings.JAVA_PACKAGE)) {\n" + //
            "          $.append(s.replace(\";\", \".\" + folderName + \";\") + \"\\n\" + imports);\n" + //
            "          foundPackage = true;\n" + //
            "        } else\n" + //
            "          $.append(replaceClassName(s, className, newClassName) + \"\\n\");\n" + //
            "      return asString($);",
        "    for (final String s : contents.split(\"\\n\"))\n" + "      if (foundPackage || !s.contains(Strings.JAVA_PACKAGE))\n"
            + "        $.append(replaceClassName(s, className, newClassName) + \"\\n\");\n" + "      else {\n"
            + "        $.append(s.replace(\";\", \".\" + folderName + \";\") + \"\\n\" + imports);\n" + "        foundPackage = true;\n" + "      }\n" + "    return asString($);");
  }
  @Test public void shortestIfBranchFirst11() {
    assertSimplifiesTo("b != null && b.getNodeType() == ASTNode.BLOCK ? getBlockSingleStmnt((Block) b) : b ",
        "b==null||b.getNodeType()!=ASTNode.BLOCK?b:getBlockSingleStmnt((Block)b)");
  }
  @Test public void shortestIfBranchFirst12() {
    assertConvertsTo("if (FF() && TT()){    foo1();foo2();}else shorterFoo();", //
        "  if (!FF() || !TT())     shorterFoo();else {foo1();foo2();}");
  }
  @Test public void shortestIfBranchFirst13() {
    assertConvertsTo(
        "    int a = 0;\n" + "    if (a > 0)\n" + "      return 6;\n" + "    else {\n" + "      int b = 9;\n" + "      b *= b;\n" + "      return b;\n" + "    }\n" + "    ;",
        "int a=0;if(a>0)return 6;int b=9;b*=b;return b;");
  }
  @Test public void shortestIfBranchFirst14() {
    assertConvertsTo("    int a = 0;\n" + //
        "    if (a > 0) {\n" + //
        "      int b = 9;\n" + //
        "      b *= b;\n" + //
        "      return 6;\n" + //
        "    } else {\n" + //
        "      a = 5;\n" + //
        "      return b;\n" + //
        "    }", "int a=0;if(a>0){int b=9;b*=b;return 6;}a=5;return b;");
  }
  @Test public void shortestOperand01() {
    assertNoChange("x + y > z");
  }
  @Test public void shortestOperand03() {
    assertConvertsTo(//
        "k = k * 4;if (1 + 2 - 3 - 4 + 5 / 6 - 7 + 8 * 9 > 4+k) return true;", //
        "k=4*k;if(5 / 6+8*9+1+2-3-4-7>k+4)return true;");
  }
  @Test public void shortestOperand04() {
    assertConvertsTo("return (1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1);", //
        "return(1+2<3&4+7>1+2||6-7<1+2);");
  }
  @Test public void shortestOperand06() {
    assertConvertsTo("int a,b,c;String t = \"eureka!\";if (2 * 3.1415 * 180 > a || t.concat(\"<!>\") == \"1984\" && t.length() > 3)    return c > 5;",
        "int a,b,c;String t=\"eureka!\";if(2*180*3.1415>a||t.concat(\"<!>\")==\"1984\"&&t.length()>3)return c>5;");
  }
  @Test public void shortestOperand07() {
    assertConvertsTo("int y,o,g,i,s;return ( y + o + s > s + i |  g > 42);", "int y,o,g,i,s;return(g>42|o+s+y>i+s);");
  }
  @Test public void shortestOperand08() {
    assertConvertsTo(//
        "if (bob.father.age > 42 && bob.mother.father.age > bob.age ) return true; else return false;", "return bob.father.age>42&&bob.mother.father.age>bob.age;");
  }
  @Test public void shortestOperand09() {
    assertNoChange("return 2 - 4 < 50 - 20 - 10 - 5;} ");
  }
  @Test public void shortestOperand10() {
    assertNoChange("return b == true;} ");
  }
  @Test public void shortestOperand11() {
    assertNoChange("int h,u,m,a,n;return b == true && n + a > m - u || h > u;");
  }
  @Test public void shortestOperand26() {
    assertConvertsTo("return f(a,b,c,d) | f() | 0;} ", "return f()|f(a,b,c,d)|0;}");
  }
  @Test public void shortestOperand35() {
    assertConvertsTo("return f(a,b,c,d) * moshe; ", "   return moshe * f(a,b,c,d);");
  }
  @Test public void sortAddition() {
    assertConvertsTo(//
        "return 1 + 2 - 3 - 4 + 5 / 6 - 7 + 8 * 9 > k + 4);", //
        "return 8*9+1+2-3-4+5 / 6-7>k+4;");
  }
  @Test public void sortAddition5() {
    assertSimplifiesTo("1 + 2  + 3 + a < 3 -4", "a + 1 + 2 + 3 < 3-4");
  }
  @Test public void ternarize01() {
    assertConvertsTo(//
        "String res = s;if (s.equals(532)==true)    res = s + 0xABBA;else    res = SPAM;System.out.println(res);",
        "String res=s;res=!s.equals(532)?SPAM:s+0xABBA;System.out.println(res);");
  }
  @Test public void ternarize02() {
    assertConvertsTo(//
        "String res = s;if (s.equals(532)==true)    res = s + 0xABBA;System.out.println(res);", "String res=!s.equals(532)?s:s+0xABBA;System.out.println(res);");
  }
  @Test public void ternarize03() {
    assertConvertsTo(//
        "if (s.equals(532))    return 6;return 9;", //
        " return s.equals(532) ? 6 : 9; ");
  }
  @Test public void ternarize04() {
    assertConvertsTo("  int res = 0;if (s.equals(532))    res += 6;else    res += 9;/*if (s.equals(532))    res += 6;else    res += 9;*/   return res;",
        "int res=0;res+=s.equals(532)?6:9;return res;");
  }
  @Test public void ternarize06() {
    assertConvertsTo(//
        "String res;res = s;if (s.equals(532)==true)    res = s + 0xABBA;System.out.println(res);", //
        "String res=!s.equals(532)?s:s+0xABBA;System.out.println(res);");
  }
  @Test public void ternarize11() {
    assertConvertsTo("String res = s, foo = \"bar\";if (s.equals(532)==true)    res = s + 0xABBA;System.out.println(res);",
        "String res=!s.equals(532)?s:s+0xABBA,foo=\"bar\";System.out.println(res);");
  }
  @Test public void ternarize15() {
    assertConvertsTo("  String res = mode, foo = \"Not in test mode\";int k;k = 1984;if (mode.equals(f())==true)    foo = test-bob;foo = \"sponge-bob\";",
        "String res=mode,foo=\"Not in test mode\";int k=1984;if(mode.equals(f()))foo=test-bob;foo=\"sponge-bob\";");
  }
  @Test public void ternarize17() {
    assertConvertsTo("    int a, b;\n" + //
        "    a = 3;\n" + //
        "    b = 5;\n" + //
        "    if (a == 4)\n" + //
        "      if (b == 3)\n" + //
        "        b = r();\n" + //
        "      else\n" + //
        "        b = a;\n" + //
        "    else if (b == 3)\n" + //
        "      b = r();\n" + //
        "    else\n" + //
        "      b = a;", "int a=3,b=5;b=b!=3?a:r();");
  }
  @Test public void ternarize18() {
    assertConvertsTo(//
        "    final String s;\n" + //
            "    final String res = s;\n" + //
            "    final int a = 0;\n" + //
            "    if (s.equals(res))\n" + //
            "      System.out.println(\"hey\" + res);\n" + //
            "    else\n" + //
            "      System.out.println(\"ho\" + res + a);",
        "final String s;final String res=s;final int a=0;System.out.println(s.equals(res)?\"hey\"+res:\"ho\"+res+a);");
  }
  @Test public void bug() {
    assertConvertsTo("if (s.equals(532))    System.out.close();else    System.out.close();", " System.out.close();} ");
  }

  @Test public void ternarize23() {
    assertConvertsTo(//
        "int a=0;if (s.equals(532))   a+=y(2)+10;else a+=r(3)-6;",
        "int a=0;a+=s.equals(532)?y(2)+10:r(3)-6;");
  }
  @Test public void ternarize24() {
    assertConvertsTo(//
        "boolean c;if (s.equals(532))    c=false;else c=true;", //
        "boolean c=!s.equals(532);");
  }





  @Test public void ternarize38() {
    assertNoChange("int a, b=0;if (b==3){    a+=2+r();a-=6;");
  }
  @Test public void ternarize40() {
    assertConvertsTo(//
        "int a, b, c;a = 3;b = 5;if (a == 4)     while (b == 3)     c = a;else    while (b == 3)     c = a*a;",//
        "int a=3,b=5,c;if(a==4)while(b==3)c=a;else while(b==3)c=a*a;");
  }

  @Test public void ternarize54() {
    assertConvertsTo(//
        "if (s == null)\n" + ///
            "  return Z2;\n" + //
            "if (!s.contains(delimiter()))\n" + //
            "  return s;\n" + //
            "return s.replaceAll(delimiter(), ABC + delimiter());", //
        "return s==null?Z2:!s.contains(delimiter())?s:s.replaceAll(delimiter(),ABC+delimiter());");
  }
}
