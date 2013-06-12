package il.ac.technion.cs.ssdl.spartan.refactoring;

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

public enum VariableCounter {
  USES_SEMANTIC {
    @Override public List<Expression> list(final ASTNode n, final Expression e) {
      return countUses(n, e, true);
    }
  },
  
  USES_LEXICAL {
	@Override
	public List<Expression> list(ASTNode n, Expression e) {
		return countUses(n, e, false);
	}
  },
  
  ASSIGNMENTS {
    @Override public List<Expression> list(final ASTNode n, final Expression e) {
      final List<Expression> $ = new ArrayList<Expression>();
      n.accept(new ASTVisitor() {
        @Override public boolean visit(final AnonymousClassDeclaration node) {
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
  BOTH_SEMANTIC {
    @Override public List<Expression> list(final ASTNode n, final Expression e) {
      final List<Expression> $ = new ArrayList<Expression>(USES_SEMANTIC.list(n, e));
      addAssignments($, n, e);
      return $;
    }
  },
  BOTH_LEXICAL {
	@Override
	public List<Expression> list(ASTNode n, Expression e) {
	  final List<Expression> $ = new ArrayList<Expression>(USES_LEXICAL.list(n, e));
	  addAssignments($, n, e);
	  return $;
	}
};

  public abstract List<Expression> list(ASTNode n, Expression e);
  
  static void addAssignments(final List<Expression> $, final ASTNode n,	final Expression e) {
	$.addAll(ASSIGNMENTS.list(n, e));
      Collections.sort($, new Comparator<Expression>() {
        @Override
		public int compare(final Expression e1, final Expression e2) {
          return e1.getStartPosition() - e2.getStartPosition();
        }
      });
  }

static List<Expression> listSingle(final Expression e1, Expression e2, boolean repeated) {
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
      @Override
	  public boolean visit(final FieldDeclaration node) {
        $.addAll(node.fragments());
        return false;
      }
    });
    return $;
  }
  
  static List<Expression> countUses(final ASTNode n, final Expression e, final boolean semantic) {
	  final List<Expression> $ = new ArrayList<Expression>();
      n.accept(new ASTVisitor() {
          @Override public boolean visit(final MethodDeclaration node) {
            /*
             * Now: this is a bit complicated. Java allows declaring methods in
             * anonymous classes in which the formal parameters hide variables in
             * the enclosing scope. We don't want to count them as uses of the
             * varaible
             */
            for (final Object obj : node.parameters()) {
              if (((SingleVariableDeclaration) obj).getName().subtreeMatch(matcher, e))
                return false;
            }
            return true;
          }
          
          @Override public boolean visit(final AnonymousClassDeclaration node) {
            for (final VariableDeclarationFragment decl : getFieldsOfClass(node)) {
              if (decl.getName().subtreeMatch(matcher, e))
                return false;
            }
            return true;
          }
          
          @Override public boolean visit(final InfixExpression node) {
            $.addAll(listSingle(node.getRightOperand(), e, repeated()));
            $.addAll(listSingle(node.getLeftOperand(), e, repeated()));
            for (final Object item : node.extendedOperands())
              $.addAll(listSingle((Expression) item, e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final PrefixExpression node) {
            $.addAll(listSingle(node.getOperand(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final PostfixExpression node) {
            $.addAll(listSingle(node.getOperand(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(ParenthesizedExpression node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final Assignment node) {
            $.addAll(listSingle(node.getRightHandSide(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final CastExpression node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ArrayAccess node) {
            $.addAll(listSingle(node.getArray(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final MethodInvocation node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            for (final Object arg : node.arguments())
              $.addAll(listSingle((Expression) arg, e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ConstructorInvocation node) {
            for (final Object arg : node.arguments())
              $.addAll(listSingle((Expression) arg, e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ClassInstanceCreation node) {
            for (final Object arg : node.arguments())
              $.addAll(listSingle((Expression) arg, e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ArrayCreation node) {
            for (final Object dim : node.dimensions())
              $.addAll(listSingle((Expression) dim, e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ArrayInitializer node) {
            for (final Object item : node.expressions())
              $.addAll(listSingle((Expression) item, e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ReturnStatement node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final FieldAccess node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final QualifiedName node) {
            $.addAll(listSingle(node.getQualifier(), e, repeated()));
            return true;
          }
          
          @Override
  		public boolean visit(final VariableDeclarationFragment node) {
            $.addAll(listSingle(node.getInitializer(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final IfStatement node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final SwitchStatement node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override public boolean visit(final ForStatement node) {
        	forNesting += 1;
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            return true;
          }
          
          @Override
          public void endVisit(final ForStatement node) {
        	  forNesting -= 1;
          }
          
          @Override public boolean visit(final EnhancedForStatement node) {
            $.addAll(listSingle(node.getExpression(), e, repeated()));
            foreachNesting += 1;
            return semantic;
          }
          
          @Override
          public void endVisit(final EnhancedForStatement node) {
        	  foreachNesting -= 1;
          }

          @Override public boolean visit(final WhileStatement node) {
        	  whileNesting += 1;
        	  $.addAll(listSingle(node.getExpression(), e, repeated()));
        	  return true;
          }
          
          @Override
          public void endVisit(final WhileStatement node) {
        	  whileNesting -= 1;
          }
          
          @Override public boolean visit(final DoStatement node) {
            doWhileNesting += 1;
        	$.addAll(listSingle(node.getExpression(), e, repeated()));
        	return true;
          }
          
          @Override
          public void endVisit(final DoStatement node) {
        	  doWhileNesting -= 1;
          }
          
          @Override public boolean visit(final InstanceofExpression node) {
            $.addAll(listSingle(node.getLeftOperand(), e, repeated()));
            return true;
          }
          
          private boolean repeated() {
        	  return semantic && (forNesting + foreachNesting + whileNesting + doWhileNesting > 0);
          }
          
          private int whileNesting=0, doWhileNesting=0, forNesting=0, foreachNesting=0;
        });
      return $;
  }
  
  static final ASTMatcher matcher = new ASTMatcher();
}
