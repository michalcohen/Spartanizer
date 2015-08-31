package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.utils.Utils.asArray;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.spartan.utils.Utils;

/**
 * A utility class for finding occurrences of an {@link Expression} in an
 * {@link ASTNode}.
 *
 * @author Boris van Sosin <boris.van.sosin @ gmail.com>
 * @author Yossi Gil <yossi.gil @ gmail.com> (major refactoring 2013/07/10)
 * @since 2013/07/01
 */
public enum Occurrences {
  /** collects semantic (multiple uses for loops) uses of an expression */
  USES_SEMANTIC {
    @Override ASTVisitor[] collectors(final Expression e, final List<Expression> into) {
      return asArray(semanticalUsesCollector(into, e));
    }
  },
  /** collects lexical (single use for loops) uses of an expression */
  USES_LEXICAL {
    @Override ASTVisitor[] collectors(final Expression e, final List<Expression> into) {
      return asArray(lexicalUsesCollector(into, e));
    }
  },
  /** collects assignments of an expression */
  ASSIGNMENTS {
    @Override ASTVisitor[] collectors(final Expression e, final List<Expression> into) {
      return asArray(definitionsCollector(into, e));
    }
  },
  /**
   * collects assignments AND semantic (multiple uses for loops) uses of an
   * expression
   */
  BOTH_SEMANTIC {
    @Override ASTVisitor[] collectors(final Expression e, final List<Expression> into) {
      return asArray(semanticalUsesCollector(into, e), lexicalUsesCollector(into, e), definitionsCollector(into, e));
    }
  },
  /**
   * collects assignments AND lexical (single use for loops) uses of an
   * expression
   */
  BOTH_LEXICAL {
    @Override ASTVisitor[] collectors(final Expression e, final List<Expression> into) {
      return asArray(lexicalUsesCollector(into, e), definitionsCollector(into, e));
    }
  };
  static final ASTMatcher matcher = new ASTMatcher();
  static ASTVisitor definitionsCollector(final List<Expression> into, final Expression e) {
    return new ASTVisitor() {
      void collectExpression(final Expression candidate) {
        if (candidate != null && e.getNodeType() == candidate.getNodeType() && candidate.subtreeMatch(matcher, e))
          into.add(candidate);
      }
      @Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration _) {
        return false;
      }
      @Override public boolean visit(final Assignment a) {
        collectExpression(left(a));
        return true;
      }
      @Override public boolean visit(final VariableDeclarationFragment f) {
        collectExpression(f.getName());
        return true;
      }
    };
  }
  static ASTVisitor lexicalUsesCollector(final List<Expression> into, final Expression what) {
    return usesCollector(what, into, true);
  }
  static ASTVisitor semanticalUsesCollector(final List<Expression> into, final Expression what) {
    return usesCollector(what, into, false);
  }
  private static ASTVisitor usesCollector(final Expression what, final List<Expression> into, final boolean lexicalOnly) {
    return new ASTVisitor() {
      private int loopDepth = 0;
      private boolean add(final Object o) {
        return collect((Expression) o);
      }
      private boolean collect(final Expression e) {
        collectExpression(what, e);
        return true;
      }
      private boolean collect(@SuppressWarnings("rawtypes") final List os) {
        for (final Object o : os)
          add(o);
        return true;
      }
      void collectExpression(final Expression e, final Expression candidate) {
        if (candidate != null && e.getNodeType() == candidate.getNodeType() && candidate.subtreeMatch(matcher, e)) {
          into.add(candidate);
          if (repeated())
            into.add(candidate);
        }
      }
      @Override public void endVisit(@SuppressWarnings("unused") final DoStatement _) {
        --loopDepth;
      }
      @Override public void endVisit(@SuppressWarnings("unused") final EnhancedForStatement _) {
        --loopDepth;
      }
      @Override public void endVisit(@SuppressWarnings("unused") final ForStatement _) {
        --loopDepth;
      }
      @Override public void endVisit(@SuppressWarnings("unused") final WhileStatement _) {
        --loopDepth;
      }
      private List<VariableDeclarationFragment> getFieldsOfClass(final ASTNode classNode) {
        final List<VariableDeclarationFragment> $ = new ArrayList<>();
        classNode.accept(new ASTVisitor() {
          @Override public boolean visit(final FieldDeclaration n) {
            $.addAll(n.fragments());
            return false;
          }
        });
        return $;
      }
      private boolean repeated() {
        return !lexicalOnly && loopDepth > 0;
      }
      @Override public boolean visit(final AnonymousClassDeclaration n) {
        for (final VariableDeclarationFragment f : getFieldsOfClass(n))
          if (f.getName().subtreeMatch(matcher, what))
            return false;
        return true;
      }
      @Override public boolean visit(final ArrayAccess n) {
        return collect(n.getArray());
      }
      @Override public boolean visit(final ArrayCreation n) {
        return collect(n.dimensions());
      }
      @Override public boolean visit(final ArrayInitializer n) {
        return collect(n.expressions());
      }
      @Override public boolean visit(final Assignment a) {
        return collect(right(a));
      }
      @Override public boolean visit(final CastExpression e) {
        return collect(e.getExpression());
      }
      @Override public boolean visit(final ClassInstanceCreation n) {
        collect(n.getExpression());
        return collect(n.arguments());
      }
      @Override public boolean visit(final ConditionalExpression n) {
        collect(n.getThenExpression());
        collect(n.getExpression());
        return collect(n.getElseExpression());
      }
      @Override public boolean visit(final ConstructorInvocation n) {
        return collect(n.arguments());
      }
      @Override public boolean visit(final DoStatement n) {
        ++loopDepth;
        return collect(n.getExpression());
      }
      @Override public boolean visit(final EnhancedForStatement n) {
        ++loopDepth;
        return collect(n.getExpression());
      }
      @Override public boolean visit(final FieldAccess n) {
        return collect(n.getExpression());
      }
      @Override public boolean visit(final ForStatement n) {
        ++loopDepth;
        return collect(n.getExpression());
      }
      @Override public boolean visit(final IfStatement n) {
        return collect(n.getExpression());
      }
      @Override public boolean visit(final InfixExpression e) {
        collect(right(e));
        collect(left(e));
        return collect(e.extendedOperands());
      }
      @Override public boolean visit(final InstanceofExpression e) {
        return collect(left(e));
      }
      @Override public boolean visit(final MethodDeclaration n) {
        /* Now: this is a bit complicated. Java allows declaring methods in
         * anonymous classes in which the formal parameters hide variables in
         * the enclosing scope. We don't want to collect them as uses of the
         * variable */
        for (final Object o : n.parameters())
          if (((SingleVariableDeclaration) o).getName().subtreeMatch(matcher, what))
            return false;
        return true;
      }
      @Override public boolean visit(final MethodInvocation n) {
        collect(n.getExpression());
        return collect(n.arguments());
      }
      @Override public boolean visit(final ParenthesizedExpression n) {
        return collect(n.getExpression());
      }
      @Override public boolean visit(final PostfixExpression n) {
        return collect(n.getOperand());
      }
      @Override public boolean visit(final PrefixExpression n) {
        return collect(n.getOperand());
      }
      @Override public boolean visit(final QualifiedName n) {
        collectExpression(what, n.getQualifier());
        return true;
      }
      @Override public boolean visit(final ReturnStatement n) {
        return collect(n.getExpression());
      }
      @Override public boolean visit(final SwitchStatement n) {
        return collect(n.getExpression());
      }
      @Override public boolean visit(final VariableDeclarationFragment n) {
        return collect(n.getInitializer());
      }
      @Override public boolean visit(final WhileStatement n) {
        ++loopDepth;
        return collect(n.getExpression());
      }
      @Override public boolean visit(final SimpleName n) {
        return collect(n);
      }
    };
  }
  /**
   * Lists the required occurrences
   *
   * @param what the expression to search for
   * @param ns the n in which to counted
   * @return the list of uses
   */
  final List<Expression> collect(final Expression what, final ASTNode... ns) {
    final List<Expression> $ = new ArrayList<>();
    for (final ASTNode n : ns)
      for (final ASTVisitor v : collectors(what, $))
        n.accept(v);
    Utils.removeDuplicates($);
    Collections.sort($, new Comparator<Expression>() {
      @Override public int compare(final Expression e1, final Expression e2) {
        return e1.getStartPosition() - e2.getStartPosition();
      }
    });
    return $;
  }
  abstract ASTVisitor[] collectors(final Expression e, final List<Expression> into);
  /**
   * Creates a function object for searching for a given value.
   *
   * @param e what to search for
   * @return a function object to be used for searching for the parameter in a
   *         given location
   */
  public Of of(final Expression e) {
    return new Of() {
      @Override public List<Expression> in(final ASTNode... ns) {
        return collect(e, ns);
      }
    };
  }
  /**
   * Creates a function object for searching for a given {@link SimpleName}, as
   * specified by the {@link VariableDeclarationFragment},
   *
   * @param f JD
   * @return a function object to be used for searching for the
   *         {@link SimpleName} embedded in the parameter.
   */
  public Of of(final VariableDeclarationFragment f) {
    return of(f.getName());
  }

  /**
   * An auxiliary class which makes it possible to use an easy invocation
   * sequence for the various offerings of the containing class. This class
   * should never be instantiated or inherited by clients.
   * <p>
   * This class realizes the function object concept; an instance of it records
   * the value we search for; it represents the function that, given a location
   * for the search, will carry out the search for the captured value in its
   * location parameter.
   *
   * @see Occurrences#of(Expression)
   * @author Yossi Gil <yossi.gil @ gmail.com>
   * @since 2013/14/07
   */
  public static abstract class Of {
    /**
     * Determine whether this instance occurs in a bunch of expressions
     *
     * @param ns JD
     * @return <code><b>true</b></code> <i>iff</i> this instance occurs in the
     *         Parameter.
     */
    public boolean existIn(final ASTNode... ns) {
      return !in(ns).isEmpty();
    }
    /**
     * the method that will carry out the search
     *
     * @param ns where to search
     * @return a list of occurrences of the captured value in the parameter.
     */
    public abstract List<Expression> in(ASTNode... ns);
  }
}
