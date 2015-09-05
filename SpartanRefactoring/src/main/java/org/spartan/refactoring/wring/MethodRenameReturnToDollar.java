package org.spartan.refactoring.wring;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Yossi Gil (v3)
 * @since 2013/01/01
 */
public class MethodRenameReturnToDollar extends Wring<MethodDeclaration> {
  static boolean replace(final MethodDeclaration d, final SimpleName n, final ASTRewrite r, final TextEditGroup g) {
    for (final Expression e : Search.BOTH_LEXICAL.of(n).in(d))
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
      $ += Search.BOTH_LEXICAL.of(n).in(r).size();
    return $;
  }
  private static SimpleName selectReturnVariable(final List<SimpleName> ns, final List<ReturnStatement> ss) {
    return ss == null || ss.isEmpty() ? null : bestCandidate(ns, ss);
  }
  @Override String description(final MethodDeclaration d) {
    return d.getName().toString();
  }
  @Override boolean eligible(final MethodDeclaration n) {
    return true;
  }
  @Override Rewrite make(final MethodDeclaration d) {
    return make(d, null);
  }
  @Override Rewrite make(final MethodDeclaration d, final ExclusionManager exclude) {
    final Type t = d.getReturnType2();
    if (t instanceof PrimitiveType && ((PrimitiveType) t).getPrimitiveTypeCode() == PrimitiveType.VOID)
      return null;
    final SimpleName n = selectReturnVariable(d);
    if (n == null)
      return null;
    if (exclude != null)
      exclude.exclude(d);
    return new Rewrite("Rename variable " + n + " to $ (main variable returned by " + description(d) + ")", d) {
      @Override public void go(final ASTRewrite r, final TextEditGroup editGroup) {
        fail("Something went wrong");
      }
    };
  }
  @Override boolean scopeIncludes(final MethodDeclaration n) {
    // TODO Auto-generated method stub
    return false;
  }
}
