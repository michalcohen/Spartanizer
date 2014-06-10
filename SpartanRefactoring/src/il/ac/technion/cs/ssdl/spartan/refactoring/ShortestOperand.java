package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.countNodes;
import static org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.PLUS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.TIMES;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.XOR;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         24.05.2014)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         24.05.2014)
 * @since 2014/05/24
 */
public class ShortestOperand extends Spartanization {
	/** Instantiates this class */
	public ShortestOperand() {
		super("Shortest operand first", "Make the shortest operand first in a binary commutative or semi-commutative operator");
	}
	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final InfixExpression n) {
				if (invalid( n))
					return true;
				if (longerFirst(n) && isFlipable(n.getOperator()))
					r.replace(n, transpose(t, r, n), null); // Replace old tree with
				// the new organized one
				return true;
			}

			private boolean invalid(final InfixExpression n) {
				return !inRange(m, n) || null == n.getLeftOperand() || null == n.getRightOperand();
			}
		});
	}
	/**
	 * Transpose infix expressions recursively. Makes the shortest operand first
	 * on every subtree of the node.
	 * 
	 * @param ast
	 *          The AST - for copySubTree.
	 * @param rewrite
	 *          The rewriter - to perform the change.
	 * @param n
	 *          The node.
	 * @return Number of abstract syntax tree nodes under the parameter.
	 */
	public static InfixExpression transpose(final AST ast, final ASTRewrite rewrite, final InfixExpression n) {
		final InfixExpression $ = (InfixExpression) ASTNode.copySubtree(ast, n);
		if (INFIX_EXPRESSION == $.getLeftOperand().getNodeType())
			$.setLeftOperand(transpose(ast, rewrite, (InfixExpression) $.getLeftOperand()));
		if (INFIX_EXPRESSION == $.getRightOperand().getNodeType())
			$.setRightOperand(transpose(ast, rewrite, (InfixExpression) $.getRightOperand()));
		final ASTNode newR = ASTNode.copySubtree(ast, n.getRightOperand());
		if (BOOLEAN_LITERAL == newR.getNodeType())
			return $; // Prevents the following swap: "(a>0) == true" =>
		// "true == (a>0)"
		if (isFlipable(n.getOperator()) && longerFirst(n))
			set($, (Expression) ASTNode.copySubtree(ast, n.getLeftOperand()), flipOperator(n.getOperator()), (Expression) newR);
		return $;
	}
	private static void set(final InfixExpression $, final Expression left, final Operator operator, final Expression right) {
		$.setRightOperand(left);
		$.setOperator(operator);
		$.setLeftOperand(right);
	}
	/**
	 * Makes an opposite operator from a given one, which keeps its logical
	 * operation after the node swapping. e.g. "&" is commutative, therefore no
	 * change needed. "<" isn't commutative, but it has its opposite: ">=".
	 * 
	 * @param o
	 *          The operator to flip
	 * @return The correspond operator - e.g. "<=" will become ">", "+" will stay
	 *         "+".
	 */
	public static Operator flipOperator(final Operator o) {
		return !conjugate.containsKey(o) ? o : conjugate.get(o);
	}
	private static Map<Operator, Operator> conjugate = makeConjeguates();
	/**
	 * @param o
	 *          The operator to check
	 * @return True - if the operator have opposite one in terms of operands swap.
	 * @see ShortestOperand
	 */
	public static boolean isFlipable(final Operator o) {
		// TODO: add bit wise or and bit wise not
		// TODO: add testing for XOR; it does not show up right.
		// TODO: add test case for string concatenation which uses "+" as well.
		return in(o, //
				AND, //
				EQUALS, //
				GREATER, //
				GREATER_EQUALS, //
				LESS_EQUALS, //
				LESS, //
				NOT_EQUALS, //
				OR, //
				PLUS, //
				TIMES, //
				XOR, //
				null);
	}
	private static Map<Operator, Operator> makeConjeguates() {
		final Map<Operator, Operator> $ = new HashMap<Operator, Operator>();
		$.put(GREATER, LESS);
		$.put(LESS, GREATER);
		$.put(GREATER_EQUALS, LESS_EQUALS);
		$.put(LESS_EQUALS, GREATER_EQUALS);
		return $;
	}
	private static <T> boolean in(final T candidate, final T... ts) {
		for (final T t : ts)
			if (t != null && t.equals(candidate))
				return true;
		return false;
	}
	private static final int threshold = 1;
	/**
	 * Determine if the ranges are overlapping in a part of their range
	 * 
	 * @param a
	 *          b Ranges to merge
	 * @return True - if such an overlap exists
	 * @see merge
	 */
	protected static boolean areOverlapped(final Range a, final Range b) {
		return !(a.from > b.to || b.from > a.to); // Negation of
		// "not overlapped"
	}
	/**
	 * @param a
	 *          b Ranges to merge
	 * @return A new merged range.
	 * @see areOverlapped
	 */
	protected static Range merge(final Range a, final Range b) {
		return new Range(a.from < b.from ? a.from : b.from, a.to > b.to ? a.to : b.to);
	}
	/**
	 * Tries to union the given range with one of the elements inside the given
	 * list.
	 * 
	 * @param rangeList
	 *          The list of ranges to union with
	 * @param rNew
	 *          The new range to union
	 * @return True - if the list updated and the new range consumed False - the
	 *         list remained intact
	 * 
	 * @see areOverlapped
	 * @see merge
	 */
	protected static boolean unionRangeWithList(final List<Range> rangeList, final Range rNew) {
		boolean $ = false;
		for (Range r : rangeList)
			if (areOverlapped(r, rNew)) {
				r = merge(r, rNew);
				$ = true;
			}
		return $;
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final InfixExpression n) {
				if (!longerFirst(n) || !isFlipable(n.getOperator()))
					return true;
				final Range rN = new Range(n.getParent());
				if (!unionRangeWithList(opportunities, rN))
					opportunities.add(rN);
				return true;
			}
		};
	}
	static boolean longerFirst(final InfixExpression n) {
		return null != n.getLeftOperand() && null != n.getRightOperand()
				&& countNodes(n.getLeftOperand()) > threshold + countNodes(n.getRightOperand());
	}
}
