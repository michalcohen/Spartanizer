package org.spartan.refactoring.wring;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.spartanizations.TESTUtils.asSingle;
import static org.spartan.refactoring.utils.Funcs.asIfStatement;

import java.util.Collection;

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
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.refactoring.wring.AbstractWringTest.Wringed;
import org.spartan.utils.Utils;

/**
 * Unit tests for
 * {@link Wrings#IFX_SINGLE_RETURN_MISSING_ELSE_FOLLOWED_BY_RETURN}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@RunWith(BlockJUnit4ClassRunner.class) //
public class IFX_SINGLE_RETURN_MISSING_ELSE_FOLLOWED_BY_RETURN {
  static final Wring WRING = Wrings.IFX_SINGLE_RETURN_MISSING_ELSE_FOLLOWED_BY_RETURN.inner;
  @Test public void checkSteps() {
    final Statement s = asSingle("if (a) return a = b; else a = c;");
    assertNotNull(s);
    final IfStatement i = asIfStatement(s);
    assertNotNull(i);
  }

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        new String[] { "Another distinct assignment", "if (a) a /= b; else a %= c;" }, //
        new String[] { "Expression vs. Expression", " 6 - 7 < 2 + 1   " }, //
        new String[] { "Literal vs. Literal", "if (a) return b; else c;" }, //
        new String[] { "Nested if return", "if (a) {;{{;;return b; }}} else {{{;return c;};;};}" }, //
        new String[] { "Nested if return", "if (a) {;{{;;throw b; }}} else {{{;throw c;};;};}" }, //
        new String[] { "Not same assignment", "if (a) a /= b; else a /= c;" }, //
        new String[] { "Return only on one side", "if (a) return b; else c;" }, //
        new String[] { "Simple if assign", "if (a) a = b; else a = c;" }, //
        new String[] { "Simple if plus assign", "if (a) a *= b; else a *= c;" }, //
        new String[] { "Simple if plus assign", "if (a) a += b; else a += c;" }, //
        new String[] { "Simple if return", "if (a) return b; else return c;" }, //
        new String[] { "Simple if throw", "if (a) throw b; else throw c;" }, //
        new String[] { "Simply nested if return", "{if (a)  return b; else return c;}" }, //
        new String[] { "Simply nested if throw", "{if (a)  throw b; else throw c;}" }, //
        new String[] { "Vanilla ;;", "if (a) ; else ;" }, //
        new String[] { "Vanilla {}{}", "if (a) {} else {}" }, //
        new String[] { "Vanilla ; ", "if (a) return b; else ;" }, //
        new String[] { "Vanilla ; ", "if (a) return b; else ;", }, //
        new String[] { "Vanilla {;{;;};} ", "if (a) return b; else {;{;{};};{;{}}}" }, //
        new String[] { "Vanilla {}", "if (a) return b; else {}" }, //
        new String[] { "Vanilla if-then-else", "if (a) return b;" }, //
        new String[] { "Vanilla if-then-no-else", "if (a) return b;" }, //
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
    private static String[][] cases = new String[][] { //
        new String[] { "Vanilla {}", "if (a) return b; return a();", "if (a) return b;" }, //
        new String[] { "Vanilla ; ", "if (a) return b; a(); b(); c();", "if (a) return b;" }, //
        new String[] { "Vanilla {;{;;};} ", "if (a) return b; else {;{;{};};{;{}}}", "if (a) return b;" }, //
        Utils.asArray("Simple if return", "if (a) return b; else ;", "if (a) return b;"), //
        null, //
        Utils.asArray("Compressed complex", " if (x) {;f();;;return a;;;} else {;g();{;;{}}{}}", "if (x) {;f();;;return a;;;}\n g();"), //
        null, //
        Utils.asArray("Compressed complex", " if (x) {;f();;;return a;;;} else {;g();{;;{}}{}}", "  if(x){;f();;;return a;;;} g();"), //
        Utils.asArray("Compressed complex", " if (x) {;f();;;return a;;;} else {;g();{;;{}}{}}",
            "" + //
                " if (x) {\n" + //
                "   f();\n" + //
                "   return a;\n" + //
                " }\n" + //
                " g();\n" + //
                ""),
        null, //
        Utils.asArray("Complex with many junk statements",
            "" + //
                " if (x) {\n" + //
                "   ;\n" + //
                "   f();\n" + //
                "   return a;\n" + //
                " } else {\n" + //
                "   ;\n" + //
                "   g();\n" + //
                "   {\n" + //
                "   }\n" + //
                " }\n" + //
                "",
            "" + //
                " if (x) {\n" + //
                "   f();\n" + //
                "   return a;\n" + //
                " }\n" + //
                " g();\n" + //
                ""), //
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
