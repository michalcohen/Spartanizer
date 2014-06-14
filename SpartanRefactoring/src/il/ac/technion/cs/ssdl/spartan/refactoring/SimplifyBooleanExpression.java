package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.hasNull;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Simplifies a boolean expression using De-Morgan laws
 * 
 * @author Yossi Gil
 * @since 2014/06/15
 */
public class SimplifyBooleanExpression extends Spartanization {
	/** Instantiates this class */
	public SimplifyBooleanExpression() {
		super("Simplify Boolean expression");
	}
	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final PrefixExpression e) {
				return (!(inRange(m, e)) ? true : simplifyNot(asNot(e)));
			}
			protected boolean simplifyNot(final PrefixExpression e) {
				if (e == null)
					return true;
				final Expression inner = getCore(e.getOperand());
				return perhapsDoubleNegation(e, inner) //
						|| perhapsDeMorgan(e, inner) //
						|| perhapsComparison(e, inner) //
						|| true;
			}
			private boolean perhapsDoubleNegation(final Expression e, final Expression inner) {
				return perhapsDoubleNegation(e, asNot(inner));
			}
			private boolean perhapsDoubleNegation(final Expression e, final PrefixExpression inner) {
				return replace(e, getCore(inner));
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
				$.setExpression(getCore(e));
				return $;
			}
			private PrefixExpression not(final Expression e) {
				final PrefixExpression $ = t.newPrefixExpression();
				$.setOperand(parenthesize(getCore(e)));
				$.setOperator(NOT);
				return $;
			}
			private InfixExpression makeInfixExpression(final Expression left, final Operator o, final Expression right) {
				final InfixExpression $ = t.newInfixExpression();
				$.setLeftOperand(left);
				$.setOperator(o);
				$.setRightOperand(right);
				return $;
			}

			private  boolean replace(final ASTNode original, final ASTNode replacement) {
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
		return $.getNodeType() != ASTNode.PARENTHESIZED_EXPRESSION ? $ : getCore(((ParenthesizedExpression) $).getExpression());
	}
	static PrefixExpression asNot(final PrefixExpression e) {
		return NOT.equals(e.getOperator()) ? e : null;
	}
	static PrefixExpression asNot(final Expression e) {
		return (!(e instanceof PrefixExpression) ? null : asNot((PrefixExpression) e));
	}
	static InfixExpression asAndOrOr(final InfixExpression e) {
		return isDeMorgan(e.getOperator()) ? e : null;
	}
	private static boolean isDeMorgan(final Operator operator) {
		return in(operator, CONDITIONAL_AND, CONDITIONAL_OR);
	}
	static InfixExpression asAndOrOr(final Expression e) {
		return (!(e instanceof InfixExpression) ? null : asAndOrOr((InfixExpression) e));
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
		return (!(e instanceof PrefixExpression) ? null : asComparison(e));
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
			@Override public boolean visit(final VariableDeclarationFragment node) {
				if (!(node.getParent() instanceof VariableDeclarationStatement))
					return true;
				final SimpleName varName = node.getName();
				final VariableDeclarationStatement parent = (VariableDeclarationStatement) node.getParent();
				if (1 == numOfOccur(Occurrences.USES_SEMANTIC, varName, parent.getParent())
						&& (0 != (parent.getModifiers() & Modifier.FINAL) || 1 == numOfOccur(Occurrences.ASSIGNMENTS, varName,
								parent.getParent())))
					opportunities.add(new Range(node));
				return true;
			}
		};
	}
	static int numOfOccur(final Occurrences typeOfOccur, final Expression of, final ASTNode in) {
		return typeOfOccur == null || of == null || in == null ? -1 : typeOfOccur.of(of).in(in).size();
	}
}
