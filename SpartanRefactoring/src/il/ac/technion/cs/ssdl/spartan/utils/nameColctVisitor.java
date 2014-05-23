package il.ac.technion.cs.ssdl.spartan.utils;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * @author Tomer
 *	
 *	A visitor to collect the name of the command (Return/ExpressionStatement) to later on check
 *  if it is the same command in the Else and Then Statements 
 */
public class nameColctVisitor extends ASTVisitor{
	  List<String> Names = null;
	  /**
	 * @param list
	 * 
	 * Instantiates the class.
	 * Sets the list to insert the names to, to the given list. 
	 */
	public nameColctVisitor(List<String> list){
		  super();
		  Names = list;
	  }
	  @SuppressWarnings("unused")
	  @Override public boolean visit(final ReturnStatement ret){
		  Names.add("return");
		  return false;
	  }
	  @Override public void endVisit(final SimpleName name){
		  if (name.getParent().getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT ||
				  name.getParent().getNodeType() == ASTNode.QUALIFIED_NAME){
			  Names.add(name.getIdentifier());
		  }
	  }
	  @Override public void endVisit(final Assignment asgn){
		  Names.add(asgn.getOperator().toString());
	  }
}