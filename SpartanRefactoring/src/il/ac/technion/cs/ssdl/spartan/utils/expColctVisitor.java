package il.ac.technion.cs.ssdl.spartan.utils;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Statement;

/**
 * @author Tomer
 *
 *  A visitor to collect all of the expressions in the Then and Else statements
 */
public class expColctVisitor extends ASTVisitor{
	Statement stmtToLookFor = null; 
	List<ASTNode> exps = null;
	 /**
	* @param list the list to add the expressions to
	* @param s the statement to to which we stop if we get to
	* 
	* Instantiates the class.
	* Sets the list to insert the expressions to, to the given list.
	*/
	public expColctVisitor(final List<ASTNode> list, final Statement s){
		  super();
		  exps = list;
		  stmtToLookFor = s;
	  }
	  @Override public void endVisit(final StringLiteral s){
		  ASTNode temp = s;
		  while (!temp.equals(stmtToLookFor)){
			  exps.add(temp);
			  temp=temp.getParent();
		  }
	  }
	  @Override public void endVisit(final NumberLiteral n){
		  ASTNode temp = n;
		  while (!temp.equals(stmtToLookFor)){
			  exps.add(temp);
			  temp=temp.getParent();
		  }
	  }
	  @Override public void endVisit(final BooleanLiteral n){
		  ASTNode temp = n;
		  while (!temp.equals(stmtToLookFor)){
			  exps.add(temp);
			  temp=temp.getParent();
		  }
	  }
	  @Override public void endVisit(final InfixExpression ie){
		  ASTNode temp = ie;
		  while (!temp.equals(stmtToLookFor)){
			  exps.add(temp);
			  temp=temp.getParent();
		  }
	  }
	  @Override public void endVisit(final MethodInvocation mi){
		  ASTNode temp = mi;
		  while (!temp.equals(stmtToLookFor)){
			  exps.add(temp);
			  temp=temp.getParent();
		  }
	  }
  }