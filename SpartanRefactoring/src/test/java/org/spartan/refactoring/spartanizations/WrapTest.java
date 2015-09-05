package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import org.eclipse.jface.text.Document;
import org.junit.Test;
import org.spartan.refactoring.spartanizations.Wrap;
import static org.hamcrest.CoreMatchers.is;

import org.eclipse.jdt.core.dom.CompilationUnit;

@SuppressWarnings({ "static-method", "javadoc" }) public class WrapTest {
  @Test public void method() {
    final Wrap w = Wrap.Method;
    final String input = "int f() { return a; }";
    assertThat(w.off(w.on(input)), is(input));
  }
  @Test public void statement() {
    final Wrap w = Wrap.Statement;
    final String input = "int a;";
    assertThat(w.off(w.on(input)), is(input));
  }
  @Test public void expression() {
    final Wrap w = Wrap.Expression;
    final String input = "a+b";
    assertThat(w.off(w.on(input)), is(input));
  }
  @Test public void intoCompilationUnit() {
    final Wrap w = Wrap.Expression;
    final String input = "a + b * c";
    final CompilationUnit u = w.intoCompilationUnit(input);
    assertThat(u, notNullValue());
    assertThat(w.off(u.toString()), containsString(input));
  }
  @Test public void intoDocument() {
    final Wrap w = Wrap.Expression;
    final String input = "a + b * c";
    final Document d = w.intoDocument(input);
    assertThat(d, notNullValue());
    assertThat(w.off(d.get()), containsString(input));
  }
  @Test public void findStatement() {
    assertThat(Wrap.find("for(;;);"), is(Wrap.Statement));
  }
  @Test public void findTwoStatements() {
    assertThat(Wrap.find("a(); b();"), is(Wrap.Statement));
  }
  @Test public void findExpression() {
    assertThat(Wrap.find("i++"), is(Wrap.Expression));
  }
  @Test public void findMethod() {
    assertThat(Wrap.find("f() { a(); b();}"), is(Wrap.Method));
  }
  @Test public void findError() {
    assertThat(Wrap.find("}} f() { a();} b();}"), is(nullValue()));
  }
}
