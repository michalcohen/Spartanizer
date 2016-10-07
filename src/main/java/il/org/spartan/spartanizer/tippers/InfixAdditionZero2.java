package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.Utils.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.PLUS;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import static il.org.spartan.spartanizer.ast.navigate.step.*;

import static il.org.spartan.spartanizer.ast.navigate.extract.*;

import il.org.spartan.plugin.PreferencesResources.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.tipping.*;

/** A {@link Tipper} to convert an expression such as
 *
 * <pre>
 * 0 + X = X
 * </pre>
 *
 * or
 *
 * <pre>
 * X + 0 = X
 * </pre>
 *
 * to
 *
 * <pre>
 * X
 * </i>
 * or
 * <pre>
 * X + 0 + Y
 * </pre>
 *
 * to
 *
 * <pre>
 * X + Y
 * </pre>
 *
 * @author Matteo Orrù
 * @since 2016 */
public final class InfixAdditionZero2 extends ReplaceCurrentNode<InfixExpression> implements TipperCategory.InVain {
  
  @Override public ASTNode replacement(final InfixExpression ¢) {
    System.out.println("left(¢): " + left(¢));
    System.out.println("right(¢): " + right(¢));
    System.out.println(extract.allOperands(¢));
    List<Expression> ops = extract.allOperands(¢);
    for(int i=0; i<ops.size(); i++){
      System.out.println(ops);
      if(iz.literal0(ops.get(i))){
        ops.remove(i);
      }      
    }
    if(ops.size() == 1)
      return ops.get(0);
    return null;
//    for(int j = 0; j < ops.size(); j++){
//       subject.pair(ops.get(j), ops.get(j+1)).to(Operator.PLUS);
//     }
//     
//     return iz.infixPlus(¢) ? null : null;
  }
  
  
  @Override public boolean prerequisite(final InfixExpression $) {
    return $ != null && iz.infixPlus($); // && IsSimpleMultiplication(left($)) && IsSimpleMultiplication(right($));
  }
  /**
   * @param allOperands
   * @return
   */
  private Object replacement(List<Expression> allOperands) {
    return null;
  }

  private static List<Expression> gather(final Expression x, final List<Expression> $) {
    if (x instanceof InfixExpression)
      return gather(az.infixExpression(x), $);
    $.add(x);
    return $;
  }

  private static List<Expression> gather(final InfixExpression ¢) {
    return gather(¢, new ArrayList<Expression>());
  }

  private static List<Expression> gather(final InfixExpression x, final List<Expression> $) {
    System.out.println("x:" + x);
    if (x == null)
      return $;
    System.out.println(x.getOperator());
    System.out.println(in(x.getOperator(), PLUS, MINUS));
    System.out.println(in(x.getOperator(), PLUS));
    if (!in(x.getOperator(), PLUS, MINUS)) {
      $.add(x);
      return $;
    }
    gather(core(left(x)), $);
    gather(core(right(x)), $);
    if (x.hasExtendedOperands())
      gather(extendedOperands(x), $);
    return $;
  }

  private static List<Expression> gather(final List<Expression> xs, final List<Expression> $) {
    for (final Expression ¢ : xs)
      gather(¢, $);
    return $;
  }

  @Override public String description() {
    return "remove 0 in X + 0 expressions from ";
  }

  @Override public String description(final InfixExpression ¢) {
    return description() + ¢;
  }

//  @Override public Tip tip(final InfixExpression x, final ExclusionManager exclude) {
//    System.out.println("exclude: " + exclude);
//    System.out.println("x: " + x);
//    final List<Expression> es = gather(x);
//    System.out.println("es.size(): " + es.size());
//    for(Expression exp: es)
//      System.out.println(exp);
//    if (es.size() < 2)
//      return null;
//    final int n = minus.level(es);
//    if (n == 0 || n == 1 && minus.level(first(es)) == 1)
//      return null;
//    if (exclude != null)
//      exclude.exclude(x);
//    return new Tip(description(x), x, this.getClass()) {
//      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
//        final Expression first = n % 2 == 0 ? null : es.get(0);
//        for (final Expression ¢ : es)
//          if (¢ != first && minus.level(¢) > 0)
//            r.replace(¢, plant(duplicate.of(minus.peel(¢))).into(¢.getParent()), g);
//        if (first != null)
//          r.replace(first, plant(subject.operand(minus.peel(first)).to(PrefixExpression.Operator.MINUS)).into(first.getParent()), g);
//      }
//    };
//  }

  @Override public TipperGroup tipperGroup() {
    return TipperGroup.Abbreviation;
  }
}