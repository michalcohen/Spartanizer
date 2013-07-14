package il.ac.technion.cs.ssdl.spartan.utils;

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
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @author Yossi Gil <yossi.gil@gmail.com> (major refactoring 2013/07/10)
 * 
 * @since 2013/07/01
 */
public enum Occurrences {
  /**
   * counts semantic (multiple uses for loops) uses of an expression
   */
  USES_SEMANTIC {
    @Override public List<Expression> collect(final ASTNode n, final Expression e) {
      return collect(n, e, true);
    }
  },
  /**
   * counts lexical (single use for loops) uses of an expression
   */
  USES_LEXICAL {
    @Override public List<Expression> collect(final ASTNode n, final Expression e) {
      return collect(n, e, false);
    }
  },
  /**
   * counts assignments of an expression
   */
  ASSIGNMENTS {
    @Override public List<Expression> collect(final ASTNode n, final Expression e) {
      final List<Expression> $ = new ArrayList<Expression>();
      n.accept(new ASTVisitor() {
        /**
         * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
         * @param _
         *          ignored
         */
        @Override public boolean visit(final AnonymousClassDeclaration _) {
          return false;
        }
        
        @Override public boolean visit(final Assignment node) {
          $.addAll(listSingle(node.getLeftHandSide(), e, false));
          return true;
        }
        
        @Override public boolean visit(final VariableDeclarationFragment node) {
          $.addAll(listSingle(node.getName(), e, false));
          return true;
        }
      });
      return $;
    }
  },
  /**
   * counts assignments AND semantic (multiple uses for loops) uses of an
   * expression
   */
  BOTH_SEMANTIC {
    @Override public List<Expression> collect(final ASTNode n, final Expression e) {
      final List<Expression> $ = new ArrayList<Expression>(USES_SEMANTIC.collect(n, e));
      addAssignments($, n, e);
      return $;
    }
  },
  /**
   * counts assignments AND lexical (single use for loops) uses of an expression
   */
  BOTH_LEXICAL {
    @Override public List<Expression> collect(final ASTNode n, final Expression e) {
      final List<Expression> $ = new ArrayList<Expression>(USES_LEXICAL.collect(n, e));
      addAssignments($, n, e);
      return $;
    }
  };
  /**
   * Lists the required occurrences
   * 
   * @param n
   *          the node in which to counted
   * @param e
   *          the expression to count
   * @return the list of uses/assignments
   */
  public abstract List<Expression> collect(final ASTNode n, final Expression e);
  
  static void addAssignments(final List<Expression> $, final ASTNode n, final Expression e) {
    $.addAll(ASSIGNMENTS.collect(n, e));
    Collections.sort($, new Comparator<Expression>() {
      @Override public int compare(final Expression e1, final Expression e2) {
        return e1.getStartPosition() - e2.getStartPosition();
      }
    });
  }
  
  static List<Expression> listSingle(final Expression e1, final Expression e2, final boolean repeated) {
    final List<Expression> $ = new ArrayList<Expression>();
    if (e1 != null && e1.getNodeType() == e2.getNodeType() && e1.subtreeMatch(matcher, e2)) {
      $.add(e1);
      if (repeated)
        $.add(e1);
    }
    return $;
  }
  
  protected static List<VariableDeclarationFragment> getFieldsOfClass(final ASTNode classNode) {
    final List<VariableDeclarationFragment> $ = new ArrayList<VariableDeclarationFragment>();
    classNode.accept(new ASTVisitor() {
      @Override public boolean visit(final FieldDeclaration node) {
        $.addAll(node.fragments());
        return false;
      }
    });
    return $;
  }
  
  static List<Expression> collect(final ASTNode where, final Expression what, final boolean semantic) {
    final List<Expression> $ = new ArrayList<Expression>();
    where.accept(new ASTVisitor() {
      private boolean count(final Expression e) {
        $.addAll(listSingle(e, what, repeated()));
        return true;
      }
      
      private boolean count(final Object o) {
        return count((Expression) o);
      }
      
      private boolean count(@SuppressWarnings("rawtypes") final List os) {
        for (final Object o : os)
          count(o);
        return true;
      }
      
      @Override public boolean visit(final MethodDeclaration node) {
        /*
         * Now: this is a bit complicated. Java allows declaring methods in
         * anonymous classes in which the formal parameters hide variables in
         * the enclosing scope. We don't want to count them as uses of the
         * variable
         */
        for (final Object o : node.parameters())
          if (((SingleVariableDeclaration) o).getName().subtreeMatch(matcher, what))
            return false;
        return true;
      }
      
      @Override public boolean visit(final AnonymousClassDeclaration node) {
        for (final VariableDeclarationFragment d : getFieldsOfClass(node))
          if (d.getName().subtreeMatch(matcher, what))
            return false;
        return true;
      }
      
      @Override public boolean visit(final InfixExpression node) {
        count(node.getRightOperand());
        count(node.getLeftOperand());
        return count(node.extendedOperands());
      }
      
      @Override public boolean visit(final PrefixExpression node) {
        return count(node.getOperand());
      }
      
      @Override public boolean visit(final PostfixExpression node) {
        return count(node.getOperand());
      }
      
      @Override public boolean visit(final ParenthesizedExpression node) {
        return count(node.getExpression());
      }
      
      @Override public boolean visit(final Assignment node) {
        return count(node.getRightHandSide());
      }
      
      @Override public boolean visit(final CastExpression node) {
        return count(node.getExpression());
      }
      
      @Override public boolean visit(final ArrayAccess n) {
        return count(n.getArray());
      }
      
      @Override public boolean visit(final MethodInvocation n) {
        count(n.getExpression());
        return count(n.arguments());
      }
      
      @Override public boolean visit(final ConstructorInvocation node) {
        return count(node.arguments());
      }
      
      @Override public boolean visit(final ClassInstanceCreation node) {
        return count(node.arguments());
      }
      
      @Override public boolean visit(final ArrayCreation node) {
        return count(node.dimensions());
      }
      
      @Override public boolean visit(final ArrayInitializer node) {
        return count(node.expressions());
      }
      
      @Override public boolean visit(final ReturnStatement node) {
        return count(node.getExpression());
      }
      
      @Override public boolean visit(final FieldAccess node) {
        return count(node.getExpression());
      }
      
      @Override public boolean visit(final QualifiedName node) {
        $.addAll(listSingle(node.getQualifier(), what, repeated()));
        return true;
      }
      
      @Override public boolean visit(final VariableDeclarationFragment node) {
        return count(node.getInitializer());
      }
      
      @Override public boolean visit(final IfStatement node) {
        return count(node.getExpression());
      }
      
      @Override public boolean visit(final SwitchStatement node) {
        return count(node.getExpression());
      }
      
      @Override public boolean visit(final ForStatement node) {
        forNesting++;
        return count(node.getExpression());
      }
      
      /**
       * 
       * 
       * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom
       *      .ForStatement)
       * 
       * @param _
       *          ignored
       */
      @Override public void endVisit(final ForStatement _) {
        forNesting--;
      }
      
      @Override public boolean visit(final EnhancedForStatement node) {
        foreachNesting++;
        return count(node.getExpression());
      }
      
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.EnhancedForStatement)
       * @param _
       *          ignored
       */
      @Override public void endVisit(final EnhancedForStatement _) {
        foreachNesting--;
      }
      
      @Override public boolean visit(final WhileStatement node) {
        whileNesting++;
        final Expression expression = node.getExpression();
        return count(expression);
      }
      
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.WhileStatement)
       * @param _
       *          ignored
       */
      @Override public void endVisit(final WhileStatement _) {
        whileNesting--;
      }
      
      /**
       * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.DoStatement)
       */
      @Override public boolean visit(final DoStatement node) {
        doWhileNesting++;
        return count(node.getExpression());
      }
      
      /**
       * (non-Javadoc)
       * 
       * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.DoStatement)
       * @param _
       *          ignored
       */
      @Override public void endVisit(final DoStatement _) {
        doWhileNesting--;
      }
      
      @Override public boolean visit(final InstanceofExpression node) {
        return count(node.getLeftOperand());
      }
      
      private boolean repeated() {
        return semantic && forNesting + foreachNesting + whileNesting + doWhileNesting > 0;
      }
      
      private int whileNesting = 0, doWhileNesting = 0, forNesting = 0, foreachNesting = 0;
    });
    return $;
  }
  
  static final ASTMatcher matcher = new ASTMatcher();
}
