package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ChangeReturnToDollarRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Convert Return Variable to $";
  }
  
  @Override protected ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m) {
    if (pm != null)
      pm.beginTask("Creating rewrite operation...", 1);
    final AST ast = cu.getAST();
    final ASTRewrite $ = ASTRewrite.create(ast);
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration node) {
        final VariableDeclarationFragment returnVar = getOnlyReturnVariable(node);
        if (returnVar != null) {
          if ((m == null) && isNodeOutsideSelection(returnVar))
            return true;
          if (m != null && isNodeOutsideMarker(returnVar, m))
            return true;
          for (final Expression exp : VariableCounter.BOTH_LEXICAL.list(node, returnVar.getName()))
            $.replace(exp, ast.newSimpleName("$"), null);
        }
        return true;
      }
    });
    if (pm != null)
      pm.done();
    return $;
  }
  
  static List<VariableDeclarationFragment> getCandidates(final ASTNode container) {
    final List<VariableDeclarationFragment> $ = new ArrayList<VariableDeclarationFragment>();
    container.accept(new ASTVisitor() {
      @Override public boolean visit(final AnonymousClassDeclaration node) {
        // we don't want to visit declarations inside anonymous classes
        return false;
      }
      
      @Override public boolean visit(VariableDeclarationFragment node) {
        $.add(node);
        return true;
      }
    });
    return $;
  }
  
  static List<ReturnStatement> getReturnStatements(final ASTNode container) {
    final List<ReturnStatement> $ = new ArrayList<ReturnStatement>();
    container.accept(new ASTVisitor() {
      @Override public boolean visit(final AnonymousClassDeclaration node) {
        // we don't want to visit declarations inside anonymous classes
        return false;
      }
      
      @Override public boolean visit(final ReturnStatement node) {
        $.add(node);
        return true;
      }
    });
    return $;
  }
  
  static VariableDeclarationFragment getOnlyReturnVariable(final MethodDeclaration node) {
    final List<VariableDeclarationFragment> $ = getCandidates(node);
    // check if we already have $
    for (final VariableDeclaration decl : $)
      if (decl.getName().getIdentifier().equals("$"))
        return null;
    final List<ReturnStatement> returnStatements = getReturnStatements(node);
    final Iterator<VariableDeclarationFragment> iter = $.iterator();
    int usesOfLastCondidate = 0;
    while (iter.hasNext()) {
      final VariableDeclarationFragment currDecl = iter.next();
      for (final ReturnStatement returnStmt : returnStatements) {
        if (literals.contains(Integer.valueOf(returnStmt.getExpression().getNodeType())))
          continue;
        final List<Expression> uses = VariableCounter.BOTH_LEXICAL.list(returnStmt, currDecl.getName());
        if (uses.size() == 0) {
          iter.remove();
          break;
        }
        usesOfLastCondidate = uses.size();
      }
    }
    return $.size() == 1 && returnStatements.size() > 0 && usesOfLastCondidate > 0 ? $.get(0) : null;
  }
  
@SuppressWarnings("boxing")
private static final Collection<Integer> literals = Collections.unmodifiableCollection(Arrays.asList(ASTNode.NULL_LITERAL,
      ASTNode.CHARACTER_LITERAL, ASTNode.NUMBER_LITERAL, ASTNode.STRING_LITERAL, ASTNode.BOOLEAN_LITERAL));
  
  @Override public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    final Collection<SpartanizationRange> $ = new ArrayList<SpartanizationRange>();
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration node) {
        final VariableDeclarationFragment returnVar = getOnlyReturnVariable(node);
        if (returnVar != null)
          $.add(new SpartanizationRange(returnVar));
        return true;
      }
    });
    return $;
  }
}
