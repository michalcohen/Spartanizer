package org.spartan.refactoring.spartanizations;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Rewrite;
import org.spartan.refactoring.utils.Search;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (v3)
 */
// TODO: There <b>must</b> be an option to
// * disable this warning in selected places. Consider this example: <pre>
// public
// * static &lt;T&gt; void swap(final T[] ts, final int i, final int j) { final
// T
// * t = ts[i]; ts[i] = ts[j]; ts[j] = t; } </pre> You should not inline the
// * variable t, and you should not move forward its declaration! Some
// * alternatives for disabling the warning are: First, a dedicated annotation
// of
// * name such as $Resident or
// * @Unmovable, or some other word that is single, and easy to understand.
// <pre>
// * public static &lt;T&gt; void swap(final T[] ts, final int i, final int j) {
// * &#064;Resident final T t = ts[i]; ts[i] = ts[j]; ts[j] = t; } </pre>
// Augment
// * the @SuppressWarning annotation <pre> public static &lt;T&gt; void
// swap(final
// * T[] ts, final int i, final int j) {
// * &#064;SuppressWarning(&quot;unmovable&quot;) final T t = ts[i]; ts[i] =
// * ts[j]; ts[j] = t; } </pre> Require comment <pre> public static &lt;T&gt;
// void
// * swap(final T[] ts, final int i, final int j) { final T t = ts[i]; // Don't
// * move! ts[i] = ts[j]; ts[j] = t; } </pre>
// * @since 2013/01/01
// */
public class InlineSingleUse extends Spartanization {
  /** Instantiates this class */
  public InlineSingleUse() {
    super("Inline Single Use");
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment n) {
        if (!inRange(m, n) || !(n.getParent() instanceof VariableDeclarationStatement))
          return true;
        final SimpleName name = n.getName();
        final VariableDeclarationStatement parent = (VariableDeclarationStatement) n.getParent();
        final List<Expression> uses = Search.USES_SEMANTIC.of(name).in(parent.getParent());
        if (uses.size() == 1 && (Is._final(parent) || numOfOccur(Search.DEFINITIONS, name, parent.getParent()) == 1)) {
          r.replace(uses.get(0), n.getInitializer(), null);
          r.remove(parent.fragments().size() != 1 ? n : parent, null);
        }
        return true;
      }
    });
  }
  @Override protected ASTVisitor collect(final List<Rewrite> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final VariableDeclarationFragment node) {
        return !(node.getParent() instanceof VariableDeclarationStatement) || go(node, node.getName());
      }
      private boolean go(final VariableDeclarationFragment v, final SimpleName n) {
        final VariableDeclarationStatement parent = (VariableDeclarationStatement) v.getParent();
        if (numOfOccur(Search.USES_SEMANTIC, n, parent.getParent()) == 1 && (Is._final(parent) || //
            numOfOccur(Search.DEFINITIONS, n, parent.getParent()) == 1))
          $.add(new Rewrite("", v) {
            @Override public void go(final ASTRewrite r, final TextEditGroup editGroup) {
              // TODO Auto-generated method stub
            }
          });
        return true;
      }
    };
  }
  static int numOfOccur(final Search typeOfOccur, final Expression of, final ASTNode in) {
    return typeOfOccur == null || of == null || in == null ? -1 : typeOfOccur.of(of).in(in).size();
  }
}
