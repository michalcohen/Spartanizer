package il.ac.technion.cs.ssdl.spartan.refactoring;


import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class RedundantEqualityRefactoring extends BaseRefactoring {	
	@Override
	public String getName() {
		return "Remove Redundant Equality";
	}
	
	@Override
	protected ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m) {
		pm.beginTask("Creating rewrite operation...", 1);
		
		final AST ast = cu.getAST();
		final ASTRewrite rewrite = ASTRewrite.create(ast);
		
		cu.accept(new ASTVisitor() {
			public boolean visit(InfixExpression node) {
				if ((m==null) && isNodeOutsideSelection(node))
					return true;
				if (m!=null && isNodeOutsideMarker(node, m))
					return true;
				
				if(node.getOperator() != Operator.EQUALS && node.getOperator() != Operator.NOT_EQUALS)
					return true;
				
				ASTNode nonliteral = null;
				BooleanLiteral literal = null;
				if (node.getRightOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL && node.getLeftOperand().getNodeType() != ASTNode.BOOLEAN_LITERAL) {
					nonliteral = rewrite.createMoveTarget(node.getLeftOperand());
					literal = (BooleanLiteral) node.getRightOperand();
				} else if (node.getLeftOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL && node.getRightOperand().getNodeType() != ASTNode.BOOLEAN_LITERAL) {
					nonliteral = rewrite.createMoveTarget(node.getRightOperand());
					literal = (BooleanLiteral) node.getLeftOperand();	
				} else
					return true;
				
				
				ASTNode newnode = null;
				if((literal.booleanValue() && node.getOperator() == Operator.EQUALS) || 
				  (!literal.booleanValue() && node.getOperator() == Operator.NOT_EQUALS)) {
					newnode = nonliteral;
				} else {
					ParenthesizedExpression paren = ast.newParenthesizedExpression();
					paren.setExpression((Expression) nonliteral);
					newnode = ast.newPrefixExpression();
					((PrefixExpression)newnode).setOperand(paren);
					((PrefixExpression)newnode).setOperator(PrefixExpression.Operator.NOT);
				}
				
				rewrite.replace(node, newnode, null);
				return true;
			}
		});
		pm.done();
		return rewrite;
	}

	@Override
	public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
		final Collection<SpartanizationRange> $ = new ArrayList<>();
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(final InfixExpression node) {
				if (node.getOperator() != Operator.EQUALS
						&& node.getOperator() != Operator.NOT_EQUALS)
					return true;
				if (node.getRightOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
						|| node.getLeftOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL) {
					$.add(new SpartanizationRange(node));
				}
				return true;
			}
		});
		return $;
	}
}
