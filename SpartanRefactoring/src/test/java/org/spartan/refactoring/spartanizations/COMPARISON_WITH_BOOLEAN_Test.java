package org.spartan.refactoring.spartanizations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;
import static org.spartan.refactoring.spartanizations.TESTUtils.collect;
import static org.spartan.refactoring.utils.Restructure.flatten;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
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
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@SuppressWarnings("javadoc") //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public enum COMPARISON_WITH_BOOLEAN_Test {
  ;
  @RunWith(Parameterized.class) //
  public static class WringedInput extends AbstractWringTest.Wringed.Infix {
    static String[][] cases = Utils.asArray(//
        Utils.asArray("", "a == b == c == true", "a == b == c"), //
        Utils.asArray("", "a == true", "a"), //
        Utils.asArray("", "a == false", "!(a)"), //
        Utils.asArray("", "true == a", "a"), //
        Utils.asArray("", "a != true", "!(a)"), //
        Utils.asArray("", "a != false", "a"), //
        Utils.asArray("", "false == a", "!(a)"), //
        Utils.asArray("", "true != a", "!(a)"), //
        Utils.asArray("", "false != a", "a"), //
        Utils.asArray("", "false != false", "false"), //
        Utils.asArray("", "false != true", "true"), //
        Utils.asArray("", "false == false", "!(false)"), //
        Utils.asArray("", "false == true", "!(true)"), //
        Utils.asArray("", "false != false", "false"), //
        Utils.asArray("", "true != true", "!(true)"), //
        Utils.asArray("", "true != false", "!(false)"), //
        Utils.asArray("", "true != true", "!(true)"), //
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
    @Test public void tryToSortTwice() {
      final InfixExpression e = asInfixExpression();
      final List<Expression> operands = All.operands(flatten(e));
      Wrings.tryToSort(operands, ExpressionComparator.ADDITION);
      assertFalse(Wrings.tryToSort(operands, ExpressionComparator.ADDITION));
    }
    @Override @Test public void flattenIsIdempotentt() {
      final InfixExpression flatten = flatten(asInfixExpression());
      assertThat(flatten(flatten).toString(), is(flatten.toString()));
    }
    @Override @Test public void inputIsInfixExpression() {
      final InfixExpression e = asInfixExpression();
      assertNotNull(e);
    }
    @Test public void twoOrMoreArguments() {
      final InfixExpression e = asInfixExpression();
      assertThat(All.operands(e).size(), greaterThanOrEqualTo(2));
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
    public WringedInput() {
      super(Wrings.COMPARISON_WITH_BOOLEAN.inner);
    }
  }
}