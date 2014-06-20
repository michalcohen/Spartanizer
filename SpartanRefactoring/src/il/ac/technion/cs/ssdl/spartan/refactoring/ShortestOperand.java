package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.countNodes;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isInfix;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isLiteral;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isMethodInvocation;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isStringLitrl;
import static org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         24.05.2014)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         24.05.2014)
 * @since 2014/05/24 TODO|Justification ahead|: Bug. Highlight should be on
 *        operator only. Otherwise it is too messy. | There are two main reasons
 *        not to do so: (1) We want the interface to be both intuitive and
 *        non-exhausting for the user to use - using operator only notation
 *        might lead to one of those cases: (a) Multiple clicks on single
 *        expression for even simple swaps: Consider cccc + yyy + bb + a - in
 *        one-operator approach the user will have to click more than five time
 *        (kind of manual bubble sort) (b) Say we do not want to exhaust the
 *        user - on which operator should we put it? maybe all of them? so we
 *        damage the simplicity of "what you click is what you change". Tell us
 *        it's still OK and we will change it. Done: Add options for 1 right
 *        literal swap and 2 literals swap.
 */
public class ShortestOperand extends Spartanization {
	// Option flags
	/**
	 * Enumeration for right literal rule options
	 */
	public static enum RepositionRightLiteral {
		/** When right can be swapped - do it */
		All,
		/** Swap literal only when it is not boolean or null */
		AllButBooleanAndNull,
		/** When the literal appears to the right - do not swap */
		None
	}

	/**
	 * Enumeration for both side literals rule options
	 */
	public static enum RepositionLiterals {
		/** Swap literals */
		All,
		/** Do not swap literals */
		None
	}

	RepositionRightLiteral rightLiteralOption = RepositionRightLiteral.AllButBooleanAndNull;
	RepositionLiterals bothLiteralsOption = RepositionLiterals.All;

	/** Instantiates this class */
	public ShortestOperand() {
		super(
				"Shortest operand first",
				"Make the shortest operand first in a binary commutative or semi-commutative operator");
	}

	@Override
	protected final void fillRewrite(final ASTRewrite r, final AST t,
			final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(final InfixExpression n) {
				if (invalid(n))
					return true;
				final AtomicBoolean hasChanged = new AtomicBoolean(false);
				final InfixExpression newNode = transpose(t, n, hasChanged);
				if (hasChanged.get())
					r.replace(n, newNode, null); // Replace old tree with
				return true;
			}

			private boolean invalid(final InfixExpression n) {
				return !inRange(m, n) || null == n.getLeftOperand()
						|| null == n.getRightOperand();
			}
		});
	}

	/**
	 * Transpose infix expressions recursively. Makes the shortest operand first
	 * on every subtree of the node.
	 *
	 * @param ast
	 *            The AST - for copySubTree.
	 * @param n
	 *            The node.
	 * @param hasChanged
	 *            Indicates weather a change occurred. reference to the passed
	 *            value might be changed.
	 * @return Number of abstract syntax tree nodes under the parameter.
	 */
	public InfixExpression transpose(final AST ast, final InfixExpression n,
			final AtomicBoolean hasChanged) {
		final InfixExpression $ = (InfixExpression) ASTNode.copySubtree(ast, n);
		transposeOperands($, ast, hasChanged);

		final ASTNode newR = ASTNode.copySubtree(ast, n.getRightOperand());
		final ASTNode newL = ASTNode.copySubtree(ast, n.getLeftOperand());
		final Operator o = n.getOperator();

		if (isFlipable(o) && longerFirst(n) && !inOperandExceptions(newL, o)
				&& !inRightOperandExceptions(newR, o) && !inInfixExceptions($)) {
			set($, (Expression) newL, flipOperator(o), (Expression) newR);
			hasChanged.set(true);
		}
		if (sortInfix($, ast))
			hasChanged.set(true);
		return $;
	}

	/**
	 * Sets rule option
	 *
	 * @param op
	 *            Select specific option from RepositionRightLiteral enumeration
	 */
	public void setRightLiteralRule(final RepositionRightLiteral op) {
		rightLiteralOption = op;
	}

	/**
	 * Sets rule option
	 *
	 * @param op
	 *            Select specific option from RepositionRightLiteral enumeration
	 */
	public void setBothLiteralsRule(final RepositionLiterals op) {
		bothLiteralsOption = op;
	}

	private void transposeOperands(final InfixExpression ie, final AST ast,
			final AtomicBoolean hasChanged) {

		final Expression l = ie.getLeftOperand();
		final Expression r = ie.getRightOperand();

		// sortInfix($);
		if (isInfix(l))
			ie.setLeftOperand(transpose(ast, (InfixExpression) l, hasChanged));
		if (isInfix(r))
			ie.setRightOperand(transpose(ast, (InfixExpression) r, hasChanged));
	}

	@SuppressWarnings("boxing")
	// Justification: because ASTNode is a primitive
	// int we can't use the generic "in" function on it
	// without boxing into Integer. Any other solution
	// will cause less readable/maintainable code.
	private boolean inRightOperandExceptions(final ASTNode rN, final Operator o) {
		final Integer t = new Integer(rN.getNodeType());
		if (isMethodInvocation(rN))
			return true;
		if (inOperandExceptions(rN, o) || //
				o == PLUS && (isMethodInvocation(rN) || isStringLitrl(rN)))
			return true;
		switch (rightLiteralOption) {
		case All:
			return false;
		case AllButBooleanAndNull:
			return in(t, //
					BOOLEAN_LITERAL, //
					NULL_LITERAL, //
					null);
		case None:
			return isLiteral(rN);
		default:
			return false; // All

		}

	}

	private boolean inOperandExceptions(final ASTNode n, final Operator o) {
		if (bothLiteralsOption == RepositionLiterals.None && isLiteral(n))
			return true;
		return o == PLUS && isStringLitrl(n);
	}

	private static boolean inInfixExceptions(final InfixExpression ie) {
		return isMethodInvocation(ie.getLeftOperand())
				&& isMethodInvocation(ie.getRightOperand());
	}

	private static void set(final InfixExpression $, final Expression left,
			final Operator operator, final Expression right) {
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
	 *            The operator to flip
	 * @return The correspond operator - e.g. "<=" will become ">", "+" will
	 *         stay "+".
	 */
	public static Operator flipOperator(final Operator o) {
		return !conjugate.containsKey(o) ? o : conjugate.get(o);
	}

	private static Map<Operator, Operator> conjugate = makeConjeguates();

	/**
	 * @param o
	 *            The operator to check
	 * @return True - if the operator have opposite one in terms of operands
	 *         swap.
	 * @see ShortestOperand
	 */
	public static boolean isFlipable(final Operator o) {
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
		final Map<Operator, Operator> $ = new HashMap<>();
		$.put(GREATER, LESS);
		$.put(LESS, GREATER);
		$.put(GREATER_EQUALS, LESS_EQUALS);
		$.put(LESS_EQUALS, GREATER_EQUALS);
		return $;
	}

	@SafeVarargs
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
	 *            b Ranges to merge
	 * @return True - if such an overlap exists
	 * @see merge
	 */
	protected static boolean areOverlapped(final Range a, final Range b) {
		return a.from <= b.to && b.from <= a.to; // Negation of "not overlapped"
	}

	/**
	 * @param a
	 *            b Ranges to merge
	 * @return A new merged range.
	 * @see areOverlapped
	 */
	protected static Range merge(final Range a, final Range b) {
		return new Range(a.from < b.from ? a.from : b.from, a.to > b.to ? a.to
				: b.to);
	}

	/**
	 * Tries to union the given range with one of the elements inside the given
	 * list.
	 *
	 * @param rangeList
	 *            The list of ranges to union with
	 * @param rNew
	 *            The new range to union
	 * @return True - if the list updated and the new range consumed False - the
	 *         list remained intact
	 *
	 * @see areOverlapped
	 * @see merge
	 */
	protected static boolean unionRangeWithList(final List<Range> rangeList,
			final Range rNew) {
		boolean $ = false;
		for (Range r : rangeList)
			if (areOverlapped(r, rNew)) {
				r = merge(r, rNew);
				$ = true;
			}
		return $;
	}

	@Override
	protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override
			public boolean visit(final InfixExpression n) {
				final AtomicBoolean hasChanged = new AtomicBoolean(false);
				final AST t = AST.newAST(AST.JLS4);
				transpose(t, n, hasChanged);
				if (!hasChanged.get())
					return true;
				final Range rN = new Range(n.getParent());
				if (!unionRangeWithList(opportunities, rN))
					opportunities.add(rN);
				return true;
			}
		};
	}

	static boolean longerFirst(final InfixExpression n) {
		final Expression l = n.getLeftOperand();
		final Expression r = n.getRightOperand();
		return isLarger(l, r);
	}

	static boolean largerArgsNum(final MethodInvocation a,
			final MethodInvocation b) {
		return a.arguments().size() > b.arguments().size();
	}

	static boolean isLarger(final Expression a, final Expression b) {
		if (a == null || b == null)
			return false;
		if (countNodes(a) > threshold + countNodes(b))
			return true;
		if (isMethodInvocation(a) && isMethodInvocation(b))
			return largerArgsNum((MethodInvocation) a, (MethodInvocation) b);
		return a.getLength() > b.getLength();
	}

	boolean sortInfix(final InfixExpression ie, final AST ast) {
		boolean changed = false;
		if (ie == null || !isFlipable(ie.getOperator())
				|| !ie.hasExtendedOperands())
			return changed;
		final Operator o = ie.getOperator();

		final Expression r = ie.getRightOperand();
		final Expression l = ie.getLeftOperand();
		final List<Expression> eo = ie.extendedOperands();
		// Say we have infix expression with (Left operand) (Right operand) and
		// list of extended operands | e1, e2 ,e3...
		// Infix: (Left = a) (Right = e) | d, b, c, f
		eo.add(0, (Expression) ASTNode.copySubtree(ast, l));
		eo.add(1, (Expression) ASTNode.copySubtree(ast, r));
		// | a, e, d, b, c, f
		changed = changed | sortExpressionList(eo, ast, o);
		// | a, b, c, d, e, f
		ie.setRightOperand((Expression) ASTNode.copySubtree(ast, eo.get(1)));
		ie.setLeftOperand((Expression) ASTNode.copySubtree(ast, eo.get(0)));
		// (Left = a) (Right = b) | a, b, c, d, e, f
		eo.remove(1);
		eo.remove(0);
		// (Left = a) (Right = b) | c, d, e, f
		if (longerFirst(ie)
				&& !inRightOperandExceptions(ie.getRightOperand(), o)
				&& !inInfixExceptions(ie)) {
			set(ie, (Expression) ASTNode.copySubtree(ast, ie.getLeftOperand()),
					flipOperator(o),
					(Expression) ASTNode.copySubtree(ast, ie.getRightOperand()));
			changed = true;
		}
		return changed;
	}

	private boolean moveMethodsToTheBack(final List<Expression> eList,
			final AST ast, final Operator o) {
		boolean changed = false;
		int i = 0;
		final int size = eList.size();
		// Selective bubble sort
		while (i < size) {
			int j = 0;
			while (j + 1 < size) {
				final Expression l = eList.get(j);
				final Expression s = eList.get(j + 1);
				if (isMethodInvocation(l) && !isMethodInvocation(s)
						&& !inOperandExceptions(l, o)
						&& !inOperandExceptions(s, o)) {
					eList.remove(j);
					eList.add(j + 1, (Expression) ASTNode.copySubtree(ast, l));
					changed = true;
				}
				j++;
			}
			i++;
		}
		return changed;
	}

	private boolean sortOperandList(final List<Expression> eList,
			final AST ast, final Operator o) {
		boolean changed = false;
		int i = 0;
		final int size = eList.size();
		i = 0;
		// Bubble sort
		// We cannot use overridden version of Comparator due to the copy
		// ASTNode.copySubtree necessity
		while (i < size) {
			int j = 0;
			while (j + 1 < size) {
				final Expression l = eList.get(j);
				final Expression s = eList.get(j + 1);
				if (isLarger(l, s) && !isMethodInvocation(l)
						&& !isMethodInvocation(s) && !inOperandExceptions(l, o)
						&& !inOperandExceptions(s, o)) {

					eList.remove(j);
					eList.add(j + 1, (Expression) ASTNode.copySubtree(ast, l));
					changed = true;
				}
				j++;
			}
			i++;
		}
		return changed;
	}

	private boolean sortExpressionList(final List<Expression> eList,
			final AST ast, final Operator o) {
		return moveMethodsToTheBack(eList, ast, o)
				| sortOperandList(eList, ast, o);
	}

}
