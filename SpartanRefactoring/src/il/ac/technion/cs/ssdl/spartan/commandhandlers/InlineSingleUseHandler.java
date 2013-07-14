package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.InlineSingleUse;

/**
 * a handler for {@link InlineSingleUse}
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @author Yossi Gil <yossi.gil@gmail.com> (major refactoring 2013/07/11)
 * @since 2013/07/01
 */
public class InlineSingleUseHandler extends BaseSpartanizationHandler {
  /** Instantiates this class */
  public InlineSingleUseHandler() {
    super(new InlineSingleUse());
  }
}
