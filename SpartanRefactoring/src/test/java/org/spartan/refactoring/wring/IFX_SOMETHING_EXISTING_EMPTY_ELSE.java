package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
 * Unit tests for {@link Wrings#IFX_SOMETHING_EXISTING_EMPTY_ELSE}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class IFX_SOMETHING_EXISTING_EMPTY_ELSE {
  static final Wring WRING = Wrings.IFX_SOMETHING_EXISTING_EMPTY_ELSE.inner;

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Return only on one side", "if (a) return b; else c;"), //
        Utils.asArray("Simple if return", "if (a) return b; else return c;"), //
        Utils.asArray("Simply nested if return", "{if (a)  return b; else return c;}"), //
        Utils.asArray("Nested if return", "if (a) {;{{;;return b; }}} else {{{;return c;};;};}"), //
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

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends AbstractWringTest.WringedIfStatement {
    private static String[][] cases = Utils.asArray(//
        new String[] { "Vanilla {}", "if (a) return b; else {}", "if (a) return b;" }, //
        new String[] { "Vanilla ; ", "if (a) return b; else ;", "if (a) return b;" }, //
        new String[] { "Vanilla {;{;;};} ", "if (a) return b; else {;{;{};};{;{}}}", "if (a) return b;" }, //
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
    /**
     * Instantiates the enclosing class ({@link Wringed})
     */
    public Wringed() {
      super(WRING);
    }
    @Test public void isfStatementElseIsEmpty() {
      IfStatement i = asMe();
      assertThat(Extract.statements(i.getElseStatement()).size(), is(0));
    }
    @Test public void isfStatementElseNotNull() {
      IfStatement i = asMe();
      assertNotNull(i.getElseStatement());
    }
    @Test public void isIfStatement() {
      assertNotNull(asMe());
    }
    @Test public void myScopeIncludes() {
      final IfStatement s = asMe();
      assertThat(s, notNullValue());
      assertThat(s.getElseStatement(), notNullValue());
      assertThat(Extract.statements(s.getElseStatement()), notNullValue());
      assertThat(Extract.statements(s.getElseStatement()).size(), is(0));
    }
  }
}
