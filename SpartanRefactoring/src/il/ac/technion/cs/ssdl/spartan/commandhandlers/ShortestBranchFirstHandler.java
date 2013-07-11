package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestBranchRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Shortest Branch First
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @since 2013/07/01
 */
public class ShortestBranchFirstHandler extends BaseSpartanizationHandler {
  /** Instantiate this class */
  public ShortestBranchFirstHandler() {
    super(new ShortestBranchRefactoring());
  }
}
