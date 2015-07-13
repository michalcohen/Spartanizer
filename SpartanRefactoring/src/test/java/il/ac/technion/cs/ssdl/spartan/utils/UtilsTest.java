package il.ac.technion.cs.ssdl.spartan.utils;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM) //
@SuppressWarnings({ "static-method", "javadoc" }) //
public class UtilsTest {
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
