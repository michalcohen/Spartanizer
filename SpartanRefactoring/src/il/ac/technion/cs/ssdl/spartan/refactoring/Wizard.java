package il.ac.technion.cs.ssdl.spartan.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Artium Nihamkin
 * @since 2013/01/01
 */
public class Wizard extends RefactoringWizard {
  /**
   * @param r
   *          the refactoring to be used with this wizard
   */
  public Wizard(final Refactoring r) {
    super(r, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
  }
  
  @Override protected void addUserInputPages() {
// No user pages are required
  }
}