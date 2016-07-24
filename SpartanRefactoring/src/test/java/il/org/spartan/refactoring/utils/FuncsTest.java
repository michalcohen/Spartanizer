package il.org.spartan.refactoring.utils;

import static il.org.spartan.azzert.*;
import static il.org.spartan.refactoring.utils.ExpressionComparator.*;
import static il.org.spartan.refactoring.utils.Funcs.*;
import static il.org.spartan.refactoring.utils.Into.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.mockito.Mockito.*;
import il.org.spartan.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;
import org.junit.runners.*;

/** A test suite for class {@link Funcs}
 *
 * @author Yossi Gil
 * @since 2015-07-18
 * @see Funcs */
@SuppressWarnings({ "static-method", "javadoc" }) @FixMethodOrder(MethodSorters.NAME_ASCENDING)//
public class FuncsTest {
  @Test public void arrayOfInts() {
    azzert.that(shortName(t("int[][] _;")), equalTo("iss"));
  }
  @Test public void asComparisonPrefixlExpression() {
    final PrefixExpression p = mock(PrefixExpression.class);
    doReturn(PrefixExpression.Operator.NOT).when(p).getOperator();
    azzert.isNull(asComparison(p));
  }
  @Test public void asComparisonTypicalExpression() {
    final InfixExpression i = mock(InfixExpression.class);
    doReturn(GREATER).when(i).getOperator();
    azzert.that(asComparison(i), notNullValue());
  }
  @Test public void asComparisonTypicalExpressionFalse() {
    final InfixExpression i = mock(InfixExpression.class);
    doReturn(CONDITIONAL_OR).when(i).getOperator();
    azzert.isNull(asComparison(i));
  }
  @Test public void asComparisonTypicalInfixFalse() {
    final InfixExpression i = mock(InfixExpression.class);
    doReturn(CONDITIONAL_AND).when(i).getOperator();
    azzert.isNull(asComparison(i));
  }
  @Test public void asComparisonTypicalInfixIsCorrect() {
    final InfixExpression i = mock(InfixExpression.class);
    doReturn(GREATER).when(i).getOperator();
    azzert.that(asComparison(i), is(i));
  }
  @Test public void asComparisonTypicalInfixIsNotNull() {
    final InfixExpression e = mock(InfixExpression.class);
    doReturn(GREATER).when(e).getOperator();
    azzert.that(asComparison(e), notNullValue());
  }
  @Test public void chainComparison() {
    azzert.that(right(i("a == true == b == c")).toString(), is("c"));
  }
  @Test public void countNonWhiteCharacters() {
    azzert.that(countNonWhites(e("1 + 23     *456 + \n /* aa */ 7890")), is(13));
  }
  @Test public void findFirstType() {
    azzert.that(t("int _;"), notNullValue());
  }
  @Test public void isDeMorganAND() {
    azzert.that(Is.deMorgan(CONDITIONAL_AND), is(true));
  }
  @Test public void isDeMorganGreater() {
    azzert.that(Is.deMorgan(GREATER), is(false));
  }
  @Test public void isDeMorganGreaterEuals() {
    azzert.that(Is.deMorgan(GREATER_EQUALS), is(false));
  }
  @Test public void isDeMorganOR() {
    azzert.that(Is.deMorgan(CONDITIONAL_OR), is(true));
  }
  @Test public void listOfInts() {
    azzert.that(shortName(t("List<Set<Integer>> _;")), equalTo("iss"));
  }
  @Test public void negation0Trivial() {
    azzert.that(negationLevel(e("a")), is(0));
  }
  @Test public void negation1Trivial() {
    azzert.that(negationLevel(e("-a")), is(1));
  }
  @Test public void negationOfExpressionManyNegation() {
    azzert.that(negationLevel(e("- - - - (- (-a))")), is(6));
  }
  @Test public void negationOfExpressionNoNegation() {
    azzert.that(negationLevel(e("((((4))))")), is(0));
  }
  @Test public void sameOfNullAndSomething() {
    azzert.that(Funcs.same(null, e("a")), is(false));
  }
  @Test public void sameOfNulls() {
    azzert.that(Funcs.same((ASTNode) null, (ASTNode) null), is(true));
  }
  @Test public void sameOfSomethingAndNull() {
    azzert.that(Funcs.same(e("a"), null), is(false));
  }
  @Test public void sameOfTwoExpressionsIdentical() {
    azzert.that(Funcs.same(e("a+b"), e("a+b")), is(true));
  }
  @Test public void sameOfTwoExpressionsNotSame() {
    azzert.that(Funcs.same(e("a+b+c"), e("a+b")), is(false));
  }
  @Test public void shortNameASTRewriter() {
    azzert.that(shortName(t("ASTRewriter _;")), equalTo("r"));
  }
  @Test public void shortNameDouble() {
    azzert.that(shortName(t("double _;")), equalTo("d"));
  }
  @Test public void shortNameExpression() {
    azzert.that(shortName(t("Expression _;")), equalTo("e"));
  }
  @Test public void shortNameInfrastructure() {
    azzert.that(shortName(t("int _;")), equalTo("i"));
  }
  @Test public void shortNameQualifiedType() {
    azzert.that(shortName(t("org.eclipse.jdt.core.dom.InfixExpression _;")), equalTo("e"));
  }
  private Type t(final String codeFragment) {
    return extract.firstType(s(codeFragment));
  }
}
