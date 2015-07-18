package org.spartan.utils;

import static org.junit.Assert.assertEquals;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThan;
import static org.spartan.hamcrest.OrderingComparison.lessThan;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM) //
@SuppressWarnings({ "static-method", "javadoc", }) //
public class UtilsTest {
  @Test public void compareFF() {
    assertThat(Utils.compare(false, false), is(0));
  }
  @Test public void compareTT() {
    assertThat(Utils.compare(true, true), is(0));
  }
  @Test public void compareFT() {
    assertThat(Utils.compare(false, true), lessThan(0));
  }
  @Test public void compareTF() {
    assertThat(Utils.compare(true, false), greaterThan(0));
  }
  @Test public void removePrefixTypical() {
    assertEquals(Utils.removePrefix("AAAABC", "AA"), "BC");
  }
  @Test public void removePrefiEmpty() {
    assertEquals(Utils.removePrefix("BAAAAB", "A"), "BAAAAB");
  }
  @Test public void removePrefiExhaustive() {
    assertEquals(Utils.removePrefix("AXAXAXAXAXAXAXAX", "AX"), "");
  }
  @Test public void removeSuffixTypical() {
    assertEquals(Utils.removeSuffix("AAAABC", "BC"), "AAAA");
  }
  @Test public void removeSuffiEmpty() {
    assertEquals(Utils.removeSuffix("BAAAAB", "A"), "BAAAAB");
  }
  @Test public void removeSuffiExhaustive() {
    assertEquals(Utils.removeSuffix("AXAXAXAXAXAXAXAX", "AX"), "");
  }
}
