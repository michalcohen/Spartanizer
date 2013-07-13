package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ForwardDeclarationRefactoring;

/**
 * a handler for {@link ForwardDeclarationRefactoring}
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @author Yossi Gil <yossi.gil@gmail.com> (major refactoring 2013/07/11)
 * @since 2013/07/01
 */
public class ForwardDeclarationHandler extends BaseSpartanizationHandler {
  /** Instantiates this class */
  public ForwardDeclarationHandler() {
    super(new ForwardDeclarationRefactoring());
  }
}
