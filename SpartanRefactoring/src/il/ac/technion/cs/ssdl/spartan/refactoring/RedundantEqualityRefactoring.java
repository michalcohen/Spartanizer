package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <boris.van.sosin@gmail.com> (v2)
 * 
 * 
 */
public class RedundantEqualityRefactoring extends BaseRefactoring {
  /** Instantiates this class */
  public RedundantEqualityRefactoring() {
    super("Redundant Equality", "Convert reduntant comparison to boolean constant");
  }
  
  @Override public String getName() {
    return "Remove redundant comparison to a boolean literal";
  }
  
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression n) {
        if (!inRange(m, n))
          return true;
        if (n.getOperator() != Operator.EQUALS && n.getOperator() != Operator.NOT_EQUALS)
          return true;
        ASTNode nonliteral = null;
        BooleanLiteral literal = null;
        if (n.getRightOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
            && n.getLeftOperand().getNodeType() != ASTNode.BOOLEAN_LITERAL) {
          nonliteral = r.createMoveTarget(n.getLeftOperand());
          literal = (BooleanLiteral) n.getRightOperand();
        } else if (n.getLeftOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
            && n.getRightOperand().getNodeType() != ASTNode.BOOLEAN_LITERAL) {
          nonliteral = r.createMoveTarget(n.getRightOperand());
          literal = (BooleanLiteral) n.getLeftOperand();
        } else
          return true;
        ASTNode newnode = null;
        if (literal.booleanValue() && n.getOperator() == Operator.EQUALS || !literal.booleanValue()
            && n.getOperator() == Operator.NOT_EQUALS)
          newnode = nonliteral;
        else {
          final ParenthesizedExpression paren = t.newParenthesizedExpression();
          paren.setExpression((Expression) nonliteral);
          newnode = t.newPrefixExpression();
          ((PrefixExpression) newnode).setOperand(paren);
          ((PrefixExpression) newnode).setOperator(PrefixExpression.Operator.NOT);
        }
        r.replace(n, newnode, null);
        return true;
      }
    });
  }
  
  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression n) {
        if (n.getOperator() != Operator.EQUALS && n.getOperator() != Operator.NOT_EQUALS)
          return true;
        if (n.getRightOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
            || n.getLeftOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL)
          opportunities.add(new Range(n));
        return true;
      }
    };
  }
}
