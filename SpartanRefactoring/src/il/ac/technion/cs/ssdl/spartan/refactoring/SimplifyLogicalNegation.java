package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.hasNull;
import static org.eclipse.jdt.core.dom.ASTNode.PARENTHESIZED_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Simplifies a negated boolean expression using De-Morgan laws and laws of
 * arithmetics.
 * 
 * @author Yossi Gil
 * @since 2014/06/15
 */
public class SimplifyLogicalNegation extends Spartanization {
	/** Instantiates this class */
	public SimplifyLogicalNegation() {
		super("Simplify logical negation");
	}
	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final PrefixExpression e) {
				return !inRange(m, e) ? true : simplifyNot(asNot(e));
			}
			private boolean simplifyNot(final PrefixExpression e) {
				return e == null ? true : simplifyNot(e, getCore(e.getOperand()));
			}
			private boolean simplifyNot(final PrefixExpression e, final Expression inner) {
				return perhapsDoubleNegation(e, inner) //
				    || perhapsDeMorgan(e, inner) //
				    || perhapsComparison(e, inner) //
				    || true;
			}
			boolean perhapsDoubleNegation(final Expression e, final Expression inner) {
				return perhapsDoubleNegation(e, asNot(inner));
			}
			boolean perhapsDoubleNegation(final Expression e, final PrefixExpression inner) {
				return inner != null && replace(e, inner.getOperand());
			}
			boolean perhapsDeMorgan(final Expression e, final Expression inner) {
				return perhapsDeMorgan(e, asAndOrOr(inner));
			}
			boolean perhapsDeMorgan(final Expression e, final InfixExpression inner) {
				return inner != null && deMorgan(e, inner, getCoreLeft(inner), getCoreRight(inner));
			}
			boolean deMorgan(final Expression e, final InfixExpression inner, final Expression left, final Expression right) {
				return deMorgan1(e, inner, parenthesize(left), parenthesize(right));
			}
			boolean deMorgan1(final Expression e, final InfixExpression inner, final Expression left, final Expression right) {
				return replace(e, //
				    parenthesize( //
				    addExtendedOperands(inner, //
				        makeInfixExpression(not(left), conjugate(inner.getOperator()), not(right)))));
			}
			InfixExpression addExtendedOperands(final InfixExpression from, final InfixExpression $) {
				if (from.hasExtendedOperands())
					addExtendedOperands(from.extendedOperands(), $.extendedOperands());
				return $;
			}
			void addExtendedOperands(final List<Expression> from, final List<Expression> to) {
				for (final Expression e : from)
					to.add(not(e));
			}
			boolean perhapsComparison(final Expression e, final Expression inner) {
				return perhapsComparison(e, asComparison(inner));
			}
			boolean perhapsComparison(final Expression e, final InfixExpression inner) {
				return inner != null && comparison(e, inner);
			}
			boolean comparison(final Expression e, final InfixExpression inner) {
				return replace(e, cloneInfixChangingOperator(inner, ShortestBranchFirst.negate(inner.getOperator())));
			}
			InfixExpression cloneInfixChangingOperator(final InfixExpression e, final Operator o) {
				return e == null ? null : makeInfixExpression(getCoreLeft(e), o, getCoreRight(e));
			}
			Expression parenthesize(final Expression e) {
				if (isSimple(e))
					return (Expression) ASTNode.copySubtree(t, e);
				final ParenthesizedExpression $ = t.newParenthesizedExpression();
				$.setExpression((Expression) ASTNode.copySubtree(t, getCore(e)));
				return $;
			}
			boolean isSimple(final Expression e) {
				return isSimple(e.getClass());
			}
			@SuppressWarnings("unchecked") boolean isSimple(final Class<? extends Expression> c) {
				return in(c, BooleanLiteral.class, //
				    CharacterLiteral.class, //
				    NullLiteral.class, //
				    NumberLiteral.class, //
				    StringLiteral.class, //
				    TypeLiteral.class, //
				    Name.class, //
				    QualifiedName.class, //
				    SimpleName.class, //
				    ParenthesizedExpression.class, //
				    SuperMethodInvocation.class, //
				    MethodInvocation.class, //
				    ClassInstanceCreation.class, //
				    SuperFieldAccess.class, //
				    FieldAccess.class, //
				    ThisExpression.class, //
				    null);
			}
			private PrefixExpression not(final Expression e) {
				final PrefixExpression $ = t.newPrefixExpression();
				$.setOperator(NOT);
				$.setOperand(parenthesize(e));
				return $;
			}
			private InfixExpression makeInfixExpression(final Expression left, final Operator o, final Expression right) {
				final InfixExpression $ = t.newInfixExpression();
				$.setLeftOperand((Expression) ASTNode.copySubtree(t, left));
				$.setOperator(o);
				$.setRightOperand((Expression) ASTNode.copySubtree(t, right));
				return $;
			}
			private boolean replace(final ASTNode original, final ASTNode replacement) {
				if (!hasNull(original, replacement))
					r.replace(original, replacement, null);
				return true;
			}
		});
	}
	static Expression getCoreRight(final InfixExpression e) {
		return getCore(e.getRightOperand());
	}
	static Expression getCoreLeft(final InfixExpression e) {
		return getCore(e.getLeftOperand());
	}
	static Operator conjugate(final Operator o) {
		assert isDeMorgan(o);
		return o.equals(CONDITIONAL_AND) ? CONDITIONAL_OR : CONDITIONAL_AND;
	}
	static Expression getCore(final Expression $) {
		return $.getNodeType() != PARENTHESIZED_EXPRESSION ? $ : getCore(((ParenthesizedExpression) $).getExpression());
	}
	static PrefixExpression asNot(final PrefixExpression e) {
		return NOT.equals(e.getOperator()) ? e : null;
	}
	static PrefixExpression asNot(final Expression e) {
		return !(e instanceof PrefixExpression) ? null : asNot((PrefixExpression) e);
	}
	static InfixExpression asAndOrOr(final InfixExpression e) {
		return isDeMorgan(e.getOperator()) ? e : null;
	}
	static boolean isDeMorgan(final Operator o) {
		return in(o, CONDITIONAL_AND, CONDITIONAL_OR);
	}
	static InfixExpression asAndOrOr(final Expression e) {
		return !(e instanceof InfixExpression) ? null : asAndOrOr((InfixExpression) e);
	}
	static InfixExpression asComparison(final InfixExpression e) {
		return in(e.getOperator(), //
		    GREATER, //
		    GREATER_EQUALS, //
		    LESS, //
		    LESS_EQUALS, //
		    EQUALS, //
		    NOT_EQUALS //
		) ? e : null;
	}
	static InfixExpression asComparison(final Expression e) {
		return !(e instanceof InfixExpression) ? null : asComparison((InfixExpression) e);
	}
	/**
	 * Check if a value is found among a list of other values of the same type.
	 * 
	 * @param t
	 *          some value to be examined; must not be null
	 * @param ts
	 *          candidates for equality; null values in list are ignored
	 * @return true if the given value is found among the candidates
	 */
	@SafeVarargs public static <T> boolean in(final T t, final T... ts) {
		for (final T candidate : ts)
			if (candidate != null && t.equals(candidate))
				return true;
		return false;
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final PrefixExpression e) {
				if (hasOpportunity(asNot(e)))
					opportunities.add(new Range(e));
				return true;
			}
			private boolean hasOpportunity(final PrefixExpression e) {
				return e == null ? false : hasOpportunity(getCore(e.getOperand()));
			}
			private boolean hasOpportunity(final Expression inner) {
				return asNot(inner) != null || asAndOrOr(inner) != null || asComparison(inner) != null;
			}
		};
	}
	/**
	 * A static nested class hosting unit tests for the nesting class Unit test
	 * for the containing class. Note our naming convention: a) test methods do
	 * not use the redundant "test" prefix. b) test methods begin with the name of
	 * the method they check.
	 * 
	 * @author Yossi Gil
	 * @since 2014-06-14
	 */
	@FixMethodOrder(MethodSorters.NAME_ASCENDING) @SuppressWarnings({ "static-method" })//
	public static class TEST {
		@SuppressWarnings("javadoc") @Test public void asComparisonTypicalInfixIsNotNull() {
			final InfixExpression i = mock(InfixExpression.class);
			doReturn(GREATER).when(i).getOperator();
			assertNotNull(asComparison(i));
		}
		@SuppressWarnings("javadoc") @Test public void asComparisonTypicalInfixIsCorrect() {
			final InfixExpression i = mock(InfixExpression.class);
			doReturn(GREATER).when(i).getOperator();
			assertEquals(i, asComparison(i));
		}
		@SuppressWarnings("javadoc") @Test public void asComparisonTypicalExpression() {
			final InfixExpression i = mock(InfixExpression.class);
			doReturn(GREATER).when(i).getOperator();
			final Expression e = i;
			assertNotNull(asComparison(e));
		}
		@SuppressWarnings("javadoc") @Test public void asComparisonPrefixlExpression() {
			final PrefixExpression p = mock(PrefixExpression.class);
			doReturn(NOT).when(p).getOperator();
			final Expression e = p;
			assertNull(asComparison(e));
		}
		@SuppressWarnings("javadoc") @Test public void asComparisonTypicalInfixFalse() {
			final InfixExpression i = mock(InfixExpression.class);
			doReturn(CONDITIONAL_AND).when(i).getOperator();
			assertNull(asComparison(i));
		}
		@SuppressWarnings("javadoc") @Test public void asComparisonTypicalExpressionFalse() {
			final InfixExpression i = mock(InfixExpression.class);
			doReturn(CONDITIONAL_OR).when(i).getOperator();
			final Expression e = i;
			assertNull(asComparison(e));
		}
		@SuppressWarnings("javadoc") @Test public void isDeMorganAND() {
			assertTrue(isDeMorgan(CONDITIONAL_AND));
		}
		@SuppressWarnings("javadoc") @Test public void isDeMorganOR() {
			assertTrue(isDeMorgan(CONDITIONAL_OR));
		}
		@SuppressWarnings("javadoc") @Test public void isDeMorganGreater() {
			assertFalse(isDeMorgan(GREATER));
		}
		@SuppressWarnings("javadoc") @Test public void isDeMorganGreaterEuals() {
			assertFalse(isDeMorgan(GREATER_EQUALS));
		}
		@SuppressWarnings("javadoc") @Test public void inTypicalTrue() {
			assertTrue(in("A", "A", "B", "C"));
		}
		@SuppressWarnings("javadoc") @Test public void inTypicalFalse() {
			assertFalse(in("X", "A", "B", "C"));
		}
	}
}
