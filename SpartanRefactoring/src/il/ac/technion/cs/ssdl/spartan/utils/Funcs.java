package il.ac.technion.cs.ssdl.spartan.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * 
 * Useful Functions
 *
 */
public class Funcs{
	/**
	 * @param os an unknown number of parameters
	 * @return true if one of the parameters is a null or false otherwise
	 */
	public static boolean hasNull(final Object... os) {
		for (final Object o : os)
			if (o == null)
				return true;
		return false;
	}

	/**
	 * @param t  the AST in which to create the new variable declaration fragment in
	 * @param r  the ASTRewrite for the given AST
	 * @param varName  the variable name for the new fragment
	 * @param initalizer  the initial value for the new fragment (for the variable)
	 * @return  the new variable declaration fragment or null if one of the given parameter was null
	 */
	public static VariableDeclarationFragment makeVarDeclFrag(final AST t, final ASTRewrite r,
			final SimpleName varName, final Expression initalizer) {
		if (t == null || r == null || varName == null || initalizer == null)
			return null;
		final VariableDeclarationFragment $ = t.newVariableDeclarationFragment();
		$.setInitializer(initalizer.getParent() == null ? initalizer : (Expression) r.createCopyTarget(initalizer));
		$.setName(varName.getParent() == null ? varName : (SimpleName) r.createCopyTarget(varName));
		return $;
	}

	/**
	 * @param t   the AST who is to own the new parenthesized conditional expression
	 * @param r   ASTRewrite for the given AST
	 * @param cond   the condition
	 * @param thenExp   the then statement to set in the conditional
	 * @param elseExp   the else statement to set in the conditional
	 * @return a parenthesized conditional expression
	 */
	public static ParenthesizedExpression makeParenthesizedConditionalExp(final AST t, final ASTRewrite r, final Expression cond,
			final Expression thenExp, final Expression elseExp) {
		if (t==null || r==null || cond==null || thenExp==null || elseExp==null)
			return null;
		final ConditionalExpression $ = t.newConditionalExpression();
		$.setExpression(cond.getParent() == null ? cond : (Expression) r.createCopyTarget(cond));
		$.setThenExpression(thenExp.getParent() == null ? thenExp : (Expression) r.createCopyTarget(thenExp));
		$.setElseExpression(elseExp.getParent() == null ? elseExp : (Expression) r.createCopyTarget(elseExp));
		return Funcs.makeParenthesizedExpression(t, r, $);
	}

	/**
	 * @param t  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param e  the expression to return in the return statement
	 * @return  the new return statement
	 */
	public static ReturnStatement makeReturnStatement(final AST t, final ASTRewrite r, final Expression e) {
		if (t==null || r==null)
			return null;
		final ReturnStatement $ = t.newReturnStatement();
		$.setExpression(e==null || e.getParent() == null ? e : (Expression) r.createCopyTarget(e));
		return $;
	}

	/**
	 * @param t  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param o  the operator for the new infix expression
	 * @param left  the left expression
	 * @param right  the right expression
	 * @return  the new infix expression
	 */
	public static InfixExpression makeInfixExpression(final AST t, final ASTRewrite r, final InfixExpression.Operator o,
			final Expression left, final Expression right) {
		if (t==null || r==null || o==null || right==null || left==null)
			return null;
		final InfixExpression $ = t.newInfixExpression();
		$.setOperator(o);
		$.setRightOperand(right.getParent() == null ? right : (Expression) r.createCopyTarget(right));
		$.setLeftOperand(left.getParent() == null ? left : (Expression) r.createCopyTarget(left));
		return $;
	}

	/**
	 * @param t  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param o  the assignment operator
	 * @param right  right side of the assignment, usually an expression
	 * @param left  left side of the assignment, usually a variable name
	 * @return  the new assignment
	 */
	public static Assignment makeAssigment(final AST t, final ASTRewrite r,
			final Operator o, final Expression right, final Expression left) {
		if (t==null || r==null || o==null || right==null || left==null)
			return null;
		final Assignment $ = t.newAssignment();
		$.setOperator(o);
		$.setRightHandSide(right.getParent() == null ? right : (Expression) r.createCopyTarget(right));
		$.setLeftHandSide(left.getParent() == null ? left : (Expression) r.createCopyTarget(left));
		return $;
	}

	/**
	 * @param t  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param operand  the operand for the new prefix Expression
	 * @param o  the operator for the new prefix Expression
	 * @return  the new prefix expression or null if one of the given parameters was null
	 */
	public static PrefixExpression makePrefixExpression(final AST t, final ASTRewrite r,
			final Expression operand, final PrefixExpression.Operator o) {
		if (t==null || operand==null || o==null)
			return null;
		final PrefixExpression $ = t.newPrefixExpression();
		$.setOperator(o);
		$.setOperand(operand.getParent() == null ? operand : (Expression) r.createCopyTarget(operand));
		return $;
	}

	/**
	 * @param t  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param exp  the expression to put in parenthesis
	 * @return  the given expression with parenthesis
	 */
	public static ParenthesizedExpression makeParenthesizedExpression(final AST t, final ASTRewrite r, final Expression exp) {
		if (t==null || r==null || exp==null)
			return null;
		final ParenthesizedExpression $ = t.newParenthesizedExpression();
		$.setExpression(exp.getParent() == null ? exp : (Expression) r.createCopyTarget(exp));
		return $;
	}

	/**
	 * @param n  a statement to extract an expression from
	 * @return  null if the statement is not an expression or return statement or the expression if they are
	 */
	public static Expression getExpression(final Statement n) {
		if (n==null)
			return null;
		switch(n.getNodeType()){
		case ASTNode.EXPRESSION_STATEMENT: return ((ExpressionStatement) n).getExpression();
		case ASTNode.RETURN_STATEMENT: return ((ReturnStatement) n).getExpression();
		default: return null;
		}
	}

	/**
	 * @param n a statement or a block to extract the expression statement from
	 * @return  the expression statement if n is a block or an expression statement or null if it not an expression
	 * statement or if the block contains more than one statement
	 */
	public static ExpressionStatement getExpressionStatement(final Statement n) {
		if (n == null)
			return null;
		final ASTNode s = n.getNodeType() == ASTNode.BLOCK ? getStmntFromBlock(n) : n;
		return s != null && s.getNodeType() == ASTNode.EXPRESSION_STATEMENT ? (ExpressionStatement) s : null;
	}

	/**
	 * @param n  a statement or block to extract the assignment from
	 * @return  null if the block contains more than one statement or if the statement is not an assignment
	 *  or the assignment if it exists
	 */
	public static Assignment getAssignment(final Statement n) {
		final ExpressionStatement expStmnt=getExpressionStatement(n);
		return expStmnt == null || expStmnt.getExpression().getNodeType() != ASTNode.ASSIGNMENT ? null
				: (Assignment) expStmnt.getExpression();
	}

	/**
	 * @param n  the statement or block to extract the method invocation from
	 * @return  the method invocation if it exists or null if it doesn't or if the block contains
	 *  more than one statement
	 */
	public static MethodInvocation getMethodInvocation(final Statement n) {
		final ExpressionStatement expStmnt=getExpressionStatement(n);
		return expStmnt == null || expStmnt.getExpression().getNodeType() != ASTNode.METHOD_INVOCATION ? null
				: (MethodInvocation) expStmnt.getExpression();
	}

	/**
	 * @param s  the statement or block to check if it is an assignment
	 * @return  true if it is an assignment or false if it is not or if the block
	 * Contains more than one statement
	 */
	public static boolean checkIsAssignment(final Statement s){
		if (s == null || s.getNodeType() == ASTNode.BLOCK && getStmntFromBlock(s) == null)
			return false;
		final ExpressionStatement es = Funcs.getExpressionStatement(getStmntFromBlock(s));
		return es != null && es.getExpression().getNodeType() == ASTNode.ASSIGNMENT;
	}

	/**
	 * @param b  the block to check
	 * @return  true if a return statement exists in the block or false otherwise
	 */
	public static boolean checkReturnStmnt(final Block b){
		if (b==null)
			return false;
		for (int i=0; i < b.statements().size(); i++)
			if (((ASTNode)b.statements().get(i)).getNodeType() == ASTNode.RETURN_STATEMENT)
				return true;
		return false;
	}

	/**
	 * @param b  the block to get the statement from
	 * @return  if b is a block with just 1 statement it returns that statement, if b is statement it returns b
	 * and if b is null it returns a null
	 */
	public static Statement getStmntFromBlock(final Statement b){
		Statement temp = b == null ? null : b;
		if (b!=null && b.getNodeType() == ASTNode.BLOCK && ((Block) b).statements().size() == 1)
			temp = (Statement) ((Block) b).statements().get(0);
		return temp;
	}

	/**
	 * @param s  the statement to check
	 * @return  true if s is a return statement or false otherwise
	 */
	public static boolean checkReturnStmnt(final Statement s){
		return s != null && s.getNodeType() == ASTNode.BLOCK ? checkReturnStmnt((Block)s) : s!=null && s.getNodeType() == ASTNode.RETURN_STATEMENT;
	}

	/**
	 * @param s  the statement or block to get the number of statements in
	 * @return  0 is s is null, 1 if s is a statement or the number of statement in the block is s is a block
	 */
	public static int getNumOfStmnts(final Statement s){
		if (s != null)
			return s.getNodeType() != ASTNode.BLOCK ? 1 : ((Block)s).statements().size();
		return 0;
	}


	/**
	 * adds nextReturn to the end of the then block if addToThen is true or to the else block otherwise
	 * 
	 * @param ast  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param node  the if statement to add the return to
	 * @param nextReturn  the return statement to add
	 * @param addToThen  boolean value to decide on which block to add the return statement to
	 */
	public static void addReturnStmntToIf(final AST ast, final ASTRewrite r,
			final IfStatement node, final ReturnStatement nextReturn, final boolean addToThen) {
		if (ast==null || r==null || node==null || nextReturn==null)
			return;
		final IfStatement newNode=(IfStatement)ASTNode.copySubtree(ast, node);
		final ReturnStatement newReturn=(ReturnStatement)ASTNode.copySubtree(ast, nextReturn);
		((Block)(addToThen ? newNode.getThenStatement() : newNode.getElseStatement())).statements().add(newReturn);
		r.replace(node, newNode, null);
		r.remove(nextReturn, null);
	}

	/**
	 * Extracts a return statement from a node. Expression, and the Expression
	 * contains Assignment.
	 * 
	 * @param s
	 *          The node from which to return statement assignment.
	 * @return null if it is not possible to extract the return statement.
	 */
	public static ReturnStatement getReturnStatement(final Statement s) {
		if (s == null)
			return null;
		if (s.getNodeType() == ASTNode.RETURN_STATEMENT)
			return (ReturnStatement) s;
		if (s.getNodeType() != ASTNode.BLOCK)
			return null;
		return getReturnStatement((Block) s);
	}

	/**
	 * @param b  the block to get the return statement from
	 * @return  null if the block contains more than one statement or the return statement otherwise
	 */
	public static ReturnStatement getReturnStatement(final Block b) {
		return b!=null && b.statements().size() == 1 ? getReturnStatement((ASTNode) b.statements().get(0)) : null;
	}

	/**
	 * @param s  the ASTNode to extract the return statement from
	 * @return  null if the node is not a return statement or the node if he is a return statement
	 */
	public static ReturnStatement getReturnStatement(final ASTNode s) {
		return s!=null && s.getNodeType() != ASTNode.RETURN_STATEMENT ? null : (ReturnStatement) s;
	}

	/**
	 * @param s  the variable/s declaration from which to extract the proper fragment
	 * @param name  the name by which to look for the fragment
	 * @return   the fragment if such with the given name exists or null otherwise (or if s or name are null)
	 */
	public static VariableDeclarationFragment getVarDeclFrag(final Statement s, final Expression name) {
		if (s != null && name!=null && s.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT
				&& name.getNodeType() == ASTNode.SIMPLE_NAME)
			for (final Object o : ((VariableDeclarationStatement) s).fragments())
				if (((SimpleName) name).toString().equals(((VariableDeclarationFragment) o).getName().toString()))
					return (VariableDeclarationFragment) o;
		return null;
	}

	/**
	 * String wise comparison of all the given SimpleNames
	 * 
	 * @param cmpTo  a string to compare all names to
	 * @param name  SimplesNames to compare by their string value to cmpTo
	 * @return  true if all names are the same (string wise) or false otherwise
	 */
	public static boolean cmpSimpleNames(final Expression cmpTo, final Expression... name) {
		if (cmpTo == null || name == null || cmpTo.getNodeType() != ASTNode.SIMPLE_NAME)
			return false;
		for (final Expression s : name)
			if (s==null
			|| s.getNodeType() != ASTNode.SIMPLE_NAME
			|| !((SimpleName) s).getIdentifier().equals(((SimpleName)cmpTo).getIdentifier()))
				return false;
		return true;
	}

	/**
	 * @param cmpTo  the assignment operator to compare all to
	 * @param op  A unknown number of assignments operators
	 * @return  true if all the operator are the same or false otherwise
	 */
	public static boolean cmpAsgnOps(final Assignment.Operator cmpTo, final Assignment.Operator... op) {
		if (cmpTo == null || op == null)
			return false;
		for (final Assignment.Operator o : op)
			if (o==null || cmpTo != o)
				return false;
		return true;
	}

	/**
	 * the function checks if all the given assignments has the same left hand side(variable) and operator
	 * 
	 * @param cmpTo  The assignment to compare all others to
	 * @param asgns  The assignments to compare
	 * @return  true if all assignments has the same left hand side and operator as the first one
	 * or false otherwise
	 */
	public static boolean cmpAsgns(final Assignment cmpTo, final Assignment... asgns) {
		if (cmpTo == null || asgns == null)
			return false;
		for (final Assignment asgn : asgns)
			if (asgn==null
			|| !cmpAsgnOps(cmpTo.getOperator(), asgn.getOperator())
			|| !cmpSimpleNames(cmpTo.getLeftHandSide(), asgn.getLeftHandSide()))
				return false;
		return true;
	}

	/**
	 * the function receives a condition and the then boolean value and returns the proper condition (its negation
	 * if thenValue is false)
	 * 
	 * @param t  the AST who is to own the new return statement
	 * @param r  ASTRewrite for the given AST
	 * @param cond  the condition to try to negate
	 * @param thenValue  the then value
	 * @return  the original condition if thenValue was true or its negation if it was false (or null if any of the given
	 * parameter were null)
	 */
	public static Expression tryToNegateCond(final AST t, final ASTRewrite r, final Expression cond, final boolean thenValue) {
		if (t == null || cond == null)
			return null;
		if (thenValue)
			return cond;
		return makePrefixExpression(t, r, makeParenthesizedExpression(t, r, cond), PrefixExpression.Operator.NOT);
	}
}