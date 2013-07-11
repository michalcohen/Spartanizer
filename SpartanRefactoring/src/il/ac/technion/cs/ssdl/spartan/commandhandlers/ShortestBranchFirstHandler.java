package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestBranchRefactoring;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Shortest Branch First
 * 
 * @author Boris van Sosin
 */
public class ShortestBranchFirstHandler extends BaseSpartanizationHandler {
  /**
   * Instantiate this class
   */
  public ShortestBranchFirstHandler() {
    super(new ShortestBranchRefactoring());
  }
}
