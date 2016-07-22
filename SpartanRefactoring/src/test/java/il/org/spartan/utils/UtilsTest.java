package il.org.spartan.utils;

import static il.org.spartan.Utils.*;
import static il.org.spartan.azzert.*;
import il.org.spartan.*;

import org.junit.*;
import org.junit.runners.*;

@FixMethodOrder(MethodSorters.JVM)//
@SuppressWarnings({ "static-method", "javadoc", })//
public class UtilsTest {
  @Test public void compareFF() {
   azzert.that(Utils.compare(false, false), is(0));
  }
  @Test public void compareFT() {
   azzert.that(Utils.compare(false, true), lessThan(0));
  }
  @Test public void compareTF() {
   azzert.that(Utils.compare(true, false), greaterThan(0));
  }
  @Test public void compareTT() {
   azzert.that(Utils.compare(true, true), is(0));
  }
  @Test public void inTypicalFalse() {
   azzert.that(in("X", "A", "B", "C"), is(false));
  }
  @Test public void inTypicalTrue() {
    azzert.that(in("A", "A", "B", "C"), is(true));
  }
  @Test public void removePrefiEmpty() {
   azzert.that("BAAAAB", is(Utils.removePrefix("BAAAAB", "A")));
  }
  @Test public void removePrefiExhaustive() {
   azzert.that("", is(Utils.removePrefix("AXAXAXAXAXAXAXAX", "AX")));
  }
  @Test public void removePrefixTypical() {
   azzert.that("BC", is(Utils.removePrefix("AAAABC", "AA")));
  }
  @Test public void removeSuffiEmpty() {
   azzert.that("BAAAAB", is(Utils.removeSuffix("BAAAAB", "A")));
  }
  @Test public void removeSuffiExhaustive() {
   azzert.that("", is(Utils.removeSuffix("AXAXAXAXAXAXAXAX", "AX")));
  }
  @Test public void removeSuffixTypical() {
   azzert.that("AAAA", is(Utils.removeSuffix("AAAABC", "BC")));
  }
  @Test public void removeWhites() {
   azzert.that(Utils.removeWhites("ABC"), is("ABC"));
   azzert.that(Utils.removeWhites("ABC\n"), is("ABC"));
   azzert.that(Utils.removeWhites(" ABC\n"), is("ABC"));
   azzert.that(Utils.removeWhites("A BC"), is("ABC"));
   azzert.that(Utils.removeWhites("AB\rC\n"), is("ABC"));
   azzert.that(Utils.removeWhites("A\fB\rC\n"), is("ABC"));
   azzert.that(Utils.removeWhites("\t\tA\fB\rC\n"), is("ABC"));
  }
}
