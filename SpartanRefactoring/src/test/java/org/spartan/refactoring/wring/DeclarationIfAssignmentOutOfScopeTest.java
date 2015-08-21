package org.spartan.refactoring.wring;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.utils.Utils;

@SuppressWarnings("javadoc") //
@RunWith(Parameterized.class) //
public class DeclarationIfAssignmentOutOfScopeTest extends AbstractWringTest.OutOfScope.Declaration {
  static String[][] cases = Utils.asArray(//
      new String[] { "Vanilla", "int a; a =3;", }, //
      new String[] { "Not empty else", "int a; if (x) a = 3; else a++;", }, //
      new String[] { "Vanilla", "int a =2; if (x) a += 3;", }, //
      new String[] { "Vanilla", "int a =2; if (a != 2) a = 3;", }, //
      new String[] { "Vanilla", "int a = 2; if (b) a =a+2;", }, //
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
  public DeclarationIfAssignmentOutOfScopeTest() {
    super(DeclarationIfAssginmentTest.WRING);
  }
}