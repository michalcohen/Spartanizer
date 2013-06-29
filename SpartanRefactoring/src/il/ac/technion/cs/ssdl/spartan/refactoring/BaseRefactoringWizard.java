package il.ac.technion.cs.ssdl.spartan.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Artium Nihamkin
 *
 */
public class BaseRefactoringWizard extends RefactoringWizard {
  /**
 * @param refactoring
 * 		the Rafactoring with with to open the wizard
 */
public BaseRefactoringWizard(final Refactoring refactoring) {
    super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
  }
  
  @Override protected void addUserInputPages() {
    // None of our refactorings require dialog pages, therefore we use this
    // class for all the actions.
  }
}