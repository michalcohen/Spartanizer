package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.utils.Funcs;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;
import il.ac.technion.cs.ssdl.spartan.utils.expColctVisitor;
import il.ac.technion.cs.ssdl.spartan.utils.nameColctVisitor;
import il.ac.technion.cs.ssdl.spartan.utils.replacerVisitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3)
 * 
 * @since 2013/01/01
 */
public class Ternarize extends Spartanization {
	/** Instantiates this class */
	public Ternarize() {
		super("Ternarize", "Convert conditional to an expression using the ternary (?:) operator");
	}

	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final IfStatement n) {
				return !inRange(m, n) || //
						treatAssignIfAssign(t, r, n) || //
						treatIfReturn(t, r, n) || //
						treatIfSameExpStmntOrRet(t, r, n) || //
						true;
			}
		});
	}

	static List<ASTNode> statements(final ASTNode n) {
		return n.getNodeType() != ASTNode.BLOCK ? null : statements((Block) n);
	}

	static List<ASTNode> statements(final Block b) {
		return b.statements();
	}

	static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement node) {
		final ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.BLOCK) {
			@SuppressWarnings("rawtypes")
			final List stmts = ((Block) parent).statements();
			final int ifIdx = stmts.indexOf(node);
			final ReturnStatement nextReturn = stmts.size() > ifIdx + 1 ? Funcs.getReturnStatement((Statement) stmts.get(ifIdx + 1)) : null;
			if (nextReturn != null) {
				final int numOfStmntInThen=Funcs.getNumOfStmnts(node.getThenStatement());
				final int numOfStmntInElse=Funcs.getNumOfStmnts(node.getElseStatement());
				if (Funcs.checkReturnStmnt(node.getThenStatement())) {
					if (numOfStmntInThen == 1 && numOfStmntInElse == 0){
						final ReturnStatement thenRet=Funcs.getReturnStatement(node.getThenStatement());
						if (thenRet.getExpression().getNodeType() == ASTNode.BOOLEAN_LITERAL)
							r.replace(node, Funcs.makeReturnStatement(ast, r,
									Funcs.makeParenthesizedExpression(ast, r, Funcs.tryToNegateCond(ast, r, node.getExpression(), ((BooleanLiteral)thenRet.getExpression()).booleanValue()))), null);
						else
							r.replace(node, Funcs.makeReturnStatement(ast, r, Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(),
									thenRet.getExpression(), nextReturn.getExpression())), null);
						r.remove(nextReturn, null);
					} else
						Funcs.addReturnStmntToIf(ast, r, node, nextReturn, false);
					return true;
				} else if (Funcs.checkReturnStmnt(node.getElseStatement())) {
					if (numOfStmntInThen == 0 && numOfStmntInElse == 1){
						final ReturnStatement elseRet=Funcs.getReturnStatement(node.getElseStatement());
						r.replace(node, Funcs.makeReturnStatement(ast, r, Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(),
								nextReturn.getExpression(), elseRet.getExpression())), null);
						r.remove(nextReturn, null);
					} else
						Funcs.addReturnStmntToIf(ast, r, node, nextReturn, true);
					return true;
				}
			}
		}
		return false;
	}

	static boolean treatIfSameExpStmntOrRet(final AST ast, final ASTRewrite r, final IfStatement node){
		final ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.BLOCK) {
			final Statement thenStmnt = Funcs.getStmntFromBlock(node.getThenStatement());
			final Statement elseStmnt = Funcs.getStmntFromBlock(node.getElseStatement());
			final List<String> thenNames = new ArrayList<String>();
			final List<String> elseNames = new ArrayList<String>();
			if (Funcs.hasNull(thenStmnt, elseStmnt))
				return false;
			if (thenStmnt.subtreeMatch(matcher, elseStmnt)){
				r.replace(node, Funcs.getStmntFromBlock(node.getThenStatement()), null);
				return true;
			}
			thenStmnt.accept(new nameColctVisitor(thenNames));
			elseStmnt.accept(new nameColctVisitor(elseNames));
			if (!thenNames.equals(elseNames))
				return false;
			final List<ASTNode> thenExps = new ArrayList<ASTNode>();
			final List<ASTNode> elseExps = new ArrayList<ASTNode>();
			node.getThenStatement().accept(new expColctVisitor(thenExps, thenStmnt));
			node.getElseStatement().accept(new expColctVisitor(elseExps, elseStmnt));
			if (thenStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT && elseStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT){
				final ExpressionStatement thenExpStmnt = Funcs.getExpressionStatement(node.getThenStatement());
				final ExpressionStatement elseExpStmnt = Funcs.getExpressionStatement(node.getElseStatement());
				final int thenIndex = thenExps.indexOf(thenExpStmnt.getExpression())-1;
				final int elseIndex = elseExps.indexOf(elseExpStmnt.getExpression())-1;
				final Expression thenCondSide = thenIndex < 0 ? null : (Expression)thenExps.get(thenIndex);
				final Expression elseCondSide = elseIndex < 0 ? null : (Expression)elseExps.get(elseIndex);
				if (thenCondSide != null && elseCondSide != null){
					if (Funcs.checkIsAssignment(thenExpStmnt) && Funcs.checkIsAssignment(elseExpStmnt)){
						if (!Funcs.cmpAsgns(Funcs.getAssignment(thenExpStmnt), Funcs.getAssignment(elseExpStmnt)))
							return false;
						final Assignment asgnThen = Funcs.getAssignment(thenExpStmnt);
						final int ifIdx = ((Block)parent).statements().indexOf(node);
						final int possiblePrevDeclIdx = ifIdx-1 >= 0 ? ifIdx-1 : ifIdx;
						final VariableDeclarationFragment prevDecl = Funcs.getVarDeclFrag((Statement) ((Block)parent).statements().get(possiblePrevDeclIdx), asgnThen.getLeftHandSide());
						if (asgnThen.getOperator() == Assignment.Operator.ASSIGN && prevDecl != null){
							if (asgnThen.getRightHandSide().getNodeType() == ASTNode.BOOLEAN_LITERAL)
								r.replace(prevDecl, Funcs.makeVarDeclFrag(ast, r, prevDecl.getName(), Funcs.tryToNegateCond(ast, r, node.getExpression(), ((BooleanLiteral)asgnThen.getRightHandSide()).booleanValue())), null);
							else
								r.replace(prevDecl, Funcs.makeVarDeclFrag(ast, r, prevDecl.getName(), Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(), thenCondSide, elseCondSide)), null);
							r.remove(node, null);
							return true;
						}
					}
					thenExpStmnt.accept(new replacerVisitor(thenExpStmnt.getExpression(), r,
							Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(), thenCondSide, elseCondSide), thenCondSide));
					final ExpressionStatement es = ast.newExpressionStatement((Expression) r.createCopyTarget(thenExpStmnt.getExpression()));
					r.replace(node, es, null);
					return true;
				}
			} else if (thenStmnt.getNodeType() == ASTNode.RETURN_STATEMENT){
				final ReturnStatement thenRet = Funcs.getReturnStatement(node.getThenStatement());
				final ReturnStatement elseRet = Funcs.getReturnStatement(node.getElseStatement());
				final ReturnStatement ret = ast.newReturnStatement();
				if (thenRet.getExpression().getNodeType() == ASTNode.BOOLEAN_LITERAL)
					ret.setExpression(Funcs.tryToNegateCond(ast, r, node.getExpression(), ((BooleanLiteral)thenRet.getExpression()).booleanValue()));
				else
					ret.setExpression(Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(), thenRet.getExpression(), elseRet.getExpression()));
				r.replace(node, ret, null);
				return true;
			}
		}
		return false;
	}

	static boolean treatAssignIfAssign(final AST ast, final ASTRewrite r, final IfStatement node) {
		final ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.BLOCK) {
			@SuppressWarnings("rawtypes")
			final List stmts = ((Block) parent).statements();
			final int ifIdx = stmts.indexOf(node);
			if (ifIdx >= 1) {
				final Assignment asgnThen = Funcs.getAssignment(node.getThenStatement());
				if (asgnThen == null || node.getElseStatement() != null)
					return false;
				final Assignment prevAsgn = Funcs.getAssignment((Statement) stmts.get(ifIdx - 1));
				final Assignment nextAsgn = stmts .size() > ifIdx + 1 ? Funcs.getAssignment((Statement) stmts.get(ifIdx + 1)) : null;
				VariableDeclarationFragment prevDecl = null;
				if (prevAsgn != null){
					if (ifIdx - 2 >= 0 && Funcs.cmpSimpleNames(asgnThen.getLeftHandSide(),prevAsgn.getLeftHandSide()))
						prevDecl = Funcs.getVarDeclFrag((Statement) stmts.get(ifIdx - 2), asgnThen.getLeftHandSide());
				} else if (nextAsgn != null){
					if (ifIdx - 1 >= 0 && Funcs.cmpSimpleNames(asgnThen.getLeftHandSide(),nextAsgn.getLeftHandSide()))
						prevDecl = Funcs.getVarDeclFrag((Statement) stmts.get(ifIdx - 1), nextAsgn.getLeftHandSide());
				} else if (ifIdx - 1 >= 0)
					prevDecl = Funcs.getVarDeclFrag((Statement) stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
				if (prevAsgn != null && nextAsgn != null){
					if (Funcs.cmpAsgns(nextAsgn, prevAsgn, asgnThen))
						if (prevDecl == null){
							r.replace(prevAsgn.getParent(), nextAsgn.getParent(), null);
							r.remove(node, null);
							r.remove(nextAsgn.getParent(), null);
							return true;
						} else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN){
							r.replace(prevDecl.getInitializer(), nextAsgn.getRightHandSide(), null);
							r.remove(node, null);
							r.remove(prevAsgn.getParent(), null);
							r.remove(nextAsgn.getParent(), null);
							return true;
						}
				} else if (prevAsgn != null && !dependsOn(node.getExpression(), prevAsgn.getLeftHandSide())){
					if (Funcs.cmpAsgns(prevAsgn, asgnThen) && !Funcs.checkIsAssignment(prevAsgn.getRightHandSide()))
						if (prevDecl == null){
							rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, prevAsgn.getRightHandSide());
							r.remove(prevAsgn.getParent(), null);
							return true;
						} else if (!dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())
								&& !dependsOn(prevAsgn.getRightHandSide(), prevDecl.getName())){
							if (asgnThen.getOperator() == Assignment.Operator.ASSIGN){
								r.replace(prevDecl, Funcs.makeVarDeclFrag(ast, r, (SimpleName) prevAsgn.getLeftHandSide(),
										Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(), asgnThen.getRightHandSide(), prevAsgn.getRightHandSide())), null);
								r.remove(node, null);
								r.remove(prevAsgn.getParent(), null);
								return true;
							}
						} else if (prevDecl.getInitializer() != null){
							rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, prevAsgn.getRightHandSide());
							r.remove(prevAsgn.getParent(), null);
							return true;
						}
				} else if (nextAsgn != null){
					if (Funcs.cmpAsgns(nextAsgn, asgnThen))
						if (prevDecl == null){
							if (!Funcs.checkIsAssignment(nextAsgn.getRightHandSide())){
								rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, nextAsgn.getRightHandSide());
								r.remove(nextAsgn.getParent(), null);
								return true;
							}
						} else if (!dependsOn(nextAsgn.getRightHandSide(), prevDecl.getName())){
							if (asgnThen.getOperator() == Assignment.Operator.ASSIGN){
								r.remove(nextAsgn.getParent(), null);
								r.replace(prevDecl, Funcs.makeVarDeclFrag(ast, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()), null);
								r.remove(node, null);
							} else {
								rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, nextAsgn.getRightHandSide());
								r.remove(nextAsgn.getParent(), null);
							}
							return true;
						}
				} else if (prevDecl != null && prevDecl.getInitializer() != null && node.getElseStatement() == null)
					if (!dependsOn(node.getExpression(), prevDecl.getName()) && !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())){
						r.replace(prevDecl, Funcs.makeVarDeclFrag(ast, r, prevDecl.getName(),
								Funcs.makeParenthesizedConditionalExp(ast, r, node.getExpression(), asgnThen.getRightHandSide(), prevDecl.getInitializer())), null);
						r.remove(node, null);
						return true;
					}
			}
		}
		return false;
	}

	private static void rewriteAssignIfAssignToAssignTernary(final AST t, final ASTRewrite r, final IfStatement n,
			final Assignment asgnThen, final Expression otherAsgnExp) {
		final Assignment newAsgn;
		if (asgnThen.getOperator() == Assignment.Operator.ASSIGN)
			newAsgn = Funcs.makeAssigment(t, r, asgnThen.getOperator(),
					Funcs.makeParenthesizedConditionalExp(t, r, n.getExpression(), asgnThen.getRightHandSide(), otherAsgnExp),
					asgnThen.getLeftHandSide());
		else
			newAsgn = Funcs.makeAssigment(t, r, asgnThen.getOperator(),
					Funcs.makeParenthesizedConditionalExp(t, r, n.getExpression(),
							Funcs.makeInfixExpression(t, r, InfixExpression.Operator.PLUS, asgnThen.getRightHandSide(), otherAsgnExp), otherAsgnExp),
							asgnThen.getLeftHandSide());
		r.replace(n, t.newExpressionStatement(newAsgn), null);
	}

	static Range detectIfReturn(final IfStatement n) {
		final ASTNode parent = n.getParent();
		final List<ASTNode> ss = statements(parent);
		if (ss == null)
			return null;
		final int ifIdx = ss.indexOf(n);
		if (ss.size() > ifIdx + 1) {
			final ReturnStatement nextReturn = Funcs.getReturnStatement((Statement) ss.get(ifIdx + 1));
			final ReturnStatement thenSide = Funcs.getReturnStatement(n.getThenStatement());
			final ReturnStatement elseSide = Funcs.getReturnStatement(n.getElseStatement());
			if (nextReturn != null && (thenSide != null && elseSide == null || thenSide == null && elseSide != null))
				return new Range(n, nextReturn);
		}
		return null;
	}

	static Range detectIfSameExpStmntOrRet(final IfStatement n) {
		if (Funcs.hasNull(n.getThenStatement(), n.getElseStatement())
				|| n.getThenStatement().getNodeType() != n.getElseStatement().getNodeType())
			return null;
		final int numOfStmntsInElse=Funcs.getNumOfStmnts(n.getElseStatement());
		final int numOfStmntsInThen=Funcs.getNumOfStmnts(n.getThenStatement());
		if (numOfStmntsInElse == 1 && numOfStmntsInThen == 1){
			final List<String> thenNames = new ArrayList<String>();
			final List<String> elseNames = new ArrayList<String>();
			final Statement thenStmt = Funcs.getStmntFromBlock(n.getThenStatement());
			final Statement elseStmt = Funcs.getStmntFromBlock(n.getElseStatement());
			thenStmt.accept(new nameColctVisitor(thenNames));
			elseStmt.accept(new nameColctVisitor(elseNames));
			return thenNames.equals(elseNames) ? new Range(n) : null;
		}
		return null;
	}

	static Range detectAssignIfAssign(final IfStatement node) {
		final ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.BLOCK) {
			@SuppressWarnings("rawtypes")
			final List stmts = ((Block) parent).statements();
			final int ifIdx = stmts.indexOf(node);
			if (ifIdx >= 1 || stmts.size() > ifIdx + 1){
				final Assignment asgnThen = Funcs.getAssignment(node.getThenStatement());
				if (asgnThen == null || node.getElseStatement() != null)
					return null;
				final Assignment prevAssignment = Funcs.getAssignment((Statement) stmts.get(ifIdx - 1 >= 0 ? ifIdx - 1 : 0));
				final Assignment nextAssignment = Funcs.getAssignment((Statement) stmts.get(ifIdx + 1 > stmts.size()-1 ? stmts.size()-1 : ifIdx + 1));
				final VariableDeclarationFragment prevDecl = Funcs.getVarDeclFrag(prevAssignment!=null ? (Statement) stmts.get(ifIdx-2) : (Statement) stmts.get(ifIdx-1), asgnThen.getLeftHandSide());
				if (prevAssignment != null && nextAssignment != null){
					if (Funcs.cmpAsgns(nextAssignment, prevAssignment, asgnThen)){
						if (prevDecl != null)
							return !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl,nextAssignment) : null;
							return new Range(prevAssignment, nextAssignment);
					}
				} else if (prevAssignment != null && !dependsOn(node.getExpression(), prevAssignment.getLeftHandSide())){
					if (Funcs.cmpAsgns(prevAssignment, asgnThen)){
						if (prevDecl != null && prevDecl.getInitializer() == null)
							return !dependsOn(prevAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, node) : null;
							return new Range(prevAssignment,node);
					}
				} else if (nextAssignment != null){
					if (Funcs.cmpAsgns(nextAssignment, asgnThen)){
						if (prevDecl != null && !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()))
							return new Range(prevDecl,nextAssignment);
						return new Range(node,nextAssignment);
					}
				} else if (prevDecl != null && prevDecl.getInitializer() != null)
					return !dependsOn(node.getExpression(), prevDecl.getName())
							&& !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())
							? new Range(prevDecl,node) : null;
			}
		}
		return null;
	}

	private static boolean dependsOn(final Expression e, final Expression leftHandSide) {
		return Occurrences.BOTH_SEMANTIC.of(leftHandSide).in(e).size() > 0;
	}

	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final IfStatement n) {
				return perhaps(detectAssignIfAssign(n)) || //
						perhaps(detectIfReturn(n)) || //
						perhaps(detectIfSameExpStmntOrRet(n)) || //
						true;
			}

			private boolean perhaps(final Range r) {
				return r != null && add(r);
			}

			private boolean add(final Range r) {
				opportunities.add(r);
				return true;
			}
		};
	}

	private static final ASTMatcher matcher = new ASTMatcher();
}
