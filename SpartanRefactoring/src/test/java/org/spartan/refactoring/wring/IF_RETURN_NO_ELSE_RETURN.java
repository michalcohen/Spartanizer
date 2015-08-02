package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.instanceOf;
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
import static org.spartan.refactoring.spartanizations.TESTUtils.asSingle;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.c;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;
import static org.spartan.refactoring.spartanizations.TESTUtils.e;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.p;
import static org.spartan.refactoring.spartanizations.TESTUtils.peelExpression;
import static org.spartan.refactoring.spartanizations.TESTUtils.peelStatement;
import static org.spartan.refactoring.spartanizations.TESTUtils.s;
import static org.spartan.refactoring.spartanizations.TESTUtils.wrapExpression;
import static org.spartan.refactoring.spartanizations.TESTUtils.wrapStatement;
import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.asIfStatement;
import static org.spartan.refactoring.utils.Restructure.flatten;
import static org.junit.Assert.assertNotNull;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;

import org.eclipse.jdt.core.dom.IfStatement;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.refactoring.wring.AbstractWringTest.Wringed;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public enum IF_RETURN_NO_ELSE_RETURN {
  ;
  static final Wring WRING = Wrings.IF_RETURN_NO_ELSE_RETURN.inner;

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        new String[] { "Expression vs. Expression", " 6 - 7 < 2 + 1   " }, //
        new String[] { "Return only on one side", "if (a) return b; else c;" }, //
        new String[] { "Simple if return", "if (a) return b; else return c;" }, //
        new String[] { "Simply nested if return", "{if (a)  return b; else return c;}" }, //
        new String[] { "Nested if return", "if (a) {;{{;;return b; }}} else {{{;return c;};;};}" }, //
        new String[] { "Simple if throw", "if (a) throw b; else throw c;" }, //
        new String[] { "Simply nested if throw", "{if (a)  throw b; else throw c;}" }, //
        new String[] { "Nested if return", "if (a) {;{{;;throw b; }}} else {{{;throw c;};;};}" }, //
        new String[] { "Vanilla if-then-no-else", "if (a) return b;" }, //
        new String[] { "Vanilla if-then-else", "if (a) return b;" }, //
        null);
    /** Instantiates the enclosing class ({@link OutOfScope}) */
    public OutOfScope() {
      super(WRING);
    }
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
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.WringedIfStatement {
    private static String[][] cases = new String[][] { //
        new String[] { "Vanilla {}", "if (a) return b; else {}", "if (a) return b;" }, //
        new String[] { "Vanilla ; ", "if (a) return b; else ;", "if (a) return b;" }, //
        new String[] { "Vanilla {;{;;};} ", "if (a) return b; else {;{;{};};{;{}}}", "if (a) return b;" }, //
        null };
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
    @Test public void isIfStatement() {
      assertNotNull(asMe());
    }
    @Test public void isfStatementElseNotNull() {
      IfStatement i = asMe();
      assertNotNull(i.getElseStatement());
    }
    @Test public void isfStatementElseIsEmpty() {
      IfStatement i = asMe();
      assertThat(Extract.statements(i.getElseStatement()).size(), is(0));
    }
    @Test public void myScopeIncludes() {
      final IfStatement s = asMe();
      assertThat(s, notNullValue());
      assertThat(s.getElseStatement(), notNullValue());
      assertTrue(s != null && s.getElseStatement() != null && Extract.statements(s.getElseStatement()).size() == 0);
    }
    /**
     * Instantiates the enclosing class ({@link Wringed})
     */
    public Wringed() {
      super(WRING);
    }
  }
}
