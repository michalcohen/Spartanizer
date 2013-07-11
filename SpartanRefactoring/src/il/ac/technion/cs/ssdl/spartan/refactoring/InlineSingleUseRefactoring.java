package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
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
 * @since 2013/01/01
 */
public class InlineSingleUseRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Inline single use of variable";
  }
  
  @Override protected final void fillRewrite(final ASTRewrite r, @SuppressWarnings("unused") final AST t, final CompilationUnit cu,
      final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment n) {
        if (!inRange(m, n))
          return true;
        final SimpleName varName = n.getName();
        if (n.getParent() instanceof VariableDeclarationStatement) {
          final VariableDeclarationStatement parent = (VariableDeclarationStatement) n.getParent();
          final boolean isFinal = (parent.getModifiers() & Modifier.FINAL) != 0;
          final List<Expression> uses = VariableCounter.USES_SEMANTIC.list(parent.getParent(), varName);
          if (uses.size() == 1 && (isFinal || VariableCounter.ASSIGNMENTS.list(parent.getParent(), varName).size() == 1)) {
            final ASTNode initializerExpr = r.createCopyTarget(n.getInitializer());
            r.replace(uses.get(0), initializerExpr, null);
            if (parent.fragments().size() == 1)
              r.remove(parent, null);
            else
              r.remove(n, null);
          }
        }
        return true;
      }
    });
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
