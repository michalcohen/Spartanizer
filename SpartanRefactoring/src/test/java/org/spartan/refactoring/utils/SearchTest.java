package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.utils.Funcs.asSimpleName;
import static org.spartan.refactoring.utils.Into.d;
import static org.spartan.refactoring.utils.Into.e;
import static org.spartan.refactoring.utils.Into.s;

import org.eclipse.jdt.core.dom.SimpleName;
import org.junit.Test;
import org.spartan.refactoring.utils.Search.Searcher;

@SuppressWarnings({ "javadoc" }) public class SearchTest {
  private final SimpleName n = asSimpleName(e("n"));
  @Test public void asFunction() {
    assertThat(Search.forAllOccurencesOf(n).in(s("b = n();")).size(), is(0));
  }
  @Test public void asInstanceof1() {
    assertThat(Search.forAllOccurencesOf(n).in(s("b = n instanceof x;")).size(), is(1));
  }
  @Test public void asInstanceof2() {
    assertThat(Search.forAllOccurencesOf(n).in(s("b = x instanceof n;")).size(), is(0));
  }
  @Test public void awful() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "    if (n != n) return n;\n" + //
            "    final n n = new n();\n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awful1() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    final int n = null; \n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awful2() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(2));
  }
  @Test public void awful3() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object f() {\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(1));
  }
  @Test public void awfulA() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n(Object n) {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulB() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulC() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulClassDestroyingFurtherUses() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "    }\n" + //
            "    return n; // 1\n" + //
            "  }"))
        .size(), is(1));
  }
  @Test public void awfulD() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object a() {\n" + //
            "    class n {\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulE() {
    assertThat(Search.forAllOccurencesOf(n).in(d("Object n() {\n" + //
        "  }")).size(), is(0));
  }
  @Test public void awfulF() {
    assertThat(Search.forAllOccurencesOf(n).in(d("Object a() {\n" + //
        "  }")).size(), is(0));
  }
  @Test public void awfulNestedClass() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulNestedClassVariableAfter() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "      n n;;\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulOriginal() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "    final n n = new n();\n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulShort() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awfulShortWithParameter() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n(int n) {\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulVariantWithClass() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awfulVariantWithoutClass() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awfulWithClassAfter() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    if (n != n) return n;\n" + //
            "    final n n = new n();\n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awfulWithClassAfterNoRedefinition() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("Object n() {\n" + //
            "    if (n != n) return n; // 3\n" + //
            "    if (n instanceof n) // 1\n" + //
            "      new Object();\n" + //
            "    n(); // 0\n " + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "    return n; // 1\n" + //
            "  }"))
        .size(), is(5));
  }
  @Test public void constructorCall() {
    assertThat(Search.forAllOccurencesOf(n).in(e("new n(this)\n")).size(), is(0));
  }
  @Test public void declarationVoidsUse() {
    assertThat(Search.forAllOccurencesOf(n).in(s("final A n = n * 2; a = n;")).size(), is(0));
  }
  @Test public void declarationVoidsUseA() {
    assertThat(Search.forAllOccurencesOf(n).in(s("final A n = n * 2;")).size(), is(0));
  }
  @Test public void declarationVoidsUseB() {
    assertThat(Search.forAllOccurencesOf(n).in(s("final A n = 2; a = n;")).size(), is(0));
  }
  @Test public void declarationVoidsUseC() {
    assertThat(Search.forAllOccurencesOf(n).in(s("final A n = 2;")).size(), is(0));
  }
  @Test public void definedUntilEndOfBlock() {
    assertThat(Search.forAllOccurencesOf(n).in(s("a = n; { int n; a = n * n + n;} a = n;")).size(), is(2));
  }
  @Test public void definedUntilEndOfBlockA() {
    assertThat(Search.forAllOccurencesOf(n).in(s("a = n; { int n; a = n;}")).size(), is(1));
  }
  @Test public void fieldAccess1() {
    assertThat(Search.forAllOccurencesOf(n).in(s("x = n.a;")).size(), is(1));
  }
  @Test public void fieldAccess2() {
    assertThat(Search.forAllOccurencesOf(n).in(s("x = a.n;")).size(), is(0));
  }
  @Test public void fieldAccessDummy() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("" + //
            "  public int y() {\n" + //
            "    final Z res = new Z(6);\n" + //
            "    S.out.println(res.j);\n" + //
            "    return res;\n" + //
            "  }\n" + //
            "}\n" + //
            ""))
        .size(), is(0));
  }
  @Test public void fieldAccessReal() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(d("" + //
            "  public int y() {\n" + //
            "    final Z n = new Z(6);\n" + //
            "    S.out.println(n.j);\n" + //
            "    return n;\n" + //
            "  }\n" + //
            "}\n" + //
            ""))
        .size(), is(0));
  }
  @Test public void fieldAccessSimplified() {
    assertThat(Search.forAllOccurencesOf(n)
        .in(s("" + //
            "    S.out.println(n.j);\n" + //
            ""))
        .size(), is(1));
  }
  @Test public void inEnhancedForLoop() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int n:ns) a= n;")).size(), is(0));
  }
  @Test public void inForLoop() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = n; a < n; a++);")).size(), is(2));
  }
  @Test public void inForLoop0() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = 2; a < 2; a++);")).size(), is(0));
  }
  @Test public void inForLoop1() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = n; a < 2; a++);")).size(), is(1));
  }
  @Test public void inForLoop1A() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = n * n + n; a < 2; a++);")).size(), is(3));
  }
  @Test public void inForLoop2() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = 1; a < n; a=1);")).size(), is(1));
  }
  @Test public void inForLoop3() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = 1; a < 2; a=n);")).size(), is(1));
  }
  @Test public void inForLoop4() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = 1; a < 2; a++) a=n;")).size(), is(1));
  }
  @Test public void inForLoop5() {
    assertThat(Search.forAllOccurencesOf(n).in(s("for (int a = 1; a < 2; a++) a=2; a = n;")).size(), is(1));
  }
  @Test public void inForLoop6() {
    assertThat(Search.forAllOccurencesOf(n).in(s("int a = n; for (int a = 1; a < 2; a++) a=2; a = 1;")).size(), is(1));
  }
  @Test public void inForLoop7() {
    assertThat(Search.forAllOccurencesOf(n).in(s("int a = 2; for (int a = 1; a < 2; a++) { a=2; } a = n;")).size(), is(1));
  }
  @Test public void minusMinus() {
    assertThat(Search.forAllOccurencesExcludingDefinitions(n).in(s("n--;")).size(), is(0));
  }
  @Test public void minusMinusPre() {
    assertThat(Search.forAllOccurencesExcludingDefinitions(n).in(s("--n;")).size(), is(0));
  }
  @Test public void plusPlus() {
    assertThat(Search.forAllOccurencesExcludingDefinitions(n).in(s("n++;")).size(), is(0));
  }
  @Test public void plusPlusPre() {
    assertThat(Search.forAllOccurencesExcludingDefinitions(n).in(s("++n;")).size(), is(0));
  }
  @Test public void superMethodInocation() {
    assertThat(Search.forAllOccurencesOf(n).in(e("super.n(this)\n")).size(), is(0));
  }
  @Test public void usedAsType() {
    assertThat(Search.forAllOccurencesOf(n).in(s("n n;")).size(), is(0));
  }
  @Test public void usedAsType1() {
    assertThat(Search.forAllOccurencesOf(n).in(s("n a;")).size(), is(0));
  }
  @Test public void usedAsType2() {
    assertThat(Search.forAllOccurencesOf(n).in(s("final n n = new n(n);")).size(), is(0));
  }
  @Test public void vanilla() {
    final Searcher findUses = Search.forAllOccurencesOf(n);
    assertThat(findUses, notNullValue());
    assertThat(findUses.in(s("b = n;")).size(), is(1));
  }
  @Test public void vanillaShortVersion() {
    assertThat(Search.forAllOccurencesOf(n).in(s("b = n;")).size(), is(1));
  }
}
