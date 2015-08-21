package org.spartan.refactoring.wring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class Toolbox {
  private static <N extends ASTNode> Wring<N> find(final N n, final List<Wring<N>> ws) {
    for (final Wring<N> w : ws)
      if (w.scopeIncludes(n))
        return w;
    return null;
  }
  private final Map<Class<? extends ASTNode>, List<Object>> inner = new HashMap<>();
  /**
   * Find the first {@link Wring} appropriate for an {@link ASTNode}
   *
   * @param n JD
   * @return the first {@link Wring} for which the parameter is within scope, or
   *         <code><b>null</b></code>i if no such {@link Wring} is found.
   */
  public <N extends ASTNode> Wring<N> find(final N n) {
    return find(n, get(n));
  }
  @SuppressWarnings("unchecked") <N extends ASTNode> List<Wring<N>> get(final Class<? extends ASTNode> c) {
    if (!inner.containsKey(c))
      inner.put(c, new ArrayList<>());
    return (List<Wring<N>>) (List<?>) inner.get(c);
  }
  <N extends ASTNode> List<Wring<N>> get(final N n) {
    return get(n.getClass());
  }
  public static final Toolbox instance = new Maker()//
      .add(Block.class, new BlockSimplify()) //
      .add(InfixExpression.class, //
          new InfixAdditionSort(), //
          new InfixComparisonBooleanLiteral(), //
          new InfixComparisonSpecific(), //
          new InfixComparisonShortestFirst(), //
          new InfixConditionalAndTrue(), //
          new InfixConditionalOrFalse(), //
          new InfixMultiplicationSort(), //
          new InfixPseudoAdditionSort(), //
          null)
      .add(VariableDeclarationFragment.class, //
          new DeclarationAssignment(), //
          new DeclarationIfAssginment(), //
          new DeclarationReturn(), //
          null) //
      .add(IfStatement.class, //
          new IfReturnNoElseReturn(), //
          new IfSomethingNoElse(), //
          new IfReturnFooElseReturnBar(), //
          new IfAssignToFooElseAssignToFoo(), //
          new IfCommandsSequencerElseSomething(), //
          new IfCommandsSequencerIfSameCommandsSequencer(), //
          new IfShortestFirst(), //
          new IfThrowFooElseThrowBar(), //
          null)//
      .add(PrefixExpression.class, new PrefixNotPushdown()) //
      .add(ConditionalExpression.class, //
          new TernaryBooleanLiteral(), //
          new TernaryCollapse(), //
          new TernaryEliminate(), //
          new TernaryShortestFirst(), //
          new TernaryPushdown(), //
          null) //
      .seal();

  public static class Maker extends Toolbox {
    @SafeVarargs public final <N extends ASTNode> Maker add(final Class<N> c, final Wring<N>... ws) {
      final List<Wring<N>> l = get(c);
      for (final Wring<N> w : ws)
        if (w != null)
          l.add(w);
        else
          break;
      return this;
    }
    public Toolbox seal() {
      return this;
    }
  }
}
