package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ForwardDeclarationRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Forward Declaraion of Variable";
  }
  
  @Override protected ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m) {
    if (pm != null)
      pm.beginTask("Creating rewrite operation...", 1);
    final AST ast = cu.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment node) {
        final SimpleName varName = node.getName();
        final ASTNode containingNode = node.getParent().getParent();
        if (!(containingNode instanceof Block))
          return true;
        final Block block = (Block) containingNode;
        final int declaredIdx = block.statements().indexOf(node.getParent());
        final int firstUseIdx = findFirstUse(block, varName);
        if (firstUseIdx < 0)
          return true;
        final int beginingOfDeclarationsBlockIdx = findBeginingOfDeclarationBlock(block, declaredIdx, firstUseIdx);
        if (beginingOfDeclarationsBlockIdx > declaredIdx) {
          final ASTNode declarationNode = (ASTNode) (block.statements().get(declaredIdx));
          if (((VariableDeclarationStatement) declarationNode).fragments().size() == 1) {
            final ListRewrite lstRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
            lstRewrite.remove(declarationNode, null);
            lstRewrite.insertAt(ASTNode.copySubtree(ast, declarationNode), beginingOfDeclarationsBlockIdx + 1, null);
          } else {
            rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertAt(
                ast.newVariableDeclarationStatement((VariableDeclarationFragment) (ASTNode.copySubtree(ast, node))),
                beginingOfDeclarationsBlockIdx + 1, null);
            rewrite.remove(node, null);
          }
        }
        return true;
      }
    });
    if (pm != null)
      pm.done();
    return rewrite;
  }
  
  @Override public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    final Collection<SpartanizationRange> $ = new ArrayList<SpartanizationRange>();
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment node) {
        final SimpleName varName = node.getName();
        final ASTNode containingNode = node.getParent().getParent();
        if (!(containingNode instanceof Block))
          return true;
        final Block block = (Block) containingNode;
        final int declaredIdx = block.statements().indexOf(node.getParent());
        final int firstUseIdx = findFirstUse(block, varName);
        if (firstUseIdx < 0)
          return true;
        final int beginingOfDeclarationsBlockIdx = findBeginingOfDeclarationBlock(block, declaredIdx, firstUseIdx);
        if (beginingOfDeclarationsBlockIdx > declaredIdx)
          $.add(new SpartanizationRange(node));
        return true;
      }
    });
    return $;
  }
  
  static int findFirstUse(final Block b, final SimpleName var) {
    final ASTNode declarationFragment = var.getParent();
    final ASTNode declarationStmt = declarationFragment.getParent();
    for (int i = b.statements().indexOf(declarationStmt) + 1; i < b.statements().size(); ++i) {
      final List<Expression> usesInCurrItem = VariableCounter.BOTH_LEXICAL.list((ASTNode) (b.statements().get(i)), var);
      final int usesInBlockItem = usesInCurrItem.size();
      if (usesInBlockItem > 0)
        return i; // first use!
    }
    return -1; // that means unused
  }
  
  static int findBeginingOfDeclarationBlock(final Block b, final int declaredIdx, final int firstUseIdx) {
    int beginingOfDeclarationsBlockIdx = firstUseIdx - 1;
    for (int i = firstUseIdx - 1; i > declaredIdx; --i) {
      if (!(b.statements().get(i) instanceof VariableDeclarationStatement))
        break;
      final VariableDeclarationStatement declarations = (VariableDeclarationStatement) (b.statements().get(i));
      boolean foundUsedVariable = false;
      for (Object item : declarations.fragments()) {
        final int firstUseOfCurr = findFirstUse(b, ((VariableDeclarationFragment) item).getName());
        if (firstUseOfCurr == firstUseIdx) {
          beginingOfDeclarationsBlockIdx = i - 1;
          foundUsedVariable = true;
        }
      }
      if (!foundUsedVariable)
        break;
    }
    return beginingOfDeclarationsBlockIdx;
  }
}
