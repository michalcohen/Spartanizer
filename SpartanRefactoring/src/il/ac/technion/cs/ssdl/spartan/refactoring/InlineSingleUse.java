package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeParenthesizedExpression;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

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
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3)
 * TODO: This
 *         examples triggers with no justified reason:
 * <pre>
 * final int p = s.indexOf(c);
 * return s.substring(0, p &gt;= 0 ? p : s.length());
 * </pre>
 * 
 * <Answer>
 * Added your examples as Unchanged tests and all tests pass, cant seem to find a bug here...
 * 
 * @since 2013/01/01
 */
public class InlineSingleUse extends Spartanization {
	/** Instantiates this class */
	public InlineSingleUse() {
		super("Inline variable used once", "Inline variable used once");
	}

	@Override
	protected final void fillRewrite(final ASTRewrite r, final AST t,
			final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(final VariableDeclarationFragment n) {
				if (!inRange(m, n)
						|| !(n.getParent() instanceof VariableDeclarationStatement))
					return true;
				final SimpleName varName = n.getName();
				final VariableDeclarationStatement parent = (VariableDeclarationStatement) n
						.getParent();
				final List<Expression> uses = Occurrences.USES_SEMANTIC.of(
						varName).in(parent.getParent());
				if (1 == uses.size()
						&& (0 != (parent.getModifiers() & Modifier.FINAL) || 1 == numOfOccur(
								Occurrences.ASSIGNMENTS, varName,
								parent.getParent()))) {
					r.replace(
							uses.get(0),
							makeParenthesizedExpression(t, r,
									n.getInitializer()), null);
					r.remove(1 != parent.fragments().size() ? n : parent, null);
				}
				return true;
			}
		});
	}

	@Override
	protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override
			public boolean visit(final VariableDeclarationFragment node) {
				if (!(node.getParent() instanceof VariableDeclarationStatement))
					return true;
				final SimpleName varName = node.getName();
				final VariableDeclarationStatement parent = (VariableDeclarationStatement) node
						.getParent();
				if (1 == numOfOccur(Occurrences.USES_SEMANTIC, varName,
						parent.getParent())
						&& (0 != (parent.getModifiers() & Modifier.FINAL) || 1 == numOfOccur(
								Occurrences.ASSIGNMENTS, varName,
								parent.getParent())))
					opportunities.add(new Range(node));
				return true;
			}
		};
	}

	static int numOfOccur(final Occurrences typeOfOccur, final Expression of,
			final ASTNode in) {
		return typeOfOccur == null || of == null || in == null ? -1
				: typeOfOccur.of(of).in(in).size();
	}
}
