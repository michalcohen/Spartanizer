package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.getContainerByNodeType;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.isLiteral;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * 
 * @since 2013/01/01
 */
public class RenameReturnVariableToDollar extends Spartanization {
	/** Instantiates this class */
	public RenameReturnVariableToDollar() {
		super("Rename returned variable to '$'", "Rename the variable returned by a function to '$'");
	}

	@Override protected final void fillRewrite(final ASTRewrite $, final AST t, final CompilationUnit cu, final IMarker m) {
		cu.accept(new ASTVisitor() {
			@Override public boolean visit(final MethodDeclaration n) {
				final VariableDeclarationFragment returnVar = selectReturnVariable(n);
				if (returnVar == null || !inRange(m, returnVar))
					return true;
				for (final Expression e : Occurrences.BOTH_LEXICAL.of(returnVar.getName()).in(n))
					$.replace(e, t.newSimpleName("$"), null);
				return true;
			}
		});
	}

	static List<VariableDeclarationFragment> getCandidates(final MethodDeclaration mthdDecl) {
		if (mthdDecl == null)
			return null;
		final List<VariableDeclarationFragment> $ = new ArrayList<>();
		mthdDecl.accept(new ASTVisitor() {
			/**
			 * 
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
			 *      AnonymousClassDeclaration)
			 * 
			 * @param _
			 *            ignored, we don't want to visit declarations inside
			 *            anonymous classes
			 */
			@Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration _) {
				return false;
			}

			@Override public boolean visit(final VariableDeclarationStatement n) {
				$.addAll(n.fragments());
				return true;
			}
		});
		return $;
	}

	static List<ReturnStatement> getReturnStatements(final ASTNode container) {
		final List<ReturnStatement> $ = new ArrayList<>();
		container.accept(new ASTVisitor() {
			/**
			 * 
			 * 
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
			 *      AnonymousClassDeclaration)
			 * 
			 * @param _
			 *            ignored, we don't want to visit declarations inside
			 *            anonymous classes
			 */
			@Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration _) {
				return false;
			}

			@Override public boolean visit(final ReturnStatement node) {
				$.add(node);
				return true;
			}
		});
		return $;
	}

	static VariableDeclarationFragment selectReturnVariable(final MethodDeclaration m) {
		final List<VariableDeclarationFragment> vs = getCandidates(m);
		if (vs == null || vs.isEmpty() || hasDollar(vs))
			return null;
		final List<ReturnStatement> rs = prune(getReturnStatements(m));
		return rs == null ? null : bestCandidate(vs, rs);
	}

	private static boolean hasDollar(final List<VariableDeclarationFragment> vs) {
		for (final VariableDeclaration v : vs)
			if (v.getName().getIdentifier().equals("$"))
				return true;
		return false;
	}

	private static List<ReturnStatement> prune(final List<ReturnStatement> $) {
		if ($ == null || $.isEmpty())
			return null;
		for (final Iterator<ReturnStatement> i = $.iterator(); i.hasNext();) {
			final ReturnStatement r = i.next();
			// Is enclosing method<code><b>void</b></code>?
			if (null == r.getExpression())
				return null;
			if (isLiteral(r))
				i.remove();
		}
		return $;
	}

	private static VariableDeclarationFragment bestCandidate(final List<VariableDeclarationFragment> vs,
			final List<ReturnStatement> rs) {
		VariableDeclarationFragment $ = null;
		int maxOccurrences = 0;
		for (final VariableDeclarationFragment v : vs) {
			int occurrences = 0;
			for (final ReturnStatement r : rs)
				occurrences += Occurrences.BOTH_LEXICAL.of(v.getName()).in(r).size();
			if (occurrences > maxOccurrences) {
				maxOccurrences = occurrences;
				$ = v;
			}
		}
		return $;
	}

	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final MethodDeclaration n) {
				final VariableDeclarationFragment v = selectReturnVariable(n);
				if (v != null)
					opportunities.add(new Range(getContainerByNodeType(v, ASTNode.METHOD_DECLARATION)));
				return true;
			}
		};
	}
}
