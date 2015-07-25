package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.utils.Funcs.asConditionalExpression;
import static org.spartan.refactoring.utils.Funcs.asInfixExpression;
import static org.spartan.refactoring.utils.Funcs.asPrefixExpression;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Reifying the notion of a simplifier; all concrete simplification are found in
 * the array returned by {@link Wrings#values()}. A wring is a transformation
 * that currently works on expressions only, but in the future it will work on
 * any AstNode. Such a transformation make a single simplification of the tree.
 * A wring is so small that it is idempotent: Applying a wring to the output of
 * itself is the empty operation.
 *
 * @author Yossi Gil
 * @since 2015-07-09
 */
public abstract class Wring {
  /**
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final ConditionalExpression e);
  final boolean eligible(final Expression e) {
    return eligible(asInfixExpression(e)) || eligible(asPrefixExpression(e)) || eligible(asConditionalExpression(e));
  }
  /**
   * Determines whether this {@link Wring} object is applicable for a given
   * {@link PrefixExpression} is within the "scope" of this . Note that a
   * {@link Wring} is applicable in principle to an object, but that actual
   * application will be vacuous.
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final InfixExpression e);
  /**
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final PrefixExpression e);
  abstract boolean go(ASTRewrite r, ConditionalExpression e);
  /**
   * Record a rewrite
   *
   * @param r JD
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> there is room for further
   *         simplification of this expression.
   */
  boolean go(final ASTRewrite r, final InfixExpression e) {
    if (eligible(e))
      r.replace(e, replacement(e), null);
    return true;
  }
  abstract boolean go(ASTRewrite r, PrefixExpression e);
  /**
   * Determines whether this {@link Wring} object is not applicable for a given
   * {@link PrefixExpression} is within the "scope" of this . Note that a
   * {@link Wring} is applicable in principle to an object, but that actual
   * application will be vacuous.
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is noneligible for
   *         the simplification offered by this object.
   * @see #eligible(InfixExpression)
   */
  final boolean noneligible(final Expression e) {
    return !eligible(e);
  }
  abstract Expression replacement(final ConditionalExpression e);
  final Expression replacement(final Expression e) {
    Expression $;
    return ($ = replacement(asInfixExpression(e))) != null //
        || ($ = replacement(asPrefixExpression(e))) != null //
        || ($ = replacement(asConditionalExpression(e))) != null //
            ? $ : null;
  }
  abstract Expression replacement(final InfixExpression e);
  abstract Expression replacement(final PrefixExpression e);
  abstract boolean scopeIncludes(ConditionalExpression e);
  /**
   * Determines whether this {@link Wring} object is applicable for a given
   * {@link InfixExpression} is within the "scope" of this . Note that it could
   * be the case that a {@link Wring} is applicable in principle to an object,
   * but that actual application will be vacuous.
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object
   */
  final boolean scopeIncludes(final Expression e) {
    return scopeIncludes(asInfixExpression(e)) || scopeIncludes(asPrefixExpression(e)) || scopeIncludes(asConditionalExpression(e));
  }
  /**
   * Determines whether this {@link Wring} object is applicable for a given
   * {@link InfixExpression} is within the "scope" of this . Note that it could
   * be the case that a {@link Wring} is applicable in principle to an object,
   * but that actual application will be vacuous.
   *
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object
   */
  abstract boolean scopeIncludes(InfixExpression e);
  abstract boolean scopeIncludes(PrefixExpression e);

  @SuppressWarnings("unused") abstract static class Defaults extends Wring {
    @Override boolean eligible(final ConditionalExpression e) {
      return false;
    }
    @Override boolean eligible(final InfixExpression e) {
      return false;
    }
    @Override boolean eligible(final PrefixExpression e) {
      return false;
    }
    @Override boolean go(final ASTRewrite r, final ConditionalExpression e) {
      return false;
    }
    @Override boolean go(final ASTRewrite r, final InfixExpression e) {
      return super.go(r, e);
    }
    @Override boolean go(final ASTRewrite r, final PrefixExpression e) {
      return false;
    }
    @Override Expression replacement(final ConditionalExpression e) {
      return null;
    }
    @Override Expression replacement(final InfixExpression e) {
      return null;
    }
    @Override Expression replacement(final PrefixExpression e) {
      return null;
    }
    @Override boolean scopeIncludes(final ConditionalExpression e) {
      return false;
    }
    @Override boolean scopeIncludes(final InfixExpression e) {
      return false;
    }
    @Override boolean scopeIncludes(final PrefixExpression e) {
      return false;
    }
  }

  static abstract class OfConditionalExpression extends Defaults {
    abstract boolean _eligible(final ConditionalExpression e);
    abstract Expression _replacement(final ConditionalExpression e);
    @Override abstract boolean scopeIncludes(final ConditionalExpression e);
    @Override final boolean eligible(final ConditionalExpression e) {
      assert scopeIncludes(e);
      return _eligible(e);
    }
    @Override final boolean go(final ASTRewrite r, final ConditionalExpression e) {
      if (eligible(e))
        r.replace(e, replacement(e), null);
      return true;
    }
    @Override final Expression replacement(final ConditionalExpression e) {
      assert eligible(e);
      return _replacement(e);
    }
  }

  static abstract class OfInfixExpression extends Defaults {
    abstract boolean _eligible(final InfixExpression e);
    abstract Expression _replacement(final InfixExpression e);
    @Override final boolean eligible(final InfixExpression e) {
      assert scopeIncludes(e);
      return _eligible(e);
    }
    @Override final boolean go(final ASTRewrite r, final InfixExpression e) {
      if (eligible(e))
        r.replace(e, replacement(e), null);
      return true;
    }
    @Override final Expression replacement(final InfixExpression e) {
      assert eligible(e);
      return _replacement(e);
    }
  }

  static abstract class OfPrefixExpression extends Defaults {
    abstract boolean _eligible(final PrefixExpression e);
    abstract Expression _replacement(final PrefixExpression e);
    @Override final boolean eligible(final PrefixExpression e) {
      assert scopeIncludes(e);
      return _eligible(e);
    }
    @Override final boolean go(final ASTRewrite r, final PrefixExpression e) {
      if (eligible(e))
        r.replace(e, replacement(e), null);
      return true;
    }
    @Override final Expression replacement(final PrefixExpression e) {
      assert eligible(e);
      return _replacement(e);
    }
  }
}
