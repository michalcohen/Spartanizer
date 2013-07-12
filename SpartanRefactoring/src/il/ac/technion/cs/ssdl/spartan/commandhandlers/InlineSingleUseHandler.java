package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.InlineSingleUseRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Inline Single Use
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @since 2013/07/01
 */
public class InlineSingleUseHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public InlineSingleUseHandler() {
    super(new InlineSingleUseRefactoring());
  }
}
