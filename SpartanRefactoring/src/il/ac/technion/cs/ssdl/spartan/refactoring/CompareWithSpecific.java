package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.in;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isOneOf;
import static org.eclipse.jdt.core.dom.ASTNode.ARRAY_ACCESS;
import static org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.THIS_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import il.ac.technion.cs.ssdl.spartan.utils.Range;

/**
 * Comparison with this, null, or literals (either character or integer) is
 * flipped so that these specific values are placed on the right.
 *
 * @author Yossi Gil
 * @since 2015-07-04
 */
public final class CompareWithSpecific extends SpartanizationOfInfixExpression {
  /** Instantiates this class */
  public CompareWithSpecific() {
    super("Specific comparison", "Specific values: 'null' and 'this' and literals should appear last in comparisons");
  }

  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (withinDomain(e) && applicable(e))
          opportunities.add(new Range(e));
        return true;
      }
    };
  }

  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (inRange(m, e) && withinDomain(e) && applicable(e))
          flip(r, t, e);
        return true;
      }
    });
  }

  static boolean applicable(final InfixExpression e) {
    return isSpecific(e.getLeftOperand());
  }

  static boolean withinDomain(final InfixExpression e) {
    return e != null && isComparison(e) && hasOneSpecificArgument(e);
  }

  private static boolean hasOneSpecificArgument(final InfixExpression e) {
    // One of the arguments must be specific, the other must not be.
    return isSpecific(e.getLeftOperand()) != isSpecific(e.getRightOperand());
  }

  private static boolean isComparison(final InfixExpression e) {
    return in(e.getOperator(), EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, NOT_EQUALS);
  }

  private static boolean isSpecific(final Expression e) {
    return isOneOf(e, CHARACTER_LITERAL, NUMBER_LITERAL, NULL_LITERAL, THIS_EXPRESSION);
  }

  /**
   * * A static nested class hosting unit tests for the nesting class Unit test
   * for the containing class. Note our naming convention: a) test methods do
   * not use the redundant "test" prefix. b) test methods begin with the name of
   * the method they check.
   *
   * @author Yossi Gil
   * @since 2014-07-10
   *
   */
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) @SuppressWarnings({ "static-method", "javadoc", "synthetic-access" }) //
  public static class TEST {
    @Test public void trivialNodeGetNodeTypeV0() {
      final ASTNode n = mock(ASTNode.class);
      doReturn(ARRAY_ACCESS).when(n).getNodeType();
      assertEquals(ARRAY_ACCESS, n.getNodeType());
    }

    @Test public void trivialNodeGetNodeTypeV1() {
      final ASTNode n = mock(ASTNode.class);
      doReturn(new Integer(ARRAY_ACCESS)).when(n).getNodeType();
      assertEquals(ARRAY_ACCESS, n.getNodeType());
    }

    @Test public void trivialNodeGetNodeType() {
      final ASTNode n = mock(ASTNode.class);
      doReturnInt(ARRAY_ACCESS).when(n).getNodeType();
      assertEquals(ARRAY_ACCESS, n.getNodeType());
    }

    @Test public void trivialExpressionGetNodeType() {
      final Expression e = mock(Expression.class);
      doReturnInt(ARRAY_ACCESS).when(e).getNodeType();
      assertEquals(ARRAY_ACCESS, e.getNodeType());
    }

    @Test public void simplerGetNodeType() {
      final InfixExpression e = mockExpression();
      final InfixExpression right = mockExpression();
      doReturn(right).when(e).getRightOperand();
      when(e.getRightOperand()).thenReturn(right);
    }

    @Test public void simpleGetNodeType() {
      final InfixExpression e = setOperator(mockExpression(), EQUALS);
      final InfixExpression right = mockExpression();
      // doReturnInt(ARRAY_ACCESS).when(right).getNodeType();
      doReturn(right).when(e).getRightOperand();
      when(e.getRightOperand()).thenReturn(right);
    }

    @Test public void notApplicableToPlusExpression() {
      final InfixExpression i = mockExpression();
      doReturn(Operator.PLUS).when(i).getOperator();
      assertFalse(applicable(i));
    }

    @Test public void notApplicableToDgenerateEqualsExpression() {
      final InfixExpression i = mockExpression();
      doReturn(EQUALS).when(i).getOperator();
      assertFalse(applicable(i));
    }

    @Test public void notAapplicableToDgenerateEqualsExpressionShortSyntax() {
      assertFalse(applicable(setOperator(mockExpression(), EQUALS)));
    }

    @Test public void applicableToValidEqualsExpression() {
      final InfixExpression e = setOperator(mockExpression(), EQUALS);
      doReturn(setOperator(mockExpression(), EQUALS)).when(e).getLeftOperand();
      final InfixExpression right = mockExpression();
      when(e.getRightOperand()).thenReturn(right);
      assertTrue(applicable(e));
    }

    @Test public void checkLeftOperandStubbing() {
      final InfixExpression e = setOperator(mockExpression(), EQUALS);
      doReturn(setOperator(mockExpression(), EQUALS)).when(e).getLeftOperand();
      assertNotNull(e.getLeftOperand());
    }

    @Test public void checkRightOperandStubbing() {
      final InfixExpression e = setOperator(mockExpression(), EQUALS);
      final InfixExpression right = mockExpression();
      when(e.getRightOperand()).thenReturn(right);
      assertNotNull(e.getRightOperand());
      assertEquals(right, e.getRightOperand());
    }

    @Test public void checkBothOperandsStubbing() {
      final InfixExpression e = setOperator(mockExpression(), EQUALS);
      doReturn(setOperator(mockExpression(), EQUALS)).when(e).getLeftOperand();
      assertNotNull(e.getLeftOperand());
      final InfixExpression right = mockExpression();
      assertNotNull(e.getRightOperand());
      assertEquals(right, e.getRightOperand());
      assertNotNull(e.getLeftOperand());
    }

    @Test public void isComparionToTrivialdEqualsExpression() {
      final InfixExpression e = setOperator(mockExpression(), EQUALS);
      final InfixExpression right = mockExpression();
      when(e.getRightOperand()).thenReturn(right);
      assertTrue(isComparison(e));
    }

    @Test public void isSpecificTrue() {
      final Expression e = mock(Expression.class);
      when(e.getNodeType()).thenReturn(NULL_LITERAL);
      assertTrue(isSpecific(e));
    }

    @Test public void stubbingOfIntExpression() {
      final Expression e = mock(Expression.class);
      when(e.getNodeType()).thenReturn(NULL_LITERAL);
      assertEquals(NULL_LITERAL, e.getNodeType());
    }

    @Test public void stubbingOfInt() {
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
  }
}
