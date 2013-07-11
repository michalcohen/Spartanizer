package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ConvertToTernaryRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Convert to Ternary
 * 
 * @author Boris van Sosin
 */
public class ConvertToTernaryHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public ConvertToTernaryHandler() {
    super(new ConvertToTernaryRefactoring());
  }
}
