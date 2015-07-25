package org.spartan.refactoring.spartanizations;

import static org.spartan.utils.Utils.removeDuplicates;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.utils.Range;

/**
 * Applies the the first applicable {@link Wring} object found in
 * <ode><b>enum</b></code> {@link Wrings} to a tree.
 *
 * @author Yossi Gil
 * @since 2015/07/10
 */
public class Trimmer extends Spartanization {
  /** Instantiates this class */
  public Trimmer() {
    super("Trimmer", "Simiplifies expression once");
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final PrefixExpression e) {
        final Wring w = Wrings.find(e);
        return w != null && w.noneligible(e) ? true : overrideInto(e, $);
      }
      @Override public boolean visit(final InfixExpression e) {
        final Wring w = Wrings.find(e);
        return w != null && w.noneligible(e) ? true : overrideInto(e, $);
      }
      @Override public boolean visit(final ConditionalExpression e) {
        final Wring w = Wrings.find(e);
        return w != null && w.noneligible(e) ? true : overrideInto(e, $);
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring w = Wrings.find(e);
        if (w != null)
          return w.go(r, e);
        return true;
      }
      @Override public boolean visit(final PrefixExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring w = Wrings.find(e);
        if (w != null)
          return w.go(r, e);
        return true;
      }
      @Override public boolean visit(final ConditionalExpression e) {
        if (!inRange(m, e))
          return true;
        final Wring w = Wrings.find(e);
        if (w != null)
          return w.go(r, e);
        return true;
      }
    });
  }
  static boolean overrideInto(final Expression e, final List<Range> rs) {
    return overrideInto(new Range(e), rs);
  }
  private static boolean overrideInto(final Range r, final List<Range> rs) {
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
}
