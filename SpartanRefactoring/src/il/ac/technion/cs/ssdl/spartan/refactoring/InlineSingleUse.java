package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeParenthesizedExpression;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
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
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * 
 * 
 * @since 2013/01/01
 */
public class InlineSingleUse extends Spartanization {
	/** Instantiates this class */
	public InlineSingleUse() {
		super("Inline variable used once", "Inline variable used once");
	}
	@Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final VariableDeclarationFragment n) {
				if (!inRange(m, n) || !(n.getParent() instanceof VariableDeclarationStatement))
					return true;
				final SimpleName varName = n.getName();
				final VariableDeclarationStatement parent = (VariableDeclarationStatement) n.getParent();
				final List<Expression> uses = Occurrences.USES_SEMANTIC.of(varName).in(parent.getParent());
				if (uses.size() == 1
				    && ((parent.getModifiers() & Modifier.FINAL) != 0 || Occurrences.ASSIGNMENTS.of(varName).in(parent.getParent()).size() == 1)) {
					r.replace(uses.get(0), makeParenthesizedExpression(t, r, n.getInitializer()), null);
					r.remove(parent.fragments().size() == 1 ? parent : n, null);
				}
				return true;
			}
		});
	}
	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final VariableDeclarationFragment node) {
				final SimpleName varName = node.getName();
				if (!(node.getParent() instanceof VariableDeclarationStatement))
					return true;
				final VariableDeclarationStatement parent = (VariableDeclarationStatement) node.getParent();
				if (Occurrences.USES_SEMANTIC.of(varName).in(parent.getParent()).size() == 1
				    && ((parent.getModifiers() & Modifier.FINAL) != 0 || Occurrences.ASSIGNMENTS.of(varName).in(parent.getParent()).size() == 1))
					opportunities.add(new Range(node));
				return true;
			}
		};
	}
}
