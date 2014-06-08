package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.asReturn;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.checkIfReturnStmntExist;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.compatabileName;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.compatible;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.getAssignment;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.getBlockSingleStatement;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.getChildren;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.getExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.getVarDeclFrag;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.hasNull;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isAssignment;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isConditional;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeAssigment;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeInfixExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeParenthesizedConditionalExp;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeReturnStatement;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeVarDeclFrag;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.statements;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.statementsCount;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.tryToNegateCond;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

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
import org.eclipse.jdt.core.dom.MethodInvocation;
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
			@Override public boolean visit(final IfStatement ifStmnt) {
				return !inRange(m, ifStmnt) || //
						treatAssignIfAssign(t, r, ifStmnt) || //
						treatIfReturn(t, r, ifStmnt) || //
						treatIfSameExpStmntOrRet(t, r, ifStmnt) || //
						true;
			}
		});
	}
	static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement ifStmnt) {
		final Block parent = asBlock(ifStmnt.getParent());
		return parent != null && treatIfReturn(ast, r, ifStmnt, parent);
	}
	private static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement ifStmnt, final Block parent) {
		if (!checkIfReturnStmntExist(ifStmnt.getThenStatement()))
			return false;
		final ReturnStatement nextRet = nextStatement(statements(parent), statements(parent).indexOf(ifStmnt));
		return nextRet != null && 1 == statementsCount(ifStmnt.getThenStatement()) && 0 == statementsCount(ifStmnt.getElseStatement())
				&& rewriteIfToRetStmnt(ast, r, ifStmnt, nextRet);
	}
	private static ReturnStatement nextStatement(final List<ASTNode> stmts, final int ns) {
		return stmts.size() <= ns + 1 ? null : asReturn(stmts.get(ns + 1));
	}
	private static boolean rewriteIfToRetStmnt(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final ReturnStatement nextReturn) {
		final ReturnStatement thenRet = asReturn(ifStmnt.getThenStatement());
		if (isConditional(thenRet.getExpression(), nextReturn.getExpression()))
			return false;
		r.replace(
				ifStmnt,
				makeReturnStatement(ast, r,
						determineNewExp(ast, r, ifStmnt.getExpression(), thenRet.getExpression(), nextReturn.getExpression())), null);
		r.remove(nextReturn, null);
		return true;
	}
	/**
	 * @author Tomer Zeltzer
	 * 
	 *         contains both sides for the conditional expression
	 */
	public static class TwoExpressions {
		final Expression thenExp;
		final Expression elseExp;
		/**
		 * Instantiates the class with the given Expressions
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
	/**
	 * @author Tomer Zeltzer
	 * 
	 *         contains 2 nodes (used to store the 2 nodes that are different in
	 *         the then and else tree)
	 */
	public static class TwoNodes {
		ASTNode thenNode;
		ASTNode elseNode;
		/**
		 * Instantiates the class with the given nodes
		 * 
		 * @param t
		 *          then node
		 * @param e
		 *          else node
		 */
		public TwoNodes(final ASTNode t, final ASTNode e) {
			thenNode = t;
			elseNode = e;
		}
	}
	static boolean treatIfSameExpStmntOrRet(final AST ast, final ASTRewrite r, final IfStatement ifStmt) {
		if (null == asBlock(ifStmt.getParent()))
			return false;
		final Statement thenStatment = getBlockSingleStatement(ifStmt.getThenStatement());
		final Statement elseStatment = getBlockSingleStatement(ifStmt.getElseStatement());
		if (hasNull(thenStatment, elseStatment) || thenStatment.getNodeType() != elseStatment.getNodeType())
			return false;
		if (thenStatment.subtreeMatch(matcher, elseStatment)) {
			r.replace(ifStmt, thenStatment, null);
			return true;
		}
		final TwoExpressions diffExp = findSingleDifference(thenStatment, elseStatment);
		if (diffExp == null)
			return false;
		final int ifIdx = statements(ifStmt.getParent()).indexOf(ifStmt);
		return substitute(ast, r, ifStmt, diffExp, (Statement) statements(ifStmt.getParent()).get(0 > ifIdx - 1 ? ifIdx : ifIdx - 1));
	}
	private static TwoExpressions findSingleDifference(final Statement thenStmnt, final Statement elseStmnt) {
		TwoNodes diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		final TwoExpressions $ = findDiffExps(thenStmnt, elseStmnt, diffNodes);
		if ($ == null)
			return null;
		if (!isExpressionOrReturn(thenStmnt))
			handleCaseDiffNodesAreBlocks(diffNodes);
		else
			diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		if (diffNodes.thenNode.getNodeType() != diffNodes.elseNode.getNodeType() || !isExpressionOrReturn(diffNodes.thenNode))
			return null;
		if (isExpression(diffNodes.thenNode) && checkIfOnlyDiffIsExp(diffNodes.thenNode, diffNodes.elseNode))
			return $;
		return ASTNode.RETURN_STATEMENT != diffNodes.thenNode.getNodeType() ? null : new TwoExpressions(
				getExpression(diffNodes.thenNode), getExpression(diffNodes.elseNode));
	}
	private static TwoExpressions findDiffExps(final Statement thenStmnt, final Statement elseStmnt, final TwoNodes diffNodes) {
		TwoNodes tempNodes = diffNodes;
		if (!isExpressionOrReturn(thenStmnt)) {
			if (!isOnlyDiff(thenStmnt, elseStmnt, tempNodes) || !handleCaseDiffNodesAreBlocks(tempNodes))
				return null;
			if (isExpression(tempNodes.thenNode))
				tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
			tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
		} else if (isExpression(thenStmnt))
			tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
		return tempNodes == null || isConditional(tempNodes.thenNode, tempNodes.elseNode) ? null
				: new TwoExpressions((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode);
	}
	private static boolean isOnlyDiff(final Statement thenStmnt, final Statement elseStmnt, final TwoNodes diffNodes) {
		if (hasNull(thenStmnt, elseStmnt, diffNodes))
			return false;
		final List<ASTNode> thenNodes = getChildren(thenStmnt);
		thenNodes.remove(diffNodes.thenNode);
		thenNodes.removeAll(getChildren(diffNodes.thenNode));
		final List<ASTNode> elseNodes = getChildren(elseStmnt);
		elseNodes.remove(diffNodes.elseNode);
		elseNodes.removeAll(getChildren(diffNodes.elseNode));
		return thenNodes.toString().equals(elseNodes.toString());
	}

	private static boolean handleCaseDiffNodesAreBlocks(final TwoNodes diffNodes) {
		if (1 != statementsCount(diffNodes.thenNode) && 1 != statementsCount(diffNodes.elseNode))
			return false;
		diffNodes.thenNode = getBlockSingleStatement(asBlock(diffNodes.thenNode));
		diffNodes.elseNode = getBlockSingleStatement(asBlock(diffNodes.elseNode));
		return true;
	}
	private static TwoNodes findDiffNodes(final ASTNode thenNode, final ASTNode elseNode) {
		if (hasNull(thenNode, elseNode))
			return null;
		final List<ASTNode> thenList = getChildren(thenNode);
		final List<ASTNode> elseList = getChildren(elseNode);
		for (int idx = 0; idx < thenList.size() && idx < elseList.size(); idx++)
			if (!thenList.get(idx).toString().equals(elseList.get(idx).toString()))
				return new TwoNodes(thenList.get(idx), elseList.get(idx));
		return null;
	}
	private static boolean substitute(final AST t, final ASTRewrite r, final IfStatement ifStmnt, final TwoExpressions diff,
			final Statement possiblePrevDecl) {
		final Statement thenStmnt = getBlockSingleStatement(ifStmnt.getThenStatement());
		final Statement elseStmnt = getBlockSingleStatement(ifStmnt.getElseStatement());
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		final Expression newExp = determineNewExp(t, r, ifStmnt.getExpression(), diff.thenExp, diff.elseExp);
		if (!isExpressionOrReturn(thenStmnt))
			diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		if (isAssignment(diffNodes.thenNode) && isAssignment(diffNodes.elseNode))
			if (!compatible(getAssignment((Statement) diffNodes.thenNode), getAssignment((Statement) diffNodes.elseNode)))
				return false;
			else if (handleSubIfDiffAreAsgns(t, r, ifStmnt, diff.thenExp, possiblePrevDecl, thenStmnt, diffNodes.thenNode, newExp))
				return true;
		r.replace(diff.thenExp, newExp, null);
		r.replace(ifStmnt, r.createCopyTarget(thenStmnt), null);
		return true;
	}
	private static boolean handleSubIfDiffAreAsgns(final AST t, final ASTRewrite r, final IfStatement ifStmnt,
			final Expression thenExp, final Statement possiblePrevDecl, final Statement thenStmnt, final ASTNode thenNode,
			final Expression newExp) {
		final Assignment asgnThen = getAssignment((Statement) thenNode);
		if (!isPlainAssignment(asgnThen))
			return false;
		final VariableDeclarationFragment v = getVarDeclFrag(possiblePrevDecl, asgnThen.getLeftHandSide());
		return isExpression(thenStmnt) && v!=null ? replace(t, r, ifStmnt, newExp, v) : replace(r, ifStmnt, thenExp, thenStmnt, newExp);
	}
	private static boolean isPlainAssignment(final Assignment a) {
		return a.getOperator() == Assignment.Operator.ASSIGN;
	}
	private static boolean replace(final ASTRewrite r, final IfStatement ifStmnt,
			final Expression thenExp, final Statement thenStmnt,
			final Expression newExp) {
		r.replace(thenExp, newExp, null);
		r.replace(ifStmnt, r.createCopyTarget(thenStmnt), null);
		return true;
	}
	private static boolean replace(final AST t, final ASTRewrite r,
			final IfStatement ifStmnt, final Expression newExp,
			final VariableDeclarationFragment prevDecl) {
		r.replace(prevDecl, makeVarDeclFrag(t, r, prevDecl.getName(), newExp), null);
		r.remove(ifStmnt, null);
		return true;
	}
	private static Expression determineNewExp(final AST t, final ASTRewrite r, final Expression cond, final Expression thenExp,
			final Expression elseExp) {
		return thenExp.getNodeType() == ASTNode.BOOLEAN_LITERAL && elseExp.getNodeType() == ASTNode.BOOLEAN_LITERAL ? tryToNegateCond(
				t, r, cond, ((BooleanLiteral) thenExp).booleanValue()) : makeParenthesizedConditionalExp(t, r, cond, thenExp, elseExp);
	}
	static boolean treatAssignIfAssign(final AST ast, final ASTRewrite r, final IfStatement ifStmnt) {
		final ASTNode parent = ifStmnt.getParent();
		if (!isBlock(parent))
			return false;
		final List<ASTNode> stmts = ((Block) parent).statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		final Assignment asgnThen = getAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || null != ifStmnt.getElseStatement() || ifIdx < 1)
			return false;
		final Assignment prevAsgn = getAssignment((Statement) stmts.get(ifIdx - 1));
		final Assignment nextAsgn = stmts.size() <= ifIdx + 1 ? null : getAssignment((Statement) stmts.get(ifIdx + 1));
		final VariableDeclarationFragment prevDecl = findPrevDecl(stmts, ifIdx, asgnThen, prevAsgn, nextAsgn);
		return tryHandleNextAndPrevAsgnExist(r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl) //
				|| tryHandleOnlyPrevAsgnExist(ast, r, ifStmnt, asgnThen, prevAsgn, prevDecl) //
				|| tryHandleOnlyNextAsgnExist(ast, r, ifStmnt, asgnThen, nextAsgn, prevDecl) //
				|| tryHandleNoNextNoPrevAsgn(ast, r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl);
	}
	private static boolean tryHandleNoNextNoPrevAsgn(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isNoNextNoPrevAsgnPossible(ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl))
			return false;
		r.replace(
				prevDecl,
				makeVarDeclFrag(
						ast,
						r,
						prevDecl.getName(),
						makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(), asgnThen.getRightHandSide(), prevDecl.getInitializer())),
						null);
		r.remove(ifStmnt, null);
		return true;
	}
	private static boolean isNoNextNoPrevAsgnPossible(final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		return prevAsgn == null //
				&& nextAsgn == null //
				&& !isConditional(asgnThen.getRightHandSide()) //
				&& prevDecl != null //
				&& null != prevDecl.getInitializer() //
				&& null == ifStmnt.getElseStatement() //
				&& !isConditional(prevDecl.getInitializer()) //
				&& !dependsOn(prevDecl.getName(), ifStmnt.getExpression(), asgnThen.getRightHandSide()); //
	}
	private static boolean tryHandleOnlyNextAsgnExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isOnlyNextAsgnPossible(asgnThen, nextAsgn)
				|| nextAsgn.getRightHandSide().toString().equals(asgnThen.getRightHandSide().toString()))
			return false;
		if (prevDecl == null) {
			if (!isAssignment(nextAsgn.getRightHandSide()))
				r.remove(ifStmnt, null);
		} else if (isPlainAssignment(asgnThen) && !dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide())) {
			r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()), null);
			r.remove(ifStmnt, null);
			r.remove(nextAsgn.getParent(), null);
		} else
			handleNoPrevDecl(ast, r, ifStmnt, asgnThen, nextAsgn);
		return true;
	}
	private static boolean isOnlyNextAsgnPossible(final Assignment asgnThen, final Assignment nextAsgn) {
		return nextAsgn != null && compatible(nextAsgn, asgnThen)
				&& !isConditional(nextAsgn.getRightHandSide(), asgnThen.getRightHandSide())
				&& !asgnThen.getRightHandSide().toString().equals(nextAsgn.getRightHandSide().toString());
	}
	private static boolean tryHandleOnlyPrevAsgnExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isOnlyPrevAsgnPossible(ifStmnt, asgnThen, prevAsgn)
				|| prevAsgn.getRightHandSide().toString().equals(asgnThen.getRightHandSide().toString()))
			return false;
		if (prevDecl == null) {
			handleNoPrevDecl(ast, r, ifStmnt, asgnThen, prevAsgn);
			return true;
		} else if (handlePrevDeclExist(ast, r, ifStmnt, asgnThen, prevAsgn, prevDecl))
			return true;
		return false;
	}
	private static boolean isOnlyPrevAsgnPossible(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment prevAsgn) {
		return prevAsgn != null && !dependsOn(prevAsgn.getLeftHandSide(), ifStmnt.getExpression())
				&& !isConditional(prevAsgn.getRightHandSide(), asgnThen.getRightHandSide()) && !isAssignment(prevAsgn.getRightHandSide())
				&& compatible(prevAsgn, asgnThen);
	}
	private static boolean handlePrevDeclExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
		if (!dependsOn(prevDecl.getName(), asgnThen.getRightHandSide(), prevAsgn.getRightHandSide())
				&& isPlainAssignment(asgnThen)) {
			r.replace(
					prevDecl,
					makeVarDeclFrag(
							ast,
							r,
							(SimpleName) prevAsgn.getLeftHandSide(),
							makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(), asgnThen.getRightHandSide(),
									prevAsgn.getRightHandSide())), null);
			r.remove(ifStmnt, null);
			r.remove(prevAsgn.getParent(), null);
			return true;
		} else if (null != prevDecl.getInitializer()) {
			handleNoPrevDecl(ast, r, ifStmnt, asgnThen, prevAsgn);
			return true;
		}
		return false;
	}
	private static void handleNoPrevDecl(final AST ast, final ASTRewrite r, final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn) {
		rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, prevAsgn.getRightHandSide());
		r.remove(prevAsgn.getParent(), null);
	}
	private static boolean tryHandleNextAndPrevAsgnExist(final ASTRewrite r, final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isNextAndPrevAsgnPossible(asgnThen, prevAsgn, nextAsgn))
			return false;
		if (prevDecl == null)
			r.replace(prevAsgn.getParent(), nextAsgn.getParent(), null);
		else if (isPlainAssignment(asgnThen)) {
			r.replace(prevDecl.getInitializer(), nextAsgn.getRightHandSide(), null);
			r.remove(prevAsgn.getParent(), null);
		}
		r.remove(ifStmnt, null);
		r.remove(nextAsgn.getParent(), null);
		return true;
	}
	private static boolean isNextAndPrevAsgnPossible(final Assignment asgnThen, final Assignment prevAsgn, final Assignment nextAsgn) {
		return !hasNull(prevAsgn, nextAsgn) && compatible(nextAsgn, prevAsgn, asgnThen)
				&& !isConditional(prevAsgn.getRightHandSide(), nextAsgn.getRightHandSide(), asgnThen.getRightHandSide());
	}
	private static VariableDeclarationFragment findPrevDecl(final List<ASTNode> stmts, final int ifIdx, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn) {
		VariableDeclarationFragment $ = null;
		if (prevAsgn != null) {
			if (0 <= ifIdx - 2 && compatabileName(asgnThen.getLeftHandSide(), prevAsgn.getLeftHandSide()))
				$ = getVarDeclFrag(stmts.get(ifIdx - 2), asgnThen.getLeftHandSide());
		} else if (nextAsgn == null) {
			if (0 <= ifIdx - 1)
				$ = getVarDeclFrag(stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
		} else if (0 <= ifIdx - 1 && compatabileName(asgnThen.getLeftHandSide(), nextAsgn.getLeftHandSide()))
			$ = getVarDeclFrag(stmts.get(ifIdx - 1), nextAsgn.getLeftHandSide());
		return $;
	}
	private static void rewriteAssignIfAssignToAssignTernary(final AST t, final ASTRewrite r, final IfStatement i,
			final Assignment a, final Expression o) {
		r.replace(i, makeExpression(//
				t, //
				r, //
				i, //
				a, //
				o, //
				isPlainAssignment(a) ? a.getRightHandSide()
						: makeInfixExpression(t, r, InfixExpression.Operator.PLUS, a.getRightHandSide(), o)), null);
	}
	private static ExpressionStatement makeExpression(final AST t,
			final ASTRewrite r, final IfStatement i, final Assignment a,
			final Expression o, final Expression thenSideExp) {
		return t.newExpressionStatement(//
				makeAssigment(//
						t, //
						r, //
						a.getOperator(), //
						makeParenthesizedConditionalExp(t, r, i.getExpression(), thenSideExp, o), //
						a.getLeftHandSide())
						//
				);
	}
	static Range detectIfReturn(final IfStatement ifStmnt) {
		return null == statements(ifStmnt.getParent()) ? null : detectIfReturn(ifStmnt, statements(ifStmnt.getParent()));
	}
	private static Range detectIfReturn(final IfStatement ifStmnt, final List<ASTNode> ss) {
		final int ifIdx = ss.indexOf(ifStmnt);
		if (ss.size() <= ifIdx + 1)
			return null;
		final ReturnStatement nextRet = asReturn(ss.get(ifIdx + 1));
		if (nextRet == null || isConditional(nextRet.getExpression()))
			return null;
		final ReturnStatement thenSide = asReturn(ifStmnt.getThenStatement());
		final ReturnStatement elseSide = asReturn(ifStmnt.getElseStatement());
		return thenSide != null && elseSide == null && !isConditional(thenSide.getExpression()) || thenSide == null && elseSide != null
				&& !isConditional(elseSide.getExpression()) ? new Range(ifStmnt, nextRet) : null;
	}
	static Range detectIfSameExpStmntOrRet(final IfStatement ifStmnt) {
		final Statement thenStmnt = getBlockSingleStatement(ifStmnt.getThenStatement());
		final Statement elseStmnt = getBlockSingleStatement(ifStmnt.getElseStatement());
		if (hasNull(thenStmnt, elseStmnt, asBlock(ifStmnt.getParent())) || thenStmnt.getNodeType() != elseStmnt.getNodeType())
			return null;
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		if (1 != statementsCount(diffNodes.elseNode) || 1 != statementsCount(diffNodes.thenNode))
			return null;
		if (!isExpressionOrReturn(diffNodes.thenNode)) {
			diffNodes = findDiffNodes(diffNodes.thenNode, diffNodes.elseNode);
			if (!isOnlyDiff(thenStmnt, elseStmnt, diffNodes) || !handleCaseDiffNodesAreBlocks(diffNodes))
				return null;
		}
		if (isConditional(getExpression(diffNodes.thenNode), getExpression(diffNodes.elseNode)))
			return null;
		switch (diffNodes.thenNode.getNodeType()) {
		case ASTNode.RETURN_STATEMENT:
			return new Range(ifStmnt);
		case ASTNode.EXPRESSION_STATEMENT:
			return !checkIfOnlyDiffIsExp(diffNodes.thenNode, diffNodes.elseNode) ? null : new Range(ifStmnt);
		default:
			break;
		}
		return null;
	}
	static boolean checkIfOnlyDiffIsExp(final ASTNode thenStmnt, final ASTNode elseStmnt) {
		final Expression thenExp = asExpression(thenStmnt);
		final Expression elseExp = asExpression(elseStmnt);
		if (thenExp == null || elseExp == null || thenExp.getNodeType() != elseExp.getNodeType())
			return false;
		switch (thenExp.getNodeType()) {
		case ASTNode.ASSIGNMENT:
			return compatible((Assignment) thenExp, (Assignment) elseExp);
		case ASTNode.METHOD_INVOCATION: {
			final String thenMthdName = ((MethodInvocation) thenExp).toString();
			final String elseMthdName = ((MethodInvocation) elseExp).toString();
			return thenMthdName.substring(0, thenMthdName.indexOf("(")).equals(elseMthdName.substring(0, elseMthdName.indexOf("(")));
		}
		default:
			return false;
		}
	}
	static Range detectAssignIfAssign(final IfStatement i) {
		final Block parent = asBlock(i.getParent());
		return parent == null ? null : detectAssignIfAssign(i, parent);
	}
	private static boolean isBlock(final ASTNode n) {
		return n != null && n.getNodeType() == ASTNode.BLOCK;
	}
	private static Block asBlock(final ASTNode $) {
		return isBlock($) ? (Block) $ : null;
	}
	private static Range detectAssignIfAssign(final IfStatement ifStmnt, final Block parent) {
		final List<ASTNode> stmts = parent.statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		if (ifIdx < 1 && stmts.size() <= ifIdx + 1)
			return null;
		final Assignment asgnThen = getAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || null != ifStmnt.getElseStatement())
			return null;
		final Assignment nextAsgn = getAssignment((Statement) stmts.get(ifIdx + 1 <= stmts.size() - 1 ? ifIdx + 1 : stmts.size() - 1));
		final Assignment prevAsgn = getAssignment((Statement) stmts.get(0 > ifIdx - 1 ? 0 : ifIdx - 1));
		final VariableDeclarationFragment prevDecl = getVarDeclFrag(
				prevAsgn != null ? stmts.get(0 > ifIdx - 2 ? 0 : ifIdx - 2) : stmts.get(0 > ifIdx - 1 ? 0 : ifIdx - 1),
						asgnThen.getLeftHandSide());
		Range $ = detecPrevAndNextAsgnExist(asgnThen, prevAsgn, nextAsgn, prevDecl);
		if ($ != null)
			return $;
		$ = detecOnlyPrevAsgnExist(ifStmnt, asgnThen, prevAsgn, prevDecl);
		if ($ != null)
			return $;
		$ = detecOnlyNextAsgnExist(ifStmnt, asgnThen, nextAsgn, prevDecl);
		if ($ != null)
			return $;
		$ = detecNoPrevNoNextAsgn(ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl);
		return $;
	}
	private static Range detecNoPrevNoNextAsgn(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment prevAsgn,
			final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		return prevAsgn == null && nextAsgn == null && prevDecl != null && null != prevDecl.getInitializer()
				&& !dependsOn(prevDecl.getName(), ifStmnt.getExpression(), asgnThen.getRightHandSide()) ? new Range(prevDecl, ifStmnt)
		: null;
	}
	private static Range detecOnlyNextAsgnExist(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment nextAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (nextAsgn == null || !compatible(nextAsgn, asgnThen))
			return null;
		return prevDecl != null && !dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()) ? new Range(prevDecl, nextAsgn)
		: new Range(ifStmnt, nextAsgn);
	}
	private static Range detecOnlyPrevAsgnExist(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment prevAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (prevAsgn == null || dependsOn(prevAsgn.getLeftHandSide(), ifStmnt.getExpression()) || !compatible(prevAsgn, asgnThen))
			return null;
		if (prevDecl != null && null == prevDecl.getInitializer())
			return dependsOn(prevDecl.getName(), prevAsgn.getRightHandSide()) ? null : new Range(prevDecl, ifStmnt);
		return new Range(prevAsgn, ifStmnt);
	}
	private static Range detecPrevAndNextAsgnExist(final Assignment asgnThen, final Assignment prevAsgn, final Assignment nextAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (hasNull(prevAsgn, nextAsgn) || !compatible(nextAsgn, prevAsgn, asgnThen))
			return null;
		if (prevDecl != null)
			return dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()) ? null : new Range(prevDecl, nextAsgn);
		return new Range(prevAsgn, nextAsgn);
	}
	private static boolean dependsOn(final Expression expToCheck, final Expression... possiblyDependentExps) {
		for (final Expression pde : possiblyDependentExps)
			if (0 < Occurrences.BOTH_SEMANTIC.of(expToCheck).in(pde).size())
				return true;
		return false;
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final IfStatement i) {
				return //
						perhaps(detectAssignIfAssign(i)) || //
						perhaps(detectIfReturn(i)) || //
						perhaps(detectIfSameExpStmntOrRet(i)) || //
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
