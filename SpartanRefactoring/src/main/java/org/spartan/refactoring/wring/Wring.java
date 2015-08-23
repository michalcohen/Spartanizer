package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.asStatement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Extract;
import org.spartan.utils.Range;

/**
 * Reifying the notion of a simplifier; all concrete simplification are found in
 * the array returned by {@link Wrings#values()}. A wring is a transformation
 * that currently works on expressions only, but in the future it will work on
 * any AstNode. Such a transformation make a single simplification of the tree.
 * A wring is so small that it is idempotent: Applying a wring to the output of
 * itself is the empty operation.
 *
 * @param <N> JD
 * @author Yossi Gil
 * @since 2015-07-09
 */
public abstract class Wring<N extends ASTNode> {
  /**
   * Determine whether the parameter is "eligible" for application of this
   * instance. The parameter must be within the scope of the current instance.
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final N n);
  /**
   * Record a rewrite
   *
   * @param r JD
   * @param e JD
   * @return <code><b>true</b></code> <i>iff</i> there is room for further
   *         simplification of this expression.
   */
  abstract boolean go(ASTRewrite r, N n);
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
  final boolean noneligible(final N n) {
    return !eligible(n);
  }
  /**
   * Determine the {@link Range} of characters managed by this instance.
   *
   * @param e JD
   * @return
   */
  @SuppressWarnings("static-method") Range range(final ASTNode n) {
    return new Range(n);
  }
  abstract ASTNode replacement(final N n);
  /**
   * Determines whether this {@link Wring} object is applicable for a given
   * {@link InfixExpression} is within the "scope" of this . Note that it could
   * be the case that a {@link Wring} is applicable in principle to an object,
   * but that actual application will be vacuous.
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is within the
   *         scope of this object
   */
  abstract boolean scopeIncludes(N n);

  @SuppressWarnings("unused") abstract static class Defaults<N extends ASTNode> extends Wring<N> {
    @Override boolean eligible(final N n) {
      return false;
    }
    @Override boolean go(final ASTRewrite r, final N n) {
      return false;
    }
    @Override ASTNode replacement(final N n) {
      return null;
    }
    @Override boolean scopeIncludes(final N n) {
      return false;
    }

    static final class Checker<N extends ASTNode> extends Defaults<N> {
      // Body of this class must be empty!
    }
  }

  static abstract class OfBlock extends Defaults<Block> {
    @SuppressWarnings("static-method") boolean _eligible(@SuppressWarnings("unused") final Block _) {
      return true;
    }
    abstract Statement _replacement(final Block e);
    @Override final boolean eligible(final Block b) {
      assert scopeIncludes(b);
      return _eligible(b);
    }
    @Override final boolean go(final ASTRewrite r, final Block b) {
      if (eligible(b))
        r.replace(b, replacement(b), null);
      return true;
    }
    @Override final Statement replacement(final Block b) {
      assert eligible(b);
      return _replacement(b);
    }
    @Override boolean scopeIncludes(final Block b) {
      return _replacement(b) != null;
    }
  }

  static abstract class OfConditionalExpression extends Defaults<ConditionalExpression> {
    @SuppressWarnings("static-method") boolean _eligible(@SuppressWarnings("unused") final ConditionalExpression _) {
      return true;
    }
    abstract Expression _replacement(final ConditionalExpression e);
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
    @Override boolean scopeIncludes(final ConditionalExpression e) {
      return _replacement(e) != null;
    }
  }

  static abstract class OfIfStatement extends Defaults<IfStatement> {
    @SuppressWarnings("static-method") boolean _eligible(@SuppressWarnings("unused") final IfStatement _) {
      return true;
    }
    abstract Statement _replacement(final IfStatement s);
    @Override final boolean eligible(final IfStatement s) {
      assert scopeIncludes(s);
      return _eligible(s);
    }
    @Override final boolean go(final ASTRewrite r, final IfStatement i) {
      if (eligible(i))
        r.replace(i, replacement(i), null);
      return true;
    }
    @Override final Statement replacement(final IfStatement e) {
      assert eligible(e);
      return _replacement(e);
    }
    @Override boolean scopeIncludes(final IfStatement i) {
      return i != null && _replacement(i) != null;
    }
  }

  static abstract class OfIfStatementAndSubsequentStatement extends Wring.Defaults<IfStatement> {
    @SuppressWarnings("static-method") boolean _eligible(@SuppressWarnings("unused") final IfStatement _) {
      return true;
    }
    @Override final boolean eligible(final IfStatement s) {
      assert scopeIncludes(s);
      return _eligible(s);
    }
    abstract ASTRewrite fillReplacement(IfStatement s, ASTRewrite r);
    @Override final boolean go(final ASTRewrite r, final IfStatement i) {
      if (eligible(i))
        fillReplacement(i, r);
      return true;
    }
    @Override Range range(final ASTNode e) {
      return new Range(e).merge(new Range(Extract.nextStatement(asStatement(e))));
    }
    @Override boolean scopeIncludes(final IfStatement s) {
      return fillReplacement(s, ASTRewrite.create(s.getAST())) != null;
    }
  }

  static abstract class OfInfixExpression extends Defaults<InfixExpression> {
    @SuppressWarnings("static-method") boolean _eligible(@SuppressWarnings("unused") final InfixExpression _) {
      return true;
    }
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
    @Override abstract boolean scopeIncludes(final InfixExpression e);
  }

  static abstract class OfPrefixExpression extends Defaults<PrefixExpression> {
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
    @SuppressWarnings("static-method") protected boolean _eligible(@SuppressWarnings("unused") final PrefixExpression _) {
      return true;
    }
  }

  static abstract class OfPostfixExpression extends Defaults<PostfixExpression> {
    abstract Expression _replacement(final PostfixExpression e);
    @Override final boolean eligible(final PostfixExpression e) {
      assert scopeIncludes(e);
      return _eligible(e);
    }
    abstract boolean _eligible(final PostfixExpression e);
    @Override final boolean go(final ASTRewrite r, final PostfixExpression e) {
      if (eligible(e))
        r.replace(e, replacement(e), null);
      return true;
    }
    @Override final Expression replacement(final PostfixExpression e) {
      assert eligible(e);
      return _replacement(e);
    }
  }

  abstract static class OfVariableDeclarationFragmentAndSurrounding extends Wring.Defaults<VariableDeclarationFragment> {
    @SuppressWarnings("static-method") boolean _eligible(@SuppressWarnings("unused") final VariableDeclarationFragment _) {
      return true;
    }
    @Override final boolean eligible(final VariableDeclarationFragment f) {
      assert scopeIncludes(f);
      return _eligible(f);
    }
    @Override boolean scopeIncludes(final VariableDeclarationFragment f) {
      return fillReplacement(f, ASTRewrite.create(f.getAST())) != null;
    }
    abstract ASTRewrite fillReplacement(VariableDeclarationFragment s, ASTRewrite r);
    @Override final boolean go(final ASTRewrite r, final VariableDeclarationFragment f) {
      if (eligible(f))
        fillReplacement(f, r);
      return true;
    }
    @Override Range range(final ASTNode n) {
      return new Range(n).merge(new Range(Extract.nextStatement(n)));
    }
  }
}
