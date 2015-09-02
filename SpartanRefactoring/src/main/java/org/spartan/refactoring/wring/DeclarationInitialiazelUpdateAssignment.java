package org.spartan.refactoring.wring;

import static org.eclipse.jdt.core.dom.Assignment.Operator.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;

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
public final class DeclarationInitialiazelUpdateAssignment extends Wring.VariableDeclarationFragementAndStatement {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final SimpleName n, final Expression initializer, final Statement nextStatement,
      final TextEditGroup g) {
    if (initializer == null)
      return null;
    final Assignment a = Extract.assignment(nextStatement);
    if (a == null || !same(n, left(a)) || doesUseForbiddenSiblings(f, right(a)))
      return null;
    final Operator o = a.getOperator();
    if (o == ASSIGN)
      return null;
    final Expression secondInitializer = right(a);
    final List<Expression> uses = Search.USES_SEMANTIC.of(n).in(secondInitializer);
    if (uses.size() >= 2 || Search.findsDefinitions(n).in(secondInitializer))
      return null;
    final ASTNode alternateInitializer = alernateInitializer(initializer, secondInitializer, o, n);
    if (alternateInitializer == null)
      return null;
    r.remove(nextStatement, g);
    r.replace(initializer, alternateInitializer, g);
    final List<Expression> in = Search.USES_SEMANTIC.of(n).in(alternateInitializer);
    if (!in.isEmpty())
      r.replace(in.get(0), initializer, g);
    return r;
  }
  private static InfixExpression alernateInitializer(final Expression firstInitializer, final Expression secondInitializer, final Operator o, final SimpleName name) {
    return !Is.sideEffectFree(firstInitializer) && !Search.USES_SEMANTIC.of(name).in(secondInitializer).isEmpty() ? null
        : Subject.pair(firstInitializer, secondInitializer).to(asInfix(o));
  }
  private static InfixExpression.Operator asInfix(final Assignment.Operator o) {
    return o == PLUS_ASSIGN ? PLUS
        : o == MINUS_ASSIGN ? MINUS
            : o == TIMES_ASSIGN ? TIMES
                : o == DIVIDE_ASSIGN ? DIVIDE
                    : o == BIT_AND_ASSIGN ? AND
                        : o == BIT_OR_ASSIGN ? OR
                            : o == BIT_XOR_ASSIGN ? XOR
                                : o == REMAINDER_ASSIGN ? REMAINDER
                                    : o == LEFT_SHIFT_ASSIGN ? LEFT_SHIFT
                                        : o == RIGHT_SHIFT_SIGNED_ASSIGN ? RIGHT_SHIFT_SIGNED : o != RIGHT_SHIFT_UNSIGNED_ASSIGN ? null : RIGHT_SHIFT_UNSIGNED;
  }
  @Override String description(final VariableDeclarationFragment n) {
    return "Consolidate declaration of " + n.getName() + " with its subsequent initialization";
  }
}
