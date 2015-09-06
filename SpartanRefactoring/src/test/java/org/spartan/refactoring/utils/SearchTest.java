package org.spartan.refactoring.utils;

import static org.spartan.hamcrest.CoreMatchers.notNullValue;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.utils.Into.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.spartan.refactoring.utils.Search.Searcher;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.refactoring.utils.Funcs.asSimpleName;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

@SuppressWarnings({ "static-method", "javadoc" }) public class SearchTest {
  @Test public void vanilla() {
    final SimpleName n = asSimpleName(e("n"));
    final Searcher findUses = Search.forUses(n);
    assertThat(findUses, notNullValue());
    final List<Expression> in = findUses.in(s("b = n;"));
    assertThat(in.size(), is(1));
  }
  @Test public void vanillaShortVersion() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("b = n;")).size(), is(1));
  }
  @Test public void asFunction() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("b = n();")).size(), is(0));
  }
  @Test public void asInstanceof1() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("b = n instanceof x;")).size(), is(1));
  }
  @Test public void asInstanceof2() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("b = x instanceof n;")).size(), is(0));
  }
  @Test public void plusPlus() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("n++;")).size(), is(0));
  }
  @Test public void plusPlusPre() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("++n;")).size(), is(0));
  }
  @Test public void minusMinus() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("n--;")).size(), is(0));
  }
  @Test public void minusMinusPre() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("--n;")).size(), is(0));
  }
  @Test public void fieldAccess1() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("x = n.a;")).size(), is(1));
  }
  @Test public void fieldAccess2() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("x = a.n;")).size(), is(0));
  }
  @Test public void inLoop() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("for (int a = n; a < n; a++);")).size(), is(2));
  }
  @Test public void usedAsType() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("n n;")).size(), is(0));
  }
  @Test public void usedAsType1() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("n a;")).size(), is(0));
  }
  @Test public void usedAsType2() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n).in(s("final n n = new n(n);")).size(), is(1));
  }
  @Ignore @Test public void awful() {
    final SimpleName n = asSimpleName(e("n"));
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
        .size(), is(2));
  }
  @Test public void awfulA() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "      Object n() {\n" + //
            "        return n;\n" + //
            "      }\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(4));
  }
  @Test public void awfulB() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    class n {\n" + //
            "      n n;;\n" + //
            "    }\n" + //
            "  }"))
        .size(), is(2));
  }
  @Test public void awful1() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n)
        .in(d("Object n() {\n" + //
            "    final n = null; \n" + //
            "    if (n instanceof n)\n" + //
            "      new Object();\n" + //
            "    n();\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(1));
  }
  @Test public void awful2() {
    final SimpleName n = asSimpleName(e("n"));
    assertThat(Search.forUses(n)
        .in(d("Object f() {\n" + //
            "    return n;\n" + //
            "  }"))
        .size(), is(1));
  }
}
