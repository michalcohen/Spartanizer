package org.spartan.refactoring.spartanizations;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.spartan.hamcrest.CoreMatchers.*;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.spartanizations.TESTUtils.s;
import static org.spartan.refactoring.utils.Restructure.flatten;

import org.eclipse.jdt.core.dom.Statement;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class StatementFlattentTest {
  @Test public void flattenIsNotNull() {
    final Statement s = s("{}");
    assertThat(flatten(s), is(notNullValue()));
  }
  @Test public void flattenOfTwoFunctionCallsIsCorrectSize() {
    assertThat(flatten(s("{b(); a();}")).size(), is(2));
  }
  @Test public void flattenOfDeclarationCorrectSize() {
    assertThat(flatten(s("{int a; a();}")).size(), is(2));
  }
  @Test public void flattenOfDeclarationIsNotEmpty() {
    assertThat(flatten(s("{int a; a();}")), not(empty()));
  }
  @Test public void flattenOfDeeplyNestedOneInCurlyIsNotEmpty() {
    assertThat(flatten(s("{{{{a();}}}}")), not(empty()));
  }
  @Test public void flattenOfEmptyBlockIsEmpty() {
    assertThat(flatten(s("{}")), empty());
  }
  @Test public void flattenOfEmptyStatementInBlockIsEmpty() {
    assertThat(flatten(s("{;}")), empty());
  }
  @Test public void flattenOfEmptyStatementIsEmpty() {
    assertThat(flatten(s(";")), empty());
  }
  @Test public void flattenOfManyEmptyStatementInBlockIsEmpty() {
    assertThat(flatten(s("{;};{;;{;;}};")), empty());
  }
  @Test public void flattenOfManyIsNotEmpty() {
    assertThat(flatten(s("a(); b(); c();")), not(empty()));
  }
  @Test public void flattenOfOneInCurlyIsNotEmpty() {
    assertThat(flatten(s("{a();}")), not(empty()));
  }
  @Test public void flattenOfOneIsNotEmpty() {
    assertThat(flatten(s("{a();}")), not(empty()));
  }
  @Test public void flattenOfTwoInCurlyIsNotEmpty() {
    assertThat(flatten(s("{a();b();}")), not(empty()));
  }
  @Test public void flattenOfTwoIsNotEmpty() {
    assertThat(flatten(s("a();b();")), not(empty()));
  }
  @Test public void flattenOfTwoIsCorrectSize() {
    assertThat(flatten(s("a();b();")).size(), is(2));
  }
  @Test public void flattenOfNestedTwoIsCorrectSize() {
    assertThat(flatten(s("{a();b();}")).size(), is(2));
  }
  @Test public void flattenOfFiveIsCorrectSize() {
    assertThat(flatten(s("{{a();b();}{a(); b(); {}{}{{}} c();}}")).size(), is(5));
  }
}