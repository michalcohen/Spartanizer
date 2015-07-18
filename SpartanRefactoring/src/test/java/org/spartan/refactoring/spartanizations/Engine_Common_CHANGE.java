package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.spartanizations.TESTUtils.peel;
import static org.spartan.refactoring.spartanizations.TESTUtils.wrap;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;

/**
 * @author Yossi Gil
 * @since 2015-07-15
 *
 */
@SuppressWarnings("javadoc") //
public abstract class Engine_Common_CHANGE extends Engine_Common {
  /**
   * @return the expected output of the simplification
   */
  abstract String output();
  Engine_Common_CHANGE(final Wring simplifier) {
    super(simplifier);
  }
  @Test public void peelableOutput() {
    assertEquals(output(), peel(wrap(output())));
  }
  @Test @SuppressWarnings("javadoc") public void oneOpporunity() {
    final CompilationUnit u = asCompilationUnit();
    assertEquals(u.toString(), 1, engine.findOpportunities(u).size());
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
    final ASTRewrite r = engine.createRewrite(u, null);
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
