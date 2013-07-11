package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ChangeReturnToDollarRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Change Return Variable to $
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @since July 2013
 */
public class ChangeReturnToDollarHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public ChangeReturnToDollarHandler() {
    super(new ChangeReturnToDollarRefactoring());
  }
}
