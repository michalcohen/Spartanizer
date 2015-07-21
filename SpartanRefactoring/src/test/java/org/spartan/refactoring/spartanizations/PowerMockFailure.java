package org.spartan.refactoring.spartanizations;

import static org.eclipse.jdt.core.dom.ASTNode.ARRAY_ACCESS;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.stubbing.Stubber;
import org.spartan.refactoring.utils.Is;

/**
 * * Unit tests for the nesting class Unit test for the containing class. Note
 * our naming convention: a) test methods do not use the redundant "test"
 * prefix. b) test methods begin with the name of the method they check.
 *
 * @author Yossi Gil
 * @since 2014-07-10
 */
// @RunWith(PowerMockRunner.class) //
// @PrepareForTest({ Expression.class, /* ASTNode.class */ }) //
// @PrepareForTest({ TEST.AFinalClass.class, TEST.Node.class, ASTNode.class,
// Expression.class, }) //
@FixMethodOrder(MethodSorters.JVM) //
@SuppressWarnings({ "boxing", "static-method", "javadoc" }) //
public class PowerMockFailure {
  @Test @Ignore("Unignore if using PowerMock") public void mockOfAFinalClass() {
    final PowerMockFailure.AFinalClass tested = mock(PowerMockFailure.AFinalClass.class);
    final String testInput = "A test input";
    final String mockedResult = "Mocked final echo result - " + testInput;
    when(tested.echoString(testInput)).thenReturn(mockedResult);
    // Assert the mocked result is returned from method call
    Assert.assertEquals(tested.echoString(testInput), mockedResult);
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void mockOfNode() {
    final PowerMockFailure.Node tested = mock(PowerMockFailure.Node.class);
    when(tested.getNodeType()).thenReturn(3);
    when(tested.equals(new Object())).thenReturn(false);
    // Assert the mocked result is returned from method call
    Assert.assertEquals(new Integer(3), tested.getNodeType());
  }
  @Test @Ignore("Unignore if using PowerMock") public void mockOfNodeOfInt() {
    final PowerMockFailure.NodeOfInt tested = mock(PowerMockFailure.NodeOfInt.class);
    when(tested.getNodeType()).thenReturn(13);
    // Assert the mocked result is returned from method call
    Assert.assertEquals(13, tested.getNodeType());
  }
  @Test @Ignore("Unignore if using PowerMock") public void mockOfAbstractNodeOfInt() {
    final PowerMockFailure.AbstractNodeOfInt tested = mock(PowerMockFailure.AbstractNodeOfInt.class);
    when(tested.getNodeType()).thenReturn(13);
    // Assert the mocked result is returned from method call
    Assert.assertEquals(13, tested.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void mockOfASTNode0() {
    final ASTNode tested = mock(ASTNode.class);
    when(tested.getNodeType()).thenReturn(13);
    // Assert the mocked result is returned from method call
    Assert.assertEquals(13, tested.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void mockOfASTNode() {
    final ASTNode tested = mock(ASTNode.class);
    when(tested.getNodeType()).thenReturn(3);
    // Assert the mocked result is returned from method call
    Assert.assertEquals(3, tested.getNodeType());
  }
  @Test public void trivialestNodeGetNodeTypeV0() {
    final ASTNode n = mock(ASTNode.class);
    doReturn(ARRAY_ACCESS).when(n).getNodeType();
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void trivialerNodeGetNodeTypeV1() {
    final ASTNode n = mock(ASTNode.class);
    doReturn(new Integer(ARRAY_ACCESS)).when(n).getNodeType();
    n.getNodeType();
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void trivialNodeGetNodeTypeV0() {
    final ASTNode n = mock(ASTNode.class);
    when(n.getNodeType()).thenReturn(ARRAY_ACCESS);
    assertEquals(ARRAY_ACCESS, n.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void trivialNodeGetNodeTypeV1() {
    final ASTNode n = mock(ASTNode.class);
    doReturn(new Integer(ARRAY_ACCESS)).when(n).getNodeType();
    assertEquals(ARRAY_ACCESS, n.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void trivialNodeGetNodeType() {
    final ASTNode n = mock(ASTNode.class);
    doReturnInt(ARRAY_ACCESS).when(n).getNodeType();
    assertEquals(ARRAY_ACCESS, n.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void trivialExpressionGetNodeType() {
    final Expression e = mock(Expression.class);
    doReturnInt(ARRAY_ACCESS).when(e).getNodeType();
    assertEquals(ARRAY_ACCESS, e.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void simplerGetNodeType() {
    final InfixExpression e = mockExpression();
    final InfixExpression right = mockExpression();
    doReturn(right).when(e).getRightOperand();
    when(e.getRightOperand()).thenReturn(right);
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void simpleGetNodeType() {
    final InfixExpression e = makeEqualsExpression();
    final InfixExpression right = mockExpression();
    // doReturnInt(ARRAY_ACCESS).when(right).getNodeType();
    doReturn(right).when(e).getRightOperand();
    when(e.getRightOperand()).thenReturn(right);
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void notApplicableToPlusExpression() {
    final InfixExpression i = mockExpression();
    doReturn(Operator.PLUS).when(i).getOperator();
    assertFalse(ComparisonWithSpecific.applicable(i));
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void notApplicableToDgenerateEqualsExpression() {
    final InfixExpression i = mockExpression();
    doReturn(EQUALS).when(i).getOperator();
    assertFalse(ComparisonWithSpecific.applicable(i));
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void notAapplicableToDgenerateEqualsExpressionShortSyntax() {
    assertFalse(ComparisonWithSpecific.applicable(makeEqualsExpression()));
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void applicableToValidEqualsExpression() {
    final InfixExpression e = makeEqualsExpression();
    doReturn(makeEqualsExpression()).when(e).getLeftOperand();
    final InfixExpression right = mockExpression();
    when(right.getNodeType()).thenReturn(NULL_LITERAL);
    when(e.getRightOperand()).thenReturn(right);
    assertTrue(ComparisonWithSpecific.applicable(e));
  }
  @Ignore("PowerMock stack overflow bug in presence of function equals()") private InfixExpression makeEqualsExpression() {
    return setOperator(mockExpression(), EQUALS);
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void checkLeftOperandStubbing() {
    final InfixExpression e = makeEqualsExpression();
    doReturn(makeEqualsExpression()).when(e).getLeftOperand();
    assertNotNull(e.getLeftOperand());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void checkRightOperandStubbing() {
    final InfixExpression e = makeEqualsExpression();
    final InfixExpression right = mockExpression();
    when(e.getRightOperand()).thenReturn(right);
    assertNotNull(e.getRightOperand());
    assertEquals(right, e.getRightOperand());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void checkBothOperandsStubbing() {
    final InfixExpression e = makeEqualsExpression();
    doReturn(makeEqualsExpression()).when(e).getLeftOperand();
    assertNotNull(e.getLeftOperand());
    final InfixExpression right = mockExpression();
    assertNotNull(e.getRightOperand());
    assertEquals(right, e.getRightOperand());
    assertNotNull(e.getLeftOperand());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void isComparionToTrivialdEqualsExpression() {
    final InfixExpression e = makeEqualsExpression();
    final InfixExpression right = mockExpression();
    when(e.getRightOperand()).thenReturn(right);
    assertTrue(Is.comparison(e));
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void isSpecificTrue() {
    final Expression e = mock(Expression.class);
    when(e.getNodeType()).thenReturn(NULL_LITERAL);
    assertTrue(Is.specific(e));
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void stubbingOfIntExpression() {
    final Expression e = mock(Expression.class);
    when(e.getNodeType()).thenReturn(NULL_LITERAL);
    assertEquals(NULL_LITERAL, e.getNodeType());
  }
  @Test @Ignore("PowerMock stack overflow bug in presence of function equals()") public void stubbingOfInt() {
    final InfixExpression e = mockExpression();
    when(e.getNodeType()).thenReturn(NULL_LITERAL);
    assertEquals(NULL_LITERAL, e.getNodeType());
  }
  private static InfixExpression setOperator(final InfixExpression $, final Operator o) {
    doReturn(o).when($).getOperator();
    return $;
  }
  private static InfixExpression mockExpression() {
    return mock(InfixExpression.class);
  }
  public static Stubber doReturnInt(final int valueToReturn) {
    return doReturn(new Integer(valueToReturn));
  }

  private static final class AFinalClass {
    public final String echoString(final String s) {
      return s;
    }
  }

  private static final class Node {
    @Override public final boolean equals(final Object o) {
      return o == this; // equivalent to Object.equals
    }
    public final Integer getNodeType() {
      return 17;
    }
    @Override public int hashCode() {
      return super.hashCode();
    }
  }

  private static final class NodeOfInt {
    public final int getNodeType() {
      return 137;
    }
  }

  private static final class AbstractNodeOfInt {
    public final int getNodeType() {
      return 4137;
    }
  }
}