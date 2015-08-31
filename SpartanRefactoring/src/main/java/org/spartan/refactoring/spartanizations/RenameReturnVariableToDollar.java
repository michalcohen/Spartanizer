package org.spartan.refactoring.spartanizations;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @since 2013/01/01
 */
public class RenameReturnVariableToDollar extends Spartanization {
  static boolean replace(final MethodDeclaration d, final SimpleName n, final ASTRewrite r, final TextEditGroup g) {
    for (final Expression e : Occurrences.BOTH_LEXICAL.of(n).in(d))
      r.replace(e, n, g);
    return true;
  }
  static SimpleName selectReturnVariable(final MethodDeclaration d) {
    final MethodExplorer e = new MethodExplorer(d);
    final List<SimpleName> ns = e.localVariables();
    return ns == null || ns.isEmpty() || hasDollar(ns) ? null : selectReturnVariable(ns, prune(e.returnStatements()));
  }
  private static SimpleName bestCandidate(final List<SimpleName> ns, final List<ReturnStatement> rs) {
    final int bestScore = bestScore(ns, rs);
    if (bestScore > 0)
      for (final SimpleName n : ns)
        if (bestScore == score(n, rs))
          return noRivals(n, ns, rs) ? n : null;
    return null;
  }
  private static int bestScore(final List<SimpleName> ns, final List<ReturnStatement> rs) {
    int $ = 0;
    for (final SimpleName n : ns)
      $ = Math.max($, score(n, rs));
    return $;
  }
  private static boolean hasDollar(final List<SimpleName> ns) {
    for (final SimpleName n : ns)
      if (n.getIdentifier().equals("$"))
        return true;
    return false;
  }
  private static boolean noRivals(final SimpleName candidate, final List<SimpleName> ns, final List<ReturnStatement> rs) {
    for (final SimpleName rival : ns)
      if (rival != candidate && score(rival, rs) >= score(candidate, rs))
        return false;
    return true;
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
  private static int score(final SimpleName n, final List<ReturnStatement> rs) {
    int $ = 0;
    for (final ReturnStatement r : rs)
      $ += Occurrences.BOTH_LEXICAL.of(n).in(r).size();
    return $;
  }
  private static SimpleName selectReturnVariable(final List<SimpleName> ns, final List<ReturnStatement> ss) {
    return ss == null || ss.isEmpty() ? null : bestCandidate(ns, ss);
  }
  /** Instantiates this class */
  public RenameReturnVariableToDollar() {
    super("Rename returned variable to '$'");
  }
  @Override protected ASTVisitor collect(final List<Rewrite> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration d) {
        final SimpleName n = selectReturnVariable(d);
        if (n != null)
          $.add(new Rewrite("rename variable " + d + " to $", d) {
            @Override public void go(final ASTRewrite r, final TextEditGroup g) {
              replace(d, n, r, g);
            }
          });
        return true;
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration d) {
        if (!inRange(m, d))
          return true;
        final SimpleName f = selectReturnVariable(d);
        return f == null || replace(d, f, r, null);
      }
    });
  }
}
