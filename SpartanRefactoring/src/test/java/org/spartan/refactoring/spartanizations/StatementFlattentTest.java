package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.spartanizations.TESTUtils.s;
import static org.spartan.refactoring.utils.Restructure.statements;

import org.eclipse.jdt.core.dom.Statement;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class StatementFlattentTest {
  @Test public void isNotNullOfNull() {
    assertThat(statements(null), is(notNullValue()));
  }
  @Test public void isEmptyOfNull() {
    assertThat(statements(null), empty());
  }
  @Test public void isNotNullOfValidStatement() {
    final Statement s = s("{}");
    assertThat(statements(s), is(notNullValue()));
  }
  @Test public void twoFunctionCallsIsCorrectSize() {
    assertThat(statements(s("{b(); a();}")).size(), is(2));
  }
  @Test public void declarationCorrectSize() {
    assertThat(statements(s("{int a; a();}")).size(), is(2));
  }
  @Test public void declarationIsNotEmpty() {
    assertThat(statements(s("{int a; a();}")), not(empty()));
  }
  @Test public void deeplyNestedOneInCurlyIsNotEmpty() {
    assertThat(statements(s("{{{{a();}}}}")), not(empty()));
  }
  @Test public void emptyBlockIsEmpty() {
    assertThat(statements(s("{}")), empty());
  }
  @Test public void emptyStatementInBlockIsEmpty() {
    assertThat(statements(s("{;}")), empty());
  }
  @Test public void emptyStatementIsEmpty() {
    assertThat(statements(s(";")), empty());
  }
  @Test public void manyEmptyStatementInBlockIsEmpty() {
    assertThat(statements(s("{;};{;;{;;}};")), empty());
  }
  @Test public void manyIsNotEmpty() {
    assertThat(statements(s("a(); b(); c();")), not(empty()));
  }
  @Test public void oneInCurlyIsNotEmpty() {
    assertThat(statements(s("{a();}")), not(empty()));
  }
  @Test public void oneIsNotEmpty() {
    assertThat(statements(s("{a();}")), not(empty()));
  }
  @Test public void twoInCurlyIsNotEmpty() {
    assertThat(statements(s("{a();b();}")), not(empty()));
  }
  @Test public void twoIsNotEmpty() {
    assertThat(statements(s("a();b();")), not(empty()));
  }
  @Test public void twoIsCorrectSize() {
    assertThat(statements(s("a();b();")).size(), is(2));
  }
  @Test public void nestedTwoIsCorrectSize() {
    assertThat(statements(s("{a();b();}")).size(), is(2));
  }
  @Test public void fiveIsCorrectSize() {
    assertThat(statements(s("{{a();b();}{a(); b(); {}{}{{}} c();}}")).size(), is(5));
  }
}
