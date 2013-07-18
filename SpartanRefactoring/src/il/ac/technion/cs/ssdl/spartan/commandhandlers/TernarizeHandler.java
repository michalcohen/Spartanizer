package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.Ternarize;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Convert to Ternary
 * 
 * @author Boris van Sosin <code><boris.van.sosin@gmail.com></code>
 * @author Yossi Gil <code><yossi.gil@gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/01
 */
public class TernarizeHandler extends BaseHandler {
  /** Instantiates this class */
  public TernarizeHandler() {
    super(new Ternarize());
  }
}
