package org.spartan.refactoring.spartanizations;

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
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Is;
import org.spartan.refactoring.utils.Subject;
import org.spartan.utils.Range;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 */
@Deprecated
public class ComparisonWithBoolean extends Spartanization {
  /** Instantiates this class */
  public ComparisonWithBoolean() {
    super("Comparison With Boolean", "Eliminate reduntant comparison to boolean constant");
  }
  @Override protected final void fillRewrite(final ASTRewrite r, @SuppressWarnings("unused") final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression n) {
        if (!inRange(m, n) || n.getOperator() != Operator.EQUALS && n.getOperator() != Operator.NOT_EQUALS)
          return true;
        ASTNode nonliteral;
        BooleanLiteral literal;
        if (Is.booleanLiteral(n.getRightOperand()) && !Is.booleanLiteral(n.getLeftOperand())) {
          nonliteral = r.createMoveTarget(n.getLeftOperand());
          literal = (BooleanLiteral) n.getRightOperand();
        } else {
          if (!Is.booleanLiteral(n.getLeftOperand()) && !Is.booleanLiteral(n.getRightOperand()))
            return true;
          nonliteral = r.createMoveTarget(n.getRightOperand());
          literal = (BooleanLiteral) n.getLeftOperand();
        }
        r.replace(n, literal.booleanValue() && n.getOperator() == Operator.EQUALS || !literal.booleanValue() && n.getOperator() == Operator.NOT_EQUALS ? nonliteral
            : Subject.operand((Expression) nonliteral).to(PrefixExpression.Operator.NOT), null);
        return true;
      }
    });
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression n) {
        if (n.getOperator() != Operator.EQUALS && n.getOperator() != Operator.NOT_EQUALS)
          return true;
        if (ASTNode.BOOLEAN_LITERAL == n.getRightOperand().getNodeType() || ASTNode.BOOLEAN_LITERAL == n.getLeftOperand().getNodeType())
          $.add(new Range(n));
        return true;
      }
    };
  }
}
