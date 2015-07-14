package org.spartan.refacotring.utils;

import static org.spartan.utils.Utils.asArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A utility class for finding occurrences of an {@link Expression} in an
 * {@link ASTNode}.
 * 
 * @author Boris van Sosin <boris.van.sosin @ gmail.com>
 * @author Yossi Gil <yossi.gil @ gmail.com> (major refactoring 2013/07/10)
 * 
 * @since 2013/07/01
 */
public enum Occurrences {
  /**
   * counts semantic (multiple uses for loops) uses of an expression
   */
  USES_SEMANTIC {
    @Override ASTVisitor[] collectors(final List<Expression> into, final Expression e) {
      return asArray(semanticalUsesCollector(into, e));
    }
  },
  /**
   * counts lexical (single use for loops) uses of an expression
   */
  USES_LEXICAL {
    @Override ASTVisitor[] collectors(final List<Expression> into, final Expression e) {
      return asArray(lexicalUsesCollector(into, e));
    }
  },
  /**
   * counts assignments of an expression
   */
  ASSIGNMENTS {
    @Override ASTVisitor[] collectors(final List<Expression> into, final Expression e) {
      return asArray(definitionsCollector(into, e));
    }
  },
  /**
   * collects assignments AND semantic (multiple uses for loops) uses of an
   * expression
   */
  BOTH_SEMANTIC {
    @Override ASTVisitor[] collectors(final List<Expression> into, final Expression e) {
      return asArray(semanticalUsesCollector(into, e), definitionsCollector(into, e));
    }
  },
  /**
   * collects assignments AND lexical (single use for loops) uses of an
   * expression
   */
  BOTH_LEXICAL {
    @Override ASTVisitor[] collectors(final List<Expression> into, final Expression e) {
      return asArray(lexicalUsesCollector(into, e), definitionsCollector(into, e));
    }
  };
  /**
   * Creates a function object for searching for a given value.
   * 
   * @param e
   *          what to search for
   * @return a function object which can be used for searching for the parameter
   *         in a given location
   */
  public Of of(final Expression e) {
    return new Of() {
      @Override public List<Expression> in(final ASTNode n) {
        return collect(e, n);
      }
    };
  }

  static final ASTMatcher matcher = new ASTMatcher();

  /**
   * An auxiliary class which makes it possible to use an easy invocation
   * sequence for the various offerings of the containing class. This class
   * should never be instantiated or inherited by clients. <p> This class
   * reifies the function object concept; an instance of it records the value we
   * search for; it represents the function that, given a location for the
   * search, will carry out the search for the captured value in its location
   * parameter.
   * 
   * @see Occurrences#of(Expression)
   * @author Yossi Gil <yossi.gil @ gmail.com>
   * @since 2013/14/07
   */
  public static abstract class Of {
    /**
     * the method that will carry out the search
     * 
     * @param n
     *          a location in which the search is to be carried out
     * @return a list of occurrences of the captured value in the parameter.
     */
    public abstract List<Expression> in(ASTNode n);
  }

  /**
   * Lists the required occurrences
   * 
   * @param what
   *          the expression to search for
   * @param where
   *          the n in which to counted
   * 
   * @return the list of uses
   */
  final List<Expression> collect(final Expression what, final ASTNode where) {
    final List<Expression> $ = new ArrayList<>();
    for (final ASTVisitor v : collectors($, what))
      where.accept(v);
    Collections.sort($, new Comparator<Expression>() {
      @Override public int compare(final Expression e1, final Expression e2) {
        return e1.getStartPosition() - e2.getStartPosition();
      }
    });
    return $;
  }

  abstract ASTVisitor[] collectors(final List<Expression> into, final Expression e);

  static ASTVisitor definitionsCollector(final List<Expression> into, final Expression e) {
    return new ASTVisitor() {
      @Override public boolean visit(final Assignment n) {
        collectExpression(n.getLeftHandSide());
        return true;
      }

      @Override public boolean visit(final VariableDeclarationFragment n) {
        collectExpression(n.getName());
        return true;
      }

      @Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration _) {
        return false;
      }

      void collectExpression(final Expression candidate) {
        if (candidate != null && candidate.getNodeType() == e.getNodeType() && candidate.subtreeMatch(matcher, e))
          into.add(candidate);
      }
    };
  }

  static ASTVisitor lexicalUsesCollector(final List<Expression> into, final Expression what) {
    return usesCollector(into, what, true);
  }

  static ASTVisitor semanticalUsesCollector(final List<Expression> into, final Expression what) {
    return usesCollector(into, what, false);
  }

  private static ASTVisitor usesCollector(final List<Expression> into, final Expression what, final boolean lexicalOnly) {
    return new ASTVisitor() {
      private boolean collect(final Expression e) {
        collectExpression(what, e);
        return true;
      }

      private boolean add(final Object o) {
        return collect((Expression) o);
      }

      private boolean collect(@SuppressWarnings("rawtypes") final List os) {
        for (final Object o : os)
          add(o);
        return true;
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

      @Override public boolean visit(final AnonymousClassDeclaration n) {
        for (final VariableDeclarationFragment f : getFieldsOfClass(n))
          if (f.getName().subtreeMatch(matcher, what))
            return false;
        return true;
      }

      @Override public boolean visit(final InfixExpression n) {
        collect(n.getRightOperand());
        collect(n.getLeftOperand());
        return collect(n.extendedOperands());
      }

      @Override public boolean visit(final PrefixExpression n) {
        return collect(n.getOperand());
      }

      @Override public boolean visit(final ConditionalExpression n) {
        collect(n.getThenExpression());
        collect(n.getExpression());
        return collect(n.getElseExpression());
      }

      @Override public boolean visit(final PostfixExpression n) {
        return collect(n.getOperand());
      }

      @Override public boolean visit(final ParenthesizedExpression n) {
        return collect(n.getExpression());
      }

      @Override public boolean visit(final Assignment n) {
        return collect(n.getRightHandSide());
      }

      @Override public boolean visit(final CastExpression n) {
        return collect(n.getExpression());
      }

      @Override public boolean visit(final ArrayAccess n) {
        return collect(n.getArray());
      }

      @Override public boolean visit(final MethodInvocation n) {
        collect(n.getExpression());
        return collect(n.arguments());
      }

      @Override public boolean visit(final ConstructorInvocation n) {
        return collect(n.arguments());
      }

      @Override public boolean visit(final ClassInstanceCreation n) {
        collect(n.getExpression());
        return collect(n.arguments());
      }

      @Override public boolean visit(final ArrayCreation n) {
        return collect(n.dimensions());
      }

      @Override public boolean visit(final ArrayInitializer n) {
        return collect(n.expressions());
      }

      @Override public boolean visit(final ReturnStatement n) {
        return collect(n.getExpression());
      }

      @Override public boolean visit(final FieldAccess n) {
        return collect(n.getExpression());
      }

      @Override public boolean visit(final QualifiedName n) {
        collectExpression(what, n.getQualifier());
        return true;
      }

      @Override public boolean visit(final VariableDeclarationFragment n) {
        return collect(n.getInitializer());
      }

      @Override public boolean visit(final IfStatement n) {
        return collect(n.getExpression());
      }

      @Override public boolean visit(final SwitchStatement n) {
        return collect(n.getExpression());
      }

      @Override public boolean visit(final InstanceofExpression n) {
        return collect(n.getLeftOperand());
      }

      @Override public boolean visit(final ForStatement n) {
        loopDepth++;
        return collect(n.getExpression());
      }

      @Override public boolean visit(final EnhancedForStatement n) {
        loopDepth++;
        return collect(n.getExpression());
      }

      @Override public boolean visit(final DoStatement n) {
        loopDepth++;
        return collect(n.getExpression());
      }

      @Override public boolean visit(final WhileStatement n) {
        loopDepth++;
        return collect(n.getExpression());
      }

      @Override public void endVisit(@SuppressWarnings("unused") final DoStatement _) {
        loopDepth--;
      }

      @Override public void endVisit(@SuppressWarnings("unused") final EnhancedForStatement _) {
        loopDepth--;
      }

      @Override public void endVisit(@SuppressWarnings("unused") final ForStatement _) {
        loopDepth--;
      }

      @Override public void endVisit(@SuppressWarnings("unused") final WhileStatement _) {
        loopDepth--;
      }

      void collectExpression(final Expression e, final Expression candidate) {
        if (candidate != null && candidate.getNodeType() == e.getNodeType() && candidate.subtreeMatch(matcher, e)) {
          into.add(candidate);
          if (repeated())
            into.add(candidate);
        }
      }

      private boolean repeated() {
        return !lexicalOnly && loopDepth > 0;
      }

      private int loopDepth = 0;

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
    };
  }
}
