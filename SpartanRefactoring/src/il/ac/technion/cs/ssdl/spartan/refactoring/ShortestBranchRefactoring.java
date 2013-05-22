package il.ac.technion.cs.ssdl.spartan.refactoring;


import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ShortestBranchRefactoring extends BaseRefactoring {	
	@Override
	public String getName() {
		return "Shortest Conditional Branch First";
	}
	
	/**
	 * Count number of nodes in the tree of which node is root.
	 * @param node The node.
	 * @return Number of ast nodes under the node.
	 */
	private int countNodes(ASTNode node) {
		final AtomicInteger c = new AtomicInteger(0);
		node.accept(new ASTVisitor() {
			public void preVisit(ASTNode node) {
				c.incrementAndGet();
			}
		});
		
		return c.get();		
	}
	
	@Override
	protected ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m) {
		pm.beginTask("Creating rewrite operation...", 1);
		
		final AST ast = cu.getAST();
		final ASTRewrite rewrite = ASTRewrite.create(ast);
		
		cu.accept(new ASTVisitor() {
			public boolean visit(IfStatement node) {
				if ((m==null) && isNodeOutsideSelection(node))
					return true;
				if (m!=null && isNodeOutsideMarker(node, m))
					return true;
				
				if (node.getElseStatement() == null)
					return true;
				
				int thenCount = countNodes(node.getThenStatement());
				int elseCount = countNodes(node.getElseStatement());
				
				if(thenCount - elseCount <= -threshold)
					return true;
					
				IfStatement newnode = ast.newIfStatement();	
				Expression neg = negateExpression(ast, rewrite, node.getExpression());	
				newnode.setExpression(neg);
					
				newnode.setThenStatement((org.eclipse.jdt.core.dom.Statement) rewrite.createMoveTarget(node.getElseStatement()));
				newnode.setElseStatement((org.eclipse.jdt.core.dom.Statement) rewrite.createMoveTarget(node.getThenStatement()));	
				
				rewrite.replace(node, newnode, null);						
				return true;
			}
			
			public boolean visit(ConditionalExpression node) {
				if (isNodeOutsideSelection(node))
					return true;
				
				if (node.getElseExpression() == null)
					return true;
				
				if(node.getThenExpression().getLength() - node.getElseExpression().getLength() <= -threshold)
					return true;
				
				ConditionalExpression newnode = ast.newConditionalExpression();	
				Expression neg = negateExpression(ast, rewrite, node.getExpression());	
				newnode.setExpression(neg);
					
				newnode.setThenExpression((Expression) rewrite.createMoveTarget(node.getElseExpression()));
				newnode.setElseExpression((Expression) rewrite.createMoveTarget(node.getThenExpression()));	
				
				rewrite.replace(node, newnode, null);						
				
				return true;
			}
		});
		pm.done();
		return rewrite;
	}

	/**
	 * @return Returns a prefix expression that is the negation of the provided expression.
	 */
	private Expression negateExpression(final AST ast,
			final ASTRewrite rewrite, Expression exp) {
		Expression negatedComparison = null;
		if (exp instanceof InfixExpression && (negatedComparison=tryNegateComparison(ast,rewrite,(InfixExpression)exp))!=null)
			return negatedComparison;
		Expression negatedNot = null;
		if (exp instanceof PrefixExpression && (negatedNot=tryNegatePrefix(rewrite,(PrefixExpression)exp))!=null)
			return negatedNot;
		ParenthesizedExpression paren = ast.newParenthesizedExpression();
		paren.setExpression((Expression) rewrite.createCopyTarget(exp));
		PrefixExpression neg = ast.newPrefixExpression();
		neg.setOperand(paren);
		neg.setOperator(PrefixExpression.Operator.NOT);
		return neg;
	}
	
	private Expression tryNegateComparison(final AST ast, final ASTRewrite rewrite, InfixExpression exp) {
		final InfixExpression $ = ast.newInfixExpression();
		$.setRightOperand((Expression) rewrite.createCopyTarget(exp.getRightOperand()));
		$.setLeftOperand((Expression) rewrite.createCopyTarget(exp.getLeftOperand()));
		if (exp.getOperator().equals(Operator.EQUALS)) {			
			$.setOperator(Operator.NOT_EQUALS);
			return $;
		}
		if (exp.getOperator().equals(Operator.NOT_EQUALS)) {			
			$.setOperator(Operator.EQUALS);
			return $;
		}
		if (exp.getOperator().equals(Operator.GREATER)) {			
			$.setOperator(Operator.LESS_EQUALS);
			return $;
		}
		if (exp.getOperator().equals(Operator.GREATER_EQUALS)) {			
			$.setOperator(Operator.LESS);
			return $;
		}
		if (exp.getOperator().equals(Operator.LESS)) {			
			$.setOperator(Operator.GREATER_EQUALS);
			return $;
		}
		if (exp.getOperator().equals(Operator.LESS_EQUALS)) {			
			$.setOperator(Operator.GREATER);
			return $;
		}
		return null;
	}
	
	private Expression tryNegatePrefix(final ASTRewrite rewrite, PrefixExpression exp) {
		if (exp.getOperator().equals(PrefixExpression.Operator.NOT))
			return (Expression) rewrite.createCopyTarget(exp.getOperand());
		return null;
	}
	
	private static final int threshold = 1;

	@Override
	public Collection<SpartanizationRange> checkForSpartanization(CompilationUnit cu) {
		final Collection<SpartanizationRange> $ = new ArrayList<>();
		cu.accept(new ASTVisitor() {
			public boolean visit(IfStatement node) {
				if (isNodeOutsideSelection(node))
					return true;
				
				if (node.getElseStatement() == null)
					return true;
				
				int thenCount = countNodes(node.getThenStatement());
				int elseCount = countNodes(node.getElseStatement());
				
				if(thenCount - elseCount > threshold)
					$.add(new SpartanizationRange(node));
				return true;
			}
			
			public boolean visit(ConditionalExpression node) {
				if (isNodeOutsideSelection(node))
					return true;
				
				if (node.getElseExpression() == null)
					return true;
				
				if(node.getThenExpression().getLength() - node.getElseExpression().getLength() > threshold)
					$.add(new SpartanizationRange(node));
				return true;
			}
		});
		return $;
	}
}
