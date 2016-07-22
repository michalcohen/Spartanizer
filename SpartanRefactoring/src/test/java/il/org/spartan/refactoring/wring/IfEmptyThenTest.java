package il.org.spartan.refactoring.wring;

import static il.org.spartan.azzert.*;
import static org.junit.Assert.*;
import il.org.spartan.refactoring.utils.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;

@SuppressWarnings({ "javadoc", "static-method" })//
public class IfEmptyThenTest {
  private static final Statement INPUT = Into.s("{if (b) ; else ff();}");
  private static final IfStatement IF = extract.firstIfStatement(INPUT);
  private static final IfEmptyThen WRING = new IfEmptyThen();

  @Test public void eligible() {
    assertTrue(WRING.eligible(IF));
  }
  @Test public void emptyThen() {
    assertTrue(Is.vacuousThen(IF));
  }
  @Test public void extractFirstIf() {
    that(IF, notNullValue());
  }
  @Test public void inputType() {
    that(INPUT, instanceOf(Block.class));
  }
  @Test public void scopeIncludes() {
    assertTrue(WRING.scopeIncludes(IF));
  }
}
