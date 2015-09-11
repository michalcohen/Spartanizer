package org.spartan.refactoring.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class JavaTypeNameParserTest {
  @Test public void alphaNumericMid() {
    assertThat(new JavaTypeNameParser("Base64Parser", false).shortName(), is("p"));
  }
  @Test public void alphaNumericPost() {
    assertThat(new JavaTypeNameParser("Int32", false).shortName(), is("i"));
  }
  @Test public void astNode() {
    assertThat(new JavaTypeNameParser("ASTNode", false).shortName(), is("n"));
  }
  @Test public void httpSecureConnection() {
    assertThat(new JavaTypeNameParser("HTTPSecureConnection", false).shortName(), is("c"));
  }
  @Test public void infixExpression() {
    assertThat(new JavaTypeNameParser("InfixExpression", false).shortName(), is("e"));
  }
  @Test public void jUnit() {
    assertThat(new JavaTypeNameParser("JUnit", false).shortName(), is("u"));
  }
  @Test public void onlyLowerCase() {
    assertThat(new JavaTypeNameParser("onlylowercase", false).shortName(), is("o"));
  }
  @Test public void onlyUpperCase() {
    assertThat(new JavaTypeNameParser("ONLYUPPERCASE", false).shortName(), is("o"));
  }
  @Test public void singleChar() {
    assertThat(new JavaTypeNameParser("Q", false).shortName(), is("q"));
  }
  @Test public void collection() {
    assertThat(new JavaTypeNameParser("List<Expression>", true).shortName(), is("es"));
  }
}
