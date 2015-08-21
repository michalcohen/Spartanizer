package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThan;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;

import java.util.Collection;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.spartanizations.TESTUtils;
import org.spartan.refactoring.spartanizations.Wrap;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class DeclarationIfAssginmentTest extends AbstractWringTest<VariableDeclarationFragment> {
  static final Wring<VariableDeclarationFragment> WRING = new DeclarationIfAssginment();
  public DeclarationIfAssginmentTest() {
    super(WRING);
  }
  @Test public void newlineBug() throws MalformedTreeException, BadLocationException {
    final String from = "int a = 2;\n if (b) a =3;";
    final String expected = "int a = b ? 3 : 2;";
    final Document d = new Document(Wrap.Statement.on(from));
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(d);
    final VariableDeclarationFragment f = Extract.firstVariableDeclarationFragment(u);
    assertThat(f, notNullValue());
    final ASTRewrite r = new Trimmer().createRewrite(u, null);
    final TextEdit e = r.rewriteAST(d, null);
    assertThat(e.getChildrenSize(), greaterThan(0));
    final UndoEdit b = e.apply(d);
    assertThat(b, notNullValue());
    final String peeled = Wrap.Statement.off(d.get());
    if (expected.equals(peeled))
      return;
    if (from.equals(peeled))
      fail("Nothing done on " + from);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Wringing of " + from + " amounts to mere reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
    assertSimilar(Wrap.Statement.on(expected), d);
  }
  @Test public void nonNullWring() {
    assertNotNull(WRING);
  }
  @Test public void vanilla() throws MalformedTreeException, IllegalArgumentException {
    final String from = "int a = 2;\n if (b) a =3;";
    final String expected = "int a = b ? 3 : 2;";
    final Document d = new Document(Wrap.Statement.on(from));
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(d);
    final Document actual = TESTUtils.rewrite(new Trimmer(), u, d);
    final String peeled = Wrap.Statement.off(actual.get());
    if (expected.equals(peeled))
      return;
    if (from.equals(peeled))
      fail("Nothing done on " + from);
    if (compressSpaces(peeled).equals(compressSpaces(from)))
      assertNotEquals("Wringing of " + from + " amounts to mere reformatting", compressSpaces(peeled), compressSpaces(from));
    assertSimilar(expected, peeled);
    final String s1 = expected;
    assertSimilar(Wrap.Statement.on(s1), actual);
  }

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope<VariableDeclarationFragment> {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Simple if return TWO STATEMENTS", "if (a) return b; else a(); f();"), //
        new String[] { "Vanilla", "int a; a =3;", }, //
        new String[] { "Not empty else", "int a; if (x) a = 3; else a++;", }, //
        new String[] { "Vanilla", "int a =2; if (x) a += 3;", }, //
        new String[] { "Vanilla", "int a =2; if (x) a = 3;", }, //
        new String[] { "Vanilla", "int a = 2; if (b) a =3;", }, //
        null);
    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = DESCRIPTION) //
    public static Collection<Object[]> cases() {
      return collect(cases);
    }
    /** Instantiates the enclosing class ({@link OutOfScope}) */
    public OutOfScope() {
      super(WRING);
    }
  }
}
