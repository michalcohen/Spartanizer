package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.RenameReturnToDollar;

/**
 * a handler for {@link RenameReturnToDollar}
 * 
 * @author Boris van Sosin <code><boris.van.sosin@gmail.com></code>
 * @author Yossi Gil <code><yossi.gil@gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/01
 */
public class ChangeReturnToDollarHandler extends BaseSpartanizationHandler {
  /** Instantiates this class */
  public ChangeReturnToDollarHandler() {
    super(new RenameReturnToDollar());
  }
}
