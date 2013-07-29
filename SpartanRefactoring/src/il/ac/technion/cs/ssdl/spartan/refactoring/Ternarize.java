package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

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
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * 
 * @since 2013/01/01
 */
public class Ternarize extends BaseSpartanization {
  /** Instantiates this class */
  public Ternarize() {
    super("Ternarize", "Convert conditional to an expression using the ternary (?:) operator");
  }
  
  static boolean hasNull(final Object... os) {
    for (final Object o : os)
      if (o == null)
        return true;
    return false;
  }
  
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        return !inRange(m, n) || //
            treatAssignIfAssign(t, r, n) || //
            treatAssignment(t, r, n) || //
            treatReturn(n) || //
            treatIfReturn(t, r, n) || //
            true;
      }
      
      /**
       * If possible rewrite the if statement as return of a ternary operation.
       * 
       * @param n
       *          The root if node.
       * @return Returns null if it is not possible to rewrite as return.
       *         Otherwise returns the new node.
       */
      private boolean treatReturn(final IfStatement n) {
        final ReturnStatement thenReturn = getReturnStatement(n.getThenStatement());
        final ReturnStatement elseReturn = getReturnStatement(n.getElseStatement());
        if (hasNull(thenReturn, elseReturn))
          return false;
        r.replace(n, makeReturnStatement(t, makeConditionalExpression(n, thenReturn, elseReturn)), null);
        return true;
      }
      
      private ConditionalExpression makeConditionalExpression(final IfStatement n, final ReturnStatement thenReturn,
          final ReturnStatement elseReturn) {
        final ConditionalExpression $ = t.newConditionalExpression();
        $.setExpression((Expression) r.createMoveTarget(n.getExpression()));
        $.setThenExpression((Expression) r.createMoveTarget(thenReturn.getExpression()));
        $.setElseExpression((Expression) r.createMoveTarget(elseReturn.getExpression()));
        return $;
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
    final List<ASTNode> ss = statements(n);
    if (ss != null) {
      if (ss.size() != 1)
        return null;
      final ASTNode s = ss.get(0);
      if (s.getNodeType() != ASTNode.EXPRESSION_STATEMENT)
        return null;
      expStmnt = (ExpressionStatement) s;
    } else if (n.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
      expStmnt = (ExpressionStatement) n;
    else
      return null;
    if (expStmnt.getExpression().getNodeType() != ASTNode.ASSIGNMENT)
      return null;
    return (Assignment) expStmnt.getExpression();
  }
  
  static List<ASTNode> statements(final ASTNode n) {
    return n.getNodeType() != ASTNode.BLOCK ? null : statements((Block) n);
  }
  
  static List<ASTNode> statements(final Block b) {
    return b.statements();
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
    return getReturnStatement((Block) s);
  }
  
  private static ReturnStatement getReturnStatement(final Block b) {
    return b.statements().size() != 1 ? null : getReturnStatement((ASTNode) b.statements().get(0));
  }
  
  private static ReturnStatement getReturnStatement(final ASTNode s) {
    return s.getNodeType() != ASTNode.RETURN_STATEMENT ? null : (ReturnStatement) s;
  }
  
  static ReturnStatement makeReturnStatement(final AST t, final ConditionalExpression e) {
    final ReturnStatement $ = t.newReturnStatement();
    $.setExpression(e);
    return $;
  }
  
  /**
   * If possible rewrite the if statement as assignment of a ternary operation.
   * 
   * @param n
   *          The root if node.
   * @return Returns null if it is not possible to rewrite as assignment.
   *         Otherwise returns the new node.
   */
  static boolean treatAssignment(final AST t, final ASTRewrite r, final IfStatement n) {
    final Assignment asgnThen = getAssignment(n.getThenStatement());
    final Assignment asgnElse = getAssignment(n.getElseStatement());
    if (hasNull(asgnThen, asgnElse))
      return false;
    // We will rewrite only if the two assignments assign to the same variable
    if (asgnThen.getLeftHandSide().subtreeMatch(matcher, asgnElse.getLeftHandSide())
        && asgnThen.getOperator().equals(asgnElse.getOperator())) {
      // Now create the new assignment with the conditional inside it
      final ConditionalExpression newCondExp = t.newConditionalExpression();
      newCondExp.setExpression((Expression) r.createMoveTarget(n.getExpression()));
      newCondExp.setThenExpression((Expression) r.createMoveTarget(asgnThen.getRightHandSide()));
      newCondExp.setElseExpression((Expression) r.createMoveTarget(asgnElse.getRightHandSide()));
      final Assignment newAsgn = t.newAssignment();
      newAsgn.setOperator(asgnThen.getOperator());
      newAsgn.setRightHandSide(newCondExp);
      newAsgn.setLeftHandSide((Expression) r.createMoveTarget(asgnThen.getLeftHandSide()));
      r.replace(n, t.newExpressionStatement(newAsgn), null);
      return true;
    }
    return false;
  }
  
  static boolean treatIfReturn(final AST ast, final ASTRewrite r, final IfStatement node) {
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
          newCondExp.setExpression((Expression) r.createMoveTarget(node.getExpression()));
          newCondExp.setThenExpression((Expression) r.createMoveTarget(asgnThen.getExpression()));
          newCondExp.setElseExpression((Expression) r.createMoveTarget(nextReturn.getExpression()));
          final ReturnStatement newReturn = makeReturnStatement(ast, newCondExp);
          r.replace(node, newReturn, null);
          r.remove(nextReturn, null);
          return true;
        }
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
            r.remove(prevAsgn.getParent(), null);
          rewriteAssignIfAssignToAssignTernary(ast, r, node, asgnThen, prevAsgn.getRightHandSide());
          return true;
        }
        final VariableDeclarationStatement prevDecl = getSingleDeclaration((Statement) stmts.get(ifIdx - 1),
            asgnThen.getLeftHandSide());
        if (prevDecl != null) {
          rewriteAssignIfAssignToDeclareTernary(ast, r, node, asgnThen,
              getDeclarationFragment(prevDecl, asgnThen.getLeftHandSide()));
          r.remove(node, null);
          return true;
        }
        final VariableDeclarationStatement prevMultiDecl = getDeclaration((Statement) stmts.get(ifIdx - 1),
            asgnThen.getLeftHandSide());
        if (prevMultiDecl != null) {
          final VariableDeclarationFragment singleDecl = getDeclarationFragment(prevMultiDecl, asgnThen.getLeftHandSide());
          rewriteAssignIfAssignToDeclareTernary(ast, r, node, asgnThen, singleDecl);
          r.remove(node, null);
          return true;
        }
      }
    }
    return false;
  }
  
  private static void rewriteAssignIfAssignToAssignTernary(final AST t, final ASTRewrite r, final IfStatement n,
      final Assignment asgnThen, final Expression prevAsgn) {
    final ConditionalExpression newCondExp = t.newConditionalExpression();
    newCondExp.setExpression((Expression) r.createMoveTarget(n.getExpression()));
    newCondExp.setThenExpression((Expression) r.createMoveTarget(asgnThen.getRightHandSide()));
    newCondExp.setElseExpression((Expression) r.createCopyTarget(prevAsgn));
    final Assignment newAsgn = t.newAssignment();
    newAsgn.setOperator(asgnThen.getOperator());
    newAsgn.setRightHandSide(newCondExp);
    newAsgn.setLeftHandSide((Expression) r.createMoveTarget(asgnThen.getLeftHandSide()));
    r.replace(n, t.newExpressionStatement(newAsgn), null);
  }
  
  private static void rewriteAssignIfAssignToDeclareTernary(final AST ast, final ASTRewrite rewrite, final IfStatement node,
      final Assignment asgnThen, final VariableDeclarationFragment prevDecl) {
    final ConditionalExpression newCondExp = ast.newConditionalExpression();
    newCondExp.setExpression((Expression) rewrite.createMoveTarget(node.getExpression()));
    newCondExp.setThenExpression((Expression) rewrite.createMoveTarget(asgnThen.getRightHandSide()));
    newCondExp.setElseExpression((Expression) rewrite.createCopyTarget(prevDecl.getInitializer()));
    rewrite.replace(prevDecl.getInitializer(), newCondExp, null);
  }
  
  static Range detectAssignment(final IfStatement node) {
    final Assignment asgnThen = getAssignment(node.getThenStatement());
    final Assignment asgnElse = getAssignment(node.getElseStatement());
    if (hasNull(asgnThen, asgnElse))
      return null;
    if (asgnThen.getLeftHandSide().subtreeMatch(matcher, asgnElse.getLeftHandSide())
        && asgnThen.getOperator().equals(asgnElse.getOperator()))
      return new Range(node);
    return null;
  }
  
  static Range detectReturn(final IfStatement node) {
    final ReturnStatement retThen = getReturnStatement(node.getThenStatement());
    final ReturnStatement retElse = getReturnStatement(node.getElseStatement());
    return hasNull(retThen, retElse) ? null : new Range(node);
  }
  
  static Range detectIfReturn(final IfStatement n) {
    final ASTNode parent = n.getParent();
    final List<ASTNode> ss = statements(parent);
    if (ss == null)
      return null;
    final int ifIdx = ss.indexOf(n);
    if (ss.size() > ifIdx + 1) {
      final ReturnStatement nextReturn = getReturnStatement((Statement) ss.get(ifIdx + 1));
      final ReturnStatement thenSide = getReturnStatement(n.getThenStatement());
      if (!hasNull(nextReturn, thenSide))
        return new Range(n, nextReturn);
    }
    return null;
  }
  
  static Range detectAssignIfAssign(final IfStatement node) {
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
          return new Range(possibleAssignment, node);
      }
    }
    return null;
  }
  
  private static boolean dependsOn(final Expression e, final Expression leftHandSide) {
    return Occurrences.BOTH_SEMANTIC.of(leftHandSide).in(e).size() > 0;
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
    return e.getNodeType() != ASTNode.SIMPLE_NAME ? null : VariableDeclarationFragment(d, (SimpleName) e);
  }
  
  private static VariableDeclarationFragment VariableDeclarationFragment(final VariableDeclarationStatement d, final SimpleName name) {
    for (final Object o : d.fragments())
      if (name.subtreeMatch(matcher, ((VariableDeclarationFragment) o).getName()))
        return (VariableDeclarationFragment) o;
    return null;
  }
  
  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final IfStatement n) {
        return perhaps(detectAssignIfAssign(n)) || //
            perhaps(detectAssignment(n)) || //
            perhaps(detectReturn(n)) || //
            perhaps(detectIfReturn(n)) || //
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
