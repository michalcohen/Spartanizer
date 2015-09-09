package org.spartan.refactoring.wring;

import static org.spartan.utils.Utils.removeDuplicates;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.Rewrite;
import org.spartan.utils.Range;

/**
 * @author Yossi Gil
 * @since 2015/07/10
 */
public class Trimmer extends Spartanization {
  /**
   * Apply trimming repeatedly, until no more changes
   *
   * @param from what to process
   * @return the trimmed text
   */
  public static String fixedPoint(final String from) {
    final Trimmer trimmer = new Trimmer();
    final Document $ = new Document(from);
    for (;;) {
      final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast($.get());
      final ASTRewrite r = trimmer.createRewrite(u, null);
      final TextEdit e = r.rewriteAST($, null);
      try {
        e.apply($);
      } catch (final MalformedTreeException | IllegalArgumentException | BadLocationException x) {
        x.printStackTrace();
      }
      if (!e.hasChildren())
        return $.get();
    }
  }
  /**
   * Tries to union the given range with one of the elements inside the given
   * list.
   *
   * @param rs The list of ranges to union with
   * @param rNew The new range to union
   * @return True - if the list updated and the new range consumed False - the
   *         list remained intact
   * @see areOverlapped
   * @see mergerangeList
   */
  protected static boolean unionRangeWithList(final List<Range> rs, final Range rNew) {
    boolean $ = false;
    for (final Range r : rs)
      if (r.overlapping(rNew)) {
        rs.add(r.merge(rNew));
        $ = true;
        break;
      }
    removeDuplicates(rs);
    return $;
  }
  static boolean overrideInto(final Rewrite r, final List<Rewrite> rs) {
    if (r != null) {
      r.pruneIncluders(rs);
      rs.add(r);
    }
    return true;
  }
  /** Instantiates this class */
  public Trimmer() {
    super("Trimmer");
  }
  @Override protected ASTVisitor collect(final List<Rewrite> $) {
    return new DispatchingVisitor() {
      @Override <N extends ASTNode> boolean go(final N n) {
        assert Toolbox.instance != null;
        if (Toolbox.instance == null)
          System.exit(1);
        final Wring<N> w = Toolbox.instance.find(n);
        return w == null || w.nonEligible(n) || overrideInto(w.make(n, exclude), $);
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final CompilationUnit u, final IMarker m) {
    u.accept(new DispatchingVisitor() {
      @Override <N extends ASTNode> boolean go(final N n) {
        if (!inRange(m, n))
          return true;
        final Wring<N> w = Toolbox.instance.find(n);
        if (w != null) {
          final Rewrite make = w.make(n, exclude);
          if (make != null)
            make.go(r, null);
        }
        return true;
      }
    });
  }
  @SuppressWarnings("static-method") ExclusionManager makeExcluder() {
    return new ExclusionManager();
  }

  abstract class DispatchingVisitor extends ASTVisitor {
    final ExclusionManager exclude = makeExcluder();
    @Override public final boolean visit(final ReturnStatement it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final MethodDeclaration it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final Assignment it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final Block it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final ConditionalExpression it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final IfStatement it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final InfixExpression it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final PostfixExpression it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final PrefixExpression it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final SuperConstructorInvocation it) {
      return cautiousGo(it);
    }
    @Override public final boolean visit(final VariableDeclarationFragment it) {
      return cautiousGo(it);
    }
    private boolean cautiousGo(final ASTNode n) {
      return !exclude.isExcluded(n) && go(n);
    }
    abstract <N extends ASTNode> boolean go(final N n);
  }
}
