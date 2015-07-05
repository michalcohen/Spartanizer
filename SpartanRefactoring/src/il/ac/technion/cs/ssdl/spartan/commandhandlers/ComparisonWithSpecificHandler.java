package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.CompareWithSpecific;

/**
 * a handler for {@link CompareWithSpecific}
 *
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code>
 * @since 2013/07/01
 */
public class ComparisonWithSpecificHandler extends BaseHandler {
  /** Instantiates this class */
  public ComparisonWithSpecificHandler() {
    super(new CompareWithSpecific());
  }
}
