package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.ASTNode.*;
import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Restructure.duplicateInto;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A number of utility functions common to all wrings.
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
public enum Wrings {
  ;
  static void addAllReplacing(final List<Statement> to, final List<Statement> from, final Statement substitute, final Statement by1, final List<Statement> by2) {
    for (final Statement s : from)
      if (s != substitute)
        duplicateInto(s, to);
      else {
        duplicateInto(by1, to);
        duplicateInto(by2, to);
      }
  }
  static boolean degenerateElse(final IfStatement s) {
    return elze(s) != null && emptyElse(s);
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
  static boolean endsWithSequencer(final Statement s) {
    return Is.sequencer(Extract.lastStatement(s));
  }
  static IfStatement invert(final IfStatement s) {
    return Subject.pair(elze(s), then(s)).toNot(s.getExpression());
  }
  static int length(final ASTNode... ns) {
    int $ = 0;
    for (final ASTNode n : ns)
      $ += n.toString().length();
    return $;
  }
  static IfStatement makeShorterIf(final IfStatement s) {
    final Statement then1 = then(s);
    final Statement elze1 = elze(s);
    final IfStatement inverse = invert(s);
    final int rankThen = Wrings.sequencerRank(Extract.lastStatement(then1));
    final int rankElse = Wrings.sequencerRank(Extract.lastStatement(elze1));
    return rankElse > rankThen || rankThen == rankElse && !Wrings.thenIsShorter(s) ? inverse : duplicate(s);
  }
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
  static void replaceWithShorterIf(final IfStatement s, final IfStatement shorterIf, final ASTRewrite r, final TextEditGroup g) {
    final List<Statement> remainder = Extract.statements(elze(shorterIf));
    shorterIf.setElseStatement(null);
    final Block parent = asBlock(s.getParent());
    final Block newParent = s.getAST().newBlock();
    if (parent != null)
      addAllReplacing(newParent.statements(), parent.statements(), s, shorterIf, remainder);
    else {
      newParent.statements().add(shorterIf);
      duplicateInto(remainder, newParent.statements());
    }
    r.replace(s, newParent, g);
  }
  static boolean shoudlInvert(final IfStatement s) {
    final int rankThen = sequencerRank(Extract.lastStatement(then(s)));
    final int rankElse = sequencerRank(Extract.lastStatement(elze(s)));
    return rankElse > rankThen || rankThen == rankElse && !Wrings.thenIsShorter(s);
  }
  static boolean thenIsShorter(final IfStatement s) {
    final Statement then = then(s);
    final Statement elze = elze(s);
    if (elze == null)
      return true;
    final int n1 = Extract.statements(then).size();
    final int n2 = Extract.statements(elze).size();
    if (n1 < n2)
      return true;
    final IfStatement $ = invert(s);
    if (n1 > n2)
      return false;
    assert n1 == n2;
    return positivePrefixLength($) >= positivePrefixLength(invert($));
  }
  private static int positivePrefixLength(final IfStatement $) {
    return Wrings.length($.getExpression(), then($));
  }
  private static int sequencerRank(final ASTNode s) {
    switch (s.getNodeType()) {
      default:
        return -1;
      case BREAK_STATEMENT:
        return 0;
      case CONTINUE_STATEMENT:
        return 1;
      case RETURN_STATEMENT:
        return 2;
      case THROW_STATEMENT:
        return 3;
    }
  }
}
