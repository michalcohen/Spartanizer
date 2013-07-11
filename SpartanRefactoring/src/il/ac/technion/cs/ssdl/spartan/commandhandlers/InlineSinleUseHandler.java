package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.InlineSingleUseRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Inline Single Use
 * 
 * @author Boris van Sosin
 */
public class InlineSinleUseHandler extends BaseSpartanizationHandler {
  /**
   * Instantiate this class
   */
  public InlineSinleUseHandler() {
    super(new InlineSingleUseRefactoring());
  }
}
