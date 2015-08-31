package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.left;
import static org.spartan.refactoring.utils.Funcs.right;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Funcs.then;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.spartanizations.TESTUtils;
import org.spartan.refactoring.spartanizations.Wrap;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Occurrences;
import org.spartan.refactoring.utils.Rewrite;
import org.spartan.refactoring.utils.Subject;
import org.spartan.utils.Utils;

@SuppressWarnings({ "javadoc" }) //
@RunWith(Parameterized.class) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class DeclarationIfAssignmentWringedTest extends AbstractWringTest<VariableDeclarationFragment> {
  final static DeclarationIfAssginment WRING = new DeclarationIfAssginment();
  /** Description of a test case for {@link Parameter} annotation */
  protected static final String DESCRIPTION = "Test #{index}. ({0}) \"{1}\" ==> \"{2}\"";
  private static String[][] cases = Utils.asArray(//
      new String[] { "Vanilla with newline", "int a = 2; \n if (b) a =3;", "int a= b?3:2;" }, //
      new String[] { "Empty else", "int a=2; if (x) a = 3; else ;", " int a = x ? 3 : 2;" }, //
      new String[] { "Vanilla", "int a = 2; if (b) a =3;", "int a= b?3:2;" }, //
      new String[] { "Empty nested else", "int a=2; if (x) a = 3; else {{{}}}", " int a = x ? 3 : 2;" }, //
      new String[] { "Two fragments", //
          "int n2 = 0, n3;" + //
              "  if (d)\n" + //
              "    n2 = 2;", //
          "int n2 = d ? 2 : 0, n3;" },
      null);
  /**
   * Generate test cases for this parameterized class.
   *
   * @return a collection of cases, where each case is an array of three
   *         objects, the test case name, the input, and the file.
   */
  // TODO: JUnit bug: gets confused when value contains new line characters:
  // @Parameters(name = "Test #{index}. ({0}) \"{1}\" ==> \"{2}\"") //
  @Parameters(name = "Test #{index}. ({0}) ") //
  public static Collection<Object[]> cases() {
    return collect(cases);
  }
  /** What should the output be */
  @Parameter(2) public String expected;
  /** Instantiates the enclosing class ({@link Wringed}) */
  public DeclarationIfAssignmentWringedTest() {
    super(WRING);
  }
  DeclarationIfAssignmentWringedTest(final Wring<VariableDeclarationFragment> inner) {
    super(inner);
  }
  @Test public void correctSimplifier() {
    assertThat(asMe().toString(), Toolbox.instance.find(asMe()), instanceOf(inner.getClass()));
  }
  @Test public void createRewrite() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final String s = input;
    final Document d = new Document(Wrap.Statement.on(s));
    final CompilationUnit u = asCompilationUnit();
    final ASTRewrite r = new Trimmer().createRewrite(u, null);
    final TextEdit e = r.rewriteAST(d, null);
    assertThat(e, notNullValue());
    assertThat(e.apply(d), is(notNullValue()));
  }
  @Test public void checkIf() {
    final IfStatement s = findIf();
    assertThat(s, notNullValue());
    assertThat(Wrings.emptyElse(s), is(true));
  }
  private IfStatement findIf() {
    return Extract.firstIfStatement(As.STATEMENTS.ast(input));
  }
  @Test public void eligible() {
    final VariableDeclarationFragment s = asMe();
    assertTrue(s.toString(), inner.eligible(s));
  }
  @Test public void findsSimplifier() {
    assertNotNull(Toolbox.instance.find(asMe()));
  }
  @Test public void hasOpportunity() {
    assertTrue(inner.scopeIncludes(asMe()));
    final CompilationUnit u = asCompilationUnit();
    final List<Rewrite> findOpportunities = new Trimmer().findOpportunities(u);
    assertThat(u.toString(), findOpportunities.size(), is(greaterThanOrEqualTo(1)));
  }
  @Test public void hasSimplifier() {
    assertThat(asMe().toString(), Toolbox.instance.find(asMe()), is(notNullValue()));
  }
  @Test public void noneligible() {
    assertFalse(inner.nonEligible(asMe()));
  }
  @Test public void peelableOutput() {
    final String s = expected;
    assertEquals(expected, Wrap.Statement.off(Wrap.Statement.on(s)));
  }
  @Test public void rewriteNotEmpty() throws MalformedTreeException, IllegalArgumentException {
    final CompilationUnit u = asCompilationUnit();
    final ASTRewrite r = new Trimmer().createRewrite(u, null);
    assertNotNull(r);
  }
  @Test public void scopeIncludesAsMe() {
    assertThat(asMe().toString(), inner.scopeIncludes(asMe()), is(true));
  }
  @Test public void scopeIncludesAsMeExpanded() {
    final VariableDeclarationFragment f = asMe();
    final Expression initializer = f.getInitializer();
    assertNotNull(initializer);
    final IfStatement s = Extract.nextIfStatement(f);
    assertNotNull(s);
    assertThat(Wrings.emptyElse(s), is(true));
    final Assignment a = Extract.assignment(then(s));
    assertNotNull(a);
    final Expression leftHandSide = left(a);
    assertNotNull(leftHandSide);
    assertTrue(same(leftHandSide, f.getName()));
    assertThat(a.getOperator(), is(Assignment.Operator.ASSIGN));
    final List<Expression> in = Occurrences.BOTH_SEMANTIC.of(f).in(s.getExpression(), right(a));
    assertThat(in, notNullValue());
    final ASTRewrite r = ASTRewrite.create(f.getAST());
    r.replace(initializer, Subject.pair(right(a), initializer).toCondition(s.getExpression()), null);
    r.remove(s, null);
    final ASTRewrite fillReplacement = WRING.go(r, f, null, null);
    final boolean scopeIncludes = fillReplacement != null;
    assertThat(asMe().toString(), scopeIncludes, is(true));
  }
  @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException {
    if (inner == null)
      return;
    final Document d = new Document(Wrap.Statement.on(input));
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(d);
    final Document actual = TESTUtils.rewrite(new Trimmer(), u, d);
    final String peeled = Wrap.Statement.off(actual.get());
    if (expected.equals(peeled))
      return;
    if (input.equals(peeled))
      fail("Nothing done on " + input);
    if (compressSpaces(peeled).equals(compressSpaces(input)))
      assertNotEquals("Wringing of " + input + " amounts to mere reformatting", compressSpaces(peeled), compressSpaces(input));
    assertSimilar(expected, peeled);
    assertSimilar(Wrap.Statement.on(expected), actual);
  }
  @Test public void traceLegiblity() {
    final VariableDeclarationFragment f = asMe();
    final ASTRewrite r = ASTRewrite.create(f.getAST());
    final Expression initializer = f.getInitializer();
    assertNotNull(f.toString(), initializer);
    final IfStatement s = Extract.nextIfStatement(f);
    assertNotNull(s);
    assertThat(Extract.statements(elze(s)).size(), is(0));
    final Assignment a = Extract.assignment(then(s));
    assertNotNull(a);
    assertTrue(same(left(a), f.getName()));
    r.replace(initializer, Subject.pair(right(a), initializer).toCondition(s.getExpression()), null);
    r.remove(s, null);
  }
  @Override protected CompilationUnit asCompilationUnit() {
    final CompilationUnit $ = (CompilationUnit) As.COMPILIATION_UNIT.ast(Wrap.Statement.on(input));
    assertNotNull($);
    return $;
  }
  @Override protected VariableDeclarationFragment asMe() {
    return Extract.firstVariableDeclarationFragment(As.STATEMENTS.ast(input));
  }
}