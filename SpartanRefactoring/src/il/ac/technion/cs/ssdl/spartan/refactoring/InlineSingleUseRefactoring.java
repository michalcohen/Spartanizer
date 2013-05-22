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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class InlineSingleUseRefactoring extends BaseRefactoring {

	@Override
	public String getName() {
		return "Inline Single Use of Variable";
	}

	@Override
	protected ASTRewrite innerCreateRewrite(final CompilationUnit cu,
			final SubProgressMonitor pm, final IMarker m) {
		pm.beginTask("Creating rewrite operation...", 1);
		
		final AST ast = cu.getAST();
		final ASTRewrite rewrite = ASTRewrite.create(ast);
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationFragment node) {
				if ((m==null) && isNodeOutsideSelection(node))
					return true;
				if (m!=null && isNodeOutsideMarker(node, m))
					return true;
				
				final SimpleName varName = node.getName();
				if (node.getParent() instanceof VariableDeclarationStatement) {
					final VariableDeclarationStatement parent = (VariableDeclarationStatement)(node.getParent());
					boolean isFinal = (parent.getModifiers() & Modifier.FINAL) != 0;
					final List<Expression> uses = VariableCounter.USES.list(parent.getParent(), varName);
					if (uses.size()==1 &&
							(isFinal || VariableCounter.ASSIGNMENTS.list(parent.getParent(), varName).size()==1)) {
						final ASTNode initializerExpr = rewrite.createCopyTarget(node.getInitializer());
						rewrite.replace(uses.get(0), initializerExpr, null);
						if (parent.fragments().size()==1)
							rewrite.remove(parent, null);
						else
							rewrite.remove(node, null);
					}
				}
				return true;
			}
		});
		pm.done();
		return rewrite;
	}

	@Override
	public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
		final Collection<SpartanizationRange> $ = new ArrayList<>();
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationFragment node) {
				final SimpleName varName = node.getName();
				if (node.getParent() instanceof VariableDeclarationStatement) {
					final VariableDeclarationStatement parent = (VariableDeclarationStatement)(node.getParent());
					boolean isFinal = (parent.getModifiers() & Modifier.FINAL) != 0;
					if (VariableCounter.USES.list(parent.getParent(), varName).size()==1 &&
							(isFinal || VariableCounter.ASSIGNMENTS.list(parent.getParent(), varName).size()==1))
						$.add(new SpartanizationRange(node));
				}
				return true;
			}
		});
		return $;
	}
}
