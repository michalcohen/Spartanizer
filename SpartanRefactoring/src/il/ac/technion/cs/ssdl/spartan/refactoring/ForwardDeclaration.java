package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (v3 /
 *         04.06.2014)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3 /
 *         04.06.2014)
 * @since 2013/01/01 TODO: There <b>must</b> be an option to disable this
 *        warning in selected places. Consider this example:
 * 
 *        <pre>
 * public static &lt;T&gt; void swap(final T[] ts, final int i, final int j) {
 * 	final T t = ts[i];
 * 	ts[i] = ts[j];
 * 	ts[j] = t;
 * }
 * </pre>
 * 
 *        Require comment
 * 
 *        <pre>
 * public static &lt;T&gt; void swap(final T[] ts, final int i, final int j) {
 * 	final T t = ts[i]; // Don't move!
 * 	ts[i] = ts[j];
 * 	ts[j] = t;
 * }
 * </pre>
 */
public class ForwardDeclaration extends Spartanization {
	/** Instantiates this class */
	public ForwardDeclaration() {
		super("Forward declaration", "Forward declaration of a variable just prior to first use");
	}
	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final VariableDeclarationFragment v) {
				if (!inRange(m, v))
					return true;
				final ASTNode containingNode = v.getParent().getParent();
				if (!(containingNode instanceof Block))
					return true;
				final Block b = (Block) containingNode;
				final int firstUseIdx = findFirstUse(b, v.getName());
				if (0 > firstUseIdx)
					return true;
				final int declaredIdx = b.statements().indexOf(v.getParent());
				if (nextNodeIsAlreadyFixed(b, v, declaredIdx))
					return true;
				final int beginingOfDeclarationsBlockIdx = findBeginingOfDeclarationBlock(b, declaredIdx, firstUseIdx);
				if (declaredIdx >= beginingOfDeclarationsBlockIdx)
					return true;
				final ASTNode declarationNode = (ASTNode) b.statements().get(declaredIdx);
				if (1 == ((VariableDeclarationStatement) declarationNode).fragments().size())
					rewrite(beginingOfDeclarationsBlockIdx, declarationNode, r.getListRewrite(b, Block.STATEMENTS_PROPERTY));
				else {
					r.getListRewrite(b, Block.STATEMENTS_PROPERTY).insertAt(
					    t.newVariableDeclarationStatement((VariableDeclarationFragment) ASTNode.copySubtree(t, v)),
					    1 + beginingOfDeclarationsBlockIdx, null);
					r.remove(v, null);
				}
				return true;
			}
			private void rewrite(final int beginingOfDeclarationsBlockIdx, final ASTNode n, final ListRewrite lr) {
				lr.remove(n, null);
				lr.insertAt(ASTNode.copySubtree(t, n), 1 + beginingOfDeclarationsBlockIdx, null);
			}
		});
	}
	static boolean nextNodeIsAlreadyFixed(final Block block, final VariableDeclarationFragment n, final int declaredIdx) {
		final int firstUseIdx = findFirstUse(block, n.getName());
		if (0 > firstUseIdx)
			return true;
		final int beginingOfDeclarationsIdx = findBeginingOfDeclarationBlock(block, declaredIdx, firstUseIdx);
		final ASTNode nextN = (ASTNode) block.statements().get(declaredIdx + 1);
		final int nextDeclaredIdx = 1 + declaredIdx;
		if (nextN.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			final VariableDeclarationStatement nextNVDS = (VariableDeclarationStatement) nextN;
			// TODO: Convert to for (x : nextNVDS.fragments()
			for (int i = 0; i < nextNVDS.fragments().size(); i++)
				if (1 + nextDeclaredIdx == findFirstUse(block, ((VariableDeclarationFragment) nextNVDS.fragments().get(i)).getName())
				    && nextDeclaredIdx == beginingOfDeclarationsIdx)
					return true;
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
				if (nextNodeIsAlreadyFixed(b, n, declaredIdx))
					return true;
				if (declaredIdx < findBeginingOfDeclarationBlock(b, declaredIdx, firstUseIdx))
					oppportunities.add(new Range(n));
				return true;
			}
		};
	}
	static int findFirstUse(final Block b, final SimpleName name) {
		final ASTNode declarationStmt = name.getParent().getParent();
		for (int $ = 1 + b.statements().indexOf(declarationStmt); $ < b.statements().size(); ++$)
			if (0 < Occurrences.BOTH_LEXICAL.of(name).in((ASTNode) b.statements().get($)).size())
				return $; // first use!
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
				if (firstUseIdx == findFirstUse(b, ((VariableDeclarationFragment) item).getName())) {
					$ = i - 1;
					foundUsedVariable = true;
				}
			if (!foundUsedVariable)
				break;
		}
		return $;
	}
}
