package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin (v2)
 * 
 */
public class ConvertToTernaryRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Convert Conditional Into a Trenary";
  }
  
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        if (!inRange(m, n))
          return true;
        if (treatAssignIfAssign(t, r, n))
          return true;
        if (treatAssignment(t, r, n))
          return true;
        if (treatReturn(t, r, n))
          return true;
        treatIfReturn(t, r, n);
        return true;
      }
    });
  }
  
  /**
   * Extracts an assignment from a node. Expression, and the Expression contains
   * Assignment.
   * 
   * @param n
   *          The node from which to extract assignment.
   * @return null if it is not possible to extract the assignment.
   */
  static Assignment getAssignment(final Statement n) {
    if (n == null)
      return null;
    ExpressionStatement expStmnt = null;
    if (n.getNodeType() == ASTNode.BLOCK) {
      final Block b = (Block) n;
      if (b.statements().size() != 1)
        return null;
      if (((ASTNode) b.statements().get(0)).getNodeType() != ASTNode.EXPRESSION_STATEMENT)
        return null;
      expStmnt = (ExpressionStatement) (ASTNode) b.statements().get(0);
    } else if (n.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
      expStmnt = (ExpressionStatement) n;
    else
      return null;
    if (expStmnt.getExpression().getNodeType() != ASTNode.ASSIGNMENT)
      return null;
    return (Assignment) expStmnt.getExpression();
  }
  
  /**
   * Extracts a return statement from a node. Expression, and the Expression
   * contains Assignment.
   * 
   * @param s
   *          The node from which to return statement assignment.
   * @return null if it is not possible to extract the return statement.
   */
  static ReturnStatement getReturnStatement(final Statement s) {
    if (s == null)
      return null;
    if (s.getNodeType() == ASTNode.RETURN_STATEMENT)
      return (ReturnStatement) s;
    if (s.getNodeType() != ASTNode.BLOCK)
      return null;
    final Block b = (Block) s;
    if (b.statements().size() != 1)
      return null;
    if (((ASTNode) b.statements().get(0)).getNodeType() != ASTNode.RETURN_STATEMENT)
      return null;
    return (ReturnStatement) b.statements().get(0);
  }
  
  /**
   * If possible rewrite the if statement as return of a ternary operation.
   * 
   * @param node
   *          The root if node.
   * @return Returns null if it is not possible to rewrite as return. Otherwise
   *         returns the new node.
   */
  static boolean treatReturn(final AST ast, final ASTRewrite rewrite, final IfStatement node) {
    final ReturnStatement retThen = getReturnStatement(node.getThenStatement());
    final ReturnStatement retElse = getReturnStatement(node.getElseStatement());
    if (retThen == null || retElse == null)
      return false;
    final ConditionalExpression newCondExp = ast.newConditionalExpression();
    newCondExp.setExpression((Expression) rewrite.createMoveTarget(node.getExpression()));
    newCondExp.setThenExpression((Expression) rewrite.createMoveTarget(retThen.getExpression()));
    newCondExp.setElseExpression((Expression) rewrite.createMoveTarget(retElse.getExpression()));
    final ReturnStatement newnode = ast.newReturnStatement();
    newnode.setExpression(newCondExp);
    rewrite.replace(node, newnode, null);
    return true;
  }
  
  /**
   * If possible rewrite the if statement as assignment of a ternary operation.
   * 
   * @param node
   *          The root if node.
   * @return Returns null if it is not possible to rewrite as assignment.
   *         Otherwise returns the new node.
   */
  static boolean treatAssignment(final AST ast, final ASTRewrite rewrite, final IfStatement node) {
    final Assignment asgnThen = getAssignment(node.getThenStatement());
    final Assignment asgnElse = getAssignment(node.getElseStatement());
    // We will rewrite only if the two assignments assign to the same variable
    if (asgnThen != null && asgnElse != null && asgnThen.getLeftHandSide().subtreeMatch(matcher, asgnElse.getLeftHandSide())
        && asgnThen.getOperator().equals(asgnElse.getOperator())) {
      // Now create the new assignment with the conditional inside it
      final ConditionalExpression newCondExp = ast.newConditionalExpression();
      newCondExp.setExpression((Expression) rewrite.createMoveTarget(node.getExpression()));
      newCondExp.setThenExpression((Expression) rewrite.createMoveTarget(asgnThen.getRightHandSide()));
      newCondExp.setElseExpression((Expression) rewrite.createMoveTarget(asgnElse.getRightHandSide()));
      final Assignment newAsgn = ast.newAssignment();
      newAsgn.setOperator(asgnThen.getOperator());
      newAsgn.setRightHandSide(newCondExp);
      newAsgn.setLeftHandSide((Expression) rewrite.createMoveTarget(asgnThen.getLeftHandSide()));
      rewrite.replace(node, ast.newExpressionStatement(newAsgn), null);
      return true;
    }
    return false;
  }
  
  static boolean treatIfReturn(final AST ast, final ASTRewrite rewrite, final IfStatement node) {
    final ASTNode parent = node.getParent();
    if (parent.getNodeType() == ASTNode.BLOCK) {
      @SuppressWarnings("rawtypes")
      final List stmts = ((Block) parent).statements();
      final int ifIdx = stmts.indexOf(node);
      if (stmts.size() > ifIdx + 1) {
        final ReturnStatement nextReturn = getReturnStatement((Statement) stmts.get(ifIdx + 1));
        final ReturnStatement asgnThen = getReturnStatement(node.getThenStatement());
        if (nextReturn != null && asgnThen != null) {
          final ConditionalExpression newCondExp = ast.newConditionalExpression();
          newCondExp.setExpression((Expression) rewrite.createMoveTarget(node.getExpression()));
          newCondExp.setThenExpression((Expression) rewrite.createMoveTarget(asgnThen.getExpression()));
          newCondExp.setElseExpression((Expression) rewrite.createMoveTarget(nextReturn.getExpression()));
          final ReturnStatement newReturn = ast.newReturnStatement();
          newReturn.setExpression(newCondExp);
          rewrite.replace(node, newReturn, null);
          rewrite.remove(nextReturn, null);
          return true;
        }
      }
    }
    return false;
  }
  
  static boolean treatAssignIfAssign(final AST ast, final ASTRewrite rewrite, final IfStatement node) {
    final ASTNode parent = node.getParent();
    if (parent.getNodeType() == ASTNode.BLOCK) {
      @SuppressWarnings("rawtypes")
      final List stmts = ((Block) parent).statements();
      final int ifIdx = stmts.indexOf(node);
      if (ifIdx >= 1) {
        final Assignment asgnThen = getAssignment(node.getThenStatement());
        if (asgnThen == null)
          return false;
        final Assignment asgnElse = getAssignment(node.getElseStatement());
        final Assignment prevAsgn = getAssignment((Statement) stmts.get(ifIdx - 1));
        if (dependsOn(node.getExpression(), asgnThen.getLeftHandSide())
            || dependsOn(asgnThen.getRightHandSide(), asgnThen.getLeftHandSide())
            || !asgnThen.getOperator().equals(Operator.ASSIGN) || asgnElse != null)
          return false;
        if (prevAsgn != null) {
          if (prevAsgn.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
            rewrite.remove(prevAsgn.getParent(), null);
          rewriteAssignIfAssignToAssignTernary(ast, rewrite, node, asgnThen, prevAsgn.getRightHandSide());
          return true;
        }
        final VariableDeclarationStatement prevDecl = getSingleDeclaration((Statement) stmts.get(ifIdx - 1),
            asgnThen.getLeftHandSide());
        if (prevDecl != null) {
          rewriteAssignIfAssignToDeclareTernary(ast, rewrite, node, asgnThen,
              getDeclarationFragment(prevDecl, asgnThen.getLeftHandSide()));
          rewrite.remove(node, null);
          return true;
        }
        final VariableDeclarationStatement prevMultiDecl = getDeclaration((Statement) stmts.get(ifIdx - 1),
            asgnThen.getLeftHandSide());
        if (prevMultiDecl != null) {
          final VariableDeclarationFragment singleDecl = getDeclarationFragment(prevMultiDecl, asgnThen.getLeftHandSide());
          rewriteAssignIfAssignToDeclareTernary(ast, rewrite, node, asgnThen, singleDecl);
          rewrite.remove(node, null);
          return true;
        }
      }
    }
    return false;
  }
  
  private static void rewriteAssignIfAssignToAssignTernary(final AST ast, final ASTRewrite rewrite, final IfStatement node,
      final Assignment asgnThen, final Expression prevAsgn) {
    final ConditionalExpression newCondExp = ast.newConditionalExpression();
    newCondExp.setExpression((Expression) rewrite.createMoveTarget(node.getExpression()));
    newCondExp.setThenExpression((Expression) rewrite.createMoveTarget(asgnThen.getRightHandSide()));
    newCondExp.setElseExpression((Expression) rewrite.createCopyTarget(prevAsgn));
    final Assignment newAsgn = ast.newAssignment();
    newAsgn.setOperator(asgnThen.getOperator());
    newAsgn.setRightHandSide(newCondExp);
    newAsgn.setLeftHandSide((Expression) rewrite.createMoveTarget(asgnThen.getLeftHandSide()));
    rewrite.replace(node, ast.newExpressionStatement(newAsgn), null);
  }
  
  private static void rewriteAssignIfAssignToDeclareTernary(final AST ast, final ASTRewrite rewrite, final IfStatement node,
      final Assignment asgnThen, final VariableDeclarationFragment prevDecl) {
    final ConditionalExpression newCondExp = ast.newConditionalExpression();
    newCondExp.setExpression((Expression) rewrite.createMoveTarget(node.getExpression()));
    newCondExp.setThenExpression((Expression) rewrite.createMoveTarget(asgnThen.getRightHandSide()));
    newCondExp.setElseExpression((Expression) rewrite.createCopyTarget(prevDecl.getInitializer()));
    rewrite.replace(prevDecl.getInitializer(), newCondExp, null);
  }
  
  static SpartanizationRange detectAssignment(final IfStatement node) {
    final Assignment asgnThen = getAssignment(node.getThenStatement());
    final Assignment asgnElse = getAssignment(node.getElseStatement());
    if (asgnThen != null && asgnElse != null && asgnThen.getLeftHandSide().subtreeMatch(matcher, asgnElse.getLeftHandSide())
        && asgnThen.getOperator().equals(asgnElse.getOperator()))
      return new SpartanizationRange(node);
    return null;
  }
  
  static SpartanizationRange detectReturn(final IfStatement node) {
    final ReturnStatement retThen = getReturnStatement(node.getThenStatement());
    final ReturnStatement retElse = getReturnStatement(node.getElseStatement());
    return retThen == null || retElse == null ? null : new SpartanizationRange(node);
  }
  
  static SpartanizationRange detectIfReturn(final IfStatement node) {
    final ASTNode parent = node.getParent();
    if (parent.getNodeType() == ASTNode.BLOCK) {
      @SuppressWarnings("rawtypes")
      final List stmts = ((Block) parent).statements();
      final int ifIdx = stmts.indexOf(node);
      if (stmts.size() > ifIdx + 1) {
        final ReturnStatement nextReturn = getReturnStatement((Statement) stmts.get(ifIdx + 1));
        final ReturnStatement thenSide = getReturnStatement(node.getThenStatement());
        if (nextReturn != null && thenSide != null)
          return new SpartanizationRange(node, nextReturn);
      }
    }
    return null;
  }
  
  static SpartanizationRange detectAssignIfAssign(final IfStatement node) {
    final ASTNode parent = node.getParent();
    if (parent.getNodeType() == ASTNode.BLOCK) {
      @SuppressWarnings("rawtypes")
      final List stmts = ((Block) parent).statements();
      final int ifIdx = stmts.indexOf(node);
      if (ifIdx >= 1) {
        final Assignment asgnThen = getAssignment(node.getThenStatement());
        final Assignment asgnElse = getAssignment(node.getElseStatement());
        if (asgnThen == null || asgnElse != null)
          return null;
        final ASTNode possibleAssignment = getAssignmentOrDeclaration((Statement) stmts.get(ifIdx - 1), asgnThen.getLeftHandSide());
        if (possibleAssignment != null && !dependsOn(node.getExpression(), asgnThen.getLeftHandSide())
            && !dependsOn(asgnThen.getRightHandSide(), asgnThen.getLeftHandSide())
            && asgnThen.getOperator().equals(Operator.ASSIGN))
          return new SpartanizationRange(possibleAssignment, node);
      }
    }
    return null;
  }
  
  private static boolean dependsOn(final Expression e, final Expression leftHandSide) {
    return VariableCounter.BOTH_SEMANTIC.list(e, leftHandSide).size() > 0;
  }
  
  private static ASTNode getAssignmentOrDeclaration(final Statement s, final Expression e) {
    ASTNode $ = null;
    if (($ = getAssignment(s)) != null)
      return $;
    if (($ = getSingleDeclaration(s, e)) != null)
      return $;
    if (($ = getDeclaration(s, e)) != null)
      return $;
    return $;
  }
  
  private static VariableDeclarationStatement getDeclaration(final Statement s, final Expression e) {
    if (s == null)
      return null;
    if (s.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
      final VariableDeclarationStatement d = (VariableDeclarationStatement) s;
      return getDeclarationFragment(d, e) != null ? (VariableDeclarationStatement) s : null;
    }
    return null;
  }
  
  private static VariableDeclarationStatement getSingleDeclaration(final Statement s, final Expression expression) {
    if (s == null)
      return null;
    if (s.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
      final VariableDeclarationStatement d = (VariableDeclarationStatement) s;
      if (d.fragments().size() == 1 && getDeclarationFragment(d, expression) != null)
        return (VariableDeclarationStatement) s;
    }
    return null;
  }
  
  private static VariableDeclarationFragment getDeclarationFragment(final VariableDeclarationStatement d, final Expression e) {
    if (e.getNodeType() == ASTNode.SIMPLE_NAME) {
      final SimpleName name = (SimpleName) e;
      for (final Object obj : d.fragments())
        if (name.subtreeMatch(matcher, ((VariableDeclarationFragment) obj).getName()))
          return (VariableDeclarationFragment) obj;
    }
    return null;
  }
  
  @Override public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    final Collection<SpartanizationRange> $ = new ArrayList<SpartanizationRange>();
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement node) {
        SpartanizationRange rng;
        if ((rng = detectAssignIfAssign(node)) != null) {
          $.add(rng);
          return true;
        }
        if ((rng = detectAssignment(node)) != null) {
          $.add(rng);
          return true;
        }
        if ((rng = detectReturn(node)) != null) {
          $.add(rng);
          return true;
        }
        if ((rng = detectIfReturn(node)) != null) {
          $.add(rng);
          return true;
        }
        return true;
      }
    });
    return $;
  }
  
  private static final ASTMatcher matcher = new ASTMatcher();
}
