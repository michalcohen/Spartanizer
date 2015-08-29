package org.spartan.refactoring.spartanizations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.AncestorSearch;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Occurrences;
import org.spartan.refactoring.utils.Rewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @since 2013/01/01
 */
public class RenameReturnVariableToDollar extends Spartanization {
  /** Instantiates this class */
  public RenameReturnVariableToDollar() {
    super("Rename returned variable to '$'");
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration n) {
        if (!inRange(m, n))
          return true;
        final VariableDeclarationFragment returnVar = selectReturnVariable(n);
        if (returnVar == null)
          return true;
        final List<Expression> es = Occurrences.BOTH_LEXICAL.of(returnVar).in(n);
        return replace(es, returnVar.getName(), n.getAST().newSimpleName("$"));
      }
      private boolean replace(final List<Expression> es, final SimpleName n, final SimpleName newSimpleName) {
        for (final Expression e : es)
          r.replace(e, newSimpleName, null);
        return true;
      }
    });
  }
  static List<VariableDeclarationFragment> getCandidates(final MethodDeclaration d) {
    if (d == null)
      return null;
    final List<VariableDeclarationFragment> $ = new ArrayList<>();
    d.accept(new ASTVisitor() {
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
       *      AnonymousClassDeclaration)
       * @param _ ignored, we don't want to visit declarations inside anonymous
       *          classes
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
  static VariableDeclarationFragment selectReturnVariable(final MethodDeclaration d) {
    final List<VariableDeclarationFragment> vs = getCandidates(d);
    return vs == null || vs.isEmpty() || hasDollar(vs) ? null : selectReturnVariable(vs, prune(getReturnStatements(d)));
  }
  private static VariableDeclarationFragment selectReturnVariable(final List<VariableDeclarationFragment> fs, final List<ReturnStatement> ss) {
    return ss == null || ss.isEmpty() ? null : bestCandidate(fs, ss);
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
      // Is enclosing method <code><b>void</b></code>?
      if (r.getExpression() == null)
        return null;
      if (Is.literal(r))
        i.remove();
    }
    return $;
  }
  private static VariableDeclarationFragment bestCandidate(final List<VariableDeclarationFragment> vs, final List<ReturnStatement> rs) {
    final int bestScore = bestScore(vs, rs);
    if (bestScore > 0)
      for (final VariableDeclarationFragment v : vs)
        if (bestScore == score(v, rs))
          return noRivals(v, vs, rs) ? v : null;
    return null;
  }
  private static boolean noRivals(final VariableDeclarationFragment candidate, final List<VariableDeclarationFragment> vs, final List<ReturnStatement> rs) {
    for (final VariableDeclarationFragment rival : vs)
      if (rival != candidate && score(rival, rs) >= score(candidate, rs))
        return false;
    return true;
  }
  private static int bestScore(final List<VariableDeclarationFragment> fs, final List<ReturnStatement> rs) {
    int $ = 0;
    for (final VariableDeclarationFragment f : fs)
      $ = Math.max($, score(f, rs));
    return $;
  }
  private static int score(final VariableDeclarationFragment v, final List<ReturnStatement> rs) {
    int $ = 0;
    for (final ReturnStatement r : rs)
      $ += Occurrences.BOTH_LEXICAL.of(v).in(r).size();
    return $;
  }
  @Override protected ASTVisitor collect(final List<Rewrite> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration n) {
        final VariableDeclarationFragment f = selectReturnVariable(n);
        if (f != null)
          $.add(new Rewrite("rename", new AncestorSearch(ASTNode.METHOD_DECLARATION).of(f)) {
            @Override public void go(final ASTRewrite r, final TextEditGroup editGroup) {
              // TODO Auto-generated method stub
            }
          });
        return true;
      }
    };
  }
}
