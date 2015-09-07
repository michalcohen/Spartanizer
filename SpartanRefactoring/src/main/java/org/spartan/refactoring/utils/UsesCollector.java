package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.utils.Utils.in;

import java.util.List;

import org.eclipse.jdt.core.dom.*;

abstract class HidingDepth extends ASTVisitor {
  private int depth = 0;
  private int hideDepth = Integer.MAX_VALUE;
  @Override public final void endVisit(@SuppressWarnings("unused") final AnnotationTypeDeclaration _) {
    class A {
      int f() {
        return f();
      }
    }
    pop();
  }
  @Override public final void endVisit(@SuppressWarnings("unused") final AnonymousClassDeclaration _) {
    pop();
  }
  @Override public final void endVisit(@SuppressWarnings("unused") final TypeDeclaration _) {
    pop();
  }
  @Override public final void endVisit(@SuppressWarnings("unused") final Block _) {
    pop();
  }
  @Override public final void endVisit(@SuppressWarnings("unused") final EnhancedForStatement _) {
    pop();
  }
  @Override public final void endVisit(@SuppressWarnings("unused") final ForStatement _) {
    pop();
  }
  @Override public final boolean visit(final TypeDeclaration d) {
    push();
    return go(d);
  }
  @Override public final boolean visit(final AnnotationTypeDeclaration d) {
    push();
    return go(d);
  }
  @Override public final boolean visit(final AnonymousClassDeclaration d) {
    push();
    return go(d);
  }
  @Override public boolean visit(final Block node) {
    return push();
  }
  @Override public boolean visit(final EnhancedForStatement s) {
    push();
    return go(s);
  }
  @Override public boolean visit(final ForStatement node) {
    return push();
  }
  abstract boolean go(final AbstractTypeDeclaration d);
  abstract boolean go(final AnonymousClassDeclaration d);
  abstract boolean go(EnhancedForStatement s);
  boolean hidden() {
    return depth >= hideDepth;
  }
  void hide() {
    hideDepth = depth;
  }
  void pop() {
    if (--depth < hideDepth)
      hideDepth = Integer.MAX_VALUE;
  }
  boolean push() {
    ++depth;
    return !hidden();
  }
}

final class UsesCollector extends HidingDepth {
  private final class DeclaredInFields extends ASTVisitor {
    private final ASTNode parent;
    DeclaredInFields(final ASTNode parent) {
      this.parent = parent;
    }
    @Override public boolean visit(final FieldDeclaration f) {
      return f.getParent() == parent && !hidden() && !declaredIn(f);
    }
  }
  private final List<Expression> result;
  private final SimpleName focus;
  UsesCollector(final List<Expression> result, final SimpleName focus) {
    this.result = result;
    this.focus = focus;
  }
  @Override public boolean preVisit2(final ASTNode node) {
    return !hidden() && !(node instanceof Type);
  }
  @Override public boolean visit(final Assignment a) {
    return recurse(right(a));
  }
  @Override public boolean visit(final CastExpression e) {
    return recurse(right(e));
  }
  @Override public boolean visit(final EnhancedForStatement s) {
    push();
    return declaredIn(s.getParameter());
  }
  @Override public final boolean visit(final EnumDeclaration d) {
    push();
    return recurse(d.bodyDeclarations());
  }
  @Override public boolean visit(final FieldAccess n) {
    return recurse(n.getExpression());
  }
  @Override public boolean visit(final MethodDeclaration n) {
    return !declaredIn(n) && recurse(n.getBody());
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
  boolean declaredIn(final FieldDeclaration f) {
    for (final Object o : f.fragments())
      if (declaredIn((VariableDeclarationFragment) o))
        return true;
    return false;
  }
  @Override boolean go(final AbstractTypeDeclaration d) {
    return !declaredIn(d) && recurse(d.bodyDeclarations());
  }
  boolean go(final AnnotationTypeDeclaration d) {
    return !declaredIn(d) && recurse(d.bodyDeclarations());
  }
  @Override boolean go(final AnonymousClassDeclaration d) {
    return !declaredIn(d) && recurse(d.bodyDeclarations());
  }
  @Override boolean go(final EnhancedForStatement s) {
    return !declaredIn(s) && recurse(s.getBody());
  }
  private boolean declaredIn(final EnhancedForStatement s) {
    return declaredIn(s.getParameter());
  }
  private boolean declaredBy(final SimpleName n) {
    if (!hit(n))
      return false;
    hide();
    return true;
  }
  private boolean declaredIn(final AbstractTypeDeclaration d) {
    d.accept(new ASTVisitor() {
      @Override public boolean visit(final FieldDeclaration f) {
        return !hidden() && !declaredIn(f);
      }
    });
    return hidden();
  }
  private boolean declaredIn(final AnonymousClassDeclaration d) {
    declaresField(d);
    return hidden();
  }
  private void declaresField(final ASTNode n) {
    n.accept(new DeclaredInFields(n));
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
  private boolean declaredIn(final VariableDeclarationFragment f) {
    return declaredBy(f.getName());
  }
  private boolean hit(final SimpleName name) {
    return same(name, focus);
  }
  private boolean recurse(final ASTNode e) {
    if (e != null && !hidden())
      e.accept(new UsesCollector(result, focus));
    return false;
  }
  private boolean recurse(final List<ASTNode> ns) {
    for (final ASTNode n : ns)
      recurse(n);
    return false;
  }
}