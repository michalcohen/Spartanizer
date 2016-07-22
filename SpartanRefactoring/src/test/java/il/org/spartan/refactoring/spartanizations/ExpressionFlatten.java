package il.org.spartan.refactoring.spartanizations;

import static il.org.spartan.azzert.*;
import static il.org.spartan.refactoring.utils.Funcs.*;
import static il.org.spartan.refactoring.utils.Into.*;
import static il.org.spartan.refactoring.utils.Restructure.*;
import il.org.spartan.*;
import il.org.spartan.refactoring.utils.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;
import org.junit.runners.*;

@SuppressWarnings({ "javadoc", "static-method" })//
@FixMethodOrder(MethodSorters.NAME_ASCENDING)//
public class ExpressionFlatten {
  @Test public void flattenExists() {
    flatten(i("1+2"));
  }
  @Test public void flattenIsDistinct() {
    final InfixExpression e = i("1+2");
    azzert.that(flatten(e), is(not(e)));
  }
  @Test public void flattenIsNotNull() {
    azzert.that(flatten(i("1+2")), is(not(nullValue())));
  }
  @Test public void flattenIsSame() {
    final InfixExpression e = i("1+2");
    azzert.that(flatten(e).toString(), is(e.toString()));
  }
  @Test public void flattenLeftArgument() {
    azzert.that(left(flatten(i("1+2"))).toString(), is("1"));
  }
  @Test public void flattenOfDeepParenthesisIsCorrect() {
    azzert.that(flatten(i("(((1+2)))+(((3 + (4+5))))")).toString(), is("1 + 2 + 3+ 4+ 5"));
  }
  @Test public void flattenOfDeepParenthesisSize() {
    azzert.that(flatten(i("(1+(2))+(3)")).extendedOperands().size(), is(1));
  }
  @Test public void flattenOfDeepParenthesOtherOperatorsisIsCorrect() {
    azzert.that(flatten(i("(((1+2)))+(((3 + (4*5))))")).toString(), is("1 + 2 + 3+ 4 * 5"));
  }
  @Test public void flattenOfParenthesis() {
    azzert.that(flatten(i("1+2+(3)")).extendedOperands().size(), is(1));
  }
  @Test public void flattenOfTrivialDoesNotAddOperands() {
    azzert.that(i("1+2").extendedOperands().size(), is(0));
  }
  @Test public void hasExtendedOperands() {
    azzert.that(i("1+2").hasExtendedOperands(), is(false));
  }
  @Test public void isNotStringInfixFalse() {
    azzert.that(Is.notString(i("1+f")), is(false));
  }
  @Test public void isNotStringInfixPlain() {
    azzert.that(Is.notString(e("1+f")), is(false));
  }
  @Test public void leftOperandIsNotString() {
    azzert.that(Is.notString(left(i("1+2"))), is(true));
  }
  @Test public void leftOperandIsNumeric() {
    azzert.that(Is.numericLiteral(left(i("1+2"))), is(true));
  }
  @Test public void leftOperandIsOne() {
    azzert.that(left(i("1+2")).toString(), is("1"));
  }
  @Test public void leftOperandNotNull() {
    azzert.that(left(i("1+2")), not(nullValue()));
  }
  @Test public void rightOperandNotNull() {
    azzert.that(left(i("1+2")), not(nullValue()));
  }
}