package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ForwardDeclarationRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Forward Declaration
 * 
 * @author Boris van Sosin
 */
public class ForwardDeclarationHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public ForwardDeclarationHandler() {
    super(new ForwardDeclarationRefactoring());
  }
}
