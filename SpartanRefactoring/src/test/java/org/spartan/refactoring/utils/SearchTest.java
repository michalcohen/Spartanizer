package org.spartan.refactoring.utils;

import static org.spartan.hamcrest.CoreMatchers.notNullValue;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.utils.Into.*;

import java.util.List;

import org.junit.Test;
import org.spartan.refactoring.utils.Search.Searcher;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.refactoring.utils.Funcs.asSimpleName;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

@SuppressWarnings({ "static-method", "javadoc" }) public class SearchTest {
  private final SimpleName n = asSimpleName(e("n"));
  @Test public void asFunction() {
    assertThat(Search.forUses(n).in(s("b = n();")).size(), is(0));
  }
  @Test public void asInstanceof1() {
    assertThat(Search.forUses(n).in(s("b = n instanceof x;")).size(), is(1));
  }
  @Test public void asInstanceof2() {
    assertThat(Search.forUses(n).in(s("b = x instanceof n;")).size(), is(0));
  }
  @Test public void awfulOriginal() {
    assertThat(Search.forUses(n)
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
  @Test public void awful() {
    assertThat(Search.forUses(n)
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
  @Test public void awfulWithClassAfter() {
    assertThat(Search.forUses(n)
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
    assertThat(Search.forUses(n)
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
  @Test public void awfulClassDestroyingFurtherUses() {
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "    }\n" + //
            "    return n; // 1\n" + //
            "  }"))
        .size(), is(1));
  }
  @Test public void awfulVariantWithClass() {
    assertThat(Search.forUses(n)
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
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awfulShort() {
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(3));
  }
  @Test public void awfulShortWithParameter() {
    assertThat(Search.forUses(n)
        .in(d("Object n(int n) {\n" + //
            "    if (n != n) return n;\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulNestedClass() {
    assertThat(Search.forUses(n)
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
    assertThat(Search.forUses(n)
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
  @Test public void awful1() {
    assertThat(Search.forUses(n)
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
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(2));
  }
  @Test public void awful3() {
    assertThat(Search.forUses(n)
        .in(d("Object f() {\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(1));
  }
  @Test public void awfulA() {
    assertThat(Search.forUses(n)
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
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulC() {
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulD() {
    assertThat(Search.forUses(n)
        .in(d("Object a() {\n" + //
            "    class n {\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(0));
  }
  @Test public void awfulE() {
    assertThat(Search.forUses(n).in(d("Object n() {\n" + //
        "  }")).size(), is(0));
  }
  @Test public void awfulF() {
    assertThat(Search.forUses(n).in(d("Object a() {\n" + //
        "  }")).size(), is(0));
  }
  @Test public void declarationVoidsUse() {
    assertThat(Search.forUses(n).in(s("final A n = n * 2; a = n;")).size(), is(0));
  }
  @Test public void declarationVoidsUseA() {
    assertThat(Search.forUses(n).in(s("final A n = n * 2;")).size(), is(0));
  }
  @Test public void declarationVoidsUseB() {
    assertThat(Search.forUses(n).in(s("final A n = 2; a = n;")).size(), is(0));
  }
  @Test public void declarationVoidsUseC() {
    assertThat(Search.forUses(n).in(s("final A n = 2;")).size(), is(0));
  }
  @Test public void definedUntilEndOfBlock() {
    assertThat(Search.forUses(n).in(s("a = n; { int n; a = n * n + n;} a = n;")).size(), is(2));
  }
  @Test public void definedUntilEndOfBlockA() {
    assertThat(Search.forUses(n).in(s("a = n; { int n; a = n;}")).size(), is(1));
  }
  @Test public void fieldAccess1() {
    assertThat(Search.forUses(n).in(s("x = n.a;")).size(), is(1));
  }
  @Test public void fieldAccess2() {
    assertThat(Search.forUses(n).in(s("x = a.n;")).size(), is(0));
  }
  @Test public void inForLoop() {
    assertThat(Search.forUses(n).in(s("for (int a = n; a < n; a++);")).size(), is(2));
  }
  @Test public void inForLoop0() {
    assertThat(Search.forUses(n).in(s("for (int a = 2; a < 2; a++);")).size(), is(0));
  }
  @Test public void inForLoop1() {
    assertThat(Search.forUses(n).in(s("for (int a = n; a < 2; a++);")).size(), is(1));
  }
  @Test public void inForLoop1A() {
    assertThat(Search.forUses(n).in(s("for (int a = n * n + n; a < 2; a++);")).size(), is(3));
  }
  @Test public void inForLoop2() {
    assertThat(Search.forUses(n).in(s("for (int a = 1; a < n; a=1);")).size(), is(1));
  }
  @Test public void inForLoop3() {
    assertThat(Search.forUses(n).in(s("for (int a = 1; a < 2; a=n);")).size(), is(1));
  }
  @Test public void inForLoop4() {
    assertThat(Search.forUses(n).in(s("for (int a = 1; a < 2; a++) a=n;")).size(), is(1));
  }
  @Test public void inForLoop5() {
    assertThat(Search.forUses(n).in(s("for (int a = 1; a < 2; a++) a=2; a = n;")).size(), is(1));
  }
  @Test public void inForLoop6() {
    assertThat(Search.forUses(n).in(s("int a = n; for (int a = 1; a < 2; a++) a=2; a = 1;")).size(), is(1));
  }
  @Test public void inForLoop7() {
    assertThat(Search.forUses(n).in(s("int a = 2; for (int a = 1; a < 2; a++) { a=2; } a = n;")).size(), is(1));
  }
  @Test public void inEnhancedForLoop() {
    assertThat(Search.forUses(n).in(s("for (int n:ns) a= n;")).size(), is(0));
  }
  @Test public void minusMinus() {
    assertThat(Search.forUses(n).in(s("n--;")).size(), is(0));
  }
  @Test public void minusMinusPre() {
    assertThat(Search.forUses(n).in(s("--n;")).size(), is(0));
  }
  @Test public void plusPlus() {
    assertThat(Search.forUses(n).in(s("n++;")).size(), is(0));
  }
  @Test public void plusPlusPre() {
    assertThat(Search.forUses(n).in(s("++n;")).size(), is(0));
  }
  @Test public void usedAsType() {
    assertThat(Search.forUses(n).in(s("n n;")).size(), is(0));
  }
  @Test public void usedAsType1() {
    assertThat(Search.forUses(n).in(s("n a;")).size(), is(0));
  }
  @Test public void usedAsType2() {
    assertThat(Search.forUses(n).in(s("final n n = new n(n);")).size(), is(0));
  }
  @Test public void vanilla() {
    final Searcher findUses = Search.forUses(n);
    assertThat(findUses, notNullValue());
    final List<Expression> in = findUses.in(s("b = n;"));
    assertThat(in.size(), is(1));
  }
  @Test public void vanillaShortVersion() {
    assertThat(Search.forUses(n).in(s("b = n;")).size(), is(1));
  }
}
