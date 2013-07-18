package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ComparisonWithBoolean;

/**
 * a handler for {@link ComparisonWithBoolean}
 * 
 * @author Boris van Sosin <code><boris.van.sosin@gmail.com></code>
 * @author Yossi Gil <code><yossi.gil@gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/01
 */
public class RedundantEqualityHandler extends BaseSpartanizationHandler {
  /** Instantiates this class */
  public RedundantEqualityHandler() {
    super(new ComparisonWithBoolean());
  }
}
