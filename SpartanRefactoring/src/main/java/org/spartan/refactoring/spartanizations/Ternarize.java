package org.spartan.refactoring.spartanizations;

import static org.spartan.refacotring.utils.As.asBlock;
import static org.spartan.refacotring.utils.As.asReturn;
import static org.spartan.refacotring.utils.Funcs.compatible;
import static org.spartan.refacotring.utils.Funcs.compatibleNames;
import static org.spartan.refacotring.utils.Funcs.compatibleOps;
import static org.spartan.refacotring.utils.Funcs.containIncOrDecExp;
import static org.spartan.refacotring.utils.Funcs.getAssignment;
import static org.spartan.refacotring.utils.Funcs.getBlockSingleStmnt;
import static org.spartan.refacotring.utils.Funcs.getChildren;
import static org.spartan.refacotring.utils.Funcs.getExpression;
import static org.spartan.refacotring.utils.Funcs.getVarDeclFrag;
import static org.spartan.refacotring.utils.Funcs.hasReturn;
import static org.spartan.refacotring.utils.Funcs.makeAssigment;
import static org.spartan.refacotring.utils.Funcs.makeParenthesizedConditionalExp;
import static org.spartan.refacotring.utils.Funcs.makeReturnStatement;
import static org.spartan.refacotring.utils.Funcs.makeVarDeclFrag;
import static org.spartan.refacotring.utils.Funcs.next;
import static org.spartan.refacotring.utils.Funcs.prev;
import static org.spartan.refacotring.utils.Funcs.same;
import static org.spartan.refacotring.utils.Funcs.statements;
import static org.spartan.refacotring.utils.Funcs.statementsCount;
import static org.spartan.refacotring.utils.Funcs.tryToNegateCond;
import static org.spartan.utils.Utils.hasNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
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
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refacotring.utils.Funcs;
import org.spartan.refacotring.utils.Is;
import org.spartan.refacotring.utils.Occurrences;
import org.spartan.utils.Range;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3)
 * @since 2013/01/01
 */
public class Ternarize extends Spartanization {
  /** Instantiates this class */
  public Ternarize() {
    super("Ternarize",
        "Convert conditional to an expression using the ternary (?:) operator" + "or to a return condition statement");
  }

  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement i) {
        return // try lot's of options, but finally return true.
        !inRange(m, i) // Stop here
            || perhapsAssignIfAssign(t, r, i) //
            || perhapsIfReturn(t, r, i) //
            || perhapsIfSameExpStmntOrRet(t, r, i) //
            || true // "i" is beyond hope, perhaps its children
            ;
      }
    });
  }

  static boolean perhapsIfReturn(final AST t, final ASTRewrite r, final IfStatement i) {
    return asBlock(i.getParent()) != null && treatIfReturn(t, r, i, statements(asBlock(i.getParent())));
  }

  private static boolean treatIfReturn(final AST t, final ASTRewrite r, final IfStatement i, final List<ASTNode> stmts) {
    if (!hasReturn(i.getThenStatement()))
      return false;
    final ReturnStatement nextRet = nextStatement(stmts, stmts.indexOf(i));
    return nextRet != null && singletonThen(i) && emptyElse(i) && rewriteIfToRetStmnt(t, r, i, nextRet);
  }

  private static boolean emptyElse(final IfStatement i) {
    return statementsCount(i.getElseStatement()) == 0;
  }

  private static boolean singletonThen(final IfStatement i) {
    return statementsCount(i.getThenStatement()) == 1;
  }

  private static ReturnStatement nextStatement(final List<ASTNode> ns, final int n) {
    return n + 1 >= ns.size() ? null : asReturn(ns.get(n + 1));
  }

  private static boolean rewriteIfToRetStmnt(final AST t, final ASTRewrite r, final IfStatement i,
      final ReturnStatement nextReturn) {
    final ReturnStatement thenRet = asReturn(i.getThenStatement());
    return thenRet == null || Is.conditional(thenRet.getExpression(), nextReturn.getExpression()) ? false
        : rewriteIfToRetStmnt(t, r, i, thenRet.getExpression(), nextReturn.getExpression());
  }

  private static boolean rewriteIfToRetStmnt(final AST t, final ASTRewrite r, final IfStatement i, final Expression thenExp,
      final Expression nextExp) {
    r.replace(i, makeReturnStatement(t, r, determineNewExp(t, r, i.getExpression(), thenExp, nextExp)), null);
    r.remove(nextExp.getParent(), null);
    return true;
  }

  /**
   * @author Tomer Zeltzer contains both sides for the conditional expression
   */
  public static class TwoExpressions {
    final Expression then;
    final Expression elze;

    /**
     * Instantiates the class with the given Expressions
     *
     * @param t
     *          then Expression
     * @param e
     *          else Expression
     */
    public TwoExpressions(final Expression t, final Expression e) {
      then = t;
      elze = e;
    }
  }

  /**
   * @author Tomer Zeltzer contains 2 nodes (used to store the 2 nodes that are
   *         different in the then and else tree)
   */
  public static class TwoNodes {
    ASTNode then;
    ASTNode elze;

    /**
     * Instantiates the class with the given nodes
     *
     * @param t
     *          then node
     * @param e
     *          else node
     */
    public TwoNodes(final ASTNode t, final ASTNode e) {
      then = t;
      elze = e;
    }
  }

  static boolean perhapsIfSameExpStmntOrRet(final AST t, final ASTRewrite r, final IfStatement i) {
    final Statement then = getBlockSingleStmnt(i.getThenStatement());
    final Statement elze = getBlockSingleStmnt(i.getElseStatement());
    return !hasNull(asBlock(i.getParent()), then, elze) && treatIfSameExpStmntOrRet(t, r, i, then, elze);
  }

  private static boolean treatIfSameExpStmntOrRet(final AST t, final ASTRewrite r, final IfStatement ifStmt,
      final Statement thenStmnt, final Statement elseStmnt) {
    final List<TwoNodes> diffList = differences(thenStmnt, elseStmnt);
    if (!isDiffListValid(diffList))
      return false;
    final int ifIdx = statements(ifStmt.getParent()).indexOf(ifStmt);
    final Statement possiblePrevDecl = (Statement) statements(ifStmt.getParent()).get(ifIdx - 1 < 0 ? ifIdx : ifIdx - 1);
    boolean wasPrevDeclReplaced = false;
    for (int i = 0; i < diffList.size(); i++) {
      final TwoExpressions diffExps = findSingleDifference(diffList.get(i).then, diffList.get(i).elze);
      if (Is.conditional(diffExps.then, diffExps.elze))
        return false;
      if (canReplacePrevDecl(possiblePrevDecl, diffList.get(i)))
        wasPrevDeclReplaced = true;
      if (!isExpOnlyDiff(diffList.get(i), diffExps) || !substitute(t, r, ifStmt, diffExps, possiblePrevDecl))
        return false;
    }
    if (!wasPrevDeclReplaced)
      r.replace(ifStmt, r.createCopyTarget(thenStmnt), null);
    return true;
  }

  private static boolean isDiffListValid(final List<TwoNodes> diffList) {
    if (diffList == null)
      return false;
    for (int i = 0; i < diffList.size(); i++) {
      if (!handleCaseDiffNodesAreBlocks(diffList.get(i)))
        return false;
      if (!isExpStmntOrRet(diffList.get(i).then) || !isExpStmntOrRet(diffList.get(i).elze)) {
        if (!isExpStmntOrRet(diffList.get(i).then.getParent()) || !isExpStmntOrRet(diffList.get(i).elze.getParent()))
          return false;
        diffList.get(i).then = diffList.get(i).then.getParent();
        diffList.get(i).elze = diffList.get(i).elze.getParent();
      }
      if (!areExpsValid(diffList.get(i)))
        return false;
    }
    return true;
  }

  private static boolean areExpsValid(final TwoNodes diffNodes) {
    return diffNodes.then.getNodeType() != diffNodes.elze.getNodeType() ? false : areExpsValid(diffNodes, findDiffExps(diffNodes));
  }

  private static boolean areExpsValid(final TwoNodes diffNodes, final TwoExpressions diffExps) {
    return diffExps != null && !Is.conditional(diffExps.then, diffExps.elze) && !containIncOrDecExp(diffExps.then, diffExps.elze)
        && isExpOnlyDiff(diffNodes, diffExps);
  }

  private static TwoExpressions findSingleDifference(final ASTNode thenStmnt, final ASTNode elseStmnt) {
    final TwoNodes diffNodes = new TwoNodes(thenStmnt, elseStmnt);
    if (!handleCaseDiffNodesAreBlocks(diffNodes))
      return null;
    final TwoExpressions $ = findDiffExps(diffNodes);
    if (Is.expressionStatement(diffNodes.then))
      return $;
    return $ == null || !Is.isReturn(diffNodes.then) ? null //
        : new TwoExpressions(getExpression(diffNodes.then), getExpression(diffNodes.elze));
  }

  private static TwoExpressions findDiffExps(final TwoNodes diffNodes) {
    TwoNodes $ = findDiffNodes(diffNodes.then, diffNodes.elze);
    if (Is.expressionStatement(diffNodes.then))
      $ = findDiffNodes($.then, $.elze);
    return $ == null ? null : new TwoExpressions((Expression) $.then, (Expression) $.elze);
  }

  private static boolean isExpOnlyDiff(final TwoNodes diffNodes, final TwoExpressions diffExps) {
    return hasNull(diffNodes, diffNodes.then, diffNodes.elze) ? false //
        : isExpOnlyDiff(diffNodes.then, diffNodes.elze, diffExps);
  }

  private static boolean isExpOnlyDiff(final ASTNode then, final ASTNode elze, final TwoExpressions diffExps) {
    return diffExps != null ? isExpOnlyDiff(then, elze, diffExps.then, diffExps.elze)
        : !Is.assignment(then) //
            || !Is.assignment(elze) //
            || compatible(getAssignment((ExpressionStatement) then), getAssignment((ExpressionStatement) elze));
  }

  private static boolean isExpOnlyDiff(final ASTNode then, final ASTNode elze, final Expression thenExp, final Expression elseExp) {
    return !Is.assignment(then) || !Is.assignment(elze)//
        ? same(prepareSubTree(then, thenExp), prepareSubTree(elze, elseExp)) //
        : compatible(getAssignment((ExpressionStatement) then), getAssignment((ExpressionStatement) elze));
  }

  private static List<ASTNode> prepareSubTree(final ASTNode n, final Expression e) {
    final List<ASTNode> $ = getChildren(n);
    if (Is.expressionStatement(n))
      $.remove(((ExpressionStatement) n).getExpression());
    $.remove(e);
    $.removeAll(getChildren(e));
    return $;
  }

  private static boolean isExpStmntOrRet(final ASTNode n) {
    return n != null && (Is.expressionStatement(n) || Is.isReturn(n));
  }

  private static boolean handleCaseDiffNodesAreBlocks(final TwoNodes diffNodes) {
    if (statementsCount(diffNodes.then) != 1 || statementsCount(diffNodes.elze) != 1)
      return false;
    diffNodes.then = getStmntIfBlock(diffNodes.then);
    diffNodes.elze = getStmntIfBlock(diffNodes.elze);
    return true;
  }

  private static ASTNode getStmntIfBlock(final ASTNode n) {
    return n == null || ASTNode.BLOCK != n.getNodeType() ? n : getBlockSingleStmnt((Block) n);
  }

  private static TwoNodes findDiffNodes(final ASTNode thenNode, final ASTNode elseNode) {
    return hasNull(thenNode, elseNode) ? null : findFirstDifference(getChildren(thenNode), getChildren(elseNode));
  }

  private static TwoNodes findFirstDifference(final List<ASTNode> thenList, final List<ASTNode> elseList) {
    for (int i = 0; i < thenList.size() && i < elseList.size(); i++) {
      final ASTNode then = thenList.get(i);
      final ASTNode elze = elseList.get(i);
      if (!same(then, elze))
        return new TwoNodes(then, elze);
    }
    return null;
  }

  private static List<TwoNodes> differences(final ASTNode thenNode, final ASTNode elseNode) {
    return hasNull(thenNode, elseNode) ? null : findDiffList(getChildren(thenNode), getChildren(elseNode));
  }

  private static List<TwoNodes> findDiffList(final List<ASTNode> thenList, final List<ASTNode> elseList) {
    final List<TwoNodes> $ = new ArrayList<>();
    for (int i = 0; i < thenList.size() && i < elseList.size(); i++) {
      final ASTNode then = thenList.get(i);
      final ASTNode elze = elseList.get(i);
      if (!same(then, elze)) {
        $.add(new TwoNodes(then, elze));
        thenList.removeAll(getChildren(then));
        elseList.removeAll(getChildren(elze));
      }
    }
    return $;
  }

  private static boolean substitute(final AST t, final ASTRewrite r, final IfStatement i, final TwoExpressions diff,
      final Statement possiblePrevDecl) {
    final Statement elze = getBlockSingleStmnt(i.getElseStatement());
    final Statement then = getBlockSingleStmnt(i.getThenStatement());
    final TwoNodes diffNodes = !isExpStmntOrRet(then) ? findDiffNodes(then, elze) : new TwoNodes(then, elze);
    final Expression newExp = determineNewExp(t, r, i.getExpression(), diff.then, diff.elze);
    if (Is.assignment(diffNodes.then) && Is.assignment(diffNodes.elze))
      if (!compatible(getAssignment((Statement) diffNodes.then), getAssignment((Statement) diffNodes.elze)))
        return false;
      else if (canReplacePrevDecl(possiblePrevDecl, diffNodes))
        return handleSubIfDiffAreAsgns(t, r, i, possiblePrevDecl, diffNodes.then, newExp);
    r.replace(diff.then, newExp, null);
    return true;
  }

  private static boolean canReplacePrevDecl(final Statement possiblePrevDecl, final TwoNodes diffNodes) {
    return !Is.expressionStatement(diffNodes.then) || diffNodes.then.getNodeType() != diffNodes.elze.getNodeType() ? false
        : canReplacePrevDecl(possiblePrevDecl, (ExpressionStatement) diffNodes.then, (ExpressionStatement) diffNodes.elze);
  }

  private static boolean canReplacePrevDecl(final Statement possiblePrevDecl, final ExpressionStatement thenExpStmt,
      final ExpressionStatement elseExpStmt) {
    final List<VariableDeclarationFragment> frags = !Is.isVarDeclStmt(possiblePrevDecl) ? null
        : ((VariableDeclarationStatement) possiblePrevDecl).fragments();
    final Assignment then = getAssignment(thenExpStmt);
    final Assignment elze = getAssignment(elseExpStmt);
    return hasNull(then, elze, frags) //
        || !Is.plainAssignment(then) //
        || !compatibleOps(then.getOperator(), elze.getOperator()) ? false //
            : possibleToReplace(then, frags) && possibleToReplace(elze, frags);
  }

  private static boolean handleSubIfDiffAreAsgns(final AST t, final ASTRewrite r, final IfStatement i,
      final Statement possiblePrevDecl, final ASTNode thenNode, final Expression newExp) {
    final VariableDeclarationFragment prevDecl = getVarDeclFrag(possiblePrevDecl,
        getAssignment((Statement) thenNode).getLeftHandSide());
    r.replace(prevDecl, makeVarDeclFrag(t, r, prevDecl.getName(), newExp), null);
    r.remove(i, null);
    return true;
  }

  private static Expression determineNewExp(final AST t, final ASTRewrite r, final Expression cond, final Expression thenExp,
      final Expression elseExp) {
    return !Is.isBooleanLiteral(thenExp) || !Is.isBooleanLiteral(elseExp)
        ? makeParenthesizedConditionalExp(t, r, cond, thenExp, elseExp)
        : tryToNegateCond(t, r, cond, ((BooleanLiteral) thenExp).booleanValue());
  }

  static boolean perhapsAssignIfAssign(final AST t, final ASTRewrite r, final IfStatement i) {
    return asBlock(i.getParent()) != null && treatAssignIfAssign(t, r, i, statements(asBlock(i.getParent())));
  }

  private static boolean treatAssignIfAssign(final AST t, final ASTRewrite r, final IfStatement i, final List<ASTNode> stmts) {
    detectAssignIfAssign(i);
    final int where = stmts.indexOf(i);
    final Assignment then = getAssignment(i.getThenStatement());
    if (then == null || i.getElseStatement() != null || where < 1)
      return false;
    final Assignment prevAsgn = getAssignment((Statement) stmts.get(where - 1));
    final Assignment nextAsgn = 1 + where >= stmts.size() ? null : getAssignment((Statement) stmts.get(1 + where));
    final VariableDeclarationFragment prevDecl = findPrevDecl(stmts, where, then, prevAsgn, nextAsgn);
    return tryHandleNextAndPrevAsgnExist(r, i, then, prevAsgn, nextAsgn, prevDecl) //
        || tryHandleOnlyPrevAsgnExist(t, r, i, then, prevAsgn, prevDecl) //
        || tryHandleOnlyNextAsgnExist(t, r, i, then, nextAsgn, prevDecl) //
        || tryHandleNoNextNoPrevAsgn(t, r, i, then, prevAsgn, nextAsgn, prevDecl);
  }

  private static boolean tryHandleNoNextNoPrevAsgn(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then,
      final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    if (!isNoNextNoPrevAsgnPossible(i, then, prevAsgn, nextAsgn, prevDecl))
      return false;
    r.replace(prevDecl, makeVarDeclFrag(t, r, prevDecl.getName(),
        makeParenthesizedConditionalExp(t, r, i.getExpression(), then.getRightHandSide(), prevDecl.getInitializer())), null);
    r.remove(i, null);
    return true;
  }

  private static boolean isNoNextNoPrevAsgnPossible(final IfStatement i, final Assignment then, final Assignment prevAsgn,
      final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    return prevAsgn == null //
        && nextAsgn == null //
        && !Is.conditional(then.getRightHandSide()) //
        && prevDecl != null //
        && prevDecl.getInitializer() != null //
        && i.getElseStatement() == null //
        && !Is.conditional(prevDecl.getInitializer()) //
        && !dependsOn(prevDecl.getName(), i.getExpression(), then.getRightHandSide())//
        ;
  }

  private static boolean tryHandleOnlyNextAsgnExist(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then,
      final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    if (!isOnlyNextAsgnPossible(then, nextAsgn))
      return false;
    if (prevDecl == null && !Is.assignment(nextAsgn.getRightHandSide()))
      r.remove(i, null);
    else if (prevDecl == null || !Is.plainAssignment(then) || dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()))
      return handleNoPrevDecl(t, r, i, then, nextAsgn);
    else {
      r.replace(prevDecl, makeVarDeclFrag(t, r, (SimpleName) nextAsgn.getLeftHandSide(), nextAsgn.getRightHandSide()), null);
      r.remove(i, null);
      r.remove(nextAsgn.getParent(), null);
    }
    return true;
  }

  private static boolean isOnlyNextAsgnPossible(final Assignment then, final Assignment nextAsgn) {
    return nextAsgn != null && compatible(nextAsgn, then) && !Is.conditional(nextAsgn.getRightHandSide(), then.getRightHandSide())
        && !same(then.getRightHandSide(), nextAsgn.getRightHandSide())
        && !same(nextAsgn.getRightHandSide(), then.getRightHandSide());
  }

  private static boolean tryHandleOnlyPrevAsgnExist(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then,
      final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
    if (!isOnlyPrevAsgnPossible(i, then, prevAsgn))
      return false;
    return prevDecl == null ? handleNoPrevDecl(t, r, i, then, prevAsgn) //
        : handlePrevDeclExist(t, r, i, then, prevAsgn, prevDecl);
  }

  private static boolean isOnlyPrevAsgnPossible(final IfStatement i, final Assignment then, final Assignment prevAsgn) {
    return prevAsgn != null //
        && !dependsOn(prevAsgn.getLeftHandSide(), i.getExpression()) //
        && !Is.conditional(prevAsgn.getRightHandSide(), then.getRightHandSide()) //
        && !Is.assignment(prevAsgn.getRightHandSide()) && compatible(prevAsgn, then)
        && !same(prevAsgn.getRightHandSide(), then.getRightHandSide());
  }

  private static boolean handlePrevDeclExist(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then,
      final Assignment prevAsgn, final VariableDeclarationFragment prevDecl) {
    if (dependsOn(prevDecl.getName(), then.getRightHandSide(), prevAsgn.getRightHandSide()) || !Is.plainAssignment(then)) {
      if (prevDecl.getInitializer() != null)
        return handleNoPrevDecl(t, r, i, then, prevAsgn);
    } else {
      r.replace(prevDecl,
          makeVarDeclFrag(t, r, (SimpleName) prevAsgn.getLeftHandSide(),
              makeParenthesizedConditionalExp(t, r, i.getExpression(), then.getRightHandSide(), prevAsgn.getRightHandSide())),
          null);
      r.remove(i, null);
      r.remove(prevAsgn.getParent(), null);
      return true;
    }
    return false;
  }

  private static boolean handleNoPrevDecl(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then,
      final Assignment prevAsgn) {
    rewriteAssignIfAssignToAssignTernary(t, r, i, then, prevAsgn.getRightHandSide());
    r.remove(prevAsgn.getParent(), null);
    return true;
  }

  private static boolean tryHandleNextAndPrevAsgnExist(final ASTRewrite r, final IfStatement i, final Assignment then,
      final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    if (!isNextAndPrevAsgnPossible(then, prevAsgn, nextAsgn))
      return false;
    if (prevDecl == null)
      r.replace(prevAsgn.getParent(), nextAsgn.getParent(), null);
    else if (Is.plainAssignment(then)) {
      r.replace(prevDecl.getInitializer(), nextAsgn.getRightHandSide(), null);
      r.remove(prevAsgn.getParent(), null);
    }
    r.remove(i, null);
    r.remove(nextAsgn.getParent(), null);
    return true;
  }

  private static boolean isNextAndPrevAsgnPossible(final Assignment then, final Assignment prevAsgn, final Assignment nextAsgn) {
    return !hasNull(prevAsgn, nextAsgn) && compatible(nextAsgn, prevAsgn, then)
        && !Is.conditional(prevAsgn.getRightHandSide(), nextAsgn.getRightHandSide(), then.getRightHandSide());
  }

  private static VariableDeclarationFragment findPrevDecl(final List<ASTNode> ns, final int ifIdx, final Assignment then,
      final Assignment prev, final Assignment next) {
    VariableDeclarationFragment $ = null;
    if (prev != null && ifIdx - 2 >= 0 && compatibleNames(then.getLeftHandSide(), prev.getLeftHandSide()))
      $ = getVarDeclFrag(ns.get(ifIdx - 2), then.getLeftHandSide());
    else if (next == null && ifIdx >= 1)
      $ = getVarDeclFrag(ns.get(ifIdx - 1), then.getLeftHandSide());
    // TODO: I do not think that this conditional will ever be true
    else if (next != null && ifIdx >= 1 && compatibleNames(then.getLeftHandSide(), next.getLeftHandSide()))
      $ = getVarDeclFrag(ns.get(ifIdx - 1), next.getLeftHandSide());
    return $;
  }

  private static void rewriteAssignIfAssignToAssignTernary(final AST t, final ASTRewrite r, final IfStatement i,
      final Assignment then, final Expression otherAsgnExp) {
    final Expression thenSideExp = Is.plainAssignment(then) ? then.getRightHandSide()
        : Funcs.makeInfixExpression(r, t, then.getRightHandSide(), InfixExpression.Operator.PLUS, otherAsgnExp);
    final Expression newCond = makeParenthesizedConditionalExp(t, r, i.getExpression(), thenSideExp, otherAsgnExp);
    r.replace(i, t.newExpressionStatement(makeAssigment(t, r, then.getOperator(), newCond, then.getLeftHandSide())), null);
  }

  static Range detectIfReturn(final IfStatement i) {
    return statements(i.getParent()) == null ? null : detectIfReturn(i, statements(i.getParent()));
  }

  private static Range detectIfReturn(final IfStatement s, final List<ASTNode> ss) {
    return Is.isLast(s, ss) ? null : detectIfReturn(s.getThenStatement(), s.getElseStatement(), ss, ss.indexOf(s));
  }

  private static Range detectIfReturn(final Statement thenStmt, final Statement elseStmt, final List<ASTNode> ss, final int ifIdx) {
    final ReturnStatement nextRet = asReturn(ss.get(1 + ifIdx));
    if (nextRet == null || Is.conditional(nextRet.getExpression()))
      return null;
    final ReturnStatement then = asReturn(thenStmt);
    final ReturnStatement elze = asReturn(elseStmt);
    return (then == null || elze != null || Is.conditional(then.getExpression()))
        && (then != null || elze == null || Is.conditional(elze.getExpression())) ? null
            : new Range(thenStmt != null ? thenStmt.getParent() : elseStmt.getParent(), nextRet);
  }

  static Range detectIfSameExpStmntOrRet(final IfStatement i) {
    if (hasNull(getBlockSingleStmnt(i.getThenStatement()), getBlockSingleStmnt(i.getElseStatement()), asBlock(i.getParent())))
      return null;
    return !isDiffListValid(differences(i.getThenStatement(), i.getElseStatement())) ? null : new Range(i);
  }

  static Range detectAssignIfAssign(final IfStatement i) {
    return asBlock(i.getParent()) == null ? null : detectAssignIfAssign(i, statements(asBlock(i.getParent())));
  }

  private static Range detectAssignIfAssign(final IfStatement i, final List<ASTNode> stmts) {
    final Assignment then = getAssignment(i.getThenStatement());
    if (then == null || i.getElseStatement() != null)
      return null;
    final int ifIdx = stmts.indexOf(i);
    final Assignment nextAsgn = getAssignment((Statement) next(ifIdx, stmts));
    final Assignment prevAsgn = getAssignment((Statement) prev(ifIdx, stmts));
    final VariableDeclarationFragment prevDecl = getVarDeclFrag(prevAsgn != null ? prev2(stmts, ifIdx) : prev(ifIdx, stmts),
        then.getLeftHandSide());
    Range $ = detecPrevAndNextAsgnExist(then, prevAsgn, nextAsgn, prevDecl);
    $ = $ != null ? $ : detecOnlyPrevAsgnExist(i, then, prevAsgn, prevDecl);
    $ = $ != null ? $ : detecOnlyNextAsgnExist(i, then, nextAsgn, prevDecl);
    return $ != null ? $ : detecNoPrevNoNextAsgn(i, then, prevAsgn, nextAsgn, prevDecl);
  }

  private static ASTNode prev2(final List<ASTNode> stmts, final int ifIdx) {
    return stmts.get(ifIdx < 2 ? 0 : ifIdx - 2);
  }

  private static Range detecNoPrevNoNextAsgn(final IfStatement i, final Assignment then, final Assignment prevAsgn,
      final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    return prevAsgn != null || nextAsgn != null || prevDecl == null || prevDecl.getInitializer() == null
        || dependsOn(prevDecl.getName(), i.getExpression(), then.getRightHandSide()) ? null : new Range(prevDecl, i);
  }

  private static Range detecOnlyNextAsgnExist(final IfStatement i, final Assignment then, final Assignment nextAsgn,
      final VariableDeclarationFragment prevDecl) {
    if (nextAsgn == null || !compatible(nextAsgn, then))
      return null;
    return //
    prevDecl == null || dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()) //
        ? new Range(i, nextAsgn) //
        : new Range(prevDecl, nextAsgn);
  }

  private static Range detecOnlyPrevAsgnExist(final IfStatement i, final Assignment then, final Assignment prevAsgn,
      final VariableDeclarationFragment prevDecl) {
    if (prevAsgn == null || dependsOn(prevAsgn.getLeftHandSide(), i.getExpression()) || !compatible(prevAsgn, then))
      return null;
    if (prevDecl != null && prevDecl.getInitializer() == null)
      return dependsOn(prevDecl.getName(), prevAsgn.getRightHandSide()) ? null : new Range(prevDecl, i);
    return new Range(prevAsgn, i);
  }

  private static Range detecPrevAndNextAsgnExist(final Assignment then, final Assignment prevAsgn, final Assignment nextAsgn,
      final VariableDeclarationFragment prevDecl) {
    if (hasNull(prevAsgn, nextAsgn) || !compatible(nextAsgn, prevAsgn, then))
      return null;
    if (prevDecl != null)
      return dependsOn(prevDecl.getName(), nextAsgn.getRightHandSide()) ? null : new Range(prevDecl, nextAsgn);
    return new Range(prevAsgn, nextAsgn);
  }

  private static boolean dependsOn(final Expression expToCheck, final Expression... possiblyDependentExps) {
    for (final Expression pde : possiblyDependentExps)
      if (!Occurrences.BOTH_SEMANTIC.of(expToCheck).in(pde).isEmpty())
        return true;
    return false;
  }

  private static boolean possibleToReplace(final Assignment a, final List<VariableDeclarationFragment> frags) {
    final int i = findIndexOfAsgn(a.getLeftHandSide(), frags);
    if (i < 0)
      return false;
    for (final VariableDeclarationFragment frag : frags)
      if (same(a.getRightHandSide(), frag.getName())
          && (i < frags.indexOf(frag) || i != frags.indexOf(frag) && frag.getInitializer() == null))
        return false;
    return true;
  }

  private static int findIndexOfAsgn(final Expression name, final List<VariableDeclarationFragment> vs) {
    for (final VariableDeclarationFragment v : vs)
      if (same(v.getName(), name))
        return vs.indexOf(v);
    return -1;
  }

  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final IfStatement i) {
        return false //
            || perhaps(detectAssignIfAssign(i)) //
            || perhaps(detectIfReturn(i)) //
            || perhaps(detectIfSameExpStmntOrRet(i))//
            || true;
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
}
