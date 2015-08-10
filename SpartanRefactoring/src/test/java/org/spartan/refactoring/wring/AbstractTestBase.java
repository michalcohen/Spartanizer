package org.spartan.refactoring.wring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.internal.builders.IgnoredClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.spartan.refactoring.spartanizations.Wrap;

@SuppressWarnings({ "javadoc", "restriction" }) //
@RunWith(IgnoredClassRunner.class) //
public abstract class AbstractTestBase {
  protected static Collection<Object[]> collect(final String[][] cases) {
    final Collection<Object[]> $ = new ArrayList<>(cases.length);
    for (final String[] t : cases) {
      if (t == null)
        break;
      $.add(t);
    }
    return $;
  }
  /** The name of the specific test for this transformation */
  @Parameter(0) public String name;
  /** Where the input text can be found */
  @Parameter(1) public String input;
  @Test public void inputNotNull() {
    assertNotNull(input);
  }
  @Test public void peelableinput() {
    final String s = input;
    final String s1 = input;
    assertEquals(input, Wrap.Statement.off(Wrap.Statement.on(s1)));
  }
}
