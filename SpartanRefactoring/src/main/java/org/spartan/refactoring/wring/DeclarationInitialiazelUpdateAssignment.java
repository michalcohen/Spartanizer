package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.Assignment.Operator.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.spartan.refactoring.utils.Funcs.*;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>int a;
 * a = 3;</code> into <code>int a = 3;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationInitialiazelUpdateAssignment extends Wring.ReplaceToNextStatement<VariableDeclarationFragment> {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final Statement nextStatement, final TextEditGroup g) {
    final Expression firstInitializer = f.getInitializer();
    if (firstInitializer == null)
      return null;
    final SimpleName name = f.getName();
    if (name == null)
      return null;
    final Assignment a = Extract.assignment(nextStatement);
    if (a == null || !same(name, left(a)))
      return null;
    final Operator o = a.getOperator();
    if (o == ASSIGN)
      return null;
    final Expression secondInitializer = right(a);
    final List<Expression> uses = Occurrences.USES_SEMANTIC.of(name).in(secondInitializer);
    if (uses.size() >= 2 || !Occurrences.ASSIGNMENTS.of(name).in(secondInitializer).isEmpty())
      return null;
    final ASTNode alternateInitializer = alernateInitializer(firstInitializer, secondInitializer, o, name);
    if (alternateInitializer == null)
      return null;
    r.remove(nextStatement, g);
    r.replace(firstInitializer, alternateInitializer, g);
    final List<Expression> in = Occurrences.USES_SEMANTIC.of(name).in(alternateInitializer);
    if (!in.isEmpty())
      r.replace(in.get(0), firstInitializer, g);
    return r;
  }
  private static InfixExpression alernateInitializer(final Expression firstInitializer, final Expression secondInitializer, final Operator o, final SimpleName name) {
    final InfixExpression $ = Subject.pair(firstInitializer, secondInitializer).to(asInfix(o));
    return Is.sideEffectFree(firstInitializer) || Occurrences.USES_SEMANTIC.of(name).in(secondInitializer).isEmpty() ? $ : null;
  }
  private static InfixExpression.Operator asInfix(final Assignment.Operator o) {
    if (o == PLUS_ASSIGN)
      return PLUS;
    return null;
  }
  @Override String description(final VariableDeclarationFragment n) {
    return "Consolidate declaration of " + n.getName() + " with its subsequent initialization";
  }
}