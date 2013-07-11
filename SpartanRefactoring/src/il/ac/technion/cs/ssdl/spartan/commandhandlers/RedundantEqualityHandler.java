package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.RedundantEqualityRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Redundant Equality
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @since 2013/07/01
 */
public class RedundantEqualityHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public RedundantEqualityHandler() {
    super(new RedundantEqualityRefactoring());
  }
}
