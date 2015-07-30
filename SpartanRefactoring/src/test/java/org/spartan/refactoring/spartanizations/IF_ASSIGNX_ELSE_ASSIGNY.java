package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.*;
import static org.spartan.refactoring.spartanizations.TESTUtils.*;
import static org.spartan.refactoring.utils.Funcs.*;
import java.util.Collection;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.utils.Extract;
import org.spartan.utils.Utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@RunWith(BlockJUnit4ClassRunner.class) //
public class IF_ASSIGNX_ELSE_ASSIGNY {
  static final Wring WRING = Wrings.IF_ASSIGNX_ELSE_ASSIGNY.inner;
  @Test public void checkSteps() {
    final Statement s = asSingle("if (a) a = b; else a = c;");
    assertNotNull(s);
    final IfStatement i = asIfStatement(s);
    assertNotNull(i);
    final Assignment then = Extract.assignment(i.getThenStatement());
    assertNotNull(i.getThenStatement().toString(), then);
    final Assignment elze = Extract.assignment(i.getElseStatement());
    assertNotNull(elze);
    assertThat(compatible(then, elze), is(true));
    assertThat(WRING.scopeIncludes(s), is(true));
  }

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Literal vs. Literal", "if (a) return b; else c;"), //
        Utils.asArray("Simple if return", "if (a) return b; else return c;"), //
        Utils.asArray("Simply nested if return", "{if (a)  return b; else return c;}"), //
        Utils.asArray("Nested if return", "if (a) {;{{;;return b; }}} else {{{;return c;};;};}"), //
        Utils.asArray("Not same assignment", "if (a) a /= b; else a /= c;"), //
        Utils.asArray("Another distinct assignment", "if (a) a /= b; else a %= c;"), //
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
  public static class Wringed extends AbstractWringTest.WringedStatement {
    private static String[][] cases = Utils.asArray(//
        // Literal
        Utils.asArray("Simple if assign", "if (a) a = b; else a = c;", "a = a ? b : c;"), //
        Utils.asArray("Simple if plus assign", "if (a) a += b; else a += c;", "a += a ? b : c;"), //
        Utils.asArray("Simple if plus assign", "if (a) a *= b; else a *= c;", "a *= a ? b : c;"), //
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
  }
}
