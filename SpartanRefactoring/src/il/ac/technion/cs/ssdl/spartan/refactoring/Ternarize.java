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
		final Statement thenStatement = i.getThenStatement();
		if (!hasReturn(thenStatement))
			return false;
		final List<ASTNode> siblings = parent.statements();
		final int position = siblings.indexOf(i);
		final ReturnStatement nextRet = nextStatement(siblings, position);
		if (nextRet == null || isConditional(nextRet.getExpression()))
			return false;
		return statementsCount(thenStatement) == 1 && statementsCount(i.getElseStatement()) == 0
				&& rewriteIfToRetStmnt(ast, r, i, nextRet);
	}
	private static ReturnStatement nextStatement(final List<ASTNode> as, final int position) {
		return as.size() > position + 1 ? asReturn(as.get(position + 1)) : null;
	}
	private static boolean rewriteIfToRetStmnt(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final ReturnStatement nextReturn) {
		final ReturnStatement thenRet = asReturn(ifStmnt.getThenStatement());
		if (isConditional(thenRet.getExpression()))
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
	static boolean treatIfSameExpStmntOrRet(final AST ast, final ASTRewrite r, final IfStatement i) {
		if (asBlock(i.getParent()) == null)
			return false;
		final Statement thenStatment = asSingleStatement(i.getThenStatement());
		final Statement elseStatment = asSingleStatement(i.getElseStatement());
		if (hasNull(thenStatment, elseStatment) || thenStatment.getNodeType() != elseStatment.getNodeType())
			return false;
		if (thenStatment.subtreeMatch(matcher, elseStatment)) {
			r.replace(i, thenStatment, null);
			return true;
		}
		final TwoExpressions diffExp = findSingleDifference(thenStatment, elseStatment);
		if (diffExp == null)
			return false;
		final int ifIdx = statements(i.getParent()).indexOf(i);
		final Statement prevDecl = (Statement) statements(i.getParent()).get(ifIdx - 1 >= 0 ? ifIdx - 1 : ifIdx);
		return substitute(ast, r, i, diffExp, prevDecl);
	}
	private static TwoExpressions findSingleDifference(final Statement thenStmnt, final Statement elseStmnt) {
		TwoNodes diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		final TwoExpressions diffExps = findDiffExps(thenStmnt, elseStmnt, diffNodes);
		if (diffExps == null)
			return null;
		if (!isExpressionOrReturn(thenStmnt))
			caseDiffNodesAreBlocks(diffNodes);
		else
			diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		final ASTNode thenNode = diffNodes.thenNode;
		if (thenNode.getNodeType() != diffNodes.elseNode.getNodeType() || !isExpressionOrReturn(thenNode))
			return null;
		if (isExpression(thenNode) && checkIfOnlyDiffIsExp(thenNode, diffNodes.elseNode))
			return diffExps;
		return isReturn(thenNode) ? null : new TwoExpressions(getExpression(thenNode), getExpression(diffNodes.elseNode));
	}
	private static boolean isReturn(final ASTNode thenNode) {
		return thenNode.getNodeType() != ASTNode.RETURN_STATEMENT;
	}
	private static boolean isExpression(final ASTNode thenNode) {
		return thenNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT;
	}
	private static TwoExpressions findDiffExps(final Statement thenStmnt, final Statement elseStmnt, final TwoNodes diffNodes) {
		TwoNodes tempNodes = diffNodes;
		if (!isExpressionOrReturn(thenStmnt)) {
			if (!isOnlyDiff(thenStmnt, elseStmnt, tempNodes) || !caseDiffNodesAreBlocks(tempNodes))
				return null;
			if (isExpression(tempNodes.thenNode))
				tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
			if (findDiffNodes(tempNodes.thenNode, tempNodes.elseNode) == null
					|| isConditional((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode))
				return null;
			tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
			return new TwoExpressions((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode);
		}
		if (isExpression(thenStmnt))
			tempNodes = findDiffNodes(tempNodes.thenNode, tempNodes.elseNode);
		return tempNodes == null || isConditional((Expression) tempNodes.thenNode, (Expression) tempNodes.elseNode) ? null
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
	private static boolean isExpressionOrReturn(final ASTNode n) {
		return isExpression(n) || isReturn(n);
	}
	private static boolean caseDiffNodesAreBlocks(final TwoNodes diffNodes) {
		if (statementsCount(diffNodes.thenNode) != 1 && statementsCount(diffNodes.elseNode) != 1)
			return false;
		if (isBlock(diffNodes.thenNode))
			diffNodes.thenNode = asSingleStatement((Block) diffNodes.thenNode);
		if (isBlock(diffNodes.elseNode))
			diffNodes.elseNode = asSingleStatement((Block) diffNodes.elseNode);
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
	private static boolean substitute(final AST t, final ASTRewrite r, final IfStatement i, final TwoExpressions diff,
			final Statement possiblePrevDecl) {
		final Statement thenStmnt = asSingleStatement(i.getThenStatement());
		final Statement elseStmnt = asSingleStatement(i.getElseStatement());
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		final Expression newExp = determineNewExp(t, r, i.getExpression(), diff.thenExp, diff.elseExp);
		if (!isExpressionOrReturn(thenStmnt))
			diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		if (isAssignment(diffNodes.thenNode) && isAssignment(diffNodes.elseNode))
			if (!compatible(asAssignment(diffNodes.thenNode), asAssignment(diffNodes.elseNode)))
				return false;
			else if (handleSubIfDiffAreAsgns(t, r, i, diff.thenExp, possiblePrevDecl, thenStmnt, diffNodes.thenNode, newExp))
				return true;
		r.replace(diff.thenExp, newExp, null);
		r.replace(i, r.createCopyTarget(thenStmnt), null);
		return true;
	}
	private static boolean handleSubIfDiffAreAsgns(final AST t, final ASTRewrite r, final IfStatement ifStmnt,
			final Expression thenExp, final Statement possiblePrevDecl, final Statement thenStmnt, final ASTNode thenNode,
			final Expression newExp) {
		final Assignment asgnThen = asAssignment(thenNode);
		final VariableDeclarationFragment prevDecl = getVarDeclFrag(possiblePrevDecl, asgnThen.getLeftHandSide());
		if (asgnThen.getOperator() != Assignment.Operator.ASSIGN)
			return false;
		if (isExpression(thenStmnt) && prevDecl != null) {
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
		if (!isBlock(parent))
			return false;
		final List<ASTNode> stmts = ((Block) parent).statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		final Assignment asgnThen = asAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || ifStmnt.getElseStatement() != null || ifIdx < 1)
			return false;
		final Assignment prevAsgn = asAssignment(stmts.get(ifIdx - 1));
		final Assignment nextAsgn = stmts.size() > ifIdx + 1 ? asAssignment(stmts.get(ifIdx + 1)) : null;
		final VariableDeclarationFragment prevDecl = findPrevDecl(stmts, ifIdx, asgnThen, prevAsgn, nextAsgn);
		return tryHandleNextAndPrevAsgnExist(r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl) //
				|| tryHandleOnlyPrevAsgnExist(ast, r, ifStmnt, asgnThen, prevAsgn, prevDecl) //
				|| tryHandleOnlyNextAsgnExist(ast, r, ifStmnt, asgnThen, nextAsgn, prevDecl) //
				|| tryHandleNoNextNoPrevAsgn(ast, r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl);
	}
	private static boolean isBlock(final ASTNode n) {
		return n.getNodeType() == ASTNode.BLOCK;
	}
	private static boolean tryHandleNoNextNoPrevAsgn(final AST ast, final ASTRewrite r, final IfStatement i,
			final Assignment asgnThen, final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (prevAsgn != null || nextAsgn != null || isConditional(asgnThen.getRightHandSide()))
			return false;
		if (prevDecl != null && prevDecl.getInitializer() != null && i.getElseStatement() == null
				&& !isConditional(prevDecl.getInitializer()))
			if (!dependsOn(i.getExpression(), prevDecl.getName()) && !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())) {
				final Expression newInitalizer = makeParenthesizedConditionalExp(ast, r, i.getExpression(),
						asgnThen.getRightHandSide(), prevDecl.getInitializer());
				r.replace(prevDecl, makeVarDeclFrag(ast, r, prevDecl.getName(), newInitalizer), null);
				r.remove(i, null);
				return true;
			}
		return false;
	}
	private static boolean tryHandleOnlyNextAsgnExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (nextAsgn == null || !compatible(nextAsgn, asgnThen)
				|| isConditional(nextAsgn.getRightHandSide(), asgnThen.getRightHandSide())
				|| asgnThen.getRightHandSide().toString().equals(nextAsgn.getRightHandSide().toString()))
			return false;
		if (prevDecl == null) {
			if (!isAssignment(nextAsgn.getRightHandSide()))
				r.remove(ifStmnt, null);
		} else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN && !dependsOn(nextAsgn.getRightHandSide(), prevDecl.getName())) {
			r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()), null);
			r.remove(ifStmnt, null);
			r.remove(nextAsgn.getParent(), null);
		} else {
			rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, nextAsgn.getRightHandSide());
			r.remove(nextAsgn.getParent(), null);
		}
		return true;
	}
	private static boolean tryHandleOnlyPrevAsgnExist(final AST ast, final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
		if (prevAsgn == null || dependsOn(ifStmnt.getExpression(), prevAsgn.getLeftHandSide())
				|| prevAsgn.getRightHandSide().toString().equals(asgnThen.getRightHandSide().toString())
				|| isConditional(prevAsgn.getRightHandSide(), asgnThen.getRightHandSide()))
			return false;
		if (compatible(prevAsgn, asgnThen) && !isAssignment(prevAsgn.getRightHandSide()))
			if (prevDecl == null) {
				rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, prevAsgn.getRightHandSide());
				r.remove(prevAsgn.getParent(), null);
				return true;
			} else if (!dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())
					&& !dependsOn(prevAsgn.getRightHandSide(), prevDecl.getName())) {
				if (asgnThen.getOperator() == Assignment.Operator.ASSIGN) {
					final Expression newInitalizer = makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(),
							asgnThen.getRightHandSide(), prevAsgn.getRightHandSide());
					r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) prevAsgn.getLeftHandSide(), newInitalizer), null);
					r.remove(ifStmnt, null);
					r.remove(prevAsgn.getParent(), null);
					return true;
				}
			} else if (null != prevDecl.getInitializer()) {
				rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, prevAsgn.getRightHandSide());
				r.remove(prevAsgn.getParent(), null);
				return true;
			}
		return false;
	}
	private static boolean tryHandleNextAndPrevAsgnExist(final ASTRewrite r, final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
		if (hasNull(prevAsgn, nextAsgn)
				|| isConditional(prevAsgn.getRightHandSide(), nextAsgn.getRightHandSide(), asgnThen.getRightHandSide()))
			return false;
		if (compatible(nextAsgn, prevAsgn, asgnThen)) {
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
		return false;
	}
	private static VariableDeclarationFragment findPrevDecl(final List<ASTNode> stmts, final int ifIdx, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn) {
		VariableDeclarationFragment $ = null;
		if (prevAsgn != null) {
			if (ifIdx - 2 >= 0 && compatabileName(asgnThen.getLeftHandSide(), prevAsgn.getLeftHandSide()))
				$ = getVarDeclFrag((Statement) stmts.get(ifIdx - 2), asgnThen.getLeftHandSide());
		} else if (nextAsgn != null) {
			if (ifIdx - 1 >= 0 && compatabileName(asgnThen.getLeftHandSide(), nextAsgn.getLeftHandSide()))
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
			final ReturnStatement nextRet = asReturn(ss.get(ifIdx + 1));
			final ReturnStatement thenSide = asReturn(ifStmnt.getThenStatement());
			final ReturnStatement elseSide = asReturn(ifStmnt.getElseStatement());
			if (nextRet != null && (thenSide != null && elseSide == null || thenSide == null && elseSide != null))
				return new Range(ifStmnt, nextRet);
		}
		return null;
	}
	static Range detectIfSameExpStmntOrRet(final IfStatement ifStmnt) {
		final Statement thenStmnt = asSingleStatement(ifStmnt.getThenStatement());
		final Statement elseStmnt = asSingleStatement(ifStmnt.getElseStatement());
		if (hasNull(thenStmnt, elseStmnt, asBlock(ifStmnt.getParent())) || thenStmnt.getNodeType() != elseStmnt.getNodeType())
			return null;
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		final ASTNode elseNode = diffNodes.elseNode;
		final ASTNode thenNode = diffNodes.thenNode;
		if (!isSingletonStatement(elseNode) || !isSingletonStatement(thenNode))
			return null;
		if (!isExpressionOrReturn(thenNode)) {
			diffNodes = findDiffNodes(thenNode, elseNode);
			if (!isOnlyDiff(thenStmnt, elseStmnt, diffNodes) || !caseDiffNodesAreBlocks(diffNodes))
				return null;
		}
		if (isConditional(getExpression(thenNode), getExpression(elseNode)))
			return null;
		switch (thenNode.getNodeType()) {
		case ASTNode.RETURN_STATEMENT:
			return new Range(ifStmnt);
		case ASTNode.EXPRESSION_STATEMENT:
			return checkIfOnlyDiffIsExp(thenNode, elseNode) ? new Range(ifStmnt) : null;
		default:
			break;
		}
		return null;
	}
	private static boolean isSingletonStatement(final ASTNode n) {
		return statementsCount(n) != 1;
	}
	static boolean checkIfOnlyDiffIsExp(final ASTNode thenStmnt, final ASTNode elseStmnt) {
		if (!isExpression(thenStmnt) || !isExpression(elseStmnt))
			return false;
		final Expression thenExp = ((ExpressionStatement) thenStmnt).getExpression();
		final Expression elseExp = ((ExpressionStatement) elseStmnt).getExpression();
		if (thenExp.getNodeType() != elseExp.getNodeType())
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
		final Assignment asgnThen = asAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || ifStmnt.getElseStatement() != null)
			return null;
		final Assignment prevAssignment = asAssignment(stmts.get(ifIdx - 1 >= 0 ? ifIdx - 1 : 0));
		final Assignment nextAssignment = asAssignment(stmts.get(ifIdx + 1 > stmts.size() - 1 ? stmts.size() - 1 : ifIdx + 1));
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
		return !dependsOn(ifStmnt.getExpression(), prevDecl.getName()) && !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName()) ? new Range(
				prevDecl, ifStmnt) : null;
	}
	private static Range detecOnlyNextAsgnExist(final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment nextAssignment, final VariableDeclarationFragment prevDecl) {
		if (nextAssignment == null || !compatible(nextAssignment, asgnThen))
			return null;
		return prevDecl != null && !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl,
				nextAssignment) : new Range(ifStmnt, nextAssignment);
	}
	private static Range detecOnlyPrevAsgnExist(final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAssignment, final VariableDeclarationFragment prevDecl) {
		if (prevAssignment == null || dependsOn(ifStmnt.getExpression(), prevAssignment.getLeftHandSide())
				|| !compatible(prevAssignment, asgnThen))
			return null;
		if (prevDecl != null && prevDecl.getInitializer() == null)
			return !dependsOn(prevAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, ifStmnt) : null;
			return new Range(prevAssignment, ifStmnt);
	}
	private static Range detecPrevAndNextAsgnExist(final Assignment asgnThen, final Assignment prevAssignment,
			final Assignment nextAssignment, final VariableDeclarationFragment prevDecl) {
		if (hasNull(prevAssignment, nextAssignment) || !compatible(nextAssignment, prevAssignment, asgnThen))
			return null;
		if (prevDecl != null)
			return !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, nextAssignment) : null;
			return new Range(prevAssignment, nextAssignment);
	}
	private static boolean dependsOn(final Expression e, final Expression leftHandSide) {
		return Occurrences.BOTH_SEMANTIC.of(leftHandSide).in(e).size() > 0;
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
