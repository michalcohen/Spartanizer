package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 *
 * @since 2013/01/01
 */
public class ForwardDeclaration extends Spartanization {
	/** Instantiates this class */
	public ForwardDeclaration() {
		super("Forward declaration", "Forward declaration of a variable just prior to first use");
	}

	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final VariableDeclarationFragment n) {
				if (!inRange(m, n))
					return true;
				final ASTNode containingNode = n.getParent().getParent();
				if (!(containingNode instanceof Block))
					return true;
				final Block block = (Block) containingNode;
				final int firstUseIdx = findFirstUse(block, n.getName());
				if (firstUseIdx < 0)
					return true;
				final int declaredIdx = block.statements().indexOf(
						n.getParent());

				if (nextNodeIsAlreadyFixed(block, n, declaredIdx)) return true;

				final int beginingOfDeclarationsBlockIdx = findBeginingOfDeclarationBlock(block, declaredIdx, firstUseIdx);
				if (beginingOfDeclarationsBlockIdx > declaredIdx) {
					final ASTNode declarationNode = (ASTNode) block.statements().get(declaredIdx);
					if (((VariableDeclarationStatement) declarationNode).fragments().size() == 1) {
						final ListRewrite lstRewrite = r.getListRewrite(block, Block.STATEMENTS_PROPERTY);
						lstRewrite.remove(declarationNode, null);
						lstRewrite.insertAt(ASTNode.copySubtree(t, declarationNode), beginingOfDeclarationsBlockIdx + 1, null);
					} else {
						r.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertAt(
								t.newVariableDeclarationStatement((VariableDeclarationFragment) ASTNode.copySubtree(t, n)),
								beginingOfDeclarationsBlockIdx + 1, null);
						r.remove(n, null);
					}
				}
				return true;
			}

		});
	}

	static boolean nextNodeIsAlreadyFixed(final Block block, final VariableDeclarationFragment n,
			final int declaredIdx) {
		final int firstUseIdx = findFirstUse(block, n.getName());
		if (firstUseIdx < 0)
			return true;

		final int beginingOfDeclarationsIdx = findBeginingOfDeclarationBlock(block, declaredIdx , firstUseIdx);

		final ASTNode nextN = (ASTNode) block.statements().get(declaredIdx + 1);
		final int nextDeclaredIdx = declaredIdx + 1;
		if (nextN.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT){
			final VariableDeclarationStatement nextNVDS =  (VariableDeclarationStatement) nextN;
			for (int i = 0; i < nextNVDS.fragments().size(); i++){
				final VariableDeclarationFragment nextVDF = (VariableDeclarationFragment) nextNVDS.fragments().get(i);
				if (findFirstUse(block, nextVDF.getName()) == nextDeclaredIdx + 1
						&& beginingOfDeclarationsIdx == nextDeclaredIdx) return true;
			}
		}
		return false;
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> oppportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final VariableDeclarationFragment n) {
				final ASTNode $ = n.getParent().getParent();
				return !($ instanceof Block) ? true : moverForward(n, (Block) $);
			}

			private boolean moverForward(final VariableDeclarationFragment n, final Block b) {
				final int firstUseIdx = findFirstUse(b, n.getName());
				if (firstUseIdx < 0)
					return true;
				final int declaredIdx = b.statements().indexOf(n.getParent());

				final int beginingOfDeclarationsIdx = findBeginingOfDeclarationBlock(b, declaredIdx , firstUseIdx);

				if (nextNodeIsAlreadyFixed(b, n, declaredIdx)) return true;

				if (beginingOfDeclarationsIdx > declaredIdx)
					oppportunities.add(new Range(n));
				return true;
			}
		};
	}

	static int findFirstUse(final Block b, final SimpleName name) {
		final ASTNode declarationFragment = name.getParent();
		final ASTNode declarationStmt = declarationFragment.getParent();
		for (int i = b.statements().indexOf(declarationStmt) + 1; i < b.statements().size(); ++i)
			if (Occurrences.BOTH_LEXICAL.of(name).in((ASTNode) b.statements().get(i)).size() > 0)
				return i; // first use!
		return -1; // that means unused
	}

	static int findBeginingOfDeclarationBlock(final Block b, final int declaredIdx, final int firstUseIdx) {
		int $ = firstUseIdx - 1;
		for (int i = firstUseIdx - 1; i > declaredIdx; --i) {
			if (!(b.statements().get(i) instanceof VariableDeclarationStatement))
				break;
			final VariableDeclarationStatement declarations = (VariableDeclarationStatement) b.statements().get(i);
			boolean foundUsedVariable = false;
			for (final Object item : declarations.fragments())
				if (findFirstUse(b, ((VariableDeclarationFragment) item).getName()) == firstUseIdx) {
					$ = i - 1;
					foundUsedVariable = true;
				}
			if (!foundUsedVariable)
				break;
		}
		return $;
	}
}
