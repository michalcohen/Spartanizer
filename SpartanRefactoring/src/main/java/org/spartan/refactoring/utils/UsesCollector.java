package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.jdt.core.dom.*;

abstract class HidingDepth extends ASTVisitor {
  private int depth = 0;
  private int hideDepth = Integer.MAX_VALUE;
  void hide() {
    hideDepth = depth;
  }
  boolean hidden() {
    return depth >= hideDepth;
  }
  boolean push() {
    ++depth;
    return !hidden();
  }
  void pop() {
    if (--depth < hideDepth)
      hideDepth = Integer.MAX_VALUE;
  }
}

final class UsesCollector extends HidingDepth {
  private final List<Expression> result;
  private final SimpleName focus;
  UsesCollector(final List<Expression> result, final SimpleName focus) {
    this.result = result;
    this.focus = focus;
  }
  @Override public void endVisit(@SuppressWarnings("unused") final Block _) {
    pop();
  }
  @Override public boolean preVisit2(final ASTNode node) {
    return !hidden() && !(node instanceof Type);
  }
  @Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration n) {
    // Placeholder: we may want to examine fields.
    return true;
  }
  @Override public boolean visit(final Assignment a) {
    return recurse(right(a));
  }
  @Override public boolean visit(final Block node) {
    return push();
  }
  @Override public boolean visit(final EnhancedForStatement s) {
    return declaredIn(s.getParameter());
  }
  @Override public boolean visit(final CastExpression e) {
    return recurse(right(e));
  }
  @Override public boolean visit(final FieldAccess n) {
    return recurse(n.getExpression());
  }
  @Override public boolean visit(final MethodDeclaration n) {
    return !declaredIn(n);
  }
  @Override public boolean visit(final ForStatement node) {
    return push();
  }
  @Override public void endVisit(final ForStatement node) {
    pop();
  }
  @Override public boolean visit(final MethodInvocation n) {
    recurse(n.getExpression());
    return recurse(n.arguments());
  }
  @Override public boolean visit(@SuppressWarnings("unused") final PostfixExpression _) {
    return false;
  }
  @Override public boolean visit(final PrefixExpression it) {
    return !in(it.getOperator(), PrefixExpression.Operator.INCREMENT, PrefixExpression.Operator.DECREMENT);
  }
  @Override public boolean visit(final QualifiedName n) {
    return recurse(n.getQualifier());
  }
  @Override public boolean visit(final SimpleName n) {
    consider(n);
    return false;
  }
  @Override public boolean visit(final VariableDeclarationFragment f) {
    return !declaredIn(f) && recurse(f.getInitializer());
  }
  void consider(final SimpleName candidate) {
    if (hit(candidate))
      result.add(candidate);
  }
  private boolean declaredIn(final MethodDeclaration n) {
    for (final Object o : n.parameters())
      if (declaredIn((SingleVariableDeclaration) o))
        return true;
    return false;
  }
  private boolean declaredIn(final SingleVariableDeclaration f) {
    return declaredBy(f.getName());
  }
  private boolean declaredBy(final SimpleName n) {
    if (!hit(n))
      return false;
    hide();
    return true;
  }
  private boolean declaredIn(final VariableDeclarationFragment f) {
    return declaredBy(f.getName());
  }
  private boolean hit(final SimpleName name) {
    return same(name, focus);
  }
  private boolean recurse(final ASTNode e) {
    if (e != null)
      e.accept(new UsesCollector(result, focus));
    return false;
  }
  private boolean recurse(final List<Expression> es) {
    for (final Expression e : es)
      recurse(e);
    return false;
  }
}