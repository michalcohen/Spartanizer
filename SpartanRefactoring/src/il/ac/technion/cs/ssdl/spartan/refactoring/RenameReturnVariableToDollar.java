package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Utils.sort;
import il.ac.technion.cs.ssdl.spartan.utils.Funcs;
import il.ac.technion.cs.ssdl.spartan.utils.Occurrences;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

import java.util.ArrayList;
import java.util.Arrays;
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
				if (returnVar != null){
					if (!inRange(m, returnVar))
						return true;
					for (final Expression e : Occurrences.BOTH_LEXICAL.of(returnVar.getName()).in(n))
						$.replace(e, t.newSimpleName("$"), null);
				}
				return true;
			}
		});
	}

	static List<VariableDeclarationFragment> getCandidates(final ASTNode container) {
		final List<VariableDeclarationFragment> $ = new ArrayList<VariableDeclarationFragment>();
		final Type mthdType = ((MethodDeclaration) container).getReturnType2();
		container.accept(new ASTVisitor() {
			/**
			 *
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
			 *      AnonymousClassDeclaration)
			 *
			 * @param _
			 *          ignored, we don't want to visit declarations inside anonymous
			 *          classes
			 */
			@Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration _) {
				return false;
			}

			@Override public boolean visit(final VariableDeclarationStatement node) {
				if (node.getType().toString().equals(mthdType.toString()))
					$.addAll(node.fragments());
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
			 *          ignored, we don't want to visit declarations inside anonymous
			 *          classes
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
		if (vs.isEmpty() || hasDollar(vs))
			return null;
		final List<ReturnStatement> rs = prune(getReturnStatements(m));
		if (rs == null)
			return null;
		return bestCandidate(vs, rs);
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
			if (r.getExpression() == null)
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

	private static boolean isLiteral(final ReturnStatement r) {
		return Arrays.binarySearch(literals, r.getExpression().getNodeType()) >= 0;
	}

	private static final int[] literals = sort(new int[] {
			//
			ASTNode.NULL_LITERAL, //
			ASTNode.CHARACTER_LITERAL, //
			ASTNode.NUMBER_LITERAL, //
			ASTNode.STRING_LITERAL, //
			ASTNode.BOOLEAN_LITERAL, //
	});

	@Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
		return new ASTVisitor() {
			@Override public boolean visit(final MethodDeclaration n) {
				final VariableDeclarationFragment v = selectReturnVariable(n);
				if (v == null) return true;
				final ASTNode containingBlock = Funcs.getContainerByNodeType(v, ASTNode.METHOD_DECLARATION);
				opportunities.add(new Range(containingBlock));
				return true;
			}
		};
	}
}
