package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.asReturnStatement;
import static org.spartan.refactoring.utils.Funcs.asSimpleName;
import static org.spartan.refactoring.utils.Funcs.collectDescendants;
import static org.spartan.refactoring.utils.Funcs.compatible;
import static org.spartan.refactoring.utils.Funcs.compatibleNames;
import static org.spartan.refactoring.utils.Funcs.compatibleOps;
import static org.spartan.refactoring.utils.Funcs.containIncOrDecExp;
import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.getVarDeclFrag;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.makeVariableDeclarationFragment;
import static org.spartan.refactoring.utils.Funcs.next;
import static org.spartan.refactoring.utils.Funcs.prev;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Funcs.then;
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
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Occurrences;
import org.spartan.refactoring.utils.Occurrences.Of;
import org.spartan.refactoring.utils.Subject;
import org.spartan.utils.Range;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3)
 * @since 2013/01/01
 */
@Deprecated public class Ternarize extends Spartanization {
  private static boolean areExpsValid(final Pair diffNodes) {
    return diffNodes.then.getNodeType() == diffNodes.elze.getNodeType() && areExpsValid(diffNodes, findDiffExps(diffNodes));
  }
  private static boolean areExpsValid(final Pair diffNodes, final TwoExpressions diffExps) {
    return diffExps != null && !Is.conditional(diffExps.then, diffExps.elze) && !containIncOrDecExp(diffExps.then, diffExps.elze) && isExpOnlyDiff(diffNodes, diffExps);
  }
  private static boolean canReplacePrevDecl(final Statement s, final ExpressionStatement thenExpStmt, final ExpressionStatement elseExpStmt) {
    final List<VariableDeclarationFragment> frags = !Is.variableDeclarationStatement(s) ? null : ((VariableDeclarationStatement) s).fragments();
    final Assignment then = Extract.assignment(thenExpStmt);
    final Assignment elze = Extract.assignment(elseExpStmt);
    return !hasNull(then, elze, frags) && Is.plainAssignment(then) && compatibleOps(then.getOperator(), elze.getOperator()) && possibleToReplace(then, frags)
        && possibleToReplace(elze, frags);
  }
  private static boolean canReplacePrevDecl(final Statement possiblePrevDecl, final Pair diffNodes) {
    return Is.expressionStatement(diffNodes.then) && diffNodes.then.getNodeType() == diffNodes.elze.getNodeType()
        && canReplacePrevDecl(possiblePrevDecl, (ExpressionStatement) diffNodes.then, (ExpressionStatement) diffNodes.elze);
  }
  private static Range detecOnlyNextAsgnExist(final IfStatement i, final Assignment then, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    return nextAsgn == null || !compatible(nextAsgn, then) ? null
        : new Range(prevDecl == null || !occurences(prevDecl).in(new Expression[] { right(nextAsgn) }).isEmpty() ? i : prevDecl, nextAsgn);
  }

  private static Range detecOnlyPrevAsgnExist(final IfStatement i, final Assignment then, final Assignment a, final VariableDeclarationFragment f) {
    final Expression[] es = { i.getExpression() };
    return a == null || !occurences(asSimpleName(left(a))).in(es).isEmpty() || !compatible(a, then) ? null
        : f == null || f.getInitializer() != null ? new Range(a, i) : !occurences(f).in(new Expression[] { right(a) }).isEmpty() ? null : new Range(f, i);
  }
  static Of occurences(final SimpleName n) {
    return Occurrences.BOTH_SEMANTIC.of(n);
  }
  static Of occurences(final VariableDeclarationFragment f) {
    return Occurrences.BOTH_SEMANTIC.of(f);
  }
  private static Range detecPrevAndNextAsgnExist(final Assignment then, final Assignment prevAsgn, final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    return hasNull(prevAsgn, nextAsgn) || !compatible(nextAsgn, prevAsgn, then) ? null
        : prevDecl == null ? new Range(prevAsgn, nextAsgn) : occurences(prevDecl).existIn(new Expression[] { right(nextAsgn) }) ? null : new Range(prevDecl, nextAsgn);
  }
  private static Range detectAssignIfAssign(final Block parent, final IfStatement i) {
    return parent == null ? null : detectAssignIfAssign(i, siblings(i));
  }
  private static Range detectAssignIfAssign(final IfStatement s, final List<Statement> ss) {
    final Assignment then = Extract.assignment(then(s));
    if (then == null || elze(s) != null)
      return null;
    final int ifIdx = ss.indexOf(s);
    final Assignment nextAsgn = Extract.assignment(next(ifIdx, ss));
    final Assignment prevAsgn = Extract.assignment(prev(ifIdx, ss));
    final VariableDeclarationFragment prevDecl = getVarDeclFrag(prevAsgn == null ? prev(ifIdx, ss) : prev2(ss, ifIdx), left(then));
    Range $;
    return //
    //
    ($ = detecPrevAndNextAsgnExist(then, prevAsgn, nextAsgn, prevDecl)) != null || //
        ($ = detecOnlyPrevAsgnExist(s, then, prevAsgn, prevDecl)) != null || //
        ($ = detecOnlyNextAsgnExist(s, then, nextAsgn, prevDecl)) != null//
            ? $ : null;
  }
  private static Range detectIfReturn(final IfStatement s, final List<Statement> ss) {
    return Is.last(s, ss) ? null : detectIfReturn(then(s), elze(s), ss, ss.indexOf(s));
  }
  private static Range detectIfReturn(final Statement thenStmt, final Statement elseStmt, final List<Statement> ss, final int ifIdx) {
    final ReturnStatement nextRet = asReturnStatement(ss.get(1 + ifIdx));
    if (nextRet == null || Is.conditional(nextRet.getExpression()))
      return null;
    final ReturnStatement then = asReturnStatement(thenStmt);
    final ReturnStatement elze = asReturnStatement(elseStmt);
    return (then == null || elze != null || Is.conditional(then.getExpression())) && (then != null || elze == null || Is.conditional(elze.getExpression())) ? null
        : new Range((thenStmt != null ? thenStmt : elseStmt).getParent(), nextRet);
  }
  private static Expression determineNewExp(final Expression cond, final Expression thenExp, final Expression elseExp) {
    return !Is.booleanLiteral(thenExp) || !Is.booleanLiteral(elseExp) ? Subject.pair(thenExp, elseExp).toCondition(cond)
        : tryToNegateCond(cond, ((BooleanLiteral) thenExp).booleanValue());
  }
  private static List<Pair> differences(final ASTNode thenNode, final ASTNode elseNode) {
    return hasNull(thenNode, elseNode) ? null : findDiffList(collectDescendants(thenNode), collectDescendants(elseNode));
  }
  private static TwoExpressions findDiffExps(final Pair diffNodes) {
    final Pair $ = findDiffNodes(diffNodes.then, diffNodes.elze);
    if (Is.expressionStatement(diffNodes.then))
      findDiffNodes($.then, $.elze);
    return $ == null ? null : new TwoExpressions((Expression) $.then, (Expression) $.elze);
  }
  private static List<Pair> findDiffList(final List<ASTNode> thenList, final List<ASTNode> elseList) {
    final List<Pair> $ = new ArrayList<>();
    for (int i = 0; i < thenList.size() && i < elseList.size(); ++i) {
      final ASTNode then = thenList.get(i);
      final ASTNode elze = elseList.get(i);
      if (!same(then, elze)) {
        $.add(new Pair(then, elze));
        thenList.removeAll(collectDescendants(then));
        elseList.removeAll(collectDescendants(elze));
      }
    }
    return $;
  }
  private static Pair findDiffNodes(final ASTNode thenNode, final ASTNode elseNode) {
    return hasNull(thenNode, elseNode) ? null : findFirstDifference(collectDescendants(thenNode), collectDescendants(elseNode));
  }
  private static Pair findFirstDifference(final List<ASTNode> thenList, final List<ASTNode> elseList) {
    for (int i = 0; i < thenList.size() && i < elseList.size(); ++i) {
      final ASTNode then = thenList.get(i);
      final ASTNode elze = elseList.get(i);
      if (!same(then, elze))
        return new Pair(then, elze);
    }
    return null;
  }
  private static int findIndexOfAsgn(final Expression name, final List<VariableDeclarationFragment> vs) {
    for (final VariableDeclarationFragment v : vs)
      if (same(v.getName(), name))
        return vs.indexOf(v);
    return -1;
  }
  private static VariableDeclarationFragment findPrevDecl(final List<Statement> ns, final int ifIdx, final Assignment then, final Assignment prev, final Assignment next) {
    VariableDeclarationFragment $ = null;
    if (prev != null && ifIdx - 2 >= 0 && compatibleNames(left(then), left(prev)))
      $ = getVarDeclFrag(ns.get(ifIdx - 2), left(then));
    else if (next == null && ifIdx >= 1)
      $ = getVarDeclFrag(ns.get(ifIdx - 1), left(then));
    else if (next != null && ifIdx >= 1 && compatibleNames(left(then), left(next)))
      $ = getVarDeclFrag(ns.get(ifIdx - 1), left(next));
    return $;
  }
  private static TwoExpressions findSingleDifference(final ASTNode thenStmnt, final ASTNode elseStmnt) {
    final Pair diffNodes = new Pair(thenStmnt, elseStmnt);
    if (!handleCaseDiffNodesAreBlocks(diffNodes))
      return null;
    final TwoExpressions $ = findDiffExps(diffNodes);
    return Is.expressionStatement(diffNodes.then) ? $
        : $ == null || !Is.return_(diffNodes.then) ? null : new TwoExpressions(Extract.expression(diffNodes.then), Extract.expression(diffNodes.elze));
  }
  private static boolean handleCaseDiffNodesAreBlocks(final Pair diffNodes) {
    if (!Is.singletonStatement(diffNodes.then) || !Is.singletonStatement(diffNodes.elze))
      return false;
    diffNodes.then = Extract.singleStatement(diffNodes.then);
    diffNodes.elze = Extract.singleStatement(diffNodes.elze);
    return true;
  }
  private static boolean handleNoPrevDecl(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then, final Assignment prevAsgn) {
    rewriteAssignIfAssignToAssignTernary(t, r, i, then, right(prevAsgn));
    r.remove(prevAsgn.getParent(), null);
    return true;
  }
  private static boolean handlePrevDeclExist(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then, final Assignment prev,
      final VariableDeclarationFragment f) {
        final Expression[] es = { right(then), right(prev) };
        if (!occurences(f).existIn(es) && Is.plainAssignment(then)) {
          r.replace(f, makeVariableDeclarationFragment(t, r, (SimpleName) left(prev), Subject.pair(right(then), right(prev)).toCondition(i.getExpression())), null);
          r.remove(i, null);
          r.remove(prev.getParent(), null);
          return true;
        }
        return f.getInitializer() != null && handleNoPrevDecl(t, r, i, then, prev);
      }
  private static boolean handleSubIfDiffAreAsgns(final AST t, final ASTRewrite r, final IfStatement i, final Statement possiblePrevDecl, final ASTNode thenNode,
      final Expression newExp) {
    final VariableDeclarationFragment prevDecl = getVarDeclFrag(possiblePrevDecl, left( Extract.assignment(thenNode)));
    r.replace(prevDecl, makeVariableDeclarationFragment(t, r, prevDecl.getName(), newExp), null);
    r.remove(i, null);
    return true;
  }
  private static boolean isDiffListValid(final List<Pair> diffList) {
    if (diffList == null)
      return false;
    for (int i = 0; i < diffList.size(); ++i) {
      final Pair pair = diffList.get(i);
      if (!handleCaseDiffNodesAreBlocks(pair))
        return false;
      if (!isExpStmntOrRet(pair.then) || !isExpStmntOrRet(pair.elze)) {
        if (!isExpStmntOrRet(pair.then.getParent()) || !isExpStmntOrRet(pair.elze.getParent()))
          return false;
        pair.then = pair.then.getParent();
        pair.elze = pair.elze.getParent();
      }
      if (!areExpsValid(pair))
        return false;
    }
    return true;
  }
  private static boolean isExpOnlyDiff(final ASTNode then, final ASTNode elze, final Expression thenExp, final Expression elseExp) {
    return Is.assignment(then) && Is.assignment(elze)//
        ? compatible(Extract.assignment(then), Extract.assignment(elze)) //
        : same(prepareSubTree(then, thenExp), prepareSubTree(elze, elseExp));
  }
  private static boolean isExpOnlyDiff(final ASTNode then, final ASTNode elze, final TwoExpressions diffExps) {
    return diffExps != null ? isExpOnlyDiff(then, elze, diffExps.then, diffExps.elze)
        : !Is.assignment(then) //
            || !Is.assignment(elze) //
            || compatible(Extract.assignment(then), Extract.assignment(elze));
  }
  private static boolean isExpOnlyDiff(final Pair diffNodes, final TwoExpressions diffExps) {
    return !hasNull(diffNodes, diffNodes.then, diffNodes.elze) && isExpOnlyDiff(diffNodes.then, diffNodes.elze, diffExps);
  }
  private static boolean isExpStmntOrRet(final ASTNode n) {
    return Is.expressionStatement(n) || Is.return_(n);
  }
  private static boolean isNextAndPrevAsgnPossible(final Assignment then, final Assignment prevAsgn, final Assignment nextAsgn) {
    return !hasNull(prevAsgn, nextAsgn) && compatible(nextAsgn, prevAsgn, then)
        && !Is.conditional(right(prevAsgn), right(nextAsgn), right(then));
  }
  private static boolean isNoNextNoPrevAsgnPossible(final IfStatement s, final Assignment then, final Assignment prevAsgn, final Assignment nextAsgn,
      final VariableDeclarationFragment prevDecl) {
    return prevAsgn == null //
        && nextAsgn == null //
        && !Is.conditional(right(then)) //
        && prevDecl != null //
        && prevDecl.getInitializer() != null //
        && elze(s) == null //
        && !Is.conditional(prevDecl.getInitializer()) //
        && occurences(prevDecl).in(s.getExpression(), right(then)).isEmpty();
  }
  private static boolean isOnlyNextAsgnPossible(final Assignment then, final Assignment nextAsgn) {
    return nextAsgn != null && compatible(nextAsgn, then) && !Is.conditional(right(nextAsgn), right(then))
        && !same(right(then), right(nextAsgn)) && !same(right(nextAsgn), right(then));
  }
  private static boolean isOnlyPrevAsgnPossible(final IfStatement i, final Assignment then, final Assignment prev) {
    return prev != null //
        && Occurrences.BOTH_SEMANTIC.of(asSimpleName(left(prev))).in(i.getExpression()).isEmpty()
        && !Is.conditional(right(prev), right(then)) //
        && !Is.assignment(right(prev)) //
        && compatible(prev, then) //
        && !same(right(prev), right(then));
  }
  private static ReturnStatement nextStatement(final List<Statement> ns, final int n) {
    return n + 1 >= ns.size() ? null : asReturnStatement(ns.get(n + 1));
  }
  private static boolean noElse(final IfStatement s) {
    return Is.empty(elze(s));
  }
  private static boolean possibleToReplace(final Assignment a, final List<VariableDeclarationFragment> frags) {
    final int i = findIndexOfAsgn(left(a), frags);
    if (i < 0)
      return false;
    for (final VariableDeclarationFragment frag : frags)
      if (same(right(a), frag.getName()) && (i < frags.indexOf(frag) || i != frags.indexOf(frag) && frag.getInitializer() == null))
        return false;
    return true;
  }
  private static List<ASTNode> prepareSubTree(final ASTNode n, final Expression e) {
    final List<ASTNode> $ = collectDescendants(n);
    if (Is.expressionStatement(n))
      $.remove(((ExpressionStatement) n).getExpression());
    $.remove(e);
    $.removeAll(collectDescendants(e));
    return $;
  }
  private static ASTNode prev2(final List<Statement> stmts, final int ifIdx) {
    return stmts.get(ifIdx < 2 ? 0 : ifIdx - 2);
  }
  private static void rewriteAssignIfAssignToAssignTernary(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then, final Expression otherAsgnExp) {
    final Expression thenSideExp = Is.plainAssignment(then) ? right(then) : Subject.pair(right(then), otherAsgnExp).to(InfixExpression.Operator.PLUS);
    final Expression newCond = Subject.pair(thenSideExp, otherAsgnExp).toCondition(i.getExpression());
    r.replace(i, t.newExpressionStatement(Subject.pair(left(then), newCond).to(then.getOperator())), null);
  }
  private static boolean rewriteIfToRetStmnt(final ASTRewrite r, final IfStatement s, final ReturnStatement nextReturn) {
    final ReturnStatement $ = asReturnStatement(then(s));
    return $ != null && !Is.conditional($.getExpression(), nextReturn.getExpression()) && rewriteIfToRetStmnt(r, s, $.getExpression(), nextReturn.getExpression());
  }
  private static boolean rewriteIfToRetStmnt(final ASTRewrite r, final IfStatement i, final Expression thenExp, final Expression nextExp) {
    r.replace(i, Subject.operand(determineNewExp(i.getExpression(), thenExp, nextExp)).toReturn(), null);
    r.remove(nextExp.getParent(), null);
    return true;
  }
  private static List<Statement> siblings(final IfStatement i) {
    final ASTNode parent = i.getParent();
    return parent == null ? null : Extract.statements(asBlock(parent));
  }
  private static boolean substitute(final AST t, final ASTRewrite r, final IfStatement i, final TwoExpressions diff, final Statement possiblePrevDecl) {
    final Statement elze = Extract.singleElse(i);
    final Statement then = Extract.singleThen(i);
    final Pair diffNodes = isExpStmntOrRet(then) ? new Pair(then, elze) : findDiffNodes(then, elze);
    final Expression newExp = determineNewExp(i.getExpression(), diff.then, diff.elze);
    if (Is.assignment(diffNodes.then) && Is.assignment(diffNodes.elze)) {
      if (!compatible(Extract.assignment(diffNodes.then), Extract.assignment(diffNodes.elze)))
        return false;
      if (canReplacePrevDecl(possiblePrevDecl, diffNodes))
        return handleSubIfDiffAreAsgns(t, r, i, possiblePrevDecl, diffNodes.then, newExp);
    }
    r.replace(diff.then, newExp, null);
    return true;
  }
  private static boolean treatAssignIfAssign(final AST t, final ASTRewrite r, final IfStatement s, final List<Statement> ss) {
    detectAssignIfAssign(s);
    final int where = ss.indexOf(s);
    final Assignment then = Extract.assignment(then(s));
    if (then == null || elze(s) != null || where < 1)
      return false;
    final Assignment prevAsgn = Extract.assignment(ss.get(where - 1));
    final Assignment nextAsgn = where + 1 >= ss.size() ? null : Extract.assignment(ss.get(1 + where));
    final VariableDeclarationFragment prevDecl = findPrevDecl(ss, where, then, prevAsgn, nextAsgn);
    return tryHandleNextAndPrevAsgnExist(r, s, then, prevAsgn, nextAsgn, prevDecl) //
        || tryHandleOnlyPrevAsgnExist(t, r, s, then, prevAsgn, prevDecl) //
        || tryHandleOnlyNextAsgnExist(t, r, s, then, nextAsgn, prevDecl) //
        || tryHandleNoNextNoPrevAsgn(t, r, s, then, prevAsgn, nextAsgn, prevDecl);
  }
  private static boolean treatIfReturn(final ASTRewrite r, final IfStatement s, final List<Statement> ss) {
    if (!Is.return_(Extract.lastStatement(then(s))))
      return false;
    final ReturnStatement nextRet = nextStatement(ss, ss.indexOf(s));
    return nextRet != null && Is.singletonThen(s) && noElse(s) && rewriteIfToRetStmnt(r, s, nextRet);
  }
  private static boolean treatIfSameExpStmntOrRet(final AST t, final ASTRewrite r, final IfStatement ifStmt, final Statement thenStmnt, final Statement elseStmnt) {
    final List<Pair> diffList = differences(thenStmnt, elseStmnt);
    if (!isDiffListValid(diffList))
      return false;
    final int ifIdx = whichChildAreYou(ifStmt);
    final Statement possiblePrevDecl = Extract.statements(ifStmt.getParent()).get(ifIdx < 1 ? ifIdx : ifIdx - 1);
    boolean wasPrevDeclReplaced = false;
    for (int i = 0; i < diffList.size(); ++i) {
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
  private static boolean tryHandleNextAndPrevAsgnExist(final ASTRewrite r, final IfStatement i, final Assignment then, final Assignment prevAsgn, final Assignment nextAsgn,
      final VariableDeclarationFragment prevDecl) {
    if (!isNextAndPrevAsgnPossible(then, prevAsgn, nextAsgn))
      return false;
    if (prevDecl == null)
      r.replace(prevAsgn.getParent(), nextAsgn.getParent(), null);
    else if (Is.plainAssignment(then)) {
      r.replace(prevDecl.getInitializer(), right(nextAsgn), null);
      r.remove(prevAsgn.getParent(), null);
    }
    r.remove(i, null);
    r.remove(nextAsgn.getParent(), null);
    return true;
  }
  private static boolean tryHandleNoNextNoPrevAsgn(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then, final Assignment prevAsgn,
      final Assignment nextAsgn, final VariableDeclarationFragment prevDecl) {
    if (!isNoNextNoPrevAsgnPossible(i, then, prevAsgn, nextAsgn, prevDecl))
      return false;
    r.replace(prevDecl, makeVariableDeclarationFragment(t, r, prevDecl.getName(), Subject.pair(right(then), prevDecl.getInitializer()).toCondition(i.getExpression())),
        null);
    r.remove(i, null);
    return true;
  }
  private static boolean tryHandleOnlyNextAsgnExist(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then, final Assignment next,
      final VariableDeclarationFragment prevDecl) {
    if (!isOnlyNextAsgnPossible(then, next))
      return false;
    if (prevDecl == null && !Is.assignment(right(next)))
      r.remove(i, null);
    else {
      final Expression[] es = { right(next) };
      if (prevDecl == null || !Is.plainAssignment(then) || !occurences(prevDecl).in(es).isEmpty())
        return handleNoPrevDecl(t, r, i, then, next);
      r.replace(prevDecl, makeVariableDeclarationFragment(t, r, (SimpleName) left(next), right(next)), null);
      r.remove(i, null);
      r.remove(next.getParent(), null);
    }
    return true;
  }
  private static boolean tryHandleOnlyPrevAsgnExist(final AST t, final ASTRewrite r, final IfStatement i, final Assignment then, final Assignment prevAsgn,
      final VariableDeclarationFragment prevDecl) {
    return isOnlyPrevAsgnPossible(i, then, prevAsgn) && (prevDecl == null ? handleNoPrevDecl(t, r, i, then, prevAsgn) : handlePrevDeclExist(t, r, i, then, prevAsgn, prevDecl));
  }
  /**
   * the function receives a condition and the then boolean value and returns
   * the proper condition (its negation if thenValue is false)
   *
   * @param cond the condition to try to negate
   * @param thenValue the then value
   * @return the original condition if thenValue was true or its negation if it
   *         was false (or null if any of the given parameter were null)
   */
  private static Expression tryToNegateCond(final Expression cond, final boolean thenValue) {
    return cond == null ? null : thenValue ? cond : Subject.operand(cond).to(PrefixExpression.Operator.NOT);
  }
  private static int whichChildAreYou(final IfStatement ifStmt) {
    return Extract.statements(ifStmt.getParent()).indexOf(ifStmt);
  }
  static Range detectAssignIfAssign(final IfStatement i) {
    return detectAssignIfAssign(asBlock(i.getParent()), i);
  }
  static Range detectIfReturn(final IfStatement i) {
    return Extract.statements(i.getParent()) == null ? null : detectIfReturn(i, Extract.statements(i.getParent()));
  }
  static Range detectIfSameExpStmntOrRet(final IfStatement s) {
    return hasNull(Extract.singleThen(s), Extract.singleElse(s), asBlock(s.getParent())) || !isDiffListValid(differences(then(s), elze(s))) ? null : new Range(s);
  }
  static boolean perhapsAssignIfAssign(final AST t, final ASTRewrite r, final IfStatement i) {
    return asBlock(i.getParent()) != null && treatAssignIfAssign(t, r, i, siblings(i));
  }
  static boolean perhapsIfReturn(final ASTRewrite r, final IfStatement i) {
    return asBlock(i.getParent()) != null && treatIfReturn(r, i, siblings(i));
  }
  static boolean perhapsIfSameExpStmntOrRet(final AST t, final ASTRewrite r, final IfStatement i) {
    final Statement then = Extract.singleThen(i);
    final Statement elze = Extract.singleElse(i);
    return !hasNull(asBlock(i.getParent()), then, elze) && treatIfSameExpStmntOrRet(t, r, i, then, elze);
  }
  /** Instantiates this class */
  public Ternarize() {
    super("Ternarize", "Convert conditional to an expression using the ternary (?:) operator" + //
        "or to a return condition statement");
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final IfStatement i) {
        return perhaps(detectAssignIfAssign(i)) || perhaps(detectIfReturn(i)) || perhaps(detectIfSameExpStmntOrRet(i)) || true;
      }
      private boolean add(final Range r) {
        $.add(r);
        return true;
      }
      private boolean perhaps(final Range r) {
        return r != null && add(r);
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement i) {
        return // try many alternatives, but finally return true.
        !inRange(m, i) // Stop here
            || perhapsAssignIfAssign(t, r, i) //
            || perhapsIfReturn(r, i) //
            || perhapsIfSameExpStmntOrRet(t, r, i) //
            || true // resisted our alternatives, perhaps children
            ;
      }
    });
  }

  /**
   * contains 2 nodes (used to store the 2 nodes that are different in the then
   * and else tree)
   *
   * @author Tomer Zeltzer
   */
  public static class Pair {
    ASTNode then;
    ASTNode elze;
    /**
     * Instantiates the class with the given nodes
     *
     * @param t then node
     * @param e else node
     */
    public Pair(final ASTNode t, final ASTNode e) {
      then = t;
      elze = e;
    }
  }

  /**
   * contains both sides for the conditional expression
   *
   * @author Tomer Zeltzer
   */
  public static class TwoExpressions {
    final Expression then;
    final Expression elze;
    /**
     * Instantiates the class with the given Expressions
     *
     * @param t then Expression
     * @param e else Expression
     */
    public TwoExpressions(final Expression t, final Expression e) {
      then = t;
      elze = e;
    }
  }
}
