package org.spartan.refactoring.wring;

import static org.spartan.utils.Utils.removeDuplicates;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.utils.As;
import org.spartan.utils.Range;

/**
 * Applies the first applicable {@link Wring} object found in
 * <code><b>enum</b></code> {@link Wrings} to a tree.
 *
 * @author Yossi Gil
 * @since 2015/07/10
 */
public class Trimmer extends Spartanization {
  static boolean overrideInto(final Range r, final List<Range> rs) {
    r.pruneIncluders(rs);
    rs.add(r);
    return true;
  }
  public static String fixedPoint(final String from) {
    final Document d = new Document(from);
    for (;;) {
      final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(d);
      final AST t = u.getAST();
      final ASTRewrite r = ASTRewrite.create(t);
      new Trimmer().fillRewrite(r, t, u, null);
      try {
        final TextEdit e = r.rewriteAST();
        if (e.hasChildren())
          break;
        try {
          e.apply(d);
        } catch (MalformedTreeException | BadLocationException x) {
          x.printStackTrace();
          break;
        }
      } catch (JavaModelException | IllegalArgumentException x) {
        x.printStackTrace();
        break;
      }
    }
    return d.get();
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
  static boolean overrideInto(final ASTNode e, final List<Range> rs) {
    return overrideInto(new Range(e), rs);
  }
  /** Instantiates this class */
  public Trimmer() {
    super("Trimmer", "Simiplifies expression once");
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final ConditionalExpression e) {
        final Wring<ConditionalExpression> w = Toolbox.instance.find(e);
        return w == null || w.noneligible(e) || overrideInto(w.range(e), $);
      }
      @Override public boolean visit(final VariableDeclarationFragment it) {
        final Wring<VariableDeclarationFragment> w = Toolbox.instance.find(it);
        return w == null || w.noneligible(it) || overrideInto(w.range(it), $);
      }
      @Override public boolean visit(final IfStatement i) {
        final Wring<IfStatement> w = Toolbox.instance.find(i);
        return w == null || w.noneligible(i) || overrideInto(w.range(i), $);
      }
      @Override public boolean visit(final Block b) {
        final Wring<Block> w = Toolbox.instance.find(b);
        return w == null || w.noneligible(b) || overrideInto(w.range(b), $);
      }
      @Override public boolean visit(final InfixExpression e) {
        final Wring<InfixExpression> w = Toolbox.instance.find(e);
        return w == null || w.noneligible(e) || overrideInto(w.range(e), $);
      }
      @Override public boolean visit(final PrefixExpression e) {
        final Wring<PrefixExpression> w = Toolbox.instance.find(e);
        return w == null || w.noneligible(e) || overrideInto(w.range(e), $);
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final ConditionalExpression e) {
        return go(e);
      }
      private <N extends ASTNode> boolean go(final N n) {
        if (!inRange(m, n))
          return true;
        final Wring<N> w = Toolbox.instance.find(n);
        return w == null || w.go(r, n);
      }
      @Override public boolean visit(final IfStatement s) {
        return go(s);
      }
      @Override public boolean visit(final VariableDeclarationFragment f) {
        return go(f);
      }
      @Override public boolean visit(final Block b) {
        return go(b);
      }
      @Override public boolean visit(final InfixExpression e) {
        return go(e);
      }
      @Override public boolean visit(final PrefixExpression e) {
        return go(e);
      }
    });
  }
}
