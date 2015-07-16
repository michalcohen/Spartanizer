package org.spartan.refactoring.spartanizations;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Reifying the notion of a simplifier; all concrete simplification are found in
 * the array returned by {@link #values()}.
 *
 * @author Yossi Gil
 * @since 2015-07-09
 *
 */
public abstract class Simplifier {
  /**
   * Determines whether this {@link Simplifier} object is applicable for a given
   * {@link InfixExpression} is within the "scope" of this . Note that it could
   * be the case that a {@link Simplifier} is applicable in principle to an
   * object, but that actual application will be vacuous.
   *
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object
   */
  abstract boolean scopeIncludes(InfixExpression e);
  /**
   * Determines whether this {@link Simplifier} object is applicable for a given
   * {@link PrefixExpression} is within the "scope" of this . Note that a
   * {@link Simplifier} is applicable in principle to an object, but that actual
   * application will be vacuous.
   *
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final InfixExpression e);
  final boolean noneligible(final InfixExpression e) {
    return !eligible(e);
  }
  abstract boolean withinScope(PrefixExpression e);
  /**
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final PrefixExpression e);
  abstract Expression replacement(final InfixExpression e);
  abstract Expression replacement(final PrefixExpression e);
  /**
   * Record a rewrite
   *
   * @param r
   *          JD
   * @param e
   *          JD
   * @return <code><b>true</b></code> <i>iff</i> there is room for further
   *         simplification of this expression.
   */
  boolean go(final ASTRewrite r, final InfixExpression e) {
    if (eligible(e))
      r.replace(e, replacement(e), null);
    return true;
  }
  abstract boolean go(ASTRewrite r, PrefixExpression e);
  InfixExpression asAndOrOr(final Expression e) {
    return !(e instanceof InfixExpression) ? null : asAndOrOr(e);
  }

  static abstract class OfInfixExpression extends Simplifier {
    abstract boolean _eligible(final InfixExpression e);
    abstract Expression _replacement(final InfixExpression e);
    @Override final boolean go(final ASTRewrite r, final InfixExpression e) {
      if (eligible(e))
        r.replace(e, replacement(e), null);
      return true;
    }
    @Override final boolean eligible(final InfixExpression e) {
      assert scopeIncludes(e);
      return _eligible(e);
    }
    @Override final Expression replacement(final InfixExpression e) {
      assert eligible(e);
      return _replacement(e);
    }
    @SuppressWarnings("unused") @Override final boolean withinScope(final PrefixExpression _) {
      return false;
    }
    @Override final boolean eligible(@SuppressWarnings("unused") final PrefixExpression _) {
      return false;
    }
    @SuppressWarnings("unused") @Override final Expression replacement(final PrefixExpression _) {
      return null;
    }
    @Override final boolean go(final ASTRewrite r, final PrefixExpression e) {
      return false;
    }
  }

  static abstract class OfPrefixExpression extends Simplifier {
    abstract Expression _replacement(final PrefixExpression e);
    abstract boolean _eligible(final PrefixExpression e);
    @Override final boolean go(final ASTRewrite r, final PrefixExpression e) {
      if (eligible(e))
        r.replace(e, replacement(e), null);
      return true;
    }
    @Override final boolean eligible(final PrefixExpression e) {
      assert withinScope(e);
      return _eligible(e);
    }
    @Override final Expression replacement(final PrefixExpression e) {
      assert eligible(e);
      return _replacement(e);
    }
    @Override final boolean go(final ASTRewrite r, final InfixExpression e) {
      return super.go(r, e);
    }
    @Override final boolean eligible(@SuppressWarnings("unused") final InfixExpression _) {
      return false;
    }
    @Override final boolean scopeIncludes(@SuppressWarnings("unused") final InfixExpression _) {
      return false;
    }
  }
}
