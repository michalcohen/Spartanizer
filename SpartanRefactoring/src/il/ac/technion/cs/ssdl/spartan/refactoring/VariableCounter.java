package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public enum VariableCounter {
	USES {

		@Override
		public List<Expression> list(final ASTNode n, final SimpleName v) {
			final List<Expression> $ = new ArrayList<Expression>();

			n.accept(new ASTVisitor() {
				@Override
				public boolean visit(final InfixExpression node) {
					$.addAll(listSingle(node.getRightOperand(),v));
					$.addAll(listSingle(node.getLeftOperand(),v));
					for (final Object item : node.extendedOperands())
						$.addAll(listSingle((Expression)item, v));
					return true;
				}
				
				@Override
				public boolean visit(final PrefixExpression node) {
					$.addAll(listSingle(node.getOperand(),v));
					return true;
				}
				
				@Override
				public boolean visit(final PostfixExpression node) {
					$.addAll(listSingle(node.getOperand(),v));
					return true;
				}
				
				@Override
				public boolean visit(ParenthesizedExpression node) {
					$.addAll(listSingle(node.getExpression(),v));
					return true;
				}
				
				@Override
				public boolean visit(final Assignment node) {
					$.addAll(listSingle(node.getRightHandSide(),v));
					return true;
				}
				
				@Override
				public boolean visit(final CastExpression node) {
					$.addAll(listSingle(node.getExpression(),v));
					return true;
				}
				
				@Override
				public boolean visit(final ArrayAccess node) {
					$.addAll(listSingle(node.getArray(),v));
					return true;
				}
				
				@Override
				public boolean visit(final MethodInvocation node) {
					$.addAll(listSingle(node.getExpression(),v));
					for (final Object arg : node.arguments())
						$.addAll(listSingle((Expression)arg,v));
					return true;
				}
				
				@Override
				public boolean visit(final ConstructorInvocation node) {
					for (final Object arg : node.arguments())
						$.addAll(listSingle((Expression)arg,v));
					return true;					
				}
				
				@Override
				public boolean visit(final ClassInstanceCreation node) {
					for (final Object arg : node.arguments())
						$.addAll(listSingle((Expression)arg,v));
					return true;										
				};
				
				@Override
				public boolean visit(final ArrayCreation node) {
					for (final Object dim : node.dimensions())
						$.addAll(listSingle((Expression)dim, v));
					return true;
				};
				
				@Override
				public boolean visit(final ArrayInitializer node) {
					for (final Object item : node.expressions())
						$.addAll(listSingle((Expression)item, v));
					return true;
				};
				
				@Override
				public boolean visit(final ReturnStatement node) {
					$.addAll(listSingle(node.getExpression(), v));
					return true;
				};
				
				@Override
				public boolean visit(final FieldAccess node) {
					$.addAll(listSingle(node.getExpression(), v));
					return true;
				};
				
				public boolean visit(final VariableDeclarationFragment node) {
					$.addAll(listSingle(node.getInitializer(), v));
					return true;
				};
				
				@Override
				public boolean visit(final IfStatement node) {
					$.addAll(listSingle(node.getExpression(), v));
					return true;
				};

				@Override
				public boolean visit(final SwitchStatement node) {
					$.addAll(listSingle(node.getExpression(), v));
					return true;
				};
				
				@Override
				public boolean visit(final ForStatement node) {
					$.addAll(listSingle(node.getExpression(), v));
					return true;
				};
			});
			return $;
		}},
	ASSIGNMENTS {

		@Override
		public List<Expression> list(final ASTNode n, final SimpleName v) {
			final List<Expression> $ = new ArrayList<Expression>();

			n.accept(new ASTVisitor() {
				@Override
				public boolean visit(final Assignment node) {
					$.addAll(listSingle(node.getLeftHandSide(),v));
					return true;
				}
				
				@Override
				public boolean visit(final VariableDeclarationFragment node) {
					$.addAll(listSingle(node.getName(),v));
					return true;
				};
			});
			return $;
		}},
		BOTH {

			@Override
			public List<Expression> list(final ASTNode n, final SimpleName v) {
				final List<Expression> $ = new ArrayList<Expression>(USES.list(n, v));
				$.addAll(ASSIGNMENTS.list(n, v));
				Collections.sort($, new Comparator<Expression>() {

					public int compare(final Expression e1, final Expression e2) {
						return e1.getStartPosition() - e2.getStartPosition();
					}
				});
				return $;
			}};
	public abstract List<Expression> list(ASTNode n, SimpleName v);
		
	private static List<Expression> listSingle(final Expression e, SimpleName v) {
		final List<Expression> $ = new ArrayList<Expression>();
		if (e instanceof SimpleName && ((SimpleName)e).getIdentifier().equals(v.getIdentifier()))
			$.add(e);
		return $;
	}
}
