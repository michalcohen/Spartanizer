package org.spartan.refactoring.spartanizations;

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
import static org.spartan.refactoring.spartanizations.TESTUtils.*;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;
import org.junit.internal.builders.IgnoredClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.spartan.refactoring.utils.As;
import org.spartan.utils.Range;

/**
 * @author Yossi Gil
 * @since 2015-07-18
 */
@SuppressWarnings("javadoc") //
@RunWith(IgnoredClassRunner.class) //
public abstract class AbstractWringTest {
  protected final Wring inner;
  /** The name of the specific test for this transformation */
  @Parameter(0) public String name;
  /** Where the input text can be found */
  @Parameter(1) public String input;
  /**
   * Instantiates the enclosing class ({@link AbstractWringTest})
   *
   * @param inner JD
   */
  AbstractWringTest(final Wring inner) {
    this.inner = inner;
  }
  @Test public void inputNotNull() {
    assertNotNull(input);
  }
  @Test public void peelableinput() {
    assertEquals(input, peel(wrap(input)));
  }
  protected InfixExpression asInfixExpression() {
    final InfixExpression $ = i(input);
    assertNotNull($);
    return $;
  }
  protected PrefixExpression asPrefixExpression() {
    final PrefixExpression $ = p(input);
    assertNotNull($);
    return $;
  }
  protected Expression asExpression() {
    final Expression $ = e(input);
    assertNotNull($);
    return $;
  }
  protected CompilationUnit asCompilationUnit() {
    final ASTNode $ = As.COMPILIATION_UNIT.ast(wrap(input));
    assertThat($, is(notNullValue()));
    assertThat($, is(instanceOf(CompilationUnit.class)));
    return (CompilationUnit) $;
  }
  protected ConditionalExpression asConditionalExpression() {
    final ConditionalExpression $ = c(input);
    assertNotNull($);
    return $;
  }
  protected Document asDocument() {
    return new Document(wrap(input));
  }

  /**
   * @author Yossi Gil
   * @since 2015-07-18
   */
  public static abstract class OutOfScope extends AbstractWringTest {
    /** Description of a test case for {@link Parameter} annotation */
    protected static final String DESCRIPTION = "Test #{index}. ({0}) \"{1}\" N/A";
    /** Instantiates the enclosing class ({@link OutOfScope})@param inner */
    public OutOfScope(final Wring inner) {
      super(inner);
    }
    @Test public void scopeDoesNotInclude() {
      assertThat(inner.scopeIncludes(asExpression()), is(false));
    }

    public static abstract class Infix extends OutOfScope {
      /** Instantiates the enclosing class ({@link Infix})@param simplifier */
      Infix(final Wring w) {
        super(w);
      }
      @Test public void inputIsInfixExpression() {
        final InfixExpression e = asInfixExpression();
        assertNotNull(e);
      }
    }
  }

  /**
   * @author Yossi Gil
   * @since 2015-07-18
   */
  static abstract class InScope extends AbstractWringTest {
    /**
     * @author Yossi Gil
     * @since 2015-07-15
     */
    static abstract class WringedInput extends AbstractWringTest {
      /** How should a test case like this be described? */
      protected static final String DESCRIPTION = "{index}: \"{1}\" => \"{2}\" ({0})";
      /** Where the expected output can be found? */
      @Parameter(2) public String output;
      protected final Trimmer trimmer = new Trimmer();
      /**
       * Instantiates the enclosing class ({@link Wringed})
       *
       * @param w JD
       */
      WringedInput(final Wring w) {
        super(w);
      }
      @Test public void scopeIncludes() {
        assertFalse(inner.scopeIncludes(asExpression()));
      }
      @Test public void eligible() {
        assertTrue(inner.eligible(asExpression()));
      }
      @Test public void noneligible() {
        assertFalse(inner.noneligible(asExpression()));
      }
      @Test public void peelableOutput() {
        assertEquals(output, peel(wrap(output)));
      }
      @Test public void oneOpporunity() {
        final CompilationUnit u = asCompilationUnit();
        assertEquals(u.toString(), 1, trimmer.findOpportunities(u).size());
        assertTrue(inner.scopeIncludes(asExpression()));
      }
      @Test public void hasReplacement() {
        assertNotNull(inner.replacement(asExpression()));
      }
      @Test public void createRewrite() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
        final CompilationUnit u = asCompilationUnit();
        final Document d = new Document(wrap(input));
        final ASTRewrite r = trimmer.createRewrite(u, null);
        assertThat(r.rewriteAST(d, null).apply(d), is(notNullValue()));
      }
      @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException {
        final CompilationUnit u = asCompilationUnit();
        final Document excpected = TESTUtils.rewrite(trimmer, u, new Document(wrap(input)));
        final String peeled = peel(excpected.get());
        if (output.equals(peeled))
          return;
        if (input.equals(peeled))
          fail("Nothing done on " + input);
        if (compressSpaces(peeled).equals(compressSpaces(input)))
          assertNotEquals("Wringing of " + input + " amounts to mere reformatting", compressSpaces(peeled), compressSpaces(input));
        assertSimilar(output, peeled);
        assertSimilar(wrap(output), excpected);
      }
      @Test public void findsSimplifier() {
        assertNotNull(Wrings.find(asExpression()));
      }
      @Test public void correctSimplifier() {
        assertEquals(inner, Wrings.find(asExpression()));
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
    InScope(final Wring inner) {
      super(inner);
    }
    protected final Trimmer wringer = new Trimmer();
    @Test public void findsSimplifier() {
      assertNotNull(Wrings.find(asExpression()));
    }
    @Test public void correctSimplifier() {
      assertEquals(inner, Wrings.find(asExpression()));
    }
  }

  /**
   * @author Yossi Gil
   * @since 2015-07-18
   */
  public static abstract class Noneligible extends InScope {
    /** Description of a test case for {@link Parameter} annotation */
    protected static final String DESCRIPTION = "Test #{index}. ({0}) \"{1}\" ==>|";
    /**
     * Instantiates the enclosing class ({@link Noneligible})
     *
     * @param simplifier JD
     */
    Noneligible(final Wring simplifier) {
      super(simplifier);
    }
    @Test public void eligible() {
      assertFalse(inner.eligible(asExpression()));
    }
    @Test public void noneligible() {
      assertTrue(inner.noneligible(asExpression()));
    }
    @Test public void noOpporunity() {
      final CompilationUnit u = asCompilationUnit();
      assertEquals(u.toString() + wringer.findOpportunities(u), 0, wringer.findOpportunities(u).size());
    }
    @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
      final CompilationUnit u = asCompilationUnit();
      final ASTRewrite r = wringer.createRewrite(u, null);
      final Document d = asDocument();
      r.rewriteAST(d, null).apply(d);
      assertSimilar(compressSpaces(peel(d.get())), compressSpaces(input));
      assertSimilar(wrap(input), d.get());
    }
    @Override @Test public void findsSimplifier() {
      assertNotNull(Wrings.find(asExpression()));
    }
    @Override @Test public void correctSimplifier() {
      assertThat(Wrings.find(asExpression()), is(inner));
    }

    public static abstract class Infix extends Noneligible {
      /** Instantiates the enclosing class ({@link Infix})@param simplifier */
      Infix(final Wring w) {
        super(w);
      }
      @Test public void inputIsInfixExpression() {
        final InfixExpression e = asInfixExpression();
        assertNotNull(e);
      }
      @Test public void flattenIsIdempotentt() {
        final InfixExpression flatten = flatten(asInfixExpression());
        assertThat(flatten(flatten).toString(), is(flatten.toString()));
      }
    }
  }

  /**
   * @author Yossi Gil
   * @since 2015-07-15
   */
  public static abstract class Wringed extends InScope {
    public static class Conditional extends Wringed {
      /** Instantiates the enclosing class ({@link Infix})@param simplifier */
      Conditional(final Wring w) {
        super(w);
      }
      @Test public void inputIsConditionalExpression() {
        assertNotNull(asConditionalExpression());
      }
    }
    /** Description of a test case for {@link Parameter} annotation */
    protected static final String DESCRIPTION = "Test #{index}. ({0}) \"{1}\" ==> \"{2}\"";
    /** What should the output be */
    @Parameter(2) public String expected;
    /**
     * Instantiates the enclosing class ({@link WringedInput})
     *
     * @param simplifier
     */
    Wringed(final Wring simplifier) {
      super(simplifier);
    }
    @Test public void eligible() {
      assertTrue(inner.eligible(asExpression()));
    }
    @Test public void noneligible() {
      assertFalse(inner.noneligible(asExpression()));
    }
    @Test public void peelableOutput() {
      assertEquals(expected, peel(wrap(expected)));
    }
    @Test public void scopeIncludes() {
      assertTrue(inner.scopeIncludes(asExpression()));
    }
    @Test public void hasOpportunity() {
      assertTrue(inner.scopeIncludes(asExpression()));
      final CompilationUnit u = asCompilationUnit();
      final List<Range> findOpportunities = wringer.findOpportunities(u);
      assertThat(u.toString(), findOpportunities.size(), is(greaterThanOrEqualTo(0)));
    }
    @Test public void hasReplacement() {
      assertNotNull(inner.replacement(asExpression()));
    }
    @Test public void createRewrite() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
      final CompilationUnit u = asCompilationUnit();
      final Document d = new Document(wrap(input));
      final ASTRewrite r = wringer.createRewrite(u, null);
      assertThat(r.rewriteAST(d, null).apply(d), is(notNullValue()));
    }
    @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException {
      final CompilationUnit u = asCompilationUnit();
      final Document excpected = TESTUtils.rewrite(wringer, u, new Document(wrap(input)));
      final String peeled = peel(excpected.get());
      if (expected.equals(peeled))
        return;
      if (input.equals(peeled))
        fail("Nothing done on " + input);
      if (compressSpaces(peeled).equals(compressSpaces(input)))
        assertNotEquals("Wringing of " + input + " amounts to mere reformatting", compressSpaces(peeled), compressSpaces(input));
      assertSimilar(expected, peeled);
      assertSimilar(wrap(expected), excpected);
    }
    @Override @Test public void findsSimplifier() {
      assertNotNull(Wrings.find(asExpression()));
    }
    @Override @Test public void correctSimplifier() {
      assertEquals(inner, Wrings.find(asExpression()));
    }

    public static abstract class Infix extends Wringed {
      /** Instantiates the enclosing class ({@link Infix})@param simplifier */
      Infix(final Wring w) {
        super(w);
      }
      @Test public void inputIsInfixExpression() {
        final InfixExpression e = asInfixExpression();
        assertNotNull(e);
      }
      @Test public void flattenIsIdempotentt() {
        final InfixExpression flatten = flatten(asInfixExpression());
        assertThat(flatten(flatten).toString(), is(flatten.toString()));
      }
    }
  }
}