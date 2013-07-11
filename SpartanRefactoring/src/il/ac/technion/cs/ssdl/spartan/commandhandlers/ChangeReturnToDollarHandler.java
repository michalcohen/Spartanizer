package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ChangeReturnToDollarRefactoring;

/**
 * @author Boris van Sosin a BaseSpartanizationHandler configured to return the
 *         BaseRefactoring and label relevant for Change Return Variable to $
 */
public class ChangeReturnToDollarHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public ChangeReturnToDollarHandler() {
    super(new ChangeReturnToDollarRefactoring());
  }
}
