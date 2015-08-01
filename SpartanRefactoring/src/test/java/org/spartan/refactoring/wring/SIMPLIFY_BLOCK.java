package org.spartan.refactoring.wring;

import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
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
public enum SIMPLIFY_BLOCK {
  ;
  static final Wring WRING = Wrings.SIMPLIFY_BLOCK.inner;

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
        Utils.asArray("Simple if assign", "if (a) a = b; else a = c;"), //
        Utils.asArray("Simple if plus assign", "if (a) a += b; else a += c;"), //
        Utils.asArray("Simple if plus assign", "if (a) a *= b; else a *= c;"), //
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
  public static class Wringed extends AbstractWringTest.WringedBlock {
    private static String[][] cases = Utils.asArray(//
        // Literal
        Utils.asArray("Empty", "{;;}", ""), //
        Utils.asArray("Complex empty", "{;;{;{{}}}{;}{};}", ""), //
        Utils.asArray("Deeply nested return", " {{{;return c;};;};}", " return c;"), //
        Utils.asArray("Singleton", "{if (a)  return b; else return c;}", " if(a)return b;else return c;"), //
        Utils.asArray("Complex singleton", "{;{{;;return b; }}}", " return b;"), //
        Utils.asArray("Three statements ", "{i++;{{;;return b; }}j++;}", " i++;return b;j++;"), //
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
