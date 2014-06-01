package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import il.ac.technion.cs.ssdl.spartan.utils.Funcs;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
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
		super("Shortest operand first", "Make the shortest operand first in an infix expression");
	}
	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final InfixExpression n) {
				if (!inRange(m, n) || n.getLeftOperand() == null || n.getRightOperand() == null)
					return true;
				r.replace(n, transpose(t, r, n), null); // Replace old tree with the new
				// organized one
				return true;
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
		final ASTNode newR = ASTNode.copySubtree(ast, n.getRightOperand());
		final ASTNode newL = ASTNode.copySubtree(ast, n.getLeftOperand());
		if ($.getLeftOperand().getNodeType() == INFIX_EXPRESSION)
			$.setLeftOperand(transpose(ast, rewrite, (InfixExpression) $.getLeftOperand()));
		if ($.getRightOperand().getNodeType() == INFIX_EXPRESSION)
			$.setRightOperand(transpose(ast, rewrite, (InfixExpression) $.getRightOperand()));
		if (newR.getNodeType() == BOOLEAN_LITERAL)
			return $; // Prevents the following swap: "(a>0) == true" =>
		// "true == (a>0)"
		if (isFlipable(n.getOperator()) && longerFirst(n))
			set($, (Expression) newL, flipOperator(n.getOperator()), (Expression) newR);
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
		if (o.equals(LESS))
			return GREATER_EQUALS;
		if (o.equals(GREATER))
			return LESS_EQUALS;
		if (o.equals(LESS_EQUALS))
			return GREATER;
		if (o.equals(GREATER_EQUALS))
			return LESS;
		return o;
	}
	/**
	 * @param o
	 *          The operator to check
	 * @return True - if the operator have opposite one in terms of operands swap.
	 * @see ShortestOperand
	 */
	public static boolean isFlipable(final Operator o) {
		if (o.equals(EQUALS) || o.equals(NOT_EQUALS) || o.equals(LESS) || o.equals(GREATER) || o.equals(LESS_EQUALS)
				|| o.equals(GREATER_EQUALS) || o.equals(PLUS) || o.equals(TIMES) || o.equals(XOR) || o.equals(AND) || o.equals(OR))
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
		return !(a.from > b.to || b.from > a.to); // Negation of "not overlapped"
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
			if (areOverlapped(r, rNew)) { // TODO: Check this.
				r = merge(r, rNew);
				$ = true;
			}
		return $;
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final InfixExpression n) {
				final Range rN = new Range(n.getParent());
				if (longerFirst(n) && isFlipable(n.getOperator())) { // Check if the
					// operands can be swapped
					final boolean overlapFound = unionRangeWithList(opportunities, rN);
					// Union range results
					if (!overlapFound)
						opportunities.add(rN);
				}
				return true;
			}
		};
	}
	static boolean longerFirst(final InfixExpression n) {
		return n.getLeftOperand() != null && n.getRightOperand() != null
				&& Funcs.countNodes(n.getLeftOperand()) > Funcs.countNodes(n.getRightOperand()) + threshold;
	}
}
