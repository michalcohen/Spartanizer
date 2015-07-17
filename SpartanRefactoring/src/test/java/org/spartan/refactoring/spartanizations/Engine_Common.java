package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.peel;
import static org.spartan.refactoring.spartanizations.TESTUtils.wrap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jface.text.Document;
import org.junit.Test;
import org.spartan.refactoring.utils.As;

@SuppressWarnings("javadoc") //
public abstract class Engine_Common {
  abstract String input();
  public Engine_Common(final Wring simplifier) {
    this.simplifier = simplifier;
  }

  protected final Wring simplifier;
  protected final Wringer engine = new Wringer();

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
    assertEquals(simplifier, Wrings.find(asInfixExpression()));
  }
  @Test public void scopeIncludes() {
    assertTrue(simplifier.scopeIncludes(asInfixExpression()));
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
}