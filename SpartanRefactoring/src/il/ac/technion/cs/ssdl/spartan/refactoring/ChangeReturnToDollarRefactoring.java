package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
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

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin (v2)
 * 
 */
public class ChangeReturnToDollarRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Convert Return Variable to $";
  }
  
  @Override protected final void fillRewrite(final ASTRewrite $, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration n) {
        final VariableDeclarationFragment returnVar = getOnlyReturnVariable(n);
        if (returnVar != null) {
          if (!inRange(m, n))
            return true;
          for (final Expression e : VariableCounter.BOTH_LEXICAL.list(n, returnVar.getName()))
            $.replace(e, t.newSimpleName("$"), null);
        }
        return true;
      }
    });
  }
  
  static List<VariableDeclarationFragment> getCandidates(final ASTNode container) {
    final List<VariableDeclarationFragment> $ = new ArrayList<VariableDeclarationFragment>();
    container.accept(new ASTVisitor() {
      /**
       * 
       * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
       *      AnonymousClassDeclaration)
       * 
       * @param _
       *          ignored
       */
      @Override public boolean visit(final AnonymousClassDeclaration _) {
        // we don't want to visit declarations inside anonymous classes
        return false;
      }
      
      @Override public boolean visit(final VariableDeclarationFragment node) {
        $.add(node);
        return true;
      }
    });
    return $;
  }
  
  static List<ReturnStatement> getReturnStatements(final ASTNode container) {
    final List<ReturnStatement> $ = new ArrayList<ReturnStatement>();
    container.accept(new ASTVisitor() {
      /**
       * 
       * 
       * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
       *      AnonymousClassDeclaration)
       * 
       * @param _
       *          ignored
       */
      @Override public boolean visit(final AnonymousClassDeclaration _) {
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
    for (final VariableDeclaration d : $)
      if (d.getName().getIdentifier().equals("$"))
        return null;
    final List<ReturnStatement> returnStatements = getReturnStatements(node);
    int usesOfLastCondidate = 0;
    for (final Iterator<VariableDeclarationFragment> iter = $.iterator(); iter.hasNext();) {
      final VariableDeclarationFragment currDecl = iter.next();
      for (final ReturnStatement returnStmt : returnStatements) {
        if (literals.contains(Integer.valueOf(returnStmt.getExpression().getNodeType())))
          continue;
        final int nUses = VariableCounter.BOTH_LEXICAL.list(returnStmt, currDecl.getName()).size();
        if (nUses == 0) {
          iter.remove();
          break;
        }
        usesOfLastCondidate = nUses;
      }
    }
    return $.size() == 1 && returnStatements.size() > 0 && usesOfLastCondidate > 0 ? $.get(0) : null;
  }
  
  @SuppressWarnings("boxing") private static final Collection<Integer> literals = Collections.unmodifiableCollection(Arrays.asList(
      ASTNode.NULL_LITERAL, ASTNode.CHARACTER_LITERAL, ASTNode.NUMBER_LITERAL, ASTNode.STRING_LITERAL, ASTNode.BOOLEAN_LITERAL));
  
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
