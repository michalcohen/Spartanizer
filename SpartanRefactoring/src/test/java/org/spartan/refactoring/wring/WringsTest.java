package org.spartan.refactoring.wring;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Into.es;
import static org.spartan.refactoring.wring.Wrings.mixedLiteralKind;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.junit.Test;
import org.spartan.refactoring.spartanizations.Wrap;
import org.spartan.refactoring.utils.*;

@SuppressWarnings({ "javadoc", "static-method" }) public class WringsTest {
  @Test public void mixedLiteralKindEmptyList() {
    assertThat(mixedLiteralKind(es()), is(false));
  }
  @Test public void mixedLiteralKindSingletonList() {
    assertThat(mixedLiteralKind(es("1")), is(false));
  }
  @Test public void mixedLiteralKindnPairList() {
    assertThat(mixedLiteralKind(es("1", "1.0")), is(false));
  }
  @Test public void mixedLiteralKindnTripleList() {
    assertThat(mixedLiteralKind(es("1", "1.0", "a")), is(true));
  }
  @Test public void inlineExpressionWithSideEffect() {
    final Expression e = Into.e("f()");
    assertThat(Is.sideEffectFree(e), is(false));
    final String input = "int a = f(); return a += 2 * a;";
    final CompilationUnit u = Wrap.Statement.intoCompilationUnit(input);
    final VariableDeclarationFragment f = Extract.firstVariableDeclarationFragment(u);
    assertThat(f, iz("a=f()"));
    final SimpleName n = f.getName();
    assertThat(n, iz("a"));
    final Expression initializer = f.getInitializer();
    assertThat(initializer, iz("f()"));
    assertThat(Is.sideEffectFree(initializer), is(false));
    final ASTNode parent = f.getParent();
    assertThat(parent, iz("int a = f();"));
    final ASTNode block = parent.getParent();
    assertThat(block, iz("{int a = f(); return a += 2*a;}"));
    final ReturnStatement returnStatement = (ReturnStatement) ((Block) block).statements().get(1);
    assertThat(returnStatement, iz("return a += 2 *a;"));
    final Assignment a = (Assignment) returnStatement.getExpression();
    final Operator o = a.getOperator();
    assertThat(o, iz("+="));
    final InfixExpression alternateInitializer = Subject.pair(left(a), right(a)).to(Wring.VariableDeclarationFragementAndStatement.asInfix(o));
    assertThat(alternateInitializer, iz("a + 2 * a"));
    assertThat(Search.findsDefinitions(n).in(alternateInitializer), is(false)); // &&
    assertThat(Is.sideEffectFree(initializer), is(false));
    assertThat(Search.forAllOccurencesOf(n).in(alternateInitializer).size(), is(2));
    assertThat(new LocalInliner(n).byValue(initializer).canInlineInto(alternateInitializer), is(false));
  }
}
