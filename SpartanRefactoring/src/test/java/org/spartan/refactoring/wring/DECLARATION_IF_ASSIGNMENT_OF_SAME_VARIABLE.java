package org.spartan.refactoring.wring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.utils.Extract;
import org.spartan.refactoring.utils.Subject;
import org.spartan.refactoring.wring.AbstractWringTest.OutOfScope;
import org.spartan.refactoring.wring.AbstractWringTest.Wringed;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings({ "javadoc" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@RunWith(BlockJUnit4ClassRunner.class) //
public class DECLARATION_IF_ASSIGNMENT_OF_SAME_VARIABLE {
  static final Wring WRING = Wrings.DECLARATION_IF_ASSIGNMENT_OF_SAME_VARIABLE.inner;
  @Test public void placeHolder() {
    assertNotNull(WRING);
  }

  @RunWith(Parameterized.class) //
  public static class OutOfScope extends AbstractWringTest.OutOfScope {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("Expression vs. Expression", " 6 - 7 < 2 + 1   "), //
        Utils.asArray("Simple if return TWO STATEMENTS", "if (a) return b; else a(); f();"), //
        new String[] { "Vanilla", "int a; a =3;", }, //
        new String[] { "Vanilla", "int a; if (x) b = 3; else ;", }, //
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
  public static class Wringed extends AbstractWringTest.Wringed.WringedVariableDeclarationFragmentAndSurrounding {
    private static String[][] cases = Utils.asArray(//
        new String[] { "Vanilla", "int a = 2; if (b) a =3;", "int a= b?3:2;" }, //
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
    @Test public void traceLegiblity() {
      final VariableDeclarationFragment f = asMe();
      final ASTRewrite r = ASTRewrite.create(f.getAST());
      final Expression initializer = f.getInitializer();
      assertNotNull(initializer);
      final IfStatement s = Extract.nextIfStatement(f);
      assertNotNull(s);
      assertThat(Extract.statements(s.getElseStatement()).size(), is(0));
      final Assignment a = Extract.assignment(s.getThenStatement());
      assertNotNull(a);
      assertTrue(Wrings.same(a.getLeftHandSide(), f.getName()));
      r.replace(initializer, Subject.pair(a.getRightHandSide(), initializer).toCondition(s.getExpression()), null);
      r.remove(s, null);
    }
  }
}
