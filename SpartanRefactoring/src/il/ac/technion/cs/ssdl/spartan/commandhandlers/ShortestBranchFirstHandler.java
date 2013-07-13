package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestBranchRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Shortest Branch First
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @author Yossi Gil <yossi.gil@gmail.com> (major refactoring 2013/07/11)
 * @since 2013/07/011
 */
public class ShortestBranchFirstHandler extends BaseSpartanizationHandler {
  /** Instantiates this class */
  public ShortestBranchFirstHandler() {
    super(new ShortestBranchRefactoring());
  }
}
