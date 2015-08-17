package org.spartan.refactoring.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.spartan.refactoring.utils.Into.e;

import org.junit.Test;

/**
 * Test class for class {@link Is}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
public class IsTest {
  @Test public void booleanLiteralFalseOnNull() {
    assertFalse(Is.booleanLiteral(e("null")));
  }
  @Test public void booleanLiteralFalseOnNumeric() {
    assertFalse(Is.booleanLiteral(e("12")));
  }
  @Test public void booleanLiteralFalseOnThis() {
    assertFalse(Is.booleanLiteral(e("this")));
  }
  @Test public void booleanLiteralTrueOnFalse() {
    assertTrue(Is.booleanLiteral(e("false")));
  }
  @Test public void booleanLiteralTrueOnTrue() {
    assertTrue(Is.booleanLiteral(e("true")));
  }
  @Test public void nonnAssociative() {
    assertFalse(Is.nonAssociative(e("1")));
    assertFalse(Is.nonAssociative(e("-1")));
    assertFalse(Is.nonAssociative(e("-1+2")));
    assertFalse(Is.nonAssociative(e("1+2")));
    assertTrue(Is.nonAssociative(e("2-1")));
    assertTrue(Is.nonAssociative(e("2/1")));
    assertTrue(Is.nonAssociative(e("2%1")));
    assertFalse(Is.nonAssociative(e("2*1")));
  }
  @Test public void numericLiteralFalse1() {
    assertFalse(Is.numericLiteral(e("2*3")));
  }
  @Test public void numericLiteralFalse2() {
    assertFalse(Is.numericLiteral(e("2*3")));
  }
  @Test public void numericLiteralTrue() {
    assertTrue(Is.numericLiteral(e("1")));
  }
  @Test public void sideEffectArray1() {
    assertTrue(Is.sideEffectFree(e("new a[3]")));
  }
  @Test public void sideEffectArray2() {
    assertTrue(Is.sideEffectFree(e("new int[] {12,13}")));
  }
  @Test public void sideEffectArray3() {
    assertFalse(Is.sideEffectFree(e("new int[] {12,13, i++}")));
  }
  @Test public void sideEffectArray4() {
    assertFalse(Is.sideEffectFree(e("new int[f()]")));
  }
  @Test public void sideEffectConditional1() {
    assertTrue(Is.sideEffectFree(e("3 + 4 < 10 ? 12 : 14")));
  }
  @Test public void sideEffectConditional2() {
    assertFalse(Is.sideEffectFree(e("3 + 4 < 10 + ++i ? 12 : 14")));
  }
  @Test public void sideEffectConditional3() {
    assertFalse(Is.sideEffectFree(e("3 + 4 < 10 + a? 12 * f(): 14")));
  }
  @Test public void sideEffectConditional4() {
    assertFalse(Is.sideEffectFree(e("3 + 4 < 10 ? 12 * 2: 14 * f()")));
  }
  @Test public void sideEffectFreeAddition() {
    assertFalse(Is.sideEffectFree(e("f() + 2")));
    assertFalse(Is.sideEffectFree(e("2 + f() + 2")));
    assertFalse(Is.sideEffectFree(e("3 + true + f() + 2")));
  }
  @Test public void sideEffectFreeCasetFalsee() {
    assertFalse(Is.sideEffectFree(e("(A) f()")));
  }
  @Test public void sideEffectFreeCasetTrue() {
    assertTrue(Is.sideEffectFree(e("(A) b")));
  }
  @Test public void sideEffectFreeExists() {
    Is.sideEffectFree(e("null"));
  }
  @Test public void sideEffectFreeLArraSFunctionIndex() {
    assertFalse(Is.sideEffectFree(e("a[i()]")));
  }
  @Test public void sideEffectFreeLArrayFunctionArray() {
    assertFalse(Is.sideEffectFree(e("a()[i]")));
  }
  @Test public void sideEffectFreeLArraySimple() {
    assertTrue(Is.sideEffectFree(e("a[i*3]")));
  }
  @Test public void sideEffectFreeLFieldAccess() {
    assertTrue(Is.sideEffectFree(e("this.f")));
  }
  @Test public void sideEffectFreeLiteralBoolean() {
    assertTrue(Is.sideEffectFree(e("true")));
  }
  @Test public void sideEffectFreeLiteralCharacter() {
    assertTrue(Is.sideEffectFree(e("'a'")));
  }
  @Test public void sideEffectFreeLMethodAccess() {
    assertFalse(Is.sideEffectFree(e("this.f()")));
  }
  @Test public void sideEffectFreeLQualifiedName() {
    assertTrue(Is.sideEffectFree(e("a.f")));
  }
  @Test public void sideEffectFreeMinusMinusPre() {
    assertFalse(Is.sideEffectFree(e("--a")));
  }
  @Test public void sideEffectFreeOfAssignment() {
    assertFalse(Is.sideEffectFree(e("a=b")));
  }
  @Test public void sideEffectFreeOfFunctionCall() {
    assertFalse(Is.sideEffectFree(e("f()")));
  }
  @Test public void sideEffectFreeOfFunctionCallParenthesized() {
    assertFalse(Is.sideEffectFree(e("(f())")));
  }
  @Test public void sideEffectFreeOfIntegerConstant() {
    assertTrue(Is.sideEffectFree(e("5")));
  }
  @Test public void sideEffectFreeOfMethodInovacation() {
    assertFalse(Is.sideEffectFree(e("a.f()")));
  }
  @Test public void sideEffectFreeOfMinusMinus() {
    assertFalse(Is.sideEffectFree(e("a--")));
  }
  @Test public void sideEffectFreeOfNewInstance() {
    assertFalse(Is.sideEffectFree(e("new A()")));
  }
  @Test public void sideEffectFreeOfNullLiteral() {
    assertTrue(Is.sideEffectFree(e("null")));
  }
  @Test public void sideEffectFreeOfNullPointer() {
    assertTrue(Is.sideEffectFree(null));
  }
  @Test public void sideEffectFreeOfPlusPlus() {
    assertFalse(Is.sideEffectFree(e("a++")));
  }
  @Test public void sideEffectFreePlusPlusPre() {
    assertFalse(Is.sideEffectFree(e("++a")));
  }
  @Test public void sideEffectfreeSuperAccess() {
    assertTrue(Is.sideEffectFree(e("super.f")));
  }
  @Test public void sideEffectfreeSuperInfocation() {
    assertFalse(Is.sideEffectFree(e("super.f()")));
  }
  @Test public void sideEffectFreeTypeInstanceOfTrue() {
    assertTrue(Is.sideEffectFree(e("a instanceof B")));
  }
  @Test public void sideEffectFreeTypeLiteralt() {
    assertTrue(Is.sideEffectFree(e("void.class")));
    assertTrue(Is.sideEffectFree(e("A.class")));
  }
  @Test public void sideEffectFrePrefix() {
    assertTrue(Is.sideEffectFree(e("-!a")));
    assertTrue(Is.sideEffectFree(e("~a")));
    assertTrue(Is.sideEffectFree(e("-a")));
    assertTrue(Is.sideEffectFree(e("+a")));
  }
  @Test public void sideEffectFrePrefixFunctiont() {
    assertFalse(Is.sideEffectFree(e("-!f()")));
    assertFalse(Is.sideEffectFree(e("~f()")));
    assertFalse(Is.sideEffectFree(e("-f()")));
    assertFalse(Is.sideEffectFree(e("+f()")));
  }
  @Test public void sideEffectSimpleName() {
    assertTrue(Is.sideEffectFree(e("a")));
  }
  @Test public void sideEffectStringLiteral() {
    assertTrue(Is.sideEffectFree(e("\"a\"")));
  }
  @Test public void sideEffectThisl() {
    assertTrue(Is.sideEffectFree(e("this")));
  }
}
