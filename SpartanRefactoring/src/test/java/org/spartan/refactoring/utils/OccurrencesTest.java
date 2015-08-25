package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.MatcherAssert.iz;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.spartanizations.Wrap;

/**
 * @author Yossi Gil
 * @since 2014-08-25
 */
@SuppressWarnings({ "javadoc" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class OccurrencesTest {
  private final String from = "int a = 2,b; if (a+b) a =3;";
  private final String wrap = Wrap.Statement.on(from);
  private final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(wrap);
  private final SimpleName a = Extract.firstVariableDeclarationFragment(u).getName();
  private final VariableDeclarationStatement ab = (VariableDeclarationStatement) a.getParent().getParent();
  private final SimpleName b = ((VariableDeclarationFragment) ab.fragments().get(1)).getName();
  private final IfStatement s = Extract.nextIfStatement(a);
  private final InfixExpression e = (InfixExpression) s.getExpression();
  @Test public void correctSettings() {
    assertThat(ab, iz("int a=2,b;"));
    assertThat(b.toString(), is("b"));
    assertThat(s, is(Extract.firstIfStatement(u)));
    assertThat(s, iz("if (a + b) a=3;"));
    assertThat(e, iz("a + b"));
  }
  @Test public void lexicalUsesCollector() {
    final List<Expression> into = new ArrayList<>();
    final ASTVisitor collector = Occurrences.lexicalUsesCollector(into, a);
    a.accept(collector);
    assertThat(into.size(),is(1));
  }
  @Test public void occurencesAinE() {
    assertThat(Occurrences.BOTH_SEMANTIC.of(a).in(e).size(), is(1));
  }
  @Test public void occurencesAinLeftOfE() {
    assertThat(Occurrences.BOTH_SEMANTIC.of(a).in(left(e)).size(), is(1));
  }
  @Test public void occurencesAinAL() {
    assertThat(Occurrences.BOTH_SEMANTIC.of(a).in(a).size(), is(1));
  }
  @Test public void occurencesAinAsame() {
    assertThat(same(a, a), is(true));
  }
  @Test public void occurencesAinLeftOfEsame() {
    assertThat(same(left(e), a), is(true));
  }
  @Test public void exploreLeftOfE() {
    assertThat(left(e), iz("a"));
  }
  @Test public void sameAandLeftOfE() {
    assertThat(same(a, left(e)), is(true));
  }
  @Test public void sameTypeAandLeftOfE() {
    assertThat(a, instanceOf(left(e).getClass()));
  }
  @Test public void occurencesAinRightOfE() {
    assertThat(Occurrences.BOTH_SEMANTIC.of(a).in(right(e)).size(), is(0));
  }
  @Test public void occurencesBinRightOfE() {
    assertThat(Occurrences.BOTH_SEMANTIC.of(b).in(right(e)).size(), is(1));
  }
  @Test public void occurencesBinE() {
    assertThat(Occurrences.BOTH_SEMANTIC.of(b).in(e).size(), is(1));
  }
}
