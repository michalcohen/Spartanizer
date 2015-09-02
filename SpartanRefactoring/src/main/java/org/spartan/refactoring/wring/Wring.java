package org.spartan.refactoring.wring;

import java.util.*;

import static org.spartan.refactoring.utils.Funcs.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A wring is a transformation that works on an AstNode. Such a transformation
 * make a single simplification of the tree. A wring is so small that it is
 * idempotent: Applying a wring to the output of itself is the empty operation.
 *
 * @param <N> type of node to transform
 * @author Yossi Gil
 * @since 2015-07-09
 */
public abstract class Wring<N extends ASTNode> {
  abstract String description(N n);
  /**
   * Determine whether the parameter is "eligible" for application of this
   * instance. The parameter must be within the scope of the current instance.
   *
   * @param n JD
   * @return <code><b>true</b></code> <i>iff</i> the argument is eligible for
   *         the simplification offered by this object.
   */
  abstract boolean eligible(final N n);
  abstract Rewrite make(N n);
  Rewrite make(final N n, @SuppressWarnings("unused") final ExclusionManager exclude) {
    return make(n);
  }
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
  final boolean nonEligible(final N n) {
    return !eligible(n);
  }
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

  static abstract class InfixSorting extends ReplaceCurrentNode<InfixExpression> {
    @Override boolean eligible(final InfixExpression e) {
      final List<Expression> es = Extract.allOperands(e);
      return !Wrings.mixedLiteralKind(es) && sort(es);
    }
    @Override Expression replacement(final InfixExpression e) {
      final List<Expression> operands = Extract.allOperands(e);
      return !sort(operands) ? null : Subject.operands(operands).to(e.getOperator());
    }
    abstract boolean sort(List<Expression> operands);
  }

  static abstract class ReplaceCurrentNode<N extends ASTNode> extends Wring<N> {
    @Override boolean eligible(@SuppressWarnings("unused") final N _) {
      return true;
    }
    @Override final Rewrite make(final N n) {
      return !eligible(n) ? null : new Rewrite(description(n), n) {
        @Override public void go(final ASTRewrite r, final TextEditGroup g) {
          r.replace(n, replacement(n), g);
        }
      };
    }
    abstract ASTNode replacement(N n);
    @Override boolean scopeIncludes(final N n) {
      return replacement(n) != null;
    }
  }

  static abstract class ReplaceToNextStatement<N extends ASTNode> extends Wring<N> {
    @Override boolean eligible(@SuppressWarnings("unused") final N _) {
      return true;
    }
    abstract ASTRewrite go(ASTRewrite r, N n, Statement nextStatement, TextEditGroup g);
    @Override final Rewrite make(final N n) {
      return make(n, null);
    }
    @Override Rewrite make(final N n, final ExclusionManager exclude) {
      final Statement nextStatement = Extract.nextStatement(n);
      if (nextStatement == null || !eligible(n))
        return null;
      exclude.exclude(nextStatement);
      return new Rewrite(description(n), n, nextStatement) {
        @Override public void go(final ASTRewrite r, final TextEditGroup g) {
          ReplaceToNextStatement.this.go(r, n, nextStatement, g);
        }
      };
    }
    @Override boolean scopeIncludes(final N n) {
      final Statement nextStatement = Extract.nextStatement(n);
      return nextStatement != null && go(ASTRewrite.create(n.getAST()), n, nextStatement, null) != null;
    }
  }

  static abstract class VariableDeclarationFragementAndStatement extends ReplaceToNextStatement<VariableDeclarationFragment> {
    @Override final ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final Statement nextStatement, final TextEditGroup g) {
      if (!Is.variableDeclarationStatement(f.getParent()))
        return null;
      final SimpleName n = f.getName();
      return n == null ? null : go(r, f, n, f.getInitializer(), nextStatement, g);
    }
    abstract ASTRewrite go(ASTRewrite r, VariableDeclarationFragment f, SimpleName n, Expression initializer, Statement nextStatement, TextEditGroup g);
    static List<VariableDeclarationFragment> forbiddenSiblings(final VariableDeclarationFragment f) {
      final List<VariableDeclarationFragment> $ = new ArrayList<>();
      boolean collecting = false;
      for (final VariableDeclarationFragment brother : (List<VariableDeclarationFragment>) ((VariableDeclarationStatement) f.getParent()).fragments()) {
        if (brother == f) {
          collecting = true;
          continue;
        }
        if (collecting)
          $.add(brother);
      }
      return $;
    }
    static void remove(final VariableDeclarationFragment f, final ASTRewrite r, final TextEditGroup g) {
      final VariableDeclarationStatement parent = (VariableDeclarationStatement) f.getParent();
      final List<VariableDeclarationFragment> live = live(f, parent.fragments());
      if (live.isEmpty()) {
        r.remove(parent, g);
        return;
      }
      final VariableDeclarationStatement newParent = duplicate(parent);
      newParent.fragments().clear();
      newParent.fragments().addAll(live);
      r.replace(parent, newParent, g);
    }
    static boolean useForbiddenSiblings(final VariableDeclarationFragment f, final ASTNode... ns) {
      for (final VariableDeclarationFragment b : forbiddenSiblings(f))
        if (Search.BOTH_SEMANTIC.of(b).existIn(ns))
          return true;
      return false;
    }
    private static List<VariableDeclarationFragment> live(final VariableDeclarationFragment f, final List<VariableDeclarationFragment> fs) {
      final List<VariableDeclarationFragment> $ = new ArrayList<>();
      for (final VariableDeclarationFragment brother : fs)
        if (brother != null && brother != f && brother.getInitializer() != null)
          $.add(duplicate(brother));
      return $;
    }
    protected static boolean inlineInto(final SimpleName n, final Expression replacement, final Expression oldExpression, final ASTRewrite r, final TextEditGroup g) {
      final Expression newExpression = duplicate(oldExpression);
      if (Search.findDefinitions(n).in(newExpression))
        return false;
      final List<Expression> uses = Search.USES_SEMANTIC.of(n).in(newExpression);
      if (!Is.sideEffectFree(replacement) && uses.size() > 1)
        return false;
      r.replace(oldExpression, newExpression, g);
      for (final Expression e : uses)
        r.replace(e, new Plant(replacement).into(e.getParent()), g);
      return true;
    }
    @Override Rewrite make(final VariableDeclarationFragment f, final ExclusionManager exclude) {
      final Rewrite $ = super.make(f, exclude);
      if ($ != null && exclude != null)
        exclude.exclude(f.getParent());
      return $;
    }
  }
}
