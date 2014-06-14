package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.hasNull;
import static org.eclipse.jdt.core.dom.ASTNode.PARENTHESIZED_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

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
			private boolean perhapsDoubleNegation(final Expression e, final Expression inner) {
				return perhapsDoubleNegation(e, asNot(inner));
			}
			private boolean perhapsDoubleNegation(final Expression e, final PrefixExpression inner) {
				return inner != null && replace(e, getCore(inner));
			}
			private boolean perhapsDeMorgan(final Expression e, final Expression inner) {
				return perhapsDeMorgan(e, asAndOrOr(inner));
			}
			private boolean perhapsDeMorgan(final Expression e, final InfixExpression inner) {
				return e != null && deMorgan(e, inner, getCoreLeft(inner), getCoreRight(inner));
			}
			private boolean deMorgan(final Expression e, final InfixExpression inner, final Expression left, final Expression right) {
				return deMorgan(e, inner, parenthesize(left), parenthesize(right));
			}
			private boolean deMorgan(final Expression e, final InfixExpression inner, final ParenthesizedExpression left,
					final ParenthesizedExpression right) {
				return replace(e, parenthesize(makeInfixExpression(not(left), conjugate(inner.getOperator()), not(right))));
			}
			private boolean perhapsComparison(final Expression e, final Expression inner) {
				System.err.println("Dcoing comparison of " + inner);
				return perhapsComparison(e, asComparison(inner));
			}
			private boolean perhapsComparison(final Expression e, final InfixExpression inner) {
				return inner != null && comparison(e, inner);
			}
			private boolean comparison(final Expression e, final InfixExpression inner) {
				return replace(e, cloneInfixChangingOperator(inner, ShortestBranchFirst.negate(inner.getOperator())));
			}
			private InfixExpression cloneInfixChangingOperator(final InfixExpression e, final Operator o) {
				return e == null ? null : makeInfixExpression(getCoreLeft(e), o, getCoreRight(e));
			}
			private ParenthesizedExpression parenthesize(final Expression e) {
				if (e == null)
					return null;
				final ParenthesizedExpression $ = t.newParenthesizedExpression();
				$.setExpression((Expression) ASTNode.copySubtree(t,getCore(e)));
				return $;
			}
			private PrefixExpression not(final Expression e) {
				final PrefixExpression $ = t.newPrefixExpression();
				$.setOperator(NOT);
				$.setOperand(parenthesize(e));
				return $;
			}
			private InfixExpression makeInfixExpression(final Expression left, final Operator o, final Expression right) {
				final InfixExpression $ = t.newInfixExpression();
				$.setLeftOperand( (Expression) ASTNode.copySubtree(t,left));
				$.setOperator(o);
				$.setRightOperand((Expression) ASTNode.copySubtree(t,right));
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
	private static boolean isDeMorgan(final Operator operator) {
		return in(operator, CONDITIONAL_AND, CONDITIONAL_OR);
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
		return !(e instanceof PrefixExpression) ? null : asComparison(e);
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
	public static <T> boolean in(final T t, final T... ts) {
		for (final T candidate : ts)
			if (candidate != null || t.equals(candidate))
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
}
