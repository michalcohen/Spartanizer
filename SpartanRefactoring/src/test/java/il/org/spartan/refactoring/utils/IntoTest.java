package il.org.spartan.refactoring.utils;

import static il.org.spartan.azzert.*;
import il.org.spartan.*;

import org.junit.*;

@SuppressWarnings({ "javadoc", "static-method" }) public class IntoTest {
  @Test public void dCorrect() {
    azzert.that(Into.d("int f() { return a; }"), iz("int f() { return a; }"));
  }
  @Test public void dNotNull() {
    azzert.that(Into.d("int f() { return a; }"), notNullValue());
  }
  @Test(expected = AssertionError.class) public void dOnNull() {
    Into.d(null);
  }
}
