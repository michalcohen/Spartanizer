package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.spartanizations.TESTUtils.s;
import static org.spartan.refactoring.utils.Restructure.singleStatement;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class SingletStatementTest {
  @Test public void nullGivesNull() {
    assertThat(singleStatement(null), nullValue());
  }
  @Test public void twoFunctionCallsNullValue() {
    assertThat(singleStatement(s("{b(); a();}")), nullValue());
  }
  @Test public void declarationAndStatementIsNull() {
    assertThat(singleStatement(s("{int a; a();}")), nullValue());
  }
  @Test public void deeplyNestedOneInCurlyIsNull() {
    assertThat(singleStatement(s("{{{{a();}}}}")), not(nullValue()));
  }
  @Test public void emptyBlockIsNull() {
    assertThat(singleStatement(s("{}")), nullValue());
  }
  @Test public void emptyStatementInBlockIsNull() {
    assertThat(singleStatement(s("{;}")), nullValue());
  }
  @Test public void emptyStatementIsNull() {
    assertThat(singleStatement(s(";")), nullValue());
  }
  @Test public void manyEmptyStatementInBlockIsNull() {
    assertThat(singleStatement(s("{;};{;;{;;}};")), nullValue());
  }
  @Test public void manyIsNull() {
    assertThat(singleStatement(s("a(); b(); c();")), nullValue());
  }
  @Test public void oneInCurlyIsNotNull() {
    assertThat(singleStatement(s("{a();}")), notNullValue());
  }
  @Test public void oneIsNotNull() {
    assertThat(singleStatement(s("{a();}")), notNullValue());
  }
  @Test public void twoInCurlyIsNull() {
    assertThat(singleStatement(s("{a();b();}")), nullValue());
  }
  @Test public void twoNullValue() {
    assertThat(singleStatement(s("a();b();")), nullValue());
  }
  @Test public void nestedTwoIsCorrectSize() {
    assertThat(singleStatement(s("{a();b();}")), nullValue());
  }
  @Test public void fiveIsCorrectSize() {
    assertThat(singleStatement(s("{{a();b();}{a(); b(); {}{}{{}} c();}}")), nullValue());
  }
}
