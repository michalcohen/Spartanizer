package org.spartan.refactoring.wring;

import static org.spartan.refactoring.wring.Wrings.rename;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Rewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Yossi Gil (v3)
 * @since 2013/01/01
 */
public class MethodAbbreviateParameterNames extends Wring<MethodDeclaration> {
  @Override String description(final MethodDeclaration d) {
    return d.getName().toString();
  }
  @Override Rewrite make(final MethodDeclaration d, final ExclusionManager exclude) {
    final SingleVariableDeclaration vd = find(d.parameters());
    if (vd == null)
      return null;
    final SimpleName n = vd.getName();
    d.getAST().newSimpleName("");
    if (exclude != null)
      exclude.exclude(d);
    return new Rewrite("Rename variable " + n + " to $ (main variable returned by " + description(d) + ")", d) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        rename(n, n, d, r, g);
      }
    };
  }
  private SingleVariableDeclaration find(final List<SingleVariableDeclaration> ds) {
    for (final SingleVariableDeclaration $ : ds)
      if (suitable($))
        return $;
    return null;
  }
  private boolean suitable(final SingleVariableDeclaration d) {
    d.getType();
    d.getName();
    // TODO Auto-generated method stub
    return false;
  }
}
