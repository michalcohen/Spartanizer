package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimplifiesTo;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.utils.Utils;

/**
 * * Unit tests for the nesting class Unit test for the containing class. Note
 * our naming convention: a) test methods do not use the redundant "test"
 * prefix. b) test methods begin with the name of the method they check.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 *
 */
@RunWith(Parameterized.class) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc" }) //
public class SimplificationEngineTestNegation {
  /**
   * The name of the specific test for this transformation
   */
  @Parameter(value = 0) public String name;
  /**
   * Where the input text can be found
   */
  @Parameter(value = 1) public String input;
  /**
   * Where the expected output can be found?
   */
  @Parameter(value = 2) public String output;

  @Test public void simiplifies() {
    assertSimplifiesTo(input, output);
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
      Utils.asArray("not of AND", "!(f() && f(5))", "(!f() || !f(5))"), //
      Utils.asArray("not of EQ", "!(3 == 5)", "3 != 5"), //
      Utils.asArray("not of AND nested", "!(f() && (f(5)))", "(!f() || !f(5))"), //
      Utils.asArray("not of EQ nested", "!((((3 == 5))))", "3 != 5"), //
      Utils.asArray("not of GE", "!(3 >= 5)", "3 < 5"), //
      Utils.asArray("not of GT", "!(3 > 5)", "3 <= 5"), //
      Utils.asArray("not of NE", "!(3 != 5)", "3 == 5"), //
      Utils.asArray("not of LE", "!(3 <= 5)", "3 > 5"), //
      Utils.asArray("not of LT", "!(3 < 5)", "3 >= 5"), //
      Utils.asArray("not of AND", "!(a && b && c)", "(!a || !b || !c)"), //
      Utils.asArray("not of OR", "!(a || b || c)", "(!a && !b && !c)"), //
      Utils.asArray("double not", "!!f()", "f()"), //
      Utils.asArray("not of OR 2", "!(f() || f(5))", "(!f() && !f(5))") //
  );
}