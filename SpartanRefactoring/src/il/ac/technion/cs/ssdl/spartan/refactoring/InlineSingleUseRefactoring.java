package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin (v2)
 *
 */
public class InlineSingleUseRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Inline Single Use of Variable";
  }
  
  @Override protected ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m) {
    if (pm != null)
      pm.beginTask("Creating rewrite operation...", 1);
    final ASTRewrite $ = ASTRewrite.create(cu.getAST());
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment node) {
        if (m == null && isNodeOutsideSelection(node))
          return true;
        if (m != null && isNodeOutsideMarker(node, m))
          return true;
        final SimpleName varName = node.getName();
        if (node.getParent() instanceof VariableDeclarationStatement) {
          final VariableDeclarationStatement parent = (VariableDeclarationStatement) node.getParent();
          final boolean isFinal = (parent.getModifiers() & Modifier.FINAL) != 0;
          final List<Expression> uses = VariableCounter.USES_SEMANTIC.list(parent.getParent(), varName);
          if (uses.size() == 1 && (isFinal || VariableCounter.ASSIGNMENTS.list(parent.getParent(), varName).size() == 1)) {
            final ASTNode initializerExpr = $.createCopyTarget(node.getInitializer());
            $.replace(uses.get(0), initializerExpr, null);
            if (parent.fragments().size() == 1)
              $.remove(parent, null);
            else
              $.remove(node, null);
          }
        }
        return true;
      }
    });
    if (pm != null)
      pm.done();
    return $;
  }
  
  @Override public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    final Collection<SpartanizationRange> $ = new ArrayList<SpartanizationRange>();
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment node) {
        final SimpleName varName = node.getName();
        if (node.getParent() instanceof VariableDeclarationStatement) {
          final VariableDeclarationStatement parent = (VariableDeclarationStatement) node.getParent();
          final boolean isFinal = (parent.getModifiers() & Modifier.FINAL) != 0;
          if (VariableCounter.USES_SEMANTIC.list(parent.getParent(), varName).size() == 1
              && (isFinal || VariableCounter.ASSIGNMENTS.list(parent.getParent(), varName).size() == 1))
            $.add(new SpartanizationRange(node));
        }
        return true;
      }
    });
    return $;
  }
}
