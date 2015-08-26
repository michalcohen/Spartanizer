package org.spartan.refactoring.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class JavaTypeNameParserTest {
  @Test public void httpSecureConnection() {
    assertThat(new JavaTypeNameParser("HTTPSecureConnection").shortName(), is("c"));
  }
  @Test public void infixExpression() {
    assertThat(new JavaTypeNameParser("InfixExpression").shortName(), is("e"));
  }
  @Test public void astNode() {
    assertThat(new JavaTypeNameParser("ASTNode").shortName(), is("n"));
  }
}
