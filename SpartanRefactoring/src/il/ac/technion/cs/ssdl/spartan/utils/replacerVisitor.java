package il.ac.technion.cs.ssdl.spartan.utils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Tomer
 *
 *	A visitor to replace the proper expression for a conditional expression
 */
public class replacerVisitor extends ASTVisitor{
	final ASTRewrite r;
	final Expression thenExp;
	final ParenthesizedExpression condExp;
	final Expression thenMethod;
	/**
	 * @param t  is just in case the ExpressionStatement is a method invocation and it is used
	 * 			 to prevent the replacer from replacing the entire ExpressionStatement because these might
	 * 			 be method invocation in the arguments the needs to be replaced by a conditional expression
	 * @param re  	ASTRewrite in order to place the proper nodes
	 * @param cExp  	the conditional expression to replace the proper node with
	 * @param tExp 	 	is the expression to be replaced by the conditional expression
	 * 
	 * Instantiates this class.
	 * 
	 */
	public replacerVisitor(final Expression t ,final ASTRewrite re, final ParenthesizedExpression cExp, final Expression tExp){
		condExp = cExp;
		thenExp = tExp;
		r = re;
		thenMethod = t;
	}
	@Override public void endVisit(final StringLiteral s){
		ASTNode temp = s;
		while (!temp.equals(thenExp))
			temp=temp.getParent();
		r.replace(temp, condExp, null);
	}
	@Override public void endVisit(final NumberLiteral n){
		ASTNode temp = n;
		while (!temp.equals(thenExp))
			temp=temp.getParent();
		r.replace(temp, condExp, null);
	}
	@Override public void endVisit(final BooleanLiteral bool){
		ASTNode temp = bool;
		while (!temp.equals(thenExp))
			temp=temp.getParent();
		r.replace(temp, condExp, null);
	}
	@Override public void endVisit(final InfixExpression ie){
		ASTNode temp = ie;
		while (!temp.equals(thenExp))
			temp=temp.getParent();
		r.replace(temp, condExp, null);
	}
	@Override public boolean visit(final MethodInvocation mi){
		if (thenMethod.equals(mi))
			return true;
		ASTNode temp = mi;
		while (!temp.equals(thenExp))
			temp=temp.getParent();
		r.replace(temp, condExp, null);
		return false;
	}
}