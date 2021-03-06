package il.org.spartan.spartanizer.tipping;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import il.org.spartan.spartanizer.engine.*;

/** Used to replace a node with multiple replacements, taking its place in the
 * parent node children list. This class uses {@link ListRewrite} for the
 * replacement operations, so implementations may use
 * {@link ASTRewrite#createCopyTarget} and {@link ASTRewrite#createMoveTarget}
 * in order to preserve original code formatting.
 * @author Ori Roth
 * @since 2016 */
public abstract class ListReplaceCurrentNode<N extends ASTNode> extends CarefulTipper<N> {
  public abstract List<ASTNode> go(ASTRewrite r, N n, TextEditGroup g);

  /** @return child list property descriptor of the parent of the node we are
   *         replacing */
  public abstract ChildListPropertyDescriptor listDescriptor(N n);

  @Override public boolean prerequisite(final N ¢) {
    return ¢.getParent() != null && go(ASTRewrite.create(¢.getAST()), ¢, null) != null;
  }

  @Override public final Tip tip(final N n) {
    return new Tip(description(n), n, this.getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final ListRewrite l = r.getListRewrite(n.getParent(), listDescriptor(n));
        for (final ASTNode rep : ListReplaceCurrentNode.this.go(r, n, g))
          l.insertBefore(rep, n, g);
        l.remove(n, g);
      }
    };
  }
}
