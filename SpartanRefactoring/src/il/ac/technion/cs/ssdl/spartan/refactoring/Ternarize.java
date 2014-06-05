package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.*;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

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
			@Override public boolean visit(final IfStatement ifStmnt) {
				return !inRange(m, ifStmnt) || //
						treatAssignIfAssign(t, r, ifStmnt) || //
						treatIfReturn(t, r, ifStmnt) || //
						treatIfSameExpStmntOrRet(t, r, ifStmnt) || //
						true;
			}
		});
	}
	static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement i) {
		final Block parent = asBlock(i.getParent());
		return parent != null && treatIfReturn(ast, r, i, parent);
	}
	private static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement i, final Block parent) {
		if (!checkIfReturnStmntExist(i.getThenStatement()))
			return false;
		final List<ASTNode> siblings = parent.statements();
		final int position = siblings.indexOf(i);
		final ReturnStatement nextRet = nextStatement(siblings, position);
		if (nextRet == null || isOneExpCondExp(nextRet.getExpression()) )
			return false;
		return getNumOfStmnts(i.getThenStatement()) == 1 && getNumOfStmnts(i.getElseStatement()) == 0 && rewriteIfToRetStmnt(ast, r, i,
				nextRet);
	}
	private static ReturnStatement nextStatement(final List<ASTNode> stmts, final int ns) {
		return stmts.size() > ns + 1 ? getReturnStatement(stmts.get(ns + 1)) : null;
	}
	private static boolean rewriteIfToRetStmnt(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final ReturnStatement nextReturn) {
		final ReturnStatement thenRet = getReturnStatement(ifStmnt.getThenStatement());
		if (isOneExpCondExp(thenRet.getExpression()))
			return false;
		final Expression newExp = determineNewExp(ast, r, ifStmnt.getExpression(), thenRet.getExpression(), nextReturn.getExpression());
		final ReturnStatement newRet = makeReturnStatement(ast, r, newExp);
		r.replace(ifStmnt, newRet, null);
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
		if (asBlock(ifStmt.getParent()) == null)
			return false;
		final Statement thenStatment = getStmntFromBlock(ifStmt.getThenStatement());
		final Statement elseStatment = getStmntFromBlock(ifStmt.getElseStatement());
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
		final Statement prevDecl = (Statement) statements(ifStmt.getParent()).get(ifIdx - 1 >= 0 ? ifIdx - 1 : ifIdx);
		return substitute(ast, r, ifStmt, diffExp, prevDecl);
	}
	private static TwoExpressions findSingleDifference(final Statement thenStmnt, final Statement elseStmnt) {
		TwoNodes diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		final TwoExpressions diffExps = findDiffExps(thenStmnt, elseStmnt, diffNodes);
		if (diffExps == null)
			return null;
		if (!isExpStmntOrReturn(thenStmnt))
			handleCaseDiffNodesAreBlocks(diffNodes);
		else
			diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		if (diffNodes.thenNode.getNodeType() != diffNodes.elseNode.getNodeType() || !isExpStmntOrReturn(diffNodes.thenNode))
			return null;
		if (diffNodes.thenNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT
				&& checkIfOnlyDiffIsExp(diffNodes.thenNode, diffNodes.elseNode))
			return diffExps;
		return diffNodes.thenNode.getNodeType() != ASTNode.RETURN_STATEMENT ? null : new TwoExpressions(
				getExpression(diffNodes.thenNode), getExpression(diffNodes.elseNode));
	}
	private static TwoExpressions findDiffExps(final Statement thenStmnt, final Statement elseStmnt, final TwoNodes diffNodes) {
		TwoNodes tempNodes = diffNodes;
		if (!isExpStmntOrReturn(thenStmnt)) {
			if (!isOnlyDiff(thenStmnt, elseStmnt, tempNodes) || !handleCaseDiffNodesAreBlocks(tempNodes))
				return null;
			if (tempNodes.thenNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
				tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
			if (findDiffNodes(tempNodes.thenNode, tempNodes.elseNode) == null
					|| isOneExpCondExp((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode))
				return null;
			tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
			return new TwoExpressions((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode);
		}
		if (thenStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
			tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
		return tempNodes == null || isOneExpCondExp((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode) ? null
				: new TwoExpressions((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode);
	}
	private static boolean isOnlyDiff(final Statement thenStmnt, final Statement elseStmnt, final TwoNodes diffNodes) {
		if (hasNull(thenStmnt, elseStmnt, diffNodes))
			return false;
		final List<ASTNode> thenNodes = getChildren(thenStmnt);
		final List<ASTNode> elseNodes = getChildren(elseStmnt);
		thenNodes.remove(diffNodes.thenNode);
		thenNodes.removeAll(getChildren(diffNodes.thenNode));
		elseNodes.remove(diffNodes.elseNode);
		elseNodes.removeAll(getChildren(diffNodes.elseNode));
		return thenNodes.toString().equals(elseNodes.toString());
	}
	private static boolean isExpStmntOrReturn(final ASTNode n) {
		return n != null && isExpStmntOrReturn(n.getNodeType());
	}
	private static boolean isExpStmntOrReturn(final int nodeType) {
		return nodeType == ASTNode.EXPRESSION_STATEMENT || nodeType == ASTNode.RETURN_STATEMENT;
	}
	private static boolean handleCaseDiffNodesAreBlocks(final TwoNodes diffNodes) {
		if (getNumOfStmnts(diffNodes.thenNode) != 1 && getNumOfStmnts(diffNodes.elseNode) != 1)
			return false;
		if (diffNodes.thenNode.getNodeType() == ASTNode.BLOCK)
			diffNodes.thenNode = getStmntFromBlock((Block) diffNodes.thenNode);
		if (diffNodes.elseNode.getNodeType() == ASTNode.BLOCK)
			diffNodes.elseNode = getStmntFromBlock((Block) diffNodes.elseNode);
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
		final Statement thenStmnt = getStmntFromBlock(ifStmnt.getThenStatement());
		final Statement elseStmnt = getStmntFromBlock(ifStmnt.getElseStatement());
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		final Expression newExp = determineNewExp(t, r, ifStmnt.getExpression(), diff.thenExp, diff.elseExp);
		if (!isExpStmntOrReturn(thenStmnt))
			diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		if (checkIsAssignment((Statement) diffNodes.thenNode) && checkIsAssignment((Statement) diffNodes.elseNode))
			if (!cmpAsgns(getAssignment((Statement) diffNodes.thenNode), getAssignment((Statement) diffNodes.elseNode)))
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
		final VariableDeclarationFragment prevDecl = getVarDeclFrag(possiblePrevDecl, asgnThen.getLeftHandSide());
		if (asgnThen.getOperator() != Assignment.Operator.ASSIGN)
			return false;
		if (thenStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT && prevDecl != null) {
			r.replace(prevDecl, makeVarDeclFrag(t, r, prevDecl.getName(), newExp), null);
			r.remove(ifStmnt, null);
		} else {
			r.replace(thenExp, newExp, null);
			r.replace(ifStmnt, r.createCopyTarget(thenStmnt), null);
		}
		return true;
	}
	private static Expression determineNewExp(final AST t, final ASTRewrite r, final Expression cond, final Expression thenExp,
			final Expression elseExp) {
		return thenExp.getNodeType() == ASTNode.BOOLEAN_LITERAL ? tryToNegateCond(t, r, cond, ((BooleanLiteral) thenExp).booleanValue())
				: makeParenthesizedConditionalExp(t, r, cond, thenExp, elseExp);
	}
	static boolean treatAssignIfAssign(final AST ast, final ASTRewrite r, final IfStatement ifStmnt) {
		final ASTNode parent = ifStmnt.getParent();
		if (parent.getNodeType() != ASTNode.BLOCK)
			return false;
		final List<ASTNode> stmts = ((Block) parent).statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		final Assignment asgnThen = getAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || ifStmnt.getElseStatement() != null || ifIdx < 1)
			return false;
		final Assignment prevAsgn = getAssignment((Statement) stmts.get(ifIdx - 1));
		final Assignment nextAsgn = stmts.size() > ifIdx + 1 ? getAssignment((Statement) stmts.get(ifIdx + 1)) : null;
		final VariableDeclarationFragment prevDecl = findPrevDecl(stmts, ifIdx, asgnThen, prevAsgn, nextAsgn);
		return tryHandleNextAndPrevAsgnExist(r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl) //
				|| tryHandleOnlyPrevAsgnExist(ast, r, ifStmnt, asgnThen, prevAsgn, prevDecl) //
				|| tryHandleOnlyNextAsgnExist(ast, r, ifStmnt, asgnThen, nextAsgn, prevDecl) //
				|| tryHandleNoNextNoPrevAsgn(ast, r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl);
	}
	private static boolean tryHandleNoNextNoPrevAsgn(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isNoNextNoPrevAsgnPossible(ifStmnt, asgnThen, prevAsgn, nextAsgn,prevDecl))
			return false;
		final Expression newInitalizer = makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(),
				asgnThen.getRightHandSide(), prevDecl.getInitializer());
		r.replace(prevDecl, makeVarDeclFrag(ast, r, prevDecl.getName(), newInitalizer), null);
		r.remove(ifStmnt, null);
		return true;
	}
	private static boolean isNoNextNoPrevAsgnPossible(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment prevAsgn,
			final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		return prevAsgn == null //
				&& nextAsgn == null //
				&& !isOneExpCondExp(asgnThen.getRightHandSide()) //
				&& prevDecl != null //
				&& prevDecl.getInitializer() != null //
				&& ifStmnt.getElseStatement() == null //
				&& !isOneExpCondExp(prevDecl.getInitializer()) //
				&& !dependsOn(prevDecl.getName(), ifStmnt.getExpression(), asgnThen.getRightHandSide()); //
	}
	private static boolean tryHandleOnlyNextAsgnExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isOnlyNextAsgnPossible(asgnThen, nextAsgn))
			return false;
		if (prevDecl == null) {
			if (!checkIsAssignment(nextAsgn.getRightHandSide()))
				r.remove(ifStmnt, null);
		} else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN && !dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide())) {
			r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()), null);
			r.remove(ifStmnt, null);
			r.remove(nextAsgn.getParent(), null);
		} else
			handleNoPrevDecl(ast, r, ifStmnt, asgnThen, nextAsgn);
		return true;
	}
	private static boolean isOnlyNextAsgnPossible(final Assignment asgnThen, final Assignment nextAsgn) {
		return nextAsgn != null
				&& cmpAsgns(nextAsgn, asgnThen)
				&& !isOneExpCondExp(nextAsgn.getRightHandSide(), asgnThen.getRightHandSide())
				&& !asgnThen.getRightHandSide().toString().equals(nextAsgn.getRightHandSide().toString());
	}
	private static boolean tryHandleOnlyPrevAsgnExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isOnlyPrevAsgnPossible(ifStmnt, asgnThen, prevAsgn))
			return false;
		if (prevAsgn.getRightHandSide().toString().equals(asgnThen.getRightHandSide().toString())){
			r.remove(ifStmnt, null);
			return true;
		}
		if (prevDecl == null) {
			handleNoPrevDecl(ast, r, ifStmnt, asgnThen, prevAsgn);
			return true;
		} else if (handlePrevDeclExist(ast, r, ifStmnt, asgnThen, prevAsgn, prevDecl))
			return true;
		return false;
	}
	private static boolean isOnlyPrevAsgnPossible(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment prevAsgn) {
		return prevAsgn != null
				&& !dependsOn(prevAsgn.getLeftHandSide(), ifStmnt.getExpression())
				&& !isOneExpCondExp(prevAsgn.getRightHandSide(), asgnThen.getRightHandSide())
				&& !checkIsAssignment(prevAsgn.getRightHandSide())
				&& cmpAsgns(prevAsgn, asgnThen);
	}
	private static boolean handlePrevDeclExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
		if (!dependsOn(prevDecl.getName(), asgnThen.getRightHandSide(), prevAsgn.getRightHandSide())
				&& asgnThen.getOperator() == Assignment.Operator.ASSIGN){
			final Expression newInitalizer = makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(),
					asgnThen.getRightHandSide(), prevAsgn.getRightHandSide());
			r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) prevAsgn.getLeftHandSide(), newInitalizer), null);
			r.remove(ifStmnt, null);
			r.remove(prevAsgn.getParent(), null);
			return true;
		}
		else if (prevDecl.getInitializer() != null) {
			handleNoPrevDecl(ast, r, ifStmnt, asgnThen, prevAsgn);
			return true;
		}
		return false;
	}
	private static void handleNoPrevDecl(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn) {
		rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, prevAsgn.getRightHandSide());
		r.remove(prevAsgn.getParent(), null);
	}
	private static boolean tryHandleNextAndPrevAsgnExist(final ASTRewrite r, final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (!isNextAndPrevAsgnPossible(asgnThen, prevAsgn, nextAsgn))
			return false;
		if (prevDecl == null)
			r.replace(prevAsgn.getParent(), nextAsgn.getParent(), null);
		else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN) {
			r.replace(prevDecl.getInitializer(), nextAsgn.getRightHandSide(), null);
			r.remove(prevAsgn.getParent(), null);
		}
		r.remove(ifStmnt, null);
		r.remove(nextAsgn.getParent(), null);
		return true;
	}
	private static boolean isNextAndPrevAsgnPossible(final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn) {
		return !hasNull(prevAsgn, nextAsgn)
				&& cmpAsgns(nextAsgn, prevAsgn, asgnThen)
				&& !isOneExpCondExp(prevAsgn.getRightHandSide(), nextAsgn.getRightHandSide(), asgnThen.getRightHandSide());
	}
	private static VariableDeclarationFragment findPrevDecl(final List<ASTNode> stmts, final int ifIdx, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn) {
		VariableDeclarationFragment $ = null;
		if (prevAsgn != null) {
			if (ifIdx - 2 >= 0 && cmpSimpleNames(asgnThen.getLeftHandSide(), prevAsgn.getLeftHandSide()))
				$ = getVarDeclFrag((Statement) stmts.get(ifIdx - 2), asgnThen.getLeftHandSide());
		} else if (nextAsgn != null) {
			if (ifIdx - 1 >= 0 && cmpSimpleNames(asgnThen.getLeftHandSide(), nextAsgn.getLeftHandSide()))
				$ = getVarDeclFrag((Statement) stmts.get(ifIdx - 1), nextAsgn.getLeftHandSide());
		} else if (ifIdx - 1 >= 0)
			$ = getVarDeclFrag((Statement) stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
		return $;
	}
	private static void rewriteAssignIfAssignToAssignTernary(final AST t, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Expression otherAsgnExp) {
		final Expression thenSideExp = asgnThen.getOperator() == Assignment.Operator.ASSIGN ? asgnThen.getRightHandSide()
				: makeInfixExpression(t, r, InfixExpression.Operator.PLUS, asgnThen.getRightHandSide(), otherAsgnExp);
		final Expression newCond = makeParenthesizedConditionalExp(t, r, ifStmnt.getExpression(), thenSideExp, otherAsgnExp);
		final Assignment newAsgn = makeAssigment(t, r, asgnThen.getOperator(), newCond, asgnThen.getLeftHandSide());
		r.replace(ifStmnt, t.newExpressionStatement(newAsgn), null);
	}
	static Range detectIfReturn(final IfStatement ifStmnt) {
		return detectIfReturn(ifStmnt, statements(ifStmnt.getParent()));
	}
	private static Range detectIfReturn(final IfStatement ifStmnt, final List<ASTNode> ss) {
		if (ss == null)
			return null;
		final int ifIdx = ss.indexOf(ifStmnt);
		if (ss.size() > ifIdx + 1) {
			final ReturnStatement nextRet = getReturnStatement(ss.get(ifIdx + 1));
			final ReturnStatement thenSide = getReturnStatement(ifStmnt.getThenStatement());
			final ReturnStatement elseSide = getReturnStatement(ifStmnt.getElseStatement());
			if (nextRet != null && (thenSide != null && elseSide == null || thenSide == null && elseSide != null))
				return new Range(ifStmnt, nextRet);
		}
		return null;
	}
	static Range detectIfSameExpStmntOrRet(final IfStatement ifStmnt) {
		final Statement thenStmnt = getStmntFromBlock(ifStmnt.getThenStatement());
		final Statement elseStmnt = getStmntFromBlock(ifStmnt.getElseStatement());
		if (hasNull(thenStmnt, elseStmnt, asBlock(ifStmnt.getParent())) || thenStmnt.getNodeType() != elseStmnt.getNodeType())
			return null;
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		if (getNumOfStmnts(diffNodes.elseNode) != 1 || getNumOfStmnts(diffNodes.thenNode) != 1)
			return null;
		if (!isExpStmntOrReturn(diffNodes.thenNode)) {
			diffNodes = findDiffNodes(diffNodes.thenNode, diffNodes.elseNode);
			if (!isOnlyDiff(thenStmnt, elseStmnt, diffNodes) || !handleCaseDiffNodesAreBlocks(diffNodes))
				return null;
		}
		if (isOneExpCondExp(getExpression(diffNodes.thenNode), getExpression(diffNodes.elseNode)))
			return null;
		switch (diffNodes.thenNode.getNodeType()) {
		case ASTNode.RETURN_STATEMENT:
			return new Range(ifStmnt);
		case ASTNode.EXPRESSION_STATEMENT:
			return checkIfOnlyDiffIsExp(diffNodes.thenNode, diffNodes.elseNode) ? new Range(ifStmnt) : null;
		default:
			break;
		}
		return null;
	}
	static boolean checkIfOnlyDiffIsExp(final ASTNode thenStmnt, final ASTNode elseStmnt) {
		if (thenStmnt.getNodeType() != ASTNode.EXPRESSION_STATEMENT || elseStmnt.getNodeType() != ASTNode.EXPRESSION_STATEMENT)
			return false;
		final Expression thenExp = ((ExpressionStatement) thenStmnt).getExpression();
		final Expression elseExp = ((ExpressionStatement) elseStmnt).getExpression();
		if (thenExp.getNodeType() != elseExp.getNodeType())
			return false;
		switch (thenExp.getNodeType()) {
		case ASTNode.ASSIGNMENT:
			return cmpAsgns((Assignment) thenExp, (Assignment) elseExp);
		case ASTNode.METHOD_INVOCATION: {
			final String thenMthdName = ((MethodInvocation) thenExp).toString();
			final String elseMthdName = ((MethodInvocation) elseExp).toString();
			return thenMthdName.substring(0, thenMthdName.indexOf("(")).equals(elseMthdName.substring(0, elseMthdName.indexOf("(")));
		}
		default:
			return false;
		}
	}
	static Range detectAssignIfAssign(final IfStatement ifStmnt) {
		final Block parent = asBlock(ifStmnt.getParent());
		return parent == null ? null : detectAssignIfAssign(ifStmnt, parent);
	}
	private static Block asBlock(final ASTNode n) {
		return n instanceof Block ? (Block) n : null;
	}
	private static Range detectAssignIfAssign(final IfStatement ifStmnt, final Block parent) {
		final List<ASTNode> stmts = parent.statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		if (ifIdx < 1 && stmts.size() <= ifIdx + 1)
			return null;
		final Assignment asgnThen = getAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || ifStmnt.getElseStatement() != null)
			return null;
		final Assignment prevAssignment = getAssignment((Statement) stmts.get(ifIdx - 1 >= 0 ? ifIdx - 1 : 0));
		final Assignment nextAssignment = getAssignment((Statement) stmts.get(ifIdx + 1 > stmts.size() - 1 ? stmts.size() - 1
				: ifIdx + 1));
		final VariableDeclarationFragment prevDecl = getVarDeclFrag(
				prevAssignment != null ? (Statement) stmts.get(ifIdx - 2 >= 0 ? ifIdx - 2 : 0)
						: (Statement) stmts.get(ifIdx - 1 >= 0 ? ifIdx - 1 : 0), asgnThen.getLeftHandSide());
		Range $ = detecPrevAndNextAsgnExist(asgnThen, prevAssignment, nextAssignment, prevDecl);
		if ($ != null)
			return $;
		$ = detecOnlyPrevAsgnExist(ifStmnt, asgnThen, prevAssignment, prevDecl);
		if ($ != null)
			return $;
		$ = detecOnlyNextAsgnExist(ifStmnt, asgnThen, nextAssignment, prevDecl);
		if ($ != null)
			return $;
		$ = detecNoPrevNoNextAsgn(ifStmnt, asgnThen, prevAssignment, nextAssignment, prevDecl);
		return $;
	}
	private static Range detecNoPrevNoNextAsgn(final IfStatement ifStmnt, final Assignment asgnThen, final Assignment prevAssignment,
			final Assignment nextAssignment, final VariableDeclarationFragment prevDecl) {
		if (prevAssignment != null || nextAssignment != null || prevDecl == null || prevDecl.getInitializer() == null)
			return null;
		return !dependsOn(prevDecl.getName(), ifStmnt.getExpression(), asgnThen.getRightHandSide()) ? new Range(prevDecl, ifStmnt) : null;
	}
	private static Range detecOnlyNextAsgnExist(final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (nextAsgn == null || !cmpAsgns(nextAsgn, asgnThen))
			return null;
		return prevDecl != null && !dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()) ? new Range(prevDecl,
				nextAsgn) : new Range(ifStmnt, nextAsgn);
	}
	private static Range detecOnlyPrevAsgnExist(final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
		if (prevAsgn == null || dependsOn(prevAsgn.getLeftHandSide(), ifStmnt.getExpression())
				|| !cmpAsgns(prevAsgn, asgnThen))
			return null;
		if (prevDecl != null && prevDecl.getInitializer() == null)
			return !dependsOn(prevDecl.getName(), prevAsgn.getRightHandSide()) ? new Range(prevDecl, ifStmnt) : null;
			return new Range(prevAsgn, ifStmnt);
	}
	private static Range detecPrevAndNextAsgnExist(final Assignment asgnThen, final Assignment prevAsgn,
			final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (hasNull(prevAsgn, nextAsgn) || !cmpAsgns(nextAsgn, prevAsgn, asgnThen))
			return null;
		if (prevDecl != null)
			return !dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()) ? new Range(prevDecl, nextAsgn) : null;
			return new Range(prevAsgn, nextAsgn);
	}
	private static boolean dependsOn(final Expression expToCheck, final Expression... possiblyDependentExps) {
		for (final Expression pde : possiblyDependentExps)
			if (Occurrences.BOTH_SEMANTIC.of(expToCheck).in(pde).size() > 0)
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
