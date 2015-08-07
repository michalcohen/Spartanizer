package org.spartan.refactoring.wring;

import static org.spartan.utils.Utils.removeDuplicates;

import java.util.List;

import org.eclipse.core.resources.IMarker;
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
import org.spartan.refactoring.spartanizations.Spartanization;
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
        final Wring w = Wrings.find(e);
        return w == null || w.noneligible(e) || overrideInto(w.range(e), $);
      }
      @Override public boolean visit(final VariableDeclarationFragment it) {
        final Wring w = Wrings.find(it);
        return w == null || w.noneligible(it) || overrideInto(w.range(it), $);
      }
      @Override public boolean visit(final IfStatement i) {
        final Wring w = Wrings.find(i);
        return w == null || w.noneligible(i) || overrideInto(w.range(i), $);
      }
      @Override public boolean visit(final Block b) {
        final Wring w = Wrings.find(b);
        return w == null || w.noneligible(b) || overrideInto(w.range(b), $);
      }
      @Override public boolean visit(final InfixExpression e) {
        final Wring w = Wrings.find(e);
        return w == null || w.noneligible(e) || overrideInto(w.range(e), $);
      }
      @Override public boolean visit(final PrefixExpression e) {
        final Wring w = Wrings.find(e);
        return w == null || w.noneligible(e) || overrideInto(w.range(e), $);
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final ConditionalExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring w = Wrings.find(e);
        return w == null || w.go(r, e);
      }
      @Override public boolean visit(final IfStatement i) {
        if (!inRange(m, i))
          return true;
        final Wring w = Wrings.find(i);
        return w == null || w.go(r, i);
      }
      @Override public boolean visit(final VariableDeclarationFragment f) {
        if (!inRange(m, f))
          return true;
        final Wring w = Wrings.find(f);
        return w == null || w.go(r, f);
      }
      @Override public boolean visit(final Block b) {
        if (!inRange(m, b))
          return true;
        final Wring w = Wrings.find(b);
        return w == null || w.go(r, b);
      }
      @Override public boolean visit(final InfixExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring w = Wrings.find(e);
        return w == null || w.go(r, e);
      }
      @Override public boolean visit(final PrefixExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring w = Wrings.find(e);
        return w == null || w.go(r, e);
      }
    });
  }
}
