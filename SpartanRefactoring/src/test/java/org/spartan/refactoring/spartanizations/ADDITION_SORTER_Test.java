package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Are;
import org.spartan.refactoring.utils.Have;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 *
 */
@SuppressWarnings("javadoc") //
public enum ADDITION_SORTER_Test {
  ;
  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Wringed extends WringerTest.WringedInput {
    /**
     * Instantiates the enclosing class ({@link Wringed})
     */
    public Wringed() {
      super(Wrings.ADDITION_SORTER.inner);
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
    }
    @Test public void hasLiteral() {
      final InfixExpression e = asInfixExpression();
      assertTrue(e.getOperator() == Operator.PLUS && Have.numericLiteral(All.operands(e)) && Are.notString(All.operands(e)));
    }
    @Test public void isPlus() {
      final InfixExpression e = asInfixExpression();
      assertTrue(e.getOperator() == Operator.PLUS);
    }
    @Test public void literalArgument() {
      final InfixExpression e = asInfixExpression();
      assertTrue(Have.numericLiteral(All.operands(e)));
    }
    @Test public void allNotStringArgument() {
      final InfixExpression e = asInfixExpression();
      assertTrue(Are.notString(All.operands(e)));
    }
    @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = "{index}: {0} {1}") //
    public static Collection<Object[]> cases() {
      final Collection<Object[]> $ = new ArrayList<>(cases.length);
      for (final String[] t : cases)
        $.add(t);
      return $;
    }

    private static String[][] cases = Utils.asArray(//
        Utils.asArray("Add 1 to 2*3", "1+2*3", "2*3+1"), //
        Utils.asArray("Add '1' to a*b", "'1'+a*b", "a*b+'1'"), //
        Utils.asArray("Add '\\0' to a*.b", "'\0'+a*b", "a*b+'\0'"), //
        Utils.asArray("Sort from first to last", "1 + a*b + b*c", "a*b+b*c+1"), //
        Utils.asArray("Add 1", "1+a", "a+1"), //
        Utils.asArray("Add 1", "1+a", "a+1") //
    );
  }

  @RunWith(Parameterized.class) //
  @FixMethodOrder(MethodSorters.NAME_ASCENDING) //
  public static class Noneligible extends WringerTest.Noneligible {
    public Noneligible() {
      super(Wrings.ADDITION_SORTER.inner);
    }
    /**
     * Generate test cases for this parameterized class.
     *
     * @return a collection of cases, where each case is an array of three
     *         objects, the test case name, the input, and the file.
     */
    @Parameters(name = "{index}: {0} {1}") //
    public static Collection<Object[]> cases() {
      final Collection<Object[]> $ = new ArrayList<>(cases.length);
      for (final String[] t : cases)
        $.add(t);
      return $;
    }

    static String[][] cases = Utils.asArray(//
        Utils.asArray("Add 1", "a+1"), //
        Utils.asArray("Add '1'", "a+'1'"), //
        Utils.asArray("Add '\0'", "a+'\0'"), //
        Utils.asArray("Plain addition", "a+b"), //
        Utils.asArray("Literal addition", "2+3") //
    );

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
}