package org.spartan.refactoring.utils;

import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.utils.Utils.in;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

final class UsesCollector extends ASTVisitor {
  private static List<VariableDeclarationFragment> getFieldsOfClass(final ASTNode classNode) {
    final List<VariableDeclarationFragment> $ = new ArrayList<>();
    classNode.accept(new ASTVisitor() {
      @Override public boolean visit(final FieldDeclaration n) {
        $.addAll(n.fragments());
        return false;
      }
    });
    return $;
  }
  private final List<Expression> into;
  private final SimpleName what;
  UsesCollector(final List<Expression> into, final SimpleName what) {
    this.into = into;
    this.what = what;
  }
  @Override public boolean visit(@SuppressWarnings("unused") final AnonymousClassDeclaration n) {
    return false;
  }
  @Override public boolean visit(final Assignment a) {
    return recurse(right(a));
  }
  @Override public boolean visit(final CastExpression e) {
    return recurse(right(e));
  }
  @Override public boolean visit(final ClassInstanceCreation n) {
    recurse(n.getExpression());
    return recurse(n.arguments());
  }
  @Override public boolean visit(final FieldAccess n) {
    return recurse(n.getExpression());
  }
  @Override public boolean visit(final InstanceofExpression e) {
    return recurse(left(e));
  }
  // @Override public boolean visit(final MethodDeclaration n) {
  // /* Now: this is a bit complicated. Java allows declaring methods in
  // * anonymous classes in which the formal parameters hide variables in the
  // * enclosing scope. We don't want to collect them as uses of the variable */
  // for (final Object o : n.parameters())
  // if (((SingleVariableDeclaration) o).getName().subtreeMatch(Search.matcher,
  // what))
  // return false;
  // return true;
  // }
  @Override public boolean visit(final MethodInvocation n) {
    recurse(n.getExpression());
    return recurse(n.arguments());
  }
  private boolean recurse(final List<Expression> es) {
    for (final Expression e : es)
      recurse(e);
    return false;
  }
  @Override public boolean visit(@SuppressWarnings("unused") final PostfixExpression _) {
    return false;
  }
  @Override public boolean visit(final PrefixExpression it) {
    return !in(it.getOperator(), PrefixExpression.Operator.INCREMENT, PrefixExpression.Operator.DECREMENT);
  }
  @Override public boolean preVisit2(final ASTNode node) {
    return !(node instanceof Type);
  }
  @Override public boolean visit(final QualifiedName n) {
    return recurse(n.getQualifier());
  }
  @Override public boolean visit(final SimpleName n) {
    consider(n);
    return false;
  }
  @Override public boolean visit(final VariableDeclarationFragment f) {
    return recurse(f.getInitializer());
  }
  void consider(final SimpleName candidate) {
    if (same(what, candidate))
      into.add(candidate);
  }
  private boolean recurse(final ASTNode e) {
    if (e != null)
      e.accept(new UsesCollector(into, what));
    return false;
  }
}