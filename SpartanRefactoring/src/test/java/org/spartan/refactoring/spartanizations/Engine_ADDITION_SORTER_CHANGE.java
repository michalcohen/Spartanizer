package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
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
@RunWith(Parameterized.class) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class Engine_ADDITION_SORTER_CHANGE extends Engine_Common_CHANGE {
  /**
   * Instantiates the enclosing class ({@link Engine_ADDITION_SORTER_CHANGE})
   */
  public Engine_ADDITION_SORTER_CHANGE() {
    super(Wrings.ADDITION_SORTER.inner);
  }
  @Test public void hasLiteral() {
    final InfixExpression e = asInfixExpression();
    assertTrue(e.getOperator() == Operator.PLUS && Have.numericalLiteral(All.operands(e)) && Are.notString(All.operands(e)));
  }
  @Test public void isPlus() {
    final InfixExpression e = asInfixExpression();
    assertTrue(e.getOperator() == Operator.PLUS);
  }
  @Test public void literalArgument() {
    final InfixExpression e = asInfixExpression();
    assertTrue(Have.numericalLiteral(All.operands(e)));
  }
  @Test public void allNotStringArgument() {
    final InfixExpression e = asInfixExpression();
    assertTrue(Are.notString(All.operands(e)));
  }
  @Override String input() {
    return input;
  }
  @Override String output() {
    return output;
  }
  @Test public void inputIsInfixExpression() {
    final InfixExpression e = asInfixExpression();
    assertNotNull(e);
  }

  /**
   * Where the expected output can be found?
   */
  @Parameter(value = 2) public String output;
  /**
   * The name of the specific test for this transformation
   */
  @Parameter(value = 0) public String name;
  /**
   * Where the input text can be found
   */
  @Parameter(value = 1) public String input;

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
      Utils.asArray("Add 1 to 2*3", "1+2*3", "2*3+1"), //
      Utils.asArray("Add '1'", "'1'+a", "a+'1'"), //
      Utils.asArray("Add '\0'", "'\0'+a", "a+'\0'"), //
      Utils.asArray("Sor from first to last", "1 + a + b", "a+1"), //
      Utils.asArray("Add 1", "1+a", "a+1"), //
      Utils.asArray("Add 1", "1+a", "a+1") //
  );
}