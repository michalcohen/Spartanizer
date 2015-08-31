package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Restructure.duplicateInto;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.LiteralParser;
import org.spartan.refactoring.utils.Subject;

/**
 * A number of utility functions common to all wrings.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public enum Wrings {
  ;
  static boolean mixedLiteralKind(final List<Expression> es) {
    if (es.size() <= 2)
      return false;
    int previousKind = -1;
    for (final Expression e : es)
      if (e instanceof NumberLiteral || e instanceof CharacterLiteral) {
        final int currentKind = new LiteralParser(e.toString()).kind();
        assert currentKind >= 0;
        if (previousKind == -1)
          previousKind = currentKind;
        else if (previousKind != currentKind)
          return true;
      }
    return false;
  }
  static Expression eliminateLiteral(final InfixExpression e, final boolean b) {
    final List<Expression> operands = Extract.allOperands(e);
    removeAll(b, operands);
    switch (operands.size()) {
      case 0:
        return e.getAST().newBooleanLiteral(b);
      case 1:
        return duplicate(operands.get(0));
      default:
        return Subject.operands(operands).to(e.getOperator());
    }
  }
  static boolean emptyElse(final IfStatement s) {
    return Extract.statements(elze(s)).size() == 0;
  }
  static boolean emptyThen(final IfStatement s) {
    return Extract.statements(then(s)).size() == 0;
  }
  static boolean degenerateElse(final IfStatement s) {
    return elze(s) != null && emptyElse(s);
  }
  static int length(final ASTNode... ns) {
    int $ = 0;
    for (final ASTNode n : ns)
      $ += n.toString().length();
    return $;
  }
  static ASTRewrite replaceTwoStatements(final ASTRewrite r, final Statement what, final Statement by, final TextEditGroup g) {
    final Block parent = asBlock(what.getParent());
    final List<Statement> siblings = Extract.statements(parent);
    final int i = siblings.indexOf(what);
    siblings.remove(i);
    siblings.remove(i);
    siblings.add(i, by);
    final Block $ = parent.getAST().newBlock();
    duplicateInto(siblings, $.statements());
    r.replace(parent, $, g);
    return r;
  }
}
