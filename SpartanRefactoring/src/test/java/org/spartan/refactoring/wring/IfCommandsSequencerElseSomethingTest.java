package org.spartan.refactoring.wring;

import static org.junit.Assert.assertNotNull;
import static org.spartan.refactoring.spartanizations.TESTUtils.asSingle;
import static org.spartan.refactoring.utils.Funcs.asIfStatement;

import java.util.Collection;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.FixMethodOrder;
import org.junit.Test;
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
@SuppressWarnings({ "javadoc", "static-method" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class IfCommandsSequencerElseSomethingTest {
  static final Wring<IfStatement> WRING = new IfCommandsSequencerElseSomething();
  @Test public void checkSteps() {
    final Statement s = asSingle("if (a) return a = b; else a = c;");
    assertNotNull(s);
    final IfStatement i = asIfStatement(s);
    assertNotNull(i);
  }

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope<IfStatement> {
    static String[][] cases = Utils.asArray(//
         new String[] {"Literal vs. Literal", "if (a) return b; else c;"}, //
         new String[] {"Simple if return", "if (a) return b; else return c;"}, //
         new String[] {"Simply nested if return", "{if (a)  return b; else return c;}"}, //
         new String[] {"Nested if return", "if (a) {;{{;;return b; }}} else {{{;return c;};;};}"}, //
         new String[] {"Not same assignment", "if (a) a /= b; else a /= c;"}, //
         new String[] {"Another distinct assignment", "if (a) a /= b; else a %= c;"}, //
         new String[] {"Simple if assign", "if (a) a = b; else a = c;"}, //
         new String[] {"Simple if plus assign", "if (a) a += b; else a += c;"}, //
         new String[] {"Simple if plus assign", "if (a) a *= b; else a *= c;"}, //
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
  public static class Wringed extends AbstractWringTest.Wringed.IfStatementAndSurrounding {
    private static String[][] cases = Utils.asArray(//
         new String[] {"Vanilla: sequencer in then", "if (a) return b; else a();", "if(a)return b;a();"}, //
         new String[] {"Vanilla: sequencer in else", "if (a) return b; else a();", "if(a)return b;a();"}, //
         new String[] {"Plant two statements", "if (a) return b; else a(); f();", "if(a)return b;a(); f();"}, //
        null, //
         new String[] {"Compressed complex", " if (x) {;f();;;return a;;;} else {;g();{;;{}}{}}", "if (x) {;f();;;return a;;;}\n g();"}, //
        null, //
         new String[] {"Compressed complex", " if (x) {;f();;;return a;;;} else {;g();{;;{}}{}}", "  if(x){;f();;;return a;;;} g();"}, //
         new String[] {"Compressed complex", " if (x) {;f();;;return a;;;} else {;g();{;;{}}{}}",
            "" + //
                " if (x) {\n" + //
                "   f();\n" + //
                "   return a;\n" + //
                " }\n" + //
                " g();\n" + //
            ""},//
        null, //
         new String[] {"Complex with many junk statements",
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
            ""},//
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
