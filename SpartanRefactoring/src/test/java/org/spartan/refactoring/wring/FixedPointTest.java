package org.spartan.refactoring.wring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.wring.TrimmerTest.assertConvertsTo;
import static org.spartan.refactoring.wring.TrimmerTest.assertNoChange;
import static org.spartan.refactoring.wring.TrimmerTest.assertSimplifiesTo;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.spartanizations.Wrap;

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
            "      for (int i = 0;i < s.length();++i)\n" + //
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
            "      for (int i = 0; i < s.length(); i++)\n" + //
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
        "   (!(b != null) || !(b.getNodeType() == ASTNode.BLOCK) ? b : getBlockSingleStmnt((Block) b)) ");
  }
  @Test public void shortestIfBranchFirst12() {
    assertConvertsTo("if (FF() && TT()){    foo1();foo2();shorterFoo();", //
        "  if (!FF() || !TT()) {     shorterFoo();foo1();foo2();");
  }
  @Test public void shortestIfBranchFirst13() {
    assertConvertsTo("int a=0;if (a > 0)    return 6;else {    int b=9;b*=b;return b;", "");
  }
  @Test public void shortestIfBranchFirst14() {
    assertConvertsTo("int a=0;if (a > 0){    int b=9;b*=b;return 6;a = 5;return b;", "  int a=0;if (a <= 0){    a = 5;return b;int b=9;b*=b;return 6;");
  }
  @Test public void shortestOperand01() {
    assertNoChange("x + y > z");
  }
  @Test public void shortestOperand03() {
    assertConvertsTo(//
        "k = k * 4;if (1 + 2 - 3 - 4 + 5 / 6 - 7 + 8 * 9 > k + 4) return true;", //
        "k=4*k;if(8*9+1+2-3-4+5 / 6-7>k+4)return true;");
  }
  @Test public void shortestOperand04() {
    assertConvertsTo("return (1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1);", //
        "return(1+2<3&4+7>1+2||6-7<1+2);");
  }
  @Test public void shortestOperand05() {
    assertConvertsTo(//
        "  StringBuilder s = \"bob\";return s.append(\"-ha\").append(\"-ba\").toString() == \"bob-ha-banai\";",
        "  StringBuilder s = \"bob\";return \"bob-ha-banai\" == s.append(\"-ha\").append(\"-ba\").toString();");
  }
  @Test public void shortestOperand06() {
    assertConvertsTo("int a,b,c;String t = \"eureka!\";if (2 * 3.1415 * 180 > a || t.concat(\"<!>\") == \"1984\" && t.length() > 3)    return c > 5;",
        "int a,b,c;String t=\"eureka!\";if(2*180*3.1415>a||t.concat(\"<!>\")==\"1984\"&&t.length()>3)return c>5;");
  }
  @Test public void shortestOperand07() {
    assertConvertsTo("int y,o,g,i,s;return ( y + o + s > s + i |  g > 42);", " int y,o,g,i,s;return ( g > 42 | y + o + s > s + i);");
  }
  @Test public void shortestOperand08() {
    assertSimplifiesTo("  human father;human mother;int age;public boolean squarePants() {   human bob;if (bob.father.age > 42 && bob.mother.father.age > bob.age ) return true;",
        "  human father;human mother;int age;public boolean squarePants() {   human bob;if (bob.father.age ? 42 && bob.age < bob.mother.father.age ) return true;");
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
  @Test public void shortestOperand17() {
    assertConvertsTo("SomeClass a;return a.getNum() ^ 5;", " SomeClass a;return 5 ^ a.getNum();");
  }
  @Test public void shortestOperand18() {
    assertConvertsTo("SomeClass a;return k.get().parent() & a;", " SomeClass a;return a & k.get().parent();");
  }
  @Test public void shortestOperand21() {
    assertConvertsTo("return f(a, b, c, d, e) + 3333 + 222 + a + spongeBob + f(g, c, d) + f(a) + tt;", "    return a+tt+222+3333+spongeBob+f(a,b,c,d,e)+f(g,c,d)+f(a);");
  }
  @Test public void shortestOperand24() {
    assertConvertsTo("return f(a,b,c,d) & 175 & 0;} ", "   return 0 & 175 & f(a,b,c,d);}");
  }
  @Test public void shortestOperand25() {
    assertConvertsTo("return f(a,b,c,d) & bob & 0;} ", "   return 0 & bob & f(a,b,c,d);}");
  }
  @Test public void shortestOperand26() {
    assertConvertsTo("return f(a,b,c,d) | f() | 0;} ", "return f()|f(a,b,c,d)|0;}");
  }
  @Test public void shortestOperand29() {
    assertSimplifiesTo("return f(a,b,c,d) ^ f() ^ 0;} ", "   return 0 ^ f(a,b,c,d) ^ f();}");
  }
  @Test public void shortestOperand34() {
    assertConvertsTo("return f(a,b,c,d) + someVar; ", "   return someVar + f(a,b,c,d); ");
  }
  @Test public void shortestOperand35() {
    assertConvertsTo("return f(a,b,c,d) * moshe; ", "   return moshe * f(a,b,c,d);");
  }
  @Test public void shortestOperand36() {
    assertSimplifiesTo("f(a,b,c,d) ^ bob", "bob ^ f(a,b,c,d)");
  }
  @Test public void sortAddition5() {
    assertSimplifiesTo("1 + 2  + 3 + a < 3 -4", "a + 1 + 2 + 3 < 3-4");
  }
  @Test public void ternarize01() {
    assertConvertsTo(//
        "String res = s;if (s.equals(532)==true)    res = s + 0xABBA;else    res = SPAM;System.out.println(res);",
        "String res = (s.equals(532)==true ? s + 0xABBA : SPAM);System.out.println(res);");
  }
  @Test public void ternarize02() {
    assertConvertsTo(//
        "String res = s;if (s.equals(532)==true)    res = s + 0xABBA;System.out.println(res);", "String res = s.equals(532)ge ? s + 0xABBA : s);System.out.println(res);");
  }
  @Test public void ternarize03() {
    assertConvertsTo(//
        "if (s.equals(532))    return 6;return 9;", //
        " return s.equals(532) ? 6 : 9); ");
  }
  @Test public void ternarize04() {
    assertConvertsTo("  int res = 0;if (s.equals(532))    res += 6;else    res += 9;/*if (s.equals(532))    res += 6;else    res += 9;*/   return res;",
        "  int res = 0;res += (s.equals(532) ? 6 : 9);/*if (s.equals(532))    res += 6;else    res += 9;*/   return res;");
  }
  @Test public void ternarize06() {
    assertConvertsTo(//
        "String res;res = s;if (s.equals(532)==true)    res = s + 0xABBA;System.out.println(res);", "String res = (s.equals(532) ? s + 0xABBA : s);System.out.println(res);");
  }
  @Test public void ternarize08() {
    assertConvertsTo("  int res = 0;if (s.equals(532)) {    res += 6;else {    res += 9;/*if (s.equals(532))    res += 6;else    res += 9;*/   return res;",
        "  int res = 0;res += (s.equals(532) ? 6 : 9);/*if (s.equals(532))    res += 6;else    res += 9;*/   return res;");
  }
  @Test public void ternarize09() {
    assertConvertsTo("if (s.equals(532)) {    return 6;else {    return 9;", " return (s.equals(532) ? 6 : 9);} ");
  }
  @Test public void ternarize11() {
    assertConvertsTo("String res = s, foo = \"bar\";if (s.equals(532)==true)    res = s + 0xABBA;System.out.println(res);",
        "  String res = (s.equals(532)==true ? s + 0xABBA : s), foo = \"bar\";System.out.println(res);");
  }
  @Test public void ternarize15() {
    assertConvertsTo("  String res = mode, foo = \"Not in test mode\";int k;k = 1984;if (mode.equals(f())==true)    foo = \"test-bob\";foo = \"sponge-bob\";",
        "  String res = mode, foo = \"Not in test mode\";int k;k = 1984;foo = \"sponge-bob\";");
  }
  @Test public void ternarize17() {
    assertConvertsTo(
        "  return 6;}  public int y(){   return 4;public void yada(final String s) {      int a, b;a = 3;b = 5;if (a == 4)  if (b == 3)          b = r();else          b = a;else if (b == 3)         b = y();else         b = a;",
        "  return 6;}  public int y(){   return 4;public void yada(final String s) {      int a, b;a = 3;b = 5;if (b == 3)          b = (a == 4 ? r() : y());else          b = a;");
  }
  @Test public void ternarize18() {
    assertConvertsTo(//
        "String res = s;int a=0;if (s.equals(532))    System.out.println(\"hey\" + res);else    System.out.println(\"ho\" + res + a);",
        "  String res = s;int a=0;System.out.println((s.equals(532) ? \"hey\" + res : \"ho\" + res + a));");
  }
  @Test public void ternarize19() {
    assertSimplifiesTo("if (s.equals(532))    System.out.close();else    System.out.close();", " System.out.close();} ");
  }
  @Test public void ternarize20() {
    assertConvertsTo("return 0;}  public int y(int b){   return 1;public int yada(final String s) {if (s.equals(532)){    return 2 + r(2);return 3 + f(4);",
        "  return 0;}  public int y(int b){   return 1;public int yada(final String s) {   return (s.equals(532) ? 2 + r(2) : 3 + f(4));");
  }
  @Test public void ternarize23() {
    assertSimplifiesTo("return 0;}  public int y(int b){   return 1;public void yada(final String s) {   int a=0;if (s.equals(532)){    a+=y(2)+10;a+=r(3)-6;",
        "  return 0;}  public int y(int b){   return 1;public void yada(final String s) {   int a=0;a+=(s.equals(532) ? y(2)+10 : r(3)-6);");
  }
  @Test public void ternarize24() {
    assertConvertsTo("boolean c;if (s.equals(532)){    c=false;c=true;", " boolean c = !(s.equals(532));} ");
  }
  @Test public void ternarize25() {
    assertConvertsTo(" int a, b=0, c=0;if (b==3){    a+=2+r();c=6;a+=6;}", "");
  }
  @Test public void ternarize27() {
    assertConvertsTo("if (s.equals(532)){    a=4;a=3;", " int a=0;int b=0;a=3;");
  }
  @Test public void ternarize28() {
    assertConvertsTo("int a=0;a=5;if (s.equals(532)){    a=4;a=3;", " int a=3;} ");
  }
  @Test public void ternarize30() {
    assertConvertsTo("int a=0, b=0;a=5;if (b==3){    a=a+4;}", " int a=0, b=0;a = (b==3 ? a+4 : 5);");
  }
  @Test public void ternarize30a() {
    assertConvertsTo("if (b==3){ a=a+4;}", " int a=0, b=0;a = (b==3 ? a+4 : 5);");
  }
  @Test public void ternarize31() {
    assertConvertsTo("int a=0;a=5;if (a==3)  a=a+4;a=7;", "int a=7;");
  }
  @Test public void ternarize32() {
    assertConvertsTo("int a=0, b=0;if (b==3)   a=4;", " int a = (b==3 ? 4 : 0), b=0; ");
  }
  @Test public void ternarize34() {
    assertConvertsTo("int b=0;if (b==3){    return true;return false;", " int b=0;return b==3;");
  }
  @Test public void ternarize37() {
    assertConvertsTo("return 2;}  public void yada(final String s) {   int a, b=0, c=0;if (b==3){    a+=2+r()+c;a+=6;",
        "  return 2;}  public void yada(final String s) {   int a, b=0, c=0;a += (b==3 ? 2+r()+c + 6 : 6);");
  }
  @Test public void ternarize38() {
    assertNoChange("int a, b=0;if (b==3){    a+=2+r();a-=6;");
  }
  @Test public void bug0() {
    assertConvertsTo(//
        "int a, b;a = 3;b = 5;if (a == 4)  if (b == 3)          b = 2;else          b = a;else if (b == 3)         b = 2;else         b = a*a;",
        "int a=3,b;b=5;if(a==4)b=b==3?2:a;else b=b==3?2:a*a;");
  }
  @Test public void ternarize40() {
    assertConvertsTo(//
        "int a, b, c;a = 3;b = 5;if (a == 4)     while (b == 3)     c = a;else    while (b == 3)     c = a*a;", "int a=3,b,c;b=5;if(a==4)while(b==3)c=a;else while(b==3)c=a*a;");
  }
  @Test public void ternarize43() {
    assertSimplifiesTo("if (mode.equals(f())==true) if (b==3){     return 3;b=5;else if (b==3){     return 2;b=4;",
        "  if (b==3){    return (mode.equals(f())==true ? 3 : 2);b=(mode.equals(f())==true ? 5 : 4);");
  }
  @Test public void ternarize44() {
    assertConvertsTo(//
        "if (mode.equals(f())==true) if (b==3){     return 3;return 7;else if (b==3){     return 2;return 7;", "if (b==3){    return (mode.equals(f())==true ? 3 : 2);return 7;");
  }
  @Test public void ternarize47() {
    assertConvertsTo(//
        "int size = 0, a;if (mode.equals(f())==true)    for (int i=0;i < size;i++){     a+=7;else    for (int i=0;i < size;i++){     a+=8;",
        "int size = 0, a;for (int i=0;i < size;i++){    a+=(mode.equals(f())==true ? 7 : 8);");
  }
  @Test public void ternarize50() {
    assertConvertsTo(
        "  int size = 0;if (mode.equals(f())==true)    for (int i=0;i < size;i++){     System.out.println(\"Hey\");else    for (int i=0;i < size;i++){     System.out.println(\"Ho\");",
        "  int size = 0;for (int i=0;i < size;i++){    System.out.println((mode.equals(f())==true ? \"Hey\" : \"Ho\"));");
  }
  @Test public void ternarize51() {
    assertConvertsTo("int a=0,b = 0,d = 0,e = 0,c;if (a < b) {    c = d;c = e;", " int a=0,b = 0,d = 0,e = 0,c = (a < b ? d : e);} ");
  }
  @Test public void ternarize52() {
    assertConvertsTo("int a=0,b = 0,c,d = 0,e = 0;if (a < b) {    c = d;c = e;", "int a=0,b = 0,c,d = 0,e = 0;c = (a < b ? d : e);");
  }
  @Test public void ternarize54() {
    assertConvertsTo(//
        "if (s == null)\n" + ///
            "  return Z2;\n" + //
            "if (!s.contains(delimiter()))\n" + //
            "  return s;\n" + //
            "return s.replaceAll(delimiter(), ABC + delimiter());", //
        "if (s == null)\n" + //
            "  return Z2;\n" + //
            "return  !s.contains(delimiter()) ? s : s.replaceAll(delimiter(), ABC + delimiter()));"//
    );
  }
}
