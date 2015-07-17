package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

public abstract class Engine_Common_NOCHANGE extends Engine_Common {
  /**
   * Instantiates the enclosing class ({@link Engine_Common_NOCHANGE})
   *
   * @param simplifier
   *          JD
   */
  public Engine_Common_NOCHANGE(final Simplifier simplifier) {
    super(simplifier);
  }
  @Test @SuppressWarnings("javadoc") public void eligible() {
    assertFalse(simplifier.eligible(asInfixExpression()));
  }
  @Test @SuppressWarnings("javadoc") public void noneligible() {
    assertTrue(simplifier.noneligible(asInfixExpression()));
  }
  @Test @SuppressWarnings("javadoc") public void noOpporunity() {
    final CompilationUnit u = asCompilationUnit();
    assertEquals(u.toString(), 0, engine.findOpportunities(u).size());
  }
  @Test @SuppressWarnings("javadoc") public void hasNoReplacement() {
    assertNull(simplifier.replacement(asInfixExpression()));
  }
  @Test @SuppressWarnings("javadoc") public void simiplifies()
      throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final CompilationUnit u = asCompilationUnit();
    final ASTRewrite r = engine.createRewrite(u, null);
    final Document d = asDocument();
    r.rewriteAST(d, null).apply(d);
    assertSimilar(compressSpaces(peel(d.get())), compressSpaces(input()));
    assertSimilar(wrap(input()), d.get());
  }
}
