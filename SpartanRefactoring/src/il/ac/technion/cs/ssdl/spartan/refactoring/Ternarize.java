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
	static List<ASTNode> statements(final ASTNode n) {
		return n.getNodeType() != ASTNode.BLOCK ? null : statements((Block) n);
	}
	static List<ASTNode> statements(final Block b) {
		return b.statements();
	}

	static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement ifStmnt) {
		final Block parent = asBlock(ifStmnt.getParent());
		if (parent == null)
			return false;
		@SuppressWarnings("rawtypes")
		final List stmts = parent.statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		final ReturnStatement nextReturn = stmts.size() > ifIdx + 1 ? getReturnStatement((Statement) stmts.get(ifIdx + 1)) : null;
		if (nextReturn == null)
			return false;
		final int numOfStmntInThen = getNumOfStmnts(ifStmnt.getThenStatement());
		final int numOfStmntInElse = getNumOfStmnts(ifStmnt.getElseStatement());
		if (checkReturnStmnt(ifStmnt.getThenStatement())) {
			if (numOfStmntInThen == 1 && numOfStmntInElse == 0)
				rewriteIfToRetStmnt(ast, r, ifStmnt, nextReturn);
			else
				addReturnStmntToIf(ast, r, ifStmnt, nextReturn, false);
			return true;
		}
		return false;
	}
	private static void rewriteIfToRetStmnt(final AST ast, final ASTRewrite r,
			final IfStatement ifStmnt, final ReturnStatement nextReturn) {
		final ReturnStatement thenRet = getReturnStatement(ifStmnt.getThenStatement());
		final Expression newExp;
		if (thenRet.getExpression().getNodeType() == ASTNode.BOOLEAN_LITERAL)
			newExp = makeParenthesizedExpression(ast, r,
					tryToNegateCond(ast, r, ifStmnt.getExpression(), ((BooleanLiteral) thenRet.getExpression()).booleanValue()));
		else
			newExp = makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(), thenRet.getExpression(), nextReturn.getExpression());
		final ReturnStatement newRet = makeReturnStatement(ast, r, newExp);
		r.replace(ifStmnt, newRet, null);
		r.remove(nextReturn, null);
	}
	/**
	 * @author Tomer Zeltzer
	 *
	 *	contains both sides for the conditional expression
	 */
	public static class TwoExpressions {
		final Expression thenExp;
		final Expression elseExp;
		/**
		 * Instantiates the class with the given Expressions
		 * 
		 * @param t
		 * 			then Expression
		 * @param e
		 * 			else Expression
		 */
		public TwoExpressions(final Expression t, final Expression e){
			thenExp = t;
			elseExp = e;
		}
	}

	/**
	 * @author Tomer Zeltzer
	 *
	 * contains 2 nodes (used to store the 2 nodes that are different in the then and else tree)
	 */
	public static class TwoNodes {
		ASTNode thenNode;
		ASTNode elseNode;
		/**
		 * Instantiates the class with the given nodes
		 * 
		 * @param t
					then node
		 * @param e
					else node
		 */
		public TwoNodes(final ASTNode t, final ASTNode e){
			thenNode = t;
			elseNode = e;
		}
	}
	static boolean treatIfSameExpStmntOrRet(final AST ast, final ASTRewrite r, final IfStatement ifStmt){
		if (asBlock(ifStmt.getParent()) == null)
			return false;
		final Statement thenStatment = getStmntFromBlock(ifStmt.getThenStatement());
		final Statement elseStatment =	getStmntFromBlock(ifStmt.getElseStatement());
		if (hasNull(thenStatment, elseStatment) || thenStatment.getNodeType() != elseStatment.getNodeType())
			return false;
		if (thenStatment.subtreeMatch(matcher, elseStatment)){
			r.replace(ifStmt, thenStatment, null);
			return true;
		}
		final TwoExpressions diff = findSingleDifference(thenStatment, elseStatment);
		if (diff == null)
			return false;
		final int ifIdx = statements(ifStmt.getParent()).indexOf(ifStmt);
		final int possiblePrevDeclIdx = ifIdx-1 >= 0 ? ifIdx-1 : ifIdx;
		final Statement prevDecl = (Statement) statements(ifStmt.getParent()).get(possiblePrevDeclIdx);
		return substitute(ast, r, ifStmt, diff, prevDecl);
	}

	private static TwoExpressions findSingleDifference(final Statement thenStmnt, final Statement elseStmnt) {
		TwoNodes diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		TwoExpressions expsToReturn = null;
		if (!checkIfTypeIsExpStmntOrRetStmnt(thenStmnt)){
			if (!checkIfItIsTheOnlyDiff(thenStmnt, elseStmnt, diffNodes) || !handleCaseDiffNodesAreBlocks(diffNodes))
				return null;
			if (diffNodes.thenNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
				diffNodes = findDiffNodes(diffNodes.thenNode, diffNodes.elseNode);
			diffNodes = diffNodes != null ? findDiffNodes(diffNodes.thenNode, diffNodes.elseNode) : null;
			if (diffNodes == null)
				return null;
			expsToReturn = new TwoExpressions((Expression) diffNodes.thenNode, (Expression) diffNodes.elseNode);
			diffNodes = findDiffNodes(thenStmnt, elseStmnt);
			handleCaseDiffNodesAreBlocks(diffNodes);
		} else {
			if (thenStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
				diffNodes = findDiffNodes(diffNodes.thenNode, diffNodes.elseNode);
			if (diffNodes == null)
				return null;
			expsToReturn = new TwoExpressions((Expression) diffNodes.thenNode, (Expression) diffNodes.elseNode);
			diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		}
		if (diffNodes.thenNode.getNodeType() != diffNodes.elseNode.getNodeType() || !checkIfTypeIsExpStmntOrRetStmnt(diffNodes.thenNode))
			return null;
		if (diffNodes.thenNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT
				&& checkIfOnlyDiffIsExp(diffNodes.thenNode, diffNodes.elseNode))
			return expsToReturn;
		if (diffNodes.thenNode.getNodeType() == ASTNode.RETURN_STATEMENT)
			return new TwoExpressions(getExpression(diffNodes.thenNode), getExpression(diffNodes.elseNode));
		return null;
	}


	/** TRIED TO USE .EQUALS OF LIST TYPE BUT IT RETURNS FALSE EVEN THOUGH THE LIST CONTAINS THE SAME OBJECTS(STRING WISE)
	 *AM I MISSING SOMETHING REGARDING THE WAY LISTS ARE COMPARED? USING STRING COMPARISON FOR NOW*/


	private static boolean checkIfItIsTheOnlyDiff(final Statement thenStmnt, final Statement elseStmnt,
			final TwoNodes diffNodes) {
		if (thenStmnt == null || elseStmnt == null ||diffNodes == null)
			return false;
		final List<ASTNode> thenNodes = getChildren(thenStmnt);
		final List<ASTNode> elseNodes = getChildren(elseStmnt);
		thenNodes.remove(diffNodes.thenNode);
		thenNodes.removeAll(getChildren(diffNodes.thenNode));
		elseNodes.remove(diffNodes.elseNode);
		elseNodes.removeAll(getChildren(diffNodes.elseNode));
		return thenNodes.toString().equals(elseNodes.toString());
	}

	private static boolean checkIfTypeIsExpStmntOrRetStmnt(final ASTNode node) {
		if (node == null)
			return false;
		if (node.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
			return true;
		if (node.getNodeType() == ASTNode.RETURN_STATEMENT)
			return true;
		return false;
	}

	private static boolean handleCaseDiffNodesAreBlocks(final TwoNodes diffNodes) {
		if (getNumOfStmnts(diffNodes.thenNode) != 1 && getNumOfStmnts(diffNodes.elseNode) != 1)
			return false;
		if (diffNodes.thenNode.getNodeType() == ASTNode.BLOCK)
			diffNodes.thenNode = getStmntFromBlock((Block)diffNodes.thenNode);
		if (diffNodes.elseNode.getNodeType() == ASTNode.BLOCK)
			diffNodes.elseNode = getStmntFromBlock((Block)diffNodes.elseNode);
		return true;
	}

	private static TwoNodes findDiffNodes(final ASTNode thenNode, final ASTNode elseNode){
		if (thenNode == null || elseNode == null)
			return null;
		final List<ASTNode> thenList = getChildren(thenNode);
		final List<ASTNode> elseList = getChildren(elseNode);
		for (int idx=0; idx < thenList.size() && idx < elseList.size(); idx++)
			if (!thenList.get(idx).toString().equals(elseList.get(idx).toString()))
				return new TwoNodes(thenList.get(idx), elseList.get(idx));
		return null;
	}

	private static boolean substitute(final AST t, final ASTRewrite r, final IfStatement ifStmnt, final TwoExpressions diff,
			final Statement possiblePrevDecl) {
		final Statement thenStmnt = getStmntFromBlock(ifStmnt.getThenStatement());
		final Statement elseStmnt = getStmntFromBlock(ifStmnt.getElseStatement());
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		final Expression newExp;
		if (diff.thenExp.getNodeType() == ASTNode.BOOLEAN_LITERAL)
			newExp = tryToNegateCond(t, r, ifStmnt.getExpression(), ((BooleanLiteral)diff.thenExp).booleanValue());
		else
			newExp = makeParenthesizedConditionalExp(t, r, ifStmnt.getExpression(), diff.thenExp, diff.elseExp);
		if (!checkIfTypeIsExpStmntOrRetStmnt(thenStmnt))
			diffNodes = findDiffNodes(thenStmnt, elseStmnt);
		if (checkIsAssignment((Statement) diffNodes.thenNode) && checkIsAssignment((Statement) diffNodes.elseNode)){
			if (!cmpAsgns(getAssignment((Statement) diffNodes.thenNode), getAssignment((Statement) diffNodes.elseNode)))
				return false;
			final Assignment asgnThen = getAssignment((Statement) diffNodes.thenNode);
			final VariableDeclarationFragment prevDecl = getVarDeclFrag(possiblePrevDecl, asgnThen.getLeftHandSide());
			if (asgnThen.getOperator() == Assignment.Operator.ASSIGN){
				if (thenStmnt.getNodeType() == ASTNode.EXPRESSION_STATEMENT && prevDecl != null){
					r.replace(prevDecl, makeVarDeclFrag(t, r, prevDecl.getName(), newExp), null);
					r.remove(ifStmnt, null);
				} else {
					r.replace(diff.thenExp, newExp, null);
					r.replace(ifStmnt, r.createCopyTarget(thenStmnt), null);
				}
				return true;
			}
		}
		r.replace(diff.thenExp, newExp, null);
		r.replace(ifStmnt, r.createCopyTarget(thenStmnt), null);
		return true;
	}
	static boolean treatAssignIfAssign(final AST ast, final ASTRewrite r, final IfStatement ifStmnt) {
		final ASTNode parent = ifStmnt.getParent();
		if (parent.getNodeType() != ASTNode.BLOCK)
			return false;
		final List<ASTNode> stmts = ((Block) parent).statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		if (ifIdx >= 1) {
			final Assignment asgnThen = getAssignment(ifStmnt.getThenStatement());
			if (asgnThen == null || ifStmnt.getElseStatement() != null)
				return false;
			final Assignment prevAsgn = getAssignment((Statement) stmts.get(ifIdx - 1));
			final Assignment nextAsgn = stmts.size() > ifIdx + 1 ? getAssignment((Statement) stmts.get(ifIdx + 1)) : null;
			final VariableDeclarationFragment prevDecl = findPrevDecl(stmts, ifIdx, asgnThen, prevAsgn, nextAsgn);
			if (tryHandleNextAndPrevAsgnExist(r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl) //
					|| tryHandleOnlyPrevAsgnExist(ast, r, ifStmnt, asgnThen, prevAsgn, prevDecl) //
					|| tryHandleOnlyNextAsgnExist(ast, r, ifStmnt, asgnThen, nextAsgn, prevDecl) //
					|| tryHandleNoNextNoPrevAsgn(ast, r, ifStmnt, asgnThen, prevAsgn, nextAsgn, prevDecl)) //
				return true;
		}
		return false;
	}
	private static boolean tryHandleNoNextNoPrevAsgn(final AST ast,
			final ASTRewrite r, final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (prevAsgn != null || nextAsgn != null)
			return false;
		if (prevDecl != null && prevDecl.getInitializer() != null && ifStmnt.getElseStatement() == null)
			if (!dependsOn(ifStmnt.getExpression(), prevDecl.getName()) && !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())){
				final Expression newInitalizer = makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(), asgnThen.getRightHandSide(),
						prevDecl.getInitializer());
				r.replace(prevDecl, makeVarDeclFrag(ast, r, prevDecl.getName(), newInitalizer), null);
				r.remove(ifStmnt, null);
				return true;
			}
		return false;
	}
	private static boolean tryHandleOnlyNextAsgnExist(final AST ast,
			final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment nextAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (nextAsgn == null)
			return false;
		if (cmpAsgns(nextAsgn, asgnThen)){
			if (prevDecl == null) {
				if (!checkIsAssignment(nextAsgn.getRightHandSide()))
					r.remove(ifStmnt, null);
			} else if (asgnThen.getOperator() == Assignment.Operator.ASSIGN
					&& !dependsOn(nextAsgn.getRightHandSide(), prevDecl.getName())) {
				r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()), null);
				r.remove(ifStmnt, null);
				r.remove(nextAsgn.getParent(), null);
			} else {
				rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, nextAsgn.getRightHandSide());
				r.remove(nextAsgn.getParent(), null);
			}
			return true;
		}
		return false;
	}
	private static boolean tryHandleOnlyPrevAsgnExist(final AST ast,
			final ASTRewrite r, final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (prevAsgn == null || dependsOn(ifStmnt.getExpression(), prevAsgn.getLeftHandSide()))
			return false;
		if (cmpAsgns(prevAsgn, asgnThen) && !checkIsAssignment(prevAsgn.getRightHandSide()))
			if (prevDecl == null) {
				rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, prevAsgn.getRightHandSide());
				r.remove(prevAsgn.getParent(), null);
				return true;
			} else if (!dependsOn(asgnThen.getRightHandSide(), prevDecl.getName())
					&& !dependsOn(prevAsgn.getRightHandSide(), prevDecl.getName())) {
				if (asgnThen.getOperator() == Assignment.Operator.ASSIGN) {
					final Expression newInitalizer = makeParenthesizedConditionalExp(ast, r, ifStmnt.getExpression(), asgnThen.getRightHandSide(),
							prevAsgn.getRightHandSide());
					r.replace(prevDecl, makeVarDeclFrag(ast, r, (SimpleName) prevAsgn.getLeftHandSide(), newInitalizer), null);
					r.remove(ifStmnt, null);
					r.remove(prevAsgn.getParent(), null);
					return true;
				}
			} else if (prevDecl.getInitializer() != null) {
				rewriteAssignIfAssignToAssignTernary(ast, r, ifStmnt, asgnThen, prevAsgn.getRightHandSide());
				r.remove(prevAsgn.getParent(), null);
				return true;
			}
		return false;
	}
	private static boolean tryHandleNextAndPrevAsgnExist(final ASTRewrite r,
			final IfStatement ifStmnt, final Assignment asgnThen,
			final Assignment prevAsgn, final Assignment nextAsgn,
			final VariableDeclarationFragment prevDecl) {
		if (prevAsgn == null || nextAsgn == null)
			return false;
		if (cmpAsgns(nextAsgn, prevAsgn, asgnThen)){
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
	private static VariableDeclarationFragment findPrevDecl(final List<ASTNode> stmts,
			final int ifIdx, final Assignment asgnThen,
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
		Expression thenSideExp = makeInfixExpression(t, r, InfixExpression.Operator.PLUS, asgnThen.getRightHandSide(), otherAsgnExp);
		if (asgnThen.getOperator() == Assignment.Operator.ASSIGN)
			thenSideExp = asgnThen.getRightHandSide();
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
			final ReturnStatement nextReturn = getReturnStatement((Statement) ss.get(ifIdx + 1));
			final ReturnStatement thenSide = getReturnStatement(ifStmnt.getThenStatement());
			final ReturnStatement elseSide = getReturnStatement(ifStmnt.getElseStatement());
			if (nextReturn != null && (thenSide != null && elseSide == null || thenSide == null && elseSide != null))
				return new Range(ifStmnt, nextReturn);
		}
		return null;
	}
	static Range detectIfSameExpStmntOrRet(final IfStatement ifStmnt) {
		final Block parent = asBlock(ifStmnt.getParent());
		if (parent == null)
			return null;
		final Statement thenStmnt = getStmntFromBlock(ifStmnt.getThenStatement());
		final Statement elseStmnt = getStmntFromBlock(ifStmnt.getElseStatement());
		TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
		if (hasNull(diffNodes.thenNode, diffNodes.elseNode) || diffNodes.thenNode.getNodeType() != diffNodes.elseNode.getNodeType())
			return null;
		if (getNumOfStmnts(diffNodes.elseNode) != 1 || getNumOfStmnts(diffNodes.thenNode) != 1)
			return null;
		if (diffNodes.thenNode.getNodeType() == diffNodes.elseNode.getNodeType()){
			if (!checkIfTypeIsExpStmntOrRetStmnt(diffNodes.thenNode)){
				diffNodes = findDiffNodes(diffNodes.thenNode, diffNodes.elseNode);
				if (!checkIfItIsTheOnlyDiff(thenStmnt, elseStmnt, diffNodes) || !handleCaseDiffNodesAreBlocks(diffNodes))
					return null;
			}
			switch(diffNodes.thenNode.getNodeType()){
			case ASTNode.RETURN_STATEMENT: return new Range(ifStmnt);
			case ASTNode.EXPRESSION_STATEMENT: return checkIfOnlyDiffIsExp(diffNodes.thenNode, diffNodes.elseNode) ?
					new Range(ifStmnt) : null;
			default: break;
			}
		}
		return null;
	}
	static boolean checkIfOnlyDiffIsExp(final ASTNode thenStmnt, final ASTNode elseStmnt){
		if (thenStmnt.getNodeType() != ASTNode.EXPRESSION_STATEMENT || elseStmnt.getNodeType() != ASTNode.EXPRESSION_STATEMENT)
			return false;
		final Expression thenExp = ((ExpressionStatement)thenStmnt).getExpression();
		final Expression elseExp = ((ExpressionStatement)elseStmnt).getExpression();
		if (thenExp.getNodeType() != elseExp.getNodeType())
			return false;
		switch(thenExp.getNodeType()){
		case ASTNode.ASSIGNMENT: return cmpAsgns((Assignment)thenExp, (Assignment)elseExp);
		case ASTNode.METHOD_INVOCATION: {
			final String thenMthdName = ((MethodInvocation)thenExp).toString();
			final String elseMthdName = ((MethodInvocation)elseExp).toString();
			return thenMthdName.substring(0, thenMthdName.indexOf("(")).equals(elseMthdName.substring(0, elseMthdName.indexOf("(")));
		}
		default: return false;
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
		@SuppressWarnings("rawtypes")
		final List stmts = parent.statements();
		final int ifIdx = stmts.indexOf(ifStmnt);
		if (ifIdx < 1 && stmts.size() <= ifIdx + 1)
			return null;
		final Assignment asgnThen = getAssignment(ifStmnt.getThenStatement());
		if (asgnThen == null || ifStmnt.getElseStatement() != null)
			return null;
		final Assignment prevAssignment = getAssignment((Statement) stmts.get(ifIdx - 1 >= 0 ? ifIdx - 1 : 0));
		final Assignment nextAssignment = getAssignment((Statement) stmts.get(ifIdx + 1 > stmts.size() - 1 ? stmts.size() - 1
				: ifIdx + 1));
		final VariableDeclarationFragment prevDecl = getVarDeclFrag(prevAssignment != null ? (Statement) stmts.get(ifIdx - 2)
				: (Statement) stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
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
	private static Range detecNoPrevNoNextAsgn(final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAssignment,
			final Assignment nextAssignment, final VariableDeclarationFragment prevDecl) {
		if (prevAssignment != null || nextAssignment != null)
			return null;
		if (prevDecl != null && prevDecl.getInitializer() != null)
			if (!dependsOn(ifStmnt.getExpression(), prevDecl.getName())
					&& !dependsOn(asgnThen.getRightHandSide(), prevDecl.getName()))
				return new Range(prevDecl, ifStmnt);
		return null;
	}
	private static Range detecOnlyNextAsgnExist(final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment nextAssignment,
			final VariableDeclarationFragment prevDecl) {
		if (nextAssignment == null)
			return null;
		if (cmpAsgns(nextAssignment, asgnThen)) {
			if (prevDecl != null && !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()))
				return new Range(prevDecl, nextAssignment);
			return new Range(ifStmnt, nextAssignment);
		}
		return null;
	}
	private static Range detecOnlyPrevAsgnExist(final IfStatement ifStmnt,
			final Assignment asgnThen, final Assignment prevAssignment,
			final VariableDeclarationFragment prevDecl) {
		if (prevAssignment == null || dependsOn(ifStmnt.getExpression(), prevAssignment.getLeftHandSide()))
			return null;
		if (cmpAsgns(prevAssignment, asgnThen)) {
			if (prevDecl != null && prevDecl.getInitializer() == null)
				return !dependsOn(prevAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, ifStmnt) : null;
				return new Range(prevAssignment, ifStmnt);
		}
		return null;
	}
	private static Range detecPrevAndNextAsgnExist(final Assignment asgnThen,
			final Assignment prevAssignment, final Assignment nextAssignment,
			final VariableDeclarationFragment prevDecl) {
		if (prevAssignment == null || nextAssignment == null)
			return null;
		if (cmpAsgns(nextAssignment, prevAssignment, asgnThen)) {
			if (prevDecl != null)
				return !dependsOn(nextAssignment.getRightHandSide(), prevDecl.getName()) ? new Range(prevDecl, nextAssignment) : null;
				return new Range(prevAssignment, nextAssignment);
		}
		return null;
	}
	private static boolean dependsOn(final Expression e, final Expression leftHandSide) {
		return Occurrences.BOTH_SEMANTIC.of(leftHandSide).in(e).size() > 0;
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final IfStatement ifStmnt) {
				return perhaps(detectAssignIfAssign(ifStmnt)) || //
						perhaps(detectIfReturn(ifStmnt)) || //
						perhaps(detectIfSameExpStmntOrRet(ifStmnt)) || //
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
