package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.*;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;
import il.ac.technion.cs.ssdl.spartan.utils.replacerVisitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
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
			final ReturnStatement nextReturn = stmts.size() > ifIdx + 1 ? getReturnStatement((Statement) stmts.get(ifIdx + 1)) : null;
			if (nextReturn != null) {
				final int numOfStmntInThen = getNumOfStmnts(node.getThenStatement());
				final int numOfStmntInElse = getNumOfStmnts(node.getElseStatement());
				if (checkReturnStmnt(node.getThenStatement())) {
					if (numOfStmntInThen == 1 && numOfStmntInElse == 0) {
						final ReturnStatement thenRet = getReturnStatement(node.getThenStatement());
						if (thenRet.getExpression().getNodeType() == ASTNode.BOOLEAN_LITERAL)
							r.replace(
									node,
									makeReturnStatement(
											ast,
											r,
											makeParenthesizedExpression(ast, r,
													tryToNegateCond(ast, r, node.getExpression(), ((BooleanLiteral) thenRet.getExpression()).booleanValue()))),
													null);
						else
							r.replace(
									node,
									makeReturnStatement(
											ast,
											r,
											makeParenthesizedConditionalExp(ast, r, node.getExpression(), thenRet.getExpression(),
													nextReturn.getExpression())), null);
						r.remove(nextReturn, null);
					} else
						addReturnStmntToIf(ast, r, node, nextReturn, false);
					return true;
				} else if (checkReturnStmnt(node.getElseStatement())) {
					if (numOfStmntInThen == 0 && numOfStmntInElse == 1) {
						final ReturnStatement elseRet = getReturnStatement(node.getElseStatement());
						r.replace(
								node,
								makeReturnStatement(
										ast,
										r,
										makeParenthesizedConditionalExp(ast, r, node.getExpression(), nextReturn.getExpression(),
												elseRet.getExpression())), null);
						r.remove(nextReturn, null);
					} else
						addReturnStmntToIf(ast, r, node, nextReturn, true);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @author Tomer
	 * 
	 *         contains both sides for the conditional expression
	 */
	public static class TwoExpressions {
		final Expression thenExp;
		final Expression elseExp;
		/**
		 * Instantiates a then class with the given Expressions
		 * 
		 * @param t
		 *          then Expression
		 * @param e
		 *          else Expression
		 */
		public TwoExpressions(final Expression t, final Expression e) {
			thenExp = t;
			elseExp = e;
		}
	}
	static boolean treatIfSameExpStmntOrRet(final AST ast, final ASTRewrite r, final IfStatement ifStmt) {
		final Statement thenStatment = getStmntFromBlock(ifStmt.getThenStatement());
		final Statement elseStatment = getStmntFromBlock(ifStmt.getElseStatement());
		final List<String> thenNames = new ArrayList<String>();
		final List<String> elseNames = new ArrayList<String>();
		if (hasNull(thenStatment, elseStatment))
			return false;
		if (thenStatment.subtreeMatch(matcher, elseStatment)) {
			r.replace(ifStmt, thenStatment, null);
			return true;
		}
		if (!thenNames.equals(elseNames) || thenStatment.getNodeType() != elseStatment.getNodeType())
			return false;
		if (thenStatment.getNodeType() != ASTNode.RETURN_STATEMENT && thenStatment.getNodeType() != ASTNode.EXPRESSION_STATEMENT)
			return false;
		final TwoExpressions diff = findSingleDifference1(thenStatment, elseStatment);
		if (diff == null)
			return false;
		final int ifIdx = ((Block) ifStmt.getParent()).statements().indexOf(ifStmt);
		final int possiblePrevDeclIdx = ifIdx - 1 >= 0 ? ifIdx - 1 : ifIdx;
		final Statement prevDecl = (Statement) ((Block) ifStmt.getParent()).statements().get(possiblePrevDeclIdx);
		return substitute(ast, r, ifStmt, diff, prevDecl);
	}
	private static TwoExpressions findSingleDifference1(final Statement thenStmnt, final Statement elseStmnt) {
		final List<ASTNode> thenExps = collectExpressions(thenStmnt);
		final List<ASTNode> elseExps = collectExpressions(elseStmnt);
		if (thenStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			final ExpressionStatement thenExpStmnt = getExpressionStatement(thenStmnt);
			final ExpressionStatement elseExpStmnt = getExpressionStatement(elseStmnt);
			final int thenIndex = thenExps.indexOf(thenExpStmnt.getExpression()) - 1;
			final int elseIndex = elseExps.indexOf(elseExpStmnt.getExpression()) - 1;
			final Expression thenCondSide = thenIndex < 0 ? null : (Expression) thenExps.get(thenIndex);
			final Expression elseCondSide = elseIndex < 0 ? null : (Expression) elseExps.get(elseIndex);
			if (thenCondSide != null && elseCondSide != null) {
				if (cmpAsgns(getAssignment(thenExpStmnt), getAssignment(elseExpStmnt))) {
					final Assignment asgnThen = getAssignment(thenExpStmnt);
					final Assignment asgnElse = getAssignment(elseExpStmnt);
					if (asgnThen.getOperator() == Assignment.Operator.ASSIGN)
						if (asgnThen.getRightHandSide().getNodeType() == ASTNode.BOOLEAN_LITERAL)
							return new TwoExpressions(asgnThen.getRightHandSide(), asgnElse.getRightHandSide());
				}
				return new TwoExpressions(thenCondSide, elseCondSide);
			}
		} else {
			final ReturnStatement thenRet = getReturnStatement(thenStmnt);
			final ReturnStatement elseRet = getReturnStatement(elseStmnt);
			return new TwoExpressions(thenRet.getExpression(), elseRet.getExpression());
		}
		return null;
	}
	private static boolean substitute(final AST t, final ASTRewrite r, final IfStatement ifStmnt, final TwoExpressions diff,
			final Statement possiblePrevDecl) {
		final Statement thenStmnt = getStmntFromBlock(ifStmnt.getThenStatement());
		final Statement elseStmnt = getStmntFromBlock(ifStmnt.getElseStatement());
		if (checkIsAssignment(thenStmnt) && checkIsAssignment(elseStmnt)) {
			if (!cmpAsgns(getAssignment(thenStmnt), getAssignment(elseStmnt)))
				return false;
			final Assignment asgnThen = getAssignment(thenStmnt);
			final VariableDeclarationFragment prevDecl = getVarDeclFrag(possiblePrevDecl, asgnThen.getLeftHandSide());
			if (asgnThen.getOperator() == Assignment.Operator.ASSIGN && prevDecl != null) {
				if (asgnThen.getRightHandSide().getNodeType() == ASTNode.BOOLEAN_LITERAL)
					r.replace(
							prevDecl,
							makeVarDeclFrag(t, r, prevDecl.getName(),
									tryToNegateCond(t, r, ifStmnt.getExpression(), ((BooleanLiteral) diff.thenExp).booleanValue())), null);
				else
					r.replace(
							prevDecl,
							makeVarDeclFrag(t, r, prevDecl.getName(),
									makeParenthesizedConditionalExp(t, r, ifStmnt.getExpression(), diff.thenExp, diff.elseExp)), null);
				r.remove(ifStmnt, null);
				return true;
			}
		}
		if (thenStmnt.getNodeType() == ASTNode.RETURN_STATEMENT) {
			final ReturnStatement ret = t.newReturnStatement();
			if (diff.thenExp.getNodeType() == ASTNode.BOOLEAN_LITERAL)
				ret.setExpression(tryToNegateCond(t, r, ifStmnt.getExpression(), ((BooleanLiteral) diff.thenExp).booleanValue()));
			else
				ret.setExpression(makeParenthesizedConditionalExp(t, r, ifStmnt.getExpression(), diff.thenExp, diff.elseExp));
			r.replace(ifStmnt, ret, null);
			return true;
		}
		final Expression thenExp = ((ExpressionStatement) thenStmnt).getExpression();
		thenStmnt.accept(new replacerVisitor(thenExp, r, makeParenthesizedConditionalExp(t, r, ifStmnt.getExpression(), diff.thenExp,
				diff.elseExp), diff.thenExp));
		final ExpressionStatement es = t.newExpressionStatement((Expression) r.createCopyTarget(thenExp));
		r.replace(ifStmnt, es, null);
		return true;
	}
	static boolean treatAssignIfAssign(final AST ast, final ASTRewrite r, final IfStatement node) {
		final ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.BLOCK) {
			@SuppressWarnings("rawtypes")
			final List stmts = ((Block) parent).statements();
			final int ifIdx = stmts.indexOf(node);
			if (ifIdx >= 1) {
				final Assignment asgnThen = getAssignment(node.getThenStatement());
				if (asgnThen == null || node.getElseStatement() != null)
					return false;
				final Assignment prevAsgn = getAssignment((Statement) stmts.get(ifIdx - 1));
				final Assignment nextAsgn = stmts.size() > ifIdx + 1 ? getAssignment((Statement) stmts.get(ifIdx + 1)) : null;
				VariableDeclarationFragment prevDecl = null;
				if (prevAsgn != null) {
					if (ifIdx - 2 >= 0 && cmpSimpleNames(asgnThen.getLeftHandSide(), prevAsgn.getLeftHandSide()))
						prevDecl = getVarDeclFrag((Statement) stmts.get(ifIdx - 2), asgnThen.getLeftHandSide());
				} else if (nextAsgn != null) {
					if (ifIdx - 1 >= 0 && cmpSimpleNames(asgnThen.getLeftHandSide(), nextAsgn.getLeftHandSide()))
						prevDecl = getVarDeclFrag((Statement) stmts.get(ifIdx - 1), nextAsgn.getLeftHandSide());
				} else if (ifIdx - 1 >= 0)
					prevDecl = getVarDeclFrag((Statement) stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
				if (prevAsgn != null && nextAsgn != null) {
					if (cmpAsgns(nextAsgn, prevAsgn, asgnThen))
						if (prevDecl == null) {
							r.replace(prevAsgn.getParent(), nextAsgn.getParent(), null);
							r.remove(node, null);
							r.remove(nextAsgn.getParent(), null);
							return true;
						} else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN) {
							r.replace(prevDecl.getInitializer(), nextAsgn.getRightHandSide(), null);
							r.remove(node, null);
							r.remove(prevAsgn.getParent(), null);
							r.remove(nextAsgn.getParent(), null);
							return true;
						}
				} else if (prevAsgn != null && !dependsOn(node.getExpression(), prevAsgn.getLeftHandSide())) {
					if (cmpAsgns(prevAsgn, asgnThen) && !checkIsAssignment(prevAsgn.getRightHandSide()))
						if (prevDecl == null) {
							rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, prevAsgn.getRightHandSide());
							r.remove(prevAsgn.getParent(), null);
							return true;
						} else if (!dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())
								&& !dependsOn(prevAsgn.getRightHandSide(), prevDecl.getName())) {
							if (asgnThen.getOperator() == Assignment.Operator.ASSIGN) {
								r.replace(
										prevDecl,
										makeVarDeclFrag(
												ast,
												r,
												(SimpleName) prevAsgn.getLeftHandSide(),
												makeParenthesizedConditionalExp(ast, r, node.getExpression(), asgnThen.getRightHandSide(),
														prevAsgn.getRightHandSide())), null);
								r.remove(node, null);
								r.remove(prevAsgn.getParent(), null);
								return true;
							}
						} else if (prevDecl.getInitializer() != null) {
							rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, prevAsgn.getRightHandSide());
							r.remove(prevAsgn.getParent(), null);
							return true;
						}
				} else if (nextAsgn != null) {
					if (cmpAsgns(nextAsgn, asgnThen)) {
						if (prevDecl == null) {
							if (!checkIsAssignment(nextAsgn.getRightHandSide()))
								rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, nextAsgn.getRightHandSide());
						} else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN
								&& !dependsOn(nextAsgn.getRightHandSide(), prevDecl.getName())) {
							r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()),
									null);
							r.remove(node, null);
						} else
							rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, nextAsgn.getRightHandSide());
						r.remove(nextAsgn.getParent(), null);
						return true;
					}
				} else if (prevDecl != null && prevDecl.getInitializer() != null && node.getElseStatement() == null)
					if (!dependsOn(node.getExpression(), prevDecl.getName()) && !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())) {
						r.replace(
								prevDecl,
								makeVarDeclFrag(
										ast,
										r,
										prevDecl.getName(),
										makeParenthesizedConditionalExp(ast, r, node.getExpression(), asgnThen.getRightHandSide(),
												prevDecl.getInitializer())), null);
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
			newAsgn = makeAssigment(t, r, asgnThen.getOperator(),
					makeParenthesizedConditionalExp(t, r, n.getExpression(), asgnThen.getRightHandSide(), otherAsgnExp),
					asgnThen.getLeftHandSide());
		else
			newAsgn = makeAssigment(
					t,
					r,
					asgnThen.getOperator(),
					makeParenthesizedConditionalExp(t, r, n.getExpression(),
							makeInfixExpression(t, r, InfixExpression.Operator.PLUS, asgnThen.getRightHandSide(), otherAsgnExp), otherAsgnExp),
							asgnThen.getLeftHandSide());
		r.replace(n, t.newExpressionStatement(newAsgn), null);
	}
	static Range detectIfReturn(final IfStatement n) {
		return detectIfReturn(n, statements(n.getParent()));
	}
	private static Range detectIfReturn(final IfStatement n, final List<ASTNode> ss) {
		if (ss == null)
			return null;
		final int ifIdx = ss.indexOf(n);
		if (ss.size() > ifIdx + 1) {
			final ReturnStatement nextReturn = getReturnStatement((Statement) ss.get(ifIdx + 1));
			final ReturnStatement thenSide = getReturnStatement(n.getThenStatement());
			final ReturnStatement elseSide = getReturnStatement(n.getElseStatement());
			if (nextReturn != null && (thenSide != null && elseSide == null || thenSide == null && elseSide != null))
				return new Range(n, nextReturn);
		}
		return null;
	}
	static Range detectIfSameExpStmntOrRet(final IfStatement n) {
		final Statement thenStatement = n.getThenStatement();
		final Statement elseStatement = n.getElseStatement();
		if (hasNull(thenStatement, elseStatement) || thenStatement.getNodeType() != elseStatement.getNodeType())
			return null;
		if (getNumOfStmnts(elseStatement) == 1 && getNumOfStmnts(thenStatement) == 1) {
			final List<String> thenNames = collectNames(getStmntFromBlock(thenStatement));
			final List<String> elseNames = collectNames(getStmntFromBlock(thenStatement));
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
			if (ifIdx >= 1 || stmts.size() > ifIdx + 1) {
				final Assignment asgnThen = getAssignment(node.getThenStatement());
				if (asgnThen == null || node.getElseStatement() != null)
					return null;
				final Assignment prevAssignment = getAssignment((Statement) stmts.get(ifIdx - 1 >= 0 ? ifIdx - 1 : 0));
				final Assignment nextAssignment = getAssignment((Statement) stmts.get(ifIdx + 1 > stmts.size() - 1 ? stmts.size() - 1
						: ifIdx + 1));
				final VariableDeclarationFragment prevDecl = getVarDeclFrag(prevAssignment != null ? (Statement) stmts.get(ifIdx - 2)
						: (Statement) stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
				if (prevAssignment != null && nextAssignment != null) {
					if (cmpAsgns(nextAssignment, prevAssignment, asgnThen)) {
						if (prevDecl != null)
							return !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, nextAssignment) : null;
							return new Range(prevAssignment, nextAssignment);
					}
				} else if (prevAssignment != null && !dependsOn(node.getExpression(), prevAssignment.getLeftHandSide())) {
					if (cmpAsgns(prevAssignment, asgnThen)) {
						if (prevDecl != null && prevDecl.getInitializer() == null)
							return !dependsOn(prevAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, node) : null;
							return new Range(prevAssignment, node);
					}
				} else if (nextAssignment != null) {
					if (cmpAsgns(nextAssignment, asgnThen)) {
						if (prevDecl != null && !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()))
							return new Range(prevDecl, nextAssignment);
						return new Range(node, nextAssignment);
					}
				} else if (prevDecl != null && prevDecl.getInitializer() != null)
					return !dependsOn(node.getExpression(), prevDecl.getName())
							&& !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, node) : null;
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
