package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.refactoring.TESTUtils.assertOneOpportunity;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * * Unit tests for the nesting class Unit test for the containing class. Note
 * our naming convention: a) test methods do not use the redundant "test"
 * prefix. b) test methods begin with the name of the method they check.
 *
 * @author Yossi Gil
 * @since 2014-07-10
 *
 */
// @RunWith(PowerMockRunner.class) //
// @PrepareForTest({ Expression.class, /* ASTNode.class */ }) //
// @PrepareForTest({ TEST.AFinalClass.class, TEST.Node.class, ASTNode.class,
// Expression.class, }) //
@FixMethodOrder(MethodSorters.JVM) //
@SuppressWarnings({ "static-method", "javadoc" }) //
public class SimplificationEngineTest {
  private static final String PRE = //
  "package p; \n" + //
      "public class SpongeBob {\n" + //
      " public boolean squarePants() {\n" + //
      "   return ";
  private static final String POST = //
  "" + //
      ";\n" + //
      " }" + //
      "}" + //
      "";
  public static final String example = "on * notion * of * no * nothion != the * plain + kludge";

  public static final String make(final String s) {
    return PRE + s + POST;
  }

  @Test public void oneOpportunityExample() {
    assertOneOpportunity(new SimplificationEngine(), make(example));
  }
}