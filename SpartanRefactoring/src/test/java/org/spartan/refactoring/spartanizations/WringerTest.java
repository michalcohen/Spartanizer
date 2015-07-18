package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.peel;
import static org.spartan.refactoring.spartanizations.TESTUtils.wrap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;
import org.spartan.refactoring.utils.As;

/**
 * @author Yossi Gil
 * @since 2015-07-18
 *
 */
@SuppressWarnings("javadoc") //
public abstract class WringerTest {
  protected final Wring inner;
  protected final Wringer wringer = new Wringer();

  abstract String input();
  /**
   * Instantiates the enclosing class ({@link WringerTest})
   *
   * @param inner
   */
  public WringerTest(final Wring inner) {
    this.inner = inner;
  }
  @Test public void inputNotNull() {
    assertNotNull(input());
  }
  @Test public void peelableInput() {
    assertEquals(input(), peel(wrap(input())));
  }
  @Test public void findsSimplifier() {
    assertNotNull(Wrings.find(asInfixExpression()));
  }
  @Test public void correctSimplifier() {
    assertEquals(inner, Wrings.find(asInfixExpression()));
  }
  @Test public void scopeIncludes() {
    assertTrue(inner.scopeIncludes(asInfixExpression()));
  }
  protected InfixExpression asInfixExpression() {
    final InfixExpression $ = i(input());
    assertNotNull($);
    return $;
  }
  protected CompilationUnit asCompilationUnit() {
    final ASTNode $ = As.COMPILIATION_UNIT.ast(wrap(input()));
    assertNotNull($);
    assertThat($, is(instanceOf(CompilationUnit.class)));
    return (CompilationUnit) $;
  }
  protected Document asDocument() {
    return new Document(wrap(input()));
  }

  /**
   * @author Yossi Gil
   * @since 2015-07-18
   *
   */
  public static abstract class Unchanged extends WringerTest {
    /**
     * Instantiates the enclosing class ({@link Unchanged})
     *
     * @param simplifier
     *          JD
     */
    public Unchanged(final Wring simplifier) {
      super(simplifier);
    }
    @Test public void eligible() {
      assertFalse(inner.eligible(asInfixExpression()));
    }
    @Test public void noneligible() {
      assertTrue(inner.noneligible(asInfixExpression()));
    }
    @Test public void noOpporunity() {
      final CompilationUnit u = asCompilationUnit();
      assertEquals(u.toString(), 0, wringer.findOpportunities(u).size());
    }
    @Test(expected = AssertionError.class) public void hasNoReplacement() {
      assertNull(inner.replacement(asInfixExpression()));
    }
    @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
      final CompilationUnit u = asCompilationUnit();
      final ASTRewrite r = wringer.createRewrite(u, null);
      final Document d = asDocument();
      r.rewriteAST(d, null).apply(d);
      assertSimilar(compressSpaces(peel(d.get())), compressSpaces(input()));
      assertSimilar(wrap(input()), d.get());
    }
  }

  /**
   * @author Yossi Gil
   * @since 2015-07-15
   *
   */
  public static abstract class WringedInput extends WringerTest {
    /**
     * @return the expected output of the simplification
     */
    abstract String output();
    WringedInput(final Wring simplifier) {
      super(simplifier);
    }
    @Test public void peelableOutput() {
      assertEquals(output(), peel(wrap(output())));
    }
    @Test public void oneOpporunity() {
      final CompilationUnit u = asCompilationUnit();
      assertEquals(u.toString(), 1, wringer.findOpportunities(u).size());
      assertTrue(inner.scopeIncludes(asInfixExpression()));
    }
    @Test public void eligible() {
      assertTrue(inner.eligible(asInfixExpression()));
    }
    @Test public void noneligible() {
      assertFalse(inner.noneligible(asInfixExpression()));
    }
    @Test public void hasReplacement() {
      assertNotNull(inner.replacement(asInfixExpression()));
    }
    @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
      final CompilationUnit u = asCompilationUnit();
      final ASTRewrite r = wringer.createRewrite(u, null);
      final Document d = asDocument();
      r.rewriteAST(d, null).apply(d);
      final String peeled = peel(d.get());
      if (output().equals(peeled))
        return;
      if (input().equals(peeled))
        fail("Nothing done on " + input());
      if (compressSpaces(peeled).equals(compressSpaces(input())))
        assertNotEquals("Simpification of " + input() + " is just reformatting", compressSpaces(peeled), compressSpaces(input()));
      assertSimilar(output(), peeled);
      assertSimilar(wrap(output()), d);
    }
    static Document rewrite(final Spartanization s, final CompilationUnit u, final Document d) {
      try {
        s.createRewrite(u, null).rewriteAST(d, null).apply(d);
        return d;
      } catch (final MalformedTreeException e) {
        fail(e.getMessage());
      } catch (final IllegalArgumentException e) {
        fail(e.getMessage());
      } catch (final BadLocationException e) {
        fail(e.getMessage());
      }
      return null;
    }
  }
}