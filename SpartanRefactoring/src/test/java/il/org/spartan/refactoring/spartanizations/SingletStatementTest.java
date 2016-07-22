package il.org.spartan.refactoring.spartanizations;

import static il.org.spartan.azzert.*;
import static il.org.spartan.refactoring.utils.Into.*;
import static il.org.spartan.refactoring.utils.Restructure.*;
import il.org.spartan.*;
import il.org.spartan.refactoring.utils.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;
import org.junit.runners.*;

@SuppressWarnings({ "javadoc", "static-method" })//
@FixMethodOrder(MethodSorters.NAME_ASCENDING)//
public class SingletStatementTest {
  @Test public void declarationAndStatementIsNull() {
    azzert.that(singleStatement(s("{int a; a();}")), nullValue());
  }
  @Test public void deeplyNestedOneInCurlyIsNull() {
    azzert.that(singleStatement(s("{{{{a();}}}}")), not(nullValue()));
  }
  @Test public void emptyBlockIsNull() {
    azzert.that(singleStatement(s("{}")), nullValue());
  }
  @Test public void emptyStatementInBlockIsNull() {
    azzert.that(singleStatement(s("{;}")), nullValue());
  }
  @Test public void emptyStatementIsNull() {
    azzert.that(singleStatement(s(";")), nullValue());
  }
  @Test public void fiveIsCorrectSize() {
    azzert.that(singleStatement(s("{{a();b();}{a(); b(); {}{}{{}} c();}}")), nullValue());
  }
  @Test public void manyEmptyStatementInBlockIsNull() {
    azzert.that(singleStatement(s("{;};{;;{;;}};")), nullValue());
  }
  @Test public void manyIsNull() {
    azzert.that(singleStatement(s("a(); b(); c();")), nullValue());
  }
  @Test public void nestedTwoIsCorrectSize() {
    azzert.that(singleStatement(s("{a();b();}")), nullValue());
  }
  @Test public void nullGivesNull() {
    azzert.that(singleStatement(null), nullValue());
  }
  @Test public void oneInCurlyIsNotNull() {
    azzert.that(singleStatement(s("{a();}")), notNullValue());
  }
  @Test public void oneIsNotNull() {
    azzert.that(singleStatement(s("{a();}")), notNullValue());
  }
  @Test public void peelIf() {
    final ASTNode n = ast.STATEMENTS.from("{if (a) return b; else return c;}");
    azzert.that(n, notNullValue());
    final List<Statement> ss = extract.statements(n);
    azzert.that(ss, notNullValue());
    azzert.that(ss.size(), is(1));
    azzert.that(extract.singleStatement(n), notNullValue());
  }
  @Test public void peelIPlusPlus() {
    final ASTNode n = ast.STATEMENTS.from("{i++;}");
    azzert.that(n, notNullValue());
    final List<Statement> ss = extract.statements(n);
    azzert.that(ss, notNullValue());
    azzert.that(ss.size(), is(1));
    azzert.that(extract.singleStatement(n), notNullValue());
  }
  @Test public void twoFunctionCallsNullValue() {
    azzert.that(singleStatement(s("{b(); a();}")), nullValue());
  }
  @Test public void twoInCurlyIsNull() {
    azzert.that(singleStatement(s("{a();b();}")), nullValue());
  }
  @Test public void twoNullValue() {
    azzert.that(singleStatement(s("a();b();")), nullValue());
  }
}
